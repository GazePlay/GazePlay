package net.gazeplay.components;

import javafx.event.Event;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * The round button chosen by a fixation (3/11/2026: the ring of the reviewed games instead of the progress indicator
 * of before).
 */
@ExtendWith(ApplicationExtension.class)
class ProgressButtonTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getConfiguration().getFixationLength()).thenReturn(300);
        when(gameContext.getConfiguration().getProgressBarSize()).thenReturn(100);
    }

    private static MouseEvent mouse(final javafx.event.EventType<MouseEvent> type) {
        return new MouseEvent(type, 0, 0, 0, 0, MouseButton.NONE, 0, false, false, false, false, false, false, false,
            false, false, false, null);
    }

    @Test
    void shouldFillTheRingThenRunTheActionAndHaveNoProgressIndicator() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        final ProgressButton button = WaitForAsyncUtils.asyncFx(() -> {
            final ProgressButton b = new ProgressButton();
            b.getButton().setRadius(50);
            b.assignIndicatorUpdatable(event -> runs.incrementAndGet(), gameContext);
            b.active();
            assertTrue(b.getChildren().stream().noneMatch(node -> node instanceof ProgressIndicator));
            Event.fireEvent(b, new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
            assertTrue(b.isRingShown(), "the ring of the fixation");
            return b;
        }).get();
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> runs.get() == 1);
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(button.isRingShown(), "hidden after the action");
    }

    /**
     * A button sized by its width and height, without a disc (the stables of Horses): the ring fills it (it was a
     * dot, found at the check in a real window on 3/11/2026).
     */
    @Test
    void shouldSizeTheRingOnAButtonWithoutADisc() throws Exception {
        final double radius = WaitForAsyncUtils.asyncFx(() -> {
            final ProgressButton b = new ProgressButton();
            b.setPrefWidth(280);
            b.setPrefHeight(280);
            b.assignIndicatorUpdatable(event -> { }, gameContext);
            b.active();
            Event.fireEvent(b, new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
            return b.getRingRadius();
        }).get();
        assertEquals(140, radius, 1e-9);
    }

    /**
     * Found by the maintainer on 3/11/2026: the image of the button shook while the ring filled (the button centered
     * its content again on the bounds of the arc, which change while it fills). The ring is out of the layout: the
     * image stays in its place during the whole fixation.
     */
    @Test
    void shouldKeepTheImageInItsPlaceWhileTheRingFills() throws Exception {
        final ProgressButton button = WaitForAsyncUtils.asyncFx(() -> {
            final ProgressButton b = new ProgressButton();
            b.getButton().setRadius(50);
            b.setImage(new javafx.scene.image.ImageView());
            b.assignIndicatorUpdatable(event -> { }, gameContext);
            b.active();
            final javafx.scene.layout.Pane pane = new javafx.scene.layout.Pane(b);
            new javafx.scene.Scene(pane, 300, 300);
            pane.applyCss();
            pane.layout();
            return b;
        }).get();
        final javafx.geometry.Bounds before = WaitForAsyncUtils.asyncFx(() -> {
            assertFalse(button.isRingManaged(), "out of the layout");
            Event.fireEvent(button, new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
            return button.getImage().getBoundsInParent();
        }).get();
        for (int i = 0; i < 6; i++) {
            Thread.sleep(80);
            final javafx.geometry.Bounds now = WaitForAsyncUtils.asyncFx(() -> {
                button.getParent().layout();
                return button.getImage().getBoundsInParent();
            }).get();
            assertEquals(before, now, "the image does not move");
        }
        WaitForAsyncUtils.asyncFx(() -> {
            assertEquals(button.getWidth() / 2, button.getRingCenterX(), 1e-9, "the ring centered on the button");
            return null;
        }).get();
    }

    @Test
    void shouldStopTheRingWhenTheMouseLeaves() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        final ProgressButton button = WaitForAsyncUtils.asyncFx(() -> {
            final ProgressButton b = new ProgressButton();
            b.getButton().setRadius(50);
            b.assignIndicatorUpdatable(event -> runs.incrementAndGet(), gameContext);
            b.active();
            Event.fireEvent(b, mouse(MouseEvent.MOUSE_ENTERED));
            assertTrue(b.isRingShown());
            Event.fireEvent(b, mouse(MouseEvent.MOUSE_EXITED));
            return b;
        }).get();
        assertFalse(button.isRingShown());
        assertEquals(0, button.getRingFill(), 1e-9);
        Thread.sleep(800);
        assertEquals(0, runs.get(), "no action without the whole fixation");
    }
}
