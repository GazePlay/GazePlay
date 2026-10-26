package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static javafx.application.Platform.runLater;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Characterization of the dispatch of the gaze (and mouse-as-gaze) positions to the nodes of a game by
 * {@link AbstractGazeDeviceManager}: which {@link GazeEvent}s are fired, when, and with which time. Positions are in
 * screen coordinates, as given by the eye trackers; a real (window-less) stage is shown.
 */
@ExtendWith(ApplicationExtension.class)
class GazeDispatchCharacterizationTest {

    private Scene scene;
    private Rectangle target;
    private final List<GazeEvent> events = new CopyOnWriteArrayList<>();
    private AbstractGazeDeviceManager manager;

    @Start
    void start(Stage stage) {
        Pane root = new Pane();
        target = new Rectangle(100, 100, 50, 50);
        root.getChildren().add(target);
        scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setUp() {
        target.addEventHandler(GazeEvent.ANY, events::add);
        manager = new AbstractGazeDeviceManager() {
            @Override
            public void init(Supplier<Dimension2D> dimension, Supplier<Point2D> position) {
            }

            @Override
            public void destroy() {
            }
        };
        manager.addEventFilter(target);
    }

    private void onFxThread(Runnable action) throws InterruptedException {
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
        // The events are fired by Platform.runLater calls queued during the action
        TestingUtils.waitForRunLater();
    }

    private Point2D onScreen(double sceneX, double sceneY) {
        return scene.getRoot().localToScreen(sceneX, sceneY);
    }

    private void sample(double sceneX, double sceneY, String source) throws InterruptedException {
        onFxThread(() -> manager.onGazeUpdate(onScreen(sceneX, sceneY), source));
    }

    private List<String> eventTypes() {
        return events.stream().map(event -> event.getEventType().getName()).collect(Collectors.toList());
    }

    private void startRecording() throws InterruptedException {
        onFxThread(() -> manager.addStats(new Stats(scene, "characterization")));
    }

    @Test
    void shouldFireNothingBeforeTheStatisticsAreSet() throws InterruptedException {
        sample(125, 125, "gaze");

        assertEquals(List.of(), eventTypes());
    }

    /**
     * GAZE_MOVED carries the time at which the gaze entered the node (used by the games to measure the fixation), not
     * the time of the sample; GAZE_EXITED carries -1.
     */
    @Test
    void shouldFireEnteredMovedAndExitedWithTheEntryTime() throws InterruptedException {
        startRecording();
        long before = System.currentTimeMillis();

        sample(110, 110, "gaze");
        sample(140, 140, "gaze");
        sample(300, 250, "gaze");

        assertEquals(List.of("GAZE_ENTERED", "GAZE_MOVED", "GAZE_EXITED"), eventTypes());
        long entryTime = events.get(0).getTime();
        assertTrue(entryTime >= before);
        assertEquals(entryTime, events.get(1).getTime());
        assertEquals(-1, events.get(2).getTime());
        // Coordinates local to the node
        assertEquals(140, events.get(1).getX(), 0.5);
    }

    /**
     * The events also carry the acquisition time of their sample, recorded by the statistics (format version 3); the
     * entry time used by the games is unchanged.
     */
    @Test
    void shouldGiveTheAcquisitionTimeOfTheSampleToTheEvents() throws InterruptedException {
        startRecording();

        onFxThread(() -> manager.onGazeSample(new GazeSample(1234, onScreen(110, 110), GazeSample.Source.GAZE)));
        onFxThread(() -> manager.onGazeSample(new GazeSample(1250, onScreen(140, 140), GazeSample.Source.GAZE)));

        assertEquals(List.of("GAZE_ENTERED", "GAZE_MOVED"), eventTypes());
        assertEquals(List.of(1234L, 1250L), events.stream().map(GazeEvent::getSampleTime).collect(Collectors.toList()));
        assertEquals(events.get(0).getTime(), events.get(1).getTime());
    }

    @Test
    void shouldTrackTheMouseAndTheGazeSeparately() throws InterruptedException {
        startRecording();

        sample(110, 110, "gaze");
        sample(120, 120, "mouse");
        sample(300, 250, "gaze");

        // The mouse enters although the gaze is already on the node; the gaze leaving does not make the mouse leave
        assertEquals(List.of("GAZE_ENTERED", "GAZE_ENTERED", "GAZE_EXITED"), eventTypes());
    }

    /**
     * A handler may clear the manager while the nodes are dispatched (e.g. a game ending on a GAZE_ENTERED). The events
     * are queued with Platform.runLater during the loop and handled after it: every node hit by the sample still
     * receives its event, nothing is fired afterwards, and no ConcurrentModificationException is thrown. A synchronous
     * dispatch (the GazeSource refactoring, see MODERNIZATION.md) must keep this, e.g. by iterating over a copy.
     */
    @Test
    void shouldLetAHandlerClearTheManagerDuringTheDispatch() throws InterruptedException {
        Rectangle other = new Rectangle(100, 100, 50, 50);
        List<GazeEvent> otherEvents = new CopyOnWriteArrayList<>();
        onFxThread(() -> {
            ((Pane) scene.getRoot()).getChildren().add(other);
            other.addEventHandler(GazeEvent.ANY, otherEvents::add);
            target.addEventHandler(GazeEvent.GAZE_ENTERED, e -> manager.clear());
        });
        manager.addEventFilter(other);
        startRecording();

        sample(125, 125, "gaze");
        sample(130, 130, "gaze");

        assertEquals(List.of("GAZE_ENTERED"), eventTypes());
        assertEquals(List.of("GAZE_ENTERED"), otherEvents.stream()
            .map(event -> event.getEventType().getName()).collect(Collectors.toList()));
    }

    /**
     * The gaze sources call onGazeSample from their own thread (Tobii polling thread): the nodes are read and the
     * events fired on the JavaFX thread. The former EyeTribe source used to dispatch on its own thread.
     */
    @Test
    void shouldDispatchOnTheJavaFxThreadASampleGivenByAnotherThread() throws Exception {
        startRecording();
        List<Boolean> onFxThread = new CopyOnWriteArrayList<>();
        onFxThread(() -> target.addEventHandler(GazeEvent.ANY, e -> onFxThread.add(javafx.application.Platform.isFxApplicationThread())));
        Point2D position = onScreen(125, 125);

        Thread source = new Thread(() -> manager.onGazeSample(GazeSample.now(position, GazeSample.Source.GAZE)));
        source.start();
        source.join();
        TestingUtils.waitForRunLater();

        assertEquals(List.of("GAZE_ENTERED"), eventTypes());
        assertEquals(List.of(true), onFxThread);
    }

    @Test
    void shouldMapTheSourcesToTheEventNamesOfTheRecordedSamples() {
        assertEquals("gaze", GazeSample.Source.GAZE.eventName());
        assertEquals("mouse", GazeSample.Source.MOUSE.eventName());
        assertEquals(GazeSample.Source.GAZE, GazeSample.Source.fromEventName("gaze"));
        assertEquals(GazeSample.Source.MOUSE, GazeSample.Source.fromEventName("mouse"));
    }

    @Test
    void shouldFireNothingOnADisabledNode() throws InterruptedException {
        startRecording();
        onFxThread(() -> target.setDisable(true));

        sample(125, 125, "gaze");

        assertEquals(List.of(), eventTypes());
    }
    /**
     * While the configuration menu of the game screen is open, the dispatch is paused: the node under the gaze
     * receives GAZE_EXITED at once (no fixation ends during the settings), and nothing more until the menu closes.
     */
    @Test
    void shouldReleaseTheNodesAndFireNothingWhilePaused() throws InterruptedException {
        startRecording();

        sample(110, 110, "gaze");
        onFxThread(() -> manager.setPaused(true));
        sample(120, 120, "gaze");
        sample(130, 130, "gaze");
        onFxThread(() -> manager.setPaused(false));
        sample(140, 140, "gaze");

        assertEquals(List.of("GAZE_ENTERED", "GAZE_EXITED", "GAZE_ENTERED"), eventTypes());
    }

}
