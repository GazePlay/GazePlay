package net.gazeplay.commons.utils.stats;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.FixationSequence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class StatsTest {

    @Mock
    private Scene mockScene;

    private Stats stats;

    @AfterEach
    void restoreActiveConfiguration() {
        ActiveConfigurationContext.switchToDefaultUser();
    }

    @BeforeEach
    void setUp() {
        initMocks();
        stats = new Stats(mockScene, "testGame");
    }

    void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(mockScene.getWidth()).thenReturn(1080d);
        when(mockScene.getHeight()).thenReturn(1920d);
    }

    @Test
    void shouldInstantiateHeatMapData() {
        double[][] result = new HeatMapGrid(60.0, 30.0, 3.0).counts();

        assertEquals(10, result.length);
        assertEquals(20, result[0].length);
    }

    @Test
    void shouldSetTargetAOIList() {
        ArrayList<TargetAOI> testList = new ArrayList<>(List.of(
            new TargetAOI(1, 1, 2, 1000),
            new TargetAOI(2, 2, 2, 3000),
            new TargetAOI(3, 3, 2, 6000)
        ));

        testList.get(0).setTimeEnded(2500);
        testList.get(1).setTimeEnded(5500);
        testList.get(2).setTimeEnded(6000);

        stats.setTargetAOIList(testList);
        ArrayList<TargetAOI> resultList = stats.getTargetAOIList();

        assertEquals(1500, resultList.get(0).getDuration());
        assertEquals(2500, resultList.get(1).getDuration());
        assertEquals(0, resultList.get(2).getDuration());
    }

    @Test
    void shouldSetEmptyTargetAOIList() {
        ArrayList<TargetAOI> testList = new ArrayList<>();

        stats.setTargetAOIList(testList);
        ArrayList<TargetAOI> resultList = stats.getTargetAOIList();

        assertEquals(testList, resultList);
    }

    @Test
    void shouldTakeScreenshotWhenNewRoundIsReady() {
        Stats statsSpy = spy(stats);

        statsSpy.notifyNewRoundReady();

        verify(statsSpy).takeScreenShot();
    }

    @Test
    void shouldAddRoundDurationToReportOnNextRound() {
        long start = System.currentTimeMillis();
        stats.notifyNewRoundReady();

        long end = System.currentTimeMillis();
        stats.notifyNextRound();

        Long duration = end - start;

        assertTrue(stats.getOriginalDurationsBetweenGoals().contains(duration));
    }

    @Test
    void shouldCreateVideoRecording() {
        // user.home is a temporary folder during tests (see build.gradle)
        stats.startVideoRecording();
        stats.endVideoRecording();

        File todayDirectory = stats.getGameStatsOfTheDayDirectory();
        assertTrue(todayDirectory.getPath().contains("testGame"));
        assertNotEquals(0, todayDirectory.list().length);
    }

    @Test
    void shouldEndAVideoRecordingThatNeverStarted() {
        // No recorder (not started, or it could not be created): it used to call stop() on null in a background thread
        stats.endVideoRecording();

        assertTrue(stats.awaitVideoRecordingEnd(1, java.util.concurrent.TimeUnit.SECONDS));
    }

    @Test
    void shouldCreateVideoRecordingWhenStartsAndStops() {
        Stats statsSpy = spy(stats);

        Configuration mockConfig = mock(Configuration.class);
        when(mockConfig.isVideoRecordingEnabled()).thenReturn(true);
        when(mockConfig.getUserName()).thenReturn("");
        ActiveConfigurationContext.setInstance(mockConfig);

        when(statsSpy.gameContextScene.getRoot()).thenReturn(new Pane());

        statsSpy.start();
        statsSpy.stop();

        verify(statsSpy).startVideoRecording();
        verify(statsSpy).endVideoRecording();

        statsSpy.reset();

        verify(statsSpy, atLeastOnce()).start();
    }

    @Test
    void shouldIncrementHeatMapOnGazeMoved() {
        Stats statsSpy = spy(stats);
        when(statsSpy.gameContextScene.getRoot()).thenReturn(new Pane());

        statsSpy.start();
        statsSpy.gazeMoved(new Point2D(20.0, 40.0));

        verify(statsSpy).incrementHeatMap(20, 40, true);
    }

    @Test
    void shouldSaveImageAsPNG() {
        BufferedImage image = new BufferedImage(30, 40, BufferedImage.TYPE_INT_RGB);
        File testFile = new File("testImage.png");

        Stats.saveImageAsPng(image, testFile);

        assertTrue(testFile.isFile());

        testFile.delete();
    }

    /**
     * With the fixation sequence disabled in the settings, the sequence is null when the statistics are saved.
     */
    @Test
    void shouldSaveStatsWithTheFixationSequenceDisabled() throws InterruptedException {
        Configuration config = mock(Configuration.class);
        when(config.getUserName()).thenReturn("");
        when(config.isHeatMapDisabled()).thenReturn(false);
        when(config.isFixationSequenceDisabled()).thenReturn(true);
        when(config.isVideoRecordingEnabled()).thenReturn(false);
        when(config.isAreaOfInterestDisabled()).thenReturn(false);
        when(config.isMultipleScreenshotsEnabled()).thenReturn(false);
        when(config.getHeatMapOpacity()).thenReturn(0.7);
        when(config.getHeatMapColors()).thenReturn(List.of(javafx.scene.paint.Color.BLUE, javafx.scene.paint.Color.RED));
        ActiveConfigurationContext.setInstance(config);
        when(stats.gameContextScene.getRoot()).thenReturn(new Pane());
        when(mockScene.snapshot(null)).thenReturn(new WritableImage(40, 30));

        stats.start();
        stats.takeScreenShot();

        CompletableFuture<SavedStatsInfo> saved = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                saved.complete(stats.saveStats());
            } catch (Throwable e) {
                saved.completeExceptionally(e);
            }
        });
        TestingUtils.waitForRunLater();

        SavedStatsInfo info = saved.join();
        assertTrue(info.getReplayDataFile().isFile());
        assertTrue(info.getHeatMapCsvFile().isFile());
        assertTrue(info.getGazeMetricsFileGaze().isFile());
    }

    @Test
    void shouldSaveStats() throws InterruptedException {
        // Default configuration of the temporary user.home: heatmap and fixation sequence enabled
        when(stats.gameContextScene.getRoot()).thenReturn(new Pane());
        when(mockScene.snapshot(null)).thenReturn(new WritableImage(40, 30));

        stats.start();
        stats.gazeMoved(new Point2D(30, 40));
        stats.gazeMoved(new Point2D(20, 50));
        stats.takeScreenShot();

        CompletableFuture<SavedStatsInfo> saved = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                saved.complete(stats.saveStats());
            } catch (Throwable e) {
                saved.completeExceptionally(e);
            }
        });
        TestingUtils.waitForRunLater();

        SavedStatsInfo info = saved.join();
        assertTrue(info.getHeatMapCsvFile().isFile());
        assertTrue(info.getGazeMetricsFileMouse().isFile());
        assertTrue(info.getGazeMetricsFileGaze().isFile());
        assertTrue(info.getGazeMetricsFileMouseAndGaze().isFile());
        assertTrue(info.getScreenshotFile().isFile());
        assertTrue(info.getReplayDataFile().isFile());
        assertEquals(stats.getGameStatsOfTheDayDirectory(), info.getHeatMapCsvFile().getParentFile());
        // Format version 2: one heatmap per source next to heatmap.csv (same name, suffix Gaze or Mouse)
        String heatMapName = info.getHeatMapCsvFile().getName();
        assertTrue(new File(info.getHeatMapCsvFile().getParentFile(), heatMapName.replace("-heatmap.csv", "-heatmapGaze.csv")).isFile());
        assertTrue(new File(info.getHeatMapCsvFile().getParentFile(), heatMapName.replace("-heatmap.csv", "-heatmapMouse.csv")).isFile());

        // replayData.json is a persistent format (read by the replay and by researchers): pin its structure
        JsonObject replayData;
        try {
            replayData = JsonParser.parseString(Files.readString(info.getReplayDataFile().toPath(), Charset.defaultCharset()))
                .getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertEquals(3, replayData.get("formatVersion").getAsInt());
        assertEquals(Set.of("formatVersion", "gameSeed", "gameName", "gameVariant", "gameStartedTime", "screenAspectRatio",
            "sceneAspectRatio", "statsNbGoalsReached", "statsNbGoalsToReach", "statsNbUnCountedGoalsReached",
            "statsNbMistakes", "lifeCycle", "roundsDurationReport", "fixationSequence", "coordinatesAndTimeStamp"),
            replayData.keySet());
        // stopTime is written even when null (round not stopped yet)
        assertEquals(Set.of("startTime", "stopTime"), replayData.getAsJsonObject("lifeCycle").keySet());
        assertEquals(Set.of("totalAdditiveDuration", "durationBetweenGoals"),
            replayData.getAsJsonObject("roundsDurationReport").keySet());
        // Two lists: mouse, then gaze; the samples given to gazeMoved() go to the gaze list
        JsonArray fixationSequence = replayData.getAsJsonArray("fixationSequence");
        assertEquals(2, fixationSequence.size());
        assertEquals(Set.of("timeGaze", "gazeDuration", "x", "y"),
            fixationSequence.get(1).getAsJsonArray().get(0).getAsJsonObject().keySet());
    }

    @Test
    void shouldIncrementNumberOfGoalsToReachByOne() {
        int initial = stats.getNbGoalsToReach();

        stats.incrementNumberOfGoalsToReach();

        int result = stats.getNbGoalsToReach();

        assertEquals(initial + 1, result);
    }

    @Test
    void shouldIncrementNumberOfGoalsToReachByAmount() {
        int initial = stats.getNbGoalsToReach();

        stats.incrementNumberOfGoalsToReach(5);

        int result = stats.getNbGoalsToReach();

        assertEquals(initial + 5, result);
    }

    @Test
    void shouldIncrementNumberOfGoalsReachedByOne() throws InterruptedException {
        int initial = stats.getNbGoalsReached();

        stats.setAccidentalShotPreventionPeriod(10L);
        stats.incrementNumberOfGoalsToReach();
        Thread.sleep(20);

        stats.incrementNumberOfGoalsReached();
        int result = stats.getNbGoalsReached();

        assertEquals(initial + 1, result);
    }

    @Test
    void shouldIncrementNumberOfUncountedGoalsReachedByOne() throws InterruptedException {
        int initial = stats.getNbUnCountedGoalsReached();

        stats.setAccidentalShotPreventionPeriod(50L);
        stats.incrementNumberOfGoalsToReach();
        Thread.sleep(20);

        stats.incrementNumberOfGoalsReached();
        int result = stats.getNbUnCountedGoalsReached();

        assertEquals(initial + 1, result);
    }

    @Test
    void shouldGetDefaultShotRatio() {
        int result = stats.getShotRatio();

        assertEquals(100, result);
    }

    @Test
    void shouldGetShotRatio() throws InterruptedException {
        stats.incrementNumberOfGoalsToReach(4);

        stats.setAccidentalShotPreventionPeriod(10L);
        Thread.sleep(20);

        stats.incrementNumberOfGoalsReached();

        int result = stats.getShotRatio();

        assertEquals(25, result);
    }
}
