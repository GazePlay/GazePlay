package net.gazeplay.commons.utils.stats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.FixationPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static javafx.application.Platform.runLater;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Characterization of the data recorded by {@link Stats} during a game: raw samples (replayData.json
 * "coordinatesAndTimeStamp"), heatmap and fixation sequence. Each test documents the behaviour of the code, so that
 * any change to it is deliberate. The anomalies R1, R2, R5 and R11 of the format version 1 (CLAUDE.md) are fixed in
 * the format version 2 ({@link ReplayDataFormat}); each test says what version 1 did, which is what the historical
 * files contain.
 * <p>
 * Samples are injected as JavaFX events on the root of a real scene, exactly as the gaze device managers and the mouse
 * do; timestamps come from a controlled clock. The active configuration is the default one of the temporary user.home
 * (heatmap and fixation sequence enabled, AOI and video recording disabled).
 */
@ExtendWith(ApplicationExtension.class)
class StatsRecordingCharacterizationTest {

    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;

    private final AtomicLong now = new AtomicLong(1_000_000);
    private Pane root;
    private Stats stats;

    @BeforeEach
    void setUp() {
        onFxThread(() -> {
            root = new Pane();
            Scene scene = new Scene(root, WIDTH, HEIGHT);
            stats = new Stats(scene, "characterization");
            stats.clock = now::get;
        });
    }

    private static void onFxThread(Runnable action) {
        CompletableFuture<Void> done = new CompletableFuture<>();
        runLater(() -> {
            try {
                action.run();
                done.complete(null);
            } catch (Throwable e) {
                done.completeExceptionally(e);
            }
        });
        done.join();
    }

    private void gaze(double x, double y) {
        onFxThread(() -> Event.fireEvent(root, new GazeEvent(GazeEvent.GAZE_MOVED, now.get(), x, y)));
    }

    private void mouse(EventType<MouseEvent> type, double x, double y) {
        onFxThread(() -> Event.fireEvent(root, new MouseEvent(type, x, y, x, y, MouseButton.NONE, 0,
            false, false, false, false, false, false, false, false, false, false, null)));
    }

    private List<JsonObject> samples() {
        return StreamSupport.stream(stats.coordinateData.spliterator(), false)
            .map(element -> element.getAsJsonObject())
            .collect(Collectors.toList());
    }

    private List<String> sampleSources() {
        return samples().stream().map(sample -> sample.get("event").getAsString()).collect(Collectors.toList());
    }

    private double heatMapCellAt(int x, int y) {
        return cellAt(stats.getHeatMapData(), x, y);
    }

    private static double cellAt(double[][] heatMap, int x, int y) {
        double cellSize = WIDTH / heatMap[0].length;
        // Rows follow the Y axis, columns the X axis
        return heatMap[(int) (y / cellSize)][(int) (x / cellSize)];
    }

    @Test
    void shouldRecordOneNormalizedSamplePerGazeEvent() {
        onFxThread(stats::start);
        now.addAndGet(250);

        gaze(200, 150);

        List<JsonObject> samples = samples();
        assertEquals(1, samples.size());
        assertEquals(0.25, samples.get(0).get("X").getAsDouble());
        assertEquals(0.25, samples.get(0).get("Y").getAsDouble());
        assertEquals(250, samples.get(0).get("time").getAsLong());
        assertEquals("gaze", samples.get(0).get("event").getAsString());
    }

    private void gazeAcquiredAt(long sampleTime, double x, double y) {
        onFxThread(() -> Event.fireEvent(root, new GazeEvent(GazeEvent.GAZE_MOVED, now.get(), x, y, sampleTime)));
    }

    /**
     * Version 3: a gaze sample is recorded at its acquisition time. Samples queued while the JavaFX thread was busy,
     * handled in the same millisecond, used to get that same time (in the Tobii results: 74 samples out of 1636 with
     * the time of the previous one, and gaps of up to 250 ms).
     */
    @Test
    void shouldRecordTheGazeSamplesAtTheirAcquisitionTime() {
        onFxThread(stats::start);
        long start = now.get();
        now.addAndGet(300);

        // Handled together, at start + 300 ms: acquired at start + 260 and start + 270
        gazeAcquiredAt(start + 260, 200, 150);
        gazeAcquiredAt(start + 270, 210, 150);

        List<Long> times = samples().stream().map(sample -> sample.get("time").getAsLong()).collect(Collectors.toList());
        assertEquals(List.of(260L, 270L), times);
        // The gaze fixation sequence uses the same times
        FixationPoint first = stats.getFixationSequence().get(1).getFirst();
        assertEquals(start + 260, first.getTimeGaze());
    }

