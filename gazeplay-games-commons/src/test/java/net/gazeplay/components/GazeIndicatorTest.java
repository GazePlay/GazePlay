package net.gazeplay.components;

import javafx.application.Platform;
import net.gazeplay.IGameContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class GazeIndicatorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext mockGameContext;

    @BeforeEach
    void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(mockGameContext.getConfiguration().getFixationLength()).thenReturn(10);
    }

    /**
     * Regression test: the indicator was hidden after the fixation by a java.util.Timer, from its own thread, while
     * JavaFX requires the displayed scene to be changed on its thread only.
     */
    @Test
    void shouldHideTheIndicatorOnTheJavaFxThreadAfterTheFixation() throws Exception {
        final AtomicBoolean finished = new AtomicBoolean();
        final List<Boolean> stopCallsOnTheJavaFxThread = new CopyOnWriteArrayList<>();
        final AtomicReference<GazeIndicator> indicator = new AtomicReference<>();

        WaitForAsyncUtils.asyncFx(() -> {
            final GazeIndicator gazeIndicator = new GazeIndicator(mockGameContext) {
                @Override
                public void stop() {
                    stopCallsOnTheJavaFxThread.add(Platform.isFxApplicationThread());
                    super.stop();
                }
            };
            gazeIndicator.setOnFinish(event -> finished.set(true));
            gazeIndicator.start();
            indicator.set(gazeIndicator);
        }).get();

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> finished.get() && !indicator.get().isVisible());

        assertEquals(List.of(true), stopCallsOnTheJavaFxThread);
    }
}
