package net.gazeplay.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import net.gazeplay.commons.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The slider of the speed of the elements (menu Esc and settings).
 */
@ExtendWith(ApplicationExtension.class)
class AnimationSpeedRatioControlTest {

    private final AnimationSpeedRatioControl control = AnimationSpeedRatioControl.getInstance();

    private Slider slider(final SimpleDoubleProperty speed) throws Exception {
        final Configuration config = mock(Configuration.class);
        when(config.getAnimationSpeedRatio()).thenAnswer(invocation -> speed.get());
        when(config.getAnimationSpeedRatioProperty()).thenReturn(speed);
        return WaitForAsyncUtils.asyncFx(() -> control.createSpeedEffectSlider(config, new Label())).get();
    }

    /**
     * Found by the maintainer on 18/11/2026: dragged to the end, the speed was × 9.90, not × 10 (the slider snapped on
     * ticks counted from its minimum, -8.1, which missed its maximum and its middle).
     */
    @Test
    void shouldReachEachEndAndTheSpeedByDefaultWhenDragged() throws Exception {
        final SimpleDoubleProperty speed = new SimpleDoubleProperty(1);
        final Slider slider = slider(speed);
        final double[][] dragged = {{100, 10}, {slider.getMax(), 10}, {slider.getMin(), 0.1}, {-1000, 0.1},
            {0.1, 1}, {-0.2, 1}, {0.4, 1.5}, {1.1, 2}, {-0.9, 0.9}, {-4.6, 0.5}, {8.8, 10}};
        for (final double[] each : dragged) {
            WaitForAsyncUtils.asyncFx(() -> slider.adjustValue(each[0])).get();
            assertEquals(each[1], speed.get(), 1e-9, "dragged to " + each[0]);
            assertEquals(each[1], control.sliderValueToSpeedRatio(slider.getValue()), 1e-9, "shown");
        }
    }

    /**
     * Ctrl + Up and Ctrl + Down go from one step to the next, up to each end and back.
     */
    @Test
    void shouldGoFromEndToEndWithTheKeyboard() throws Exception {
        final SimpleDoubleProperty speed = new SimpleDoubleProperty(1);
        final Slider slider = slider(speed);
        for (int i = 0; i < 30; i++) {
            WaitForAsyncUtils.asyncFx(() -> slider.setValue(AnimationSpeedRatioControl.nextValue(slider, 1))).get();
        }
        assertEquals(10, speed.get(), 1e-9);
        for (int i = 0; i < 40; i++) {
            WaitForAsyncUtils.asyncFx(() -> slider.setValue(AnimationSpeedRatioControl.nextValue(slider, -1))).get();
        }
        assertEquals(0.1, speed.get(), 1e-9);
        WaitForAsyncUtils.asyncFx(() -> slider.setValue(AnimationSpeedRatioControl.nextValue(slider, 1))).get();
        assertEquals(0.2, speed.get(), 1e-9, "up again from the minimum");
    }
}