    /**
     * A sample acquired before the start of the round, still queued at its start, belongs to the previous round.
     */
    @Test
    void shouldIgnoreTheGazeSamplesAcquiredBeforeTheRound() {
        onFxThread(stats::start);
        long start = now.get();

        gazeAcquiredAt(start - 5, 200, 150);
        gazeAcquiredAt(start + 5, 200, 150);

        List<Long> times = samples().stream().map(sample -> sample.get("time").getAsLong()).collect(Collectors.toList());
        assertEquals(List.of(5L), times);
    }

    /**
     * R1, version 2: heatmap.csv and metricsMouseAndGaze.png keep both sources together (as in version 1), and each
     * source has its own matrix (heatmapGaze.csv, heatmapMouse.csv, metricsGaze.png, metricsMouse.png). Version 1
     * used the mixed matrix for the three metrics images.
     */
    @Test
    void r1ShouldKeepAHeatMapPerSourceBesidesTheMixedOne() {
        onFxThread(stats::start);

        gaze(400, 300);
        mouse(MouseEvent.MOUSE_MOVED, 400, 300);
        mouse(MouseEvent.MOUSE_MOVED, 400, 300);

        assertEquals(3, heatMapCellAt(400, 300));
        assertEquals(1, cellAt(stats.getHeatMapData(true), 400, 300));
        assertEquals(2, cellAt(stats.getHeatMapData(false), 400, 300));
        assertEquals(List.of("gaze", "mouse", "mouse"), sampleSources());
    }

    /**
     * R1, version 2: only the mouse movements are samples. Version 1 listened to MouseEvent.ANY, so presses, releases
     * and clicks were recorded as additional samples at the same position.
     */
    @Test
    void r1ShouldNotRecordMouseButtonEventsAsSamples() {
        onFxThread(stats::start);

        mouse(MouseEvent.MOUSE_MOVED, 100, 100);
        mouse(MouseEvent.MOUSE_PRESSED, 100, 100);
        mouse(MouseEvent.MOUSE_RELEASED, 100, 100);
        mouse(MouseEvent.MOUSE_CLICKED, 100, 100);
        mouse(MouseEvent.MOUSE_DRAGGED, 120, 100);

        assertEquals(List.of("mouse", "mouse"), sampleSources());
        assertEquals(1, heatMapCellAt(100, 100));
    }

    /**
     * The two sources do not apply the same validity rule to coordinates: a gaze sample needs x > 0 AND y > 0, a mouse
     * sample x > 0 OR y > 0.
     */
    @Test
    void shouldNotFilterMouseAndGazeSamplesTheSameWay() {
        onFxThread(stats::start);

        gaze(0, 100);
        mouse(MouseEvent.MOUSE_MOVED, 0, 100);

        assertEquals(List.of("mouse"), sampleSources());
    }

    /**
     * Regression test for R4 (fixed): stop() used to remove the filters from the scene although start() had added
     * them to the root of the scene, so the samples kept being recorded after the end of the game.
     */
    @Test
    void r4ShouldStopRecordingAfterStop() {
        onFxThread(stats::start);
        onFxThread(stats::stop);

        gaze(100, 100);
        mouse(MouseEvent.MOUSE_MOVED, 100, 100);

        assertEquals(0, samples().size());
    }

    /**
     * Regression test for R4 (fixed): reset() (Replay button, "Continue" of the round statistics) calls start()
     * again, which used to add one more pair of filters: every sample was recorded twice after one reset, three times
     * after two resets...
     */
    @Test
    void r4ShouldRecordEachSampleOnceAfterResets() {
        onFxThread(stats::start);
        onFxThread(stats::stop);
        onFxThread(stats::reset);
        gaze(100, 100);
        assertEquals(1, samples().size());

        onFxThread(stats::stop);
        onFxThread(stats::reset);
        gaze(100, 100);
        assertEquals(1, samples().size());
        assertEquals(1, heatMapCellAt(100, 100));
    }

