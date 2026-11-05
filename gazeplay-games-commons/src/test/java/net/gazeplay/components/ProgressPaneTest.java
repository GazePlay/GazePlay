package net.gazeplay.components;

import javafx.event.Event;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * The pane chosen by a fixation (the dice of the Dice game): the length is that of the settings, read at each fixation
 * (maintainer's rule of 4/11/2026: it was 2 seconds whatever the settings).
 */
@ExtendWith(ApplicationExtension.class)
class ProgressPaneTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldReadTheLengthOfTheSettingsAtEachFixation() throws Exception {
        final AtomicInteger runs = new AtomicInteger();
        when(gameContext.getConfiguration().getFixationLength()).thenReturn(60000);
        final ProgressPane pane = WaitForAsyncUtils.asyncFx(() -> {
            final ProgressPane p = new ProgressPane();
            p.assignIndicator(event -> runs.incrementAndGet(), gameContext);
            Event.fireEvent(p, new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
            return p;
        }).get();
        Thread.sleep(300);
        assertEquals(0, runs.get(), "a minute in the settings");

        // Changed in the menu: the next fixation takes the new length
        when(gameContext.getConfiguration().getFixationLength()).thenReturn(100);
        WaitForAsyncUtils.asyncFx(() -> {
            Event.fireEvent(pane, new GazeEvent(GazeEvent.GAZE_EXITED, -1, 0, 0));
            Event.fireEvent(pane, new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
        }).get();
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> runs.get() == 1);
    }
}