    /**
     * Regression test for R4 (fixed): Simon and GazeRace call reset() in launch() while the statistics are already
     * started, without stop().
     */
    @Test
    void r4ShouldRecordEachSampleOnceWhenResetWithoutStop() {
        onFxThread(stats::start);
        onFxThread(stats::reset);

        gaze(100, 100);

        assertEquals(1, samples().size());
    }

    /**
     * R5, version 2: the raw samples are those of the round, like the heatmap. In version 1 they were never cleared:
     * the replayData.json of round N also contained the rounds 1 to N-1, while the time origin restarted at each
     * round ("time" goes down at each round boundary, which is how the historical files can be split).
     */
    @Test
    void r5ShouldOnlyKeepTheSamplesOfTheRound() {
        onFxThread(stats::start);
        now.addAndGet(500);
        gaze(100, 100);
        onFxThread(stats::stop);

        onFxThread(stats::reset);
        now.addAndGet(20);
        gaze(700, 500);

        List<Long> times = samples().stream().map(sample -> sample.get("time").getAsLong()).collect(Collectors.toList());
        assertEquals(List.of(20L), times);
        assertEquals(0, heatMapCellAt(100, 100));
        assertEquals(1, heatMapCellAt(700, 500));
    }

    /**
     * R13, fixed in October 2026. With the areas of interest enabled (AREA_OF_INTEREST_DISABLED=true means enabled),
     * each source keeps every change of position, in its own list. Before, the mouse kept a vertical move only one
     * event in three (condition {@code x != previousX || y != previousY && counter == 2}: here 3 of the 5 mouse
     * positions), and the gaze and mouse samples were mixed in a single list.
     */
    @Test
    void shouldKeepEveryChangeOfPositionOfEachSourceSeparately() {
        Configuration aoiEnabled = mock(Configuration.class);
        when(aoiEnabled.isAreaOfInterestDisabled()).thenReturn(true);
        when(aoiEnabled.getUserName()).thenReturn("");
        ActiveConfigurationContext.setInstance(aoiEnabled);
        try {
            onFxThread(stats::start);

            mouse(MouseEvent.MOUSE_MOVED, 10, 10);
            mouse(MouseEvent.MOUSE_MOVED, 20, 10);
            mouse(MouseEvent.MOUSE_MOVED, 20, 20);
            mouse(MouseEvent.MOUSE_MOVED, 20, 30);
            mouse(MouseEvent.MOUSE_MOVED, 20, 40);
            // Same position: not kept
            mouse(MouseEvent.MOUSE_MOVED, 20, 40);

            gaze(100, 100);
            gaze(100, 110);
            gaze(100, 110);
            gaze(100, 120);

            assertEquals(List.of("10,10", "20,10", "20,20", "20,30", "20,40"),
                stats.getMouseAreasOfInterest().getMovementHistory().stream()
                    .map(p -> p.getXValue() + "," + p.getYValue()).collect(Collectors.toList()));
            assertEquals(List.of("100,100", "100,110", "100,120"),
                stats.getGazeAreasOfInterest().getMovementHistory().stream()
                    .map(p -> p.getXValue() + "," + p.getYValue()).collect(Collectors.toList()));
        } finally {
            ActiveConfigurationContext.switchToDefaultUser();
        }
    }

    /**
     * Areas of interest computed during the game (read by the AreaOfInterest screen, never written to disk). A
     * sample closer than 150 px to the previous one, more than 10 ms later, joins the current group; otherwise a group
     * of more than 2 samples becomes an area (rectangle around its samples, with a 15 px margin), and a group of 1 or 2
     * samples becomes a 30 px square around its first sample if the jump is longer than 700 px. Pins the behaviour
     * before the extraction of this logic out of Stats.
     */
    @Test
    void shouldComputeTheAreasOfInterestFromTheGazeSamples() {
        Configuration aoiEnabled = mock(Configuration.class);
        when(aoiEnabled.isAreaOfInterestDisabled()).thenReturn(true);
        when(aoiEnabled.getUserName()).thenReturn("");
        ActiveConfigurationContext.setInstance(aoiEnabled);
        try {
            onFxThread(stats::start);
            // A group of 4 close samples, 20 ms apart, then a jump of 770 px
            for (int x : new int[]{100, 110, 120, 130, 900}) {
                now.addAndGet(20);
                gaze(x, 100);
            }
            // One sample close to the previous one, then a jump of more than 700 px
            now.addAndGet(20);
            gaze(910, 100);
            now.addAndGet(20);
            gaze(100, 500);

            AreaOfInterestRecording areas = stats.getGazeAreasOfInterest();
            assertEquals(2, areas.getAllAOIListTemp().size());
            assertEquals(4, areas.getAllAOIListTemp().get(0).size());
            assertEquals(2, areas.getAllAOIListTemp().get(1).size());
            assertEquals(List.of("0-4", "4-6"), areas.getStartAndEndIdx().stream()
                .map(startEnd -> startEnd[0] + "-" + startEnd[1]).collect(Collectors.toList()));
            assertEquals(List.of(
                    List.of(85.0, 115.0, 145.0, 115.0, 145.0, 85.0, 85.0, 85.0),
                    List.of(880.0, 130.0, 940.0, 130.0, 940.0, 70.0, 880.0, 70.0)),
                areas.getAllAOIListPolygonPt().stream().map(List::of).collect(Collectors.toList()));
            assertEquals(2, areas.getAllAOIListPolygon().size());
            // No mouse sample: no mouse area
            assertTrue(stats.getMouseAreasOfInterest().getAllAOIListTemp().isEmpty());
        } finally {
            ActiveConfigurationContext.switchToDefaultUser();
        }
    }

    /**
     * R2, version 2: a sample closer than 50 px (on each axis) to the first sample of the last point is merged into
     * it; the duration of a point runs from its first sample to the first sample of the next point, or to its last
     * sample for the last point. With the first four samples, version 1 gave 3 points: (1000, 0), (1100, 400) and
     * (1400, 300): the second sample was never merged, a merged duration was twice the elapsed time, and a new point
     * received the duration of the previous one.
     */
    @Test
    void r2ShouldComputeTheDurationsOfTheFixations() {
        LinkedList<FixationPoint> sequence = new LinkedList<>();

        now.set(1000);
        stats.incrementFixationSequence(100, 100, sequence);
        now.set(1100);
        stats.incrementFixationSequence(102, 101, sequence);
        now.set(1300);
        stats.incrementFixationSequence(101, 100, sequence);
        now.set(1400);
        stats.incrementFixationSequence(500, 500, sequence);
        now.set(1450);
        stats.incrementFixationSequence(520, 510, sequence);

        assertEquals(2, sequence.size());
        assertEquals(1000, sequence.get(0).getTimeGaze());
        assertEquals(400, sequence.get(0).getGazeDuration());
        assertEquals(1400, sequence.get(1).getTimeGaze());
        assertEquals(50, sequence.get(1).getGazeDuration());
    }

    @Test
    void r2ShouldGiveNoDurationToASingleSample() {
        LinkedList<FixationPoint> sequence = new LinkedList<>();

        now.set(1000);
        stats.incrementFixationSequence(100, 100, sequence);

        assertEquals(1, sequence.size());
        assertEquals(0, sequence.get(0).getGazeDuration());
    }

    /**
     * R11: in memory, the fixation points store the Y coordinate in their "x" field and X in "y" (in pixels, not
     * normalized), and the drawing code (FixationSequence, ScanpathView) swaps them back. Version 1 wrote them so in
     * replayData.json; version 2 writes "x" = X and "y" = Y, and the replay swaps them back when reading.
     */
    @Test
    void r11ShouldWriteXAndYInTheirFieldsFromVersion2() {
        LinkedList<FixationPoint> sequence = new LinkedList<>();

        stats.incrementFixationSequence(10, 500, sequence);

        assertEquals(500, sequence.get(0).getX());
        assertEquals(10, sequence.get(0).getY());
        JsonObject json = ReplayDataWriter.toJson(new ReplayDataWriter.Round(0, "", "", 0, "", 0, 0, 0, 0, 0,
                null, null, 0, List.of(), List.of(new LinkedList<>(), sequence), new JsonArray()))
            .getAsJsonArray("fixationSequence").get(1).getAsJsonArray().get(0).getAsJsonObject();
        assertEquals(10, json.get("x").getAsInt());
        assertEquals(500, json.get("y").getAsInt());
    }
}
