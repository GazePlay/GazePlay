package net.gazeplay.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import net.gazeplay.commons.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The sliders of the menu Esc and of the settings reach their ends and their value by default when dragged (checked on
 * 18/11/2026 after the bug of the speed: a slider snapping on ticks counted from its minimum missed values).
 */
@ExtendWith(ApplicationExtension.class)
class SettingsSlidersTest {

    private static void drag(final Slider slider, final double to) throws Exception {
        WaitForAsyncUtils.asyncFx(() -> slider.adjustValue(to)).get();
    }

    @Test
    void shouldReachEveryLengthOfFixationBy100Ms() throws Exception {
        final SimpleIntegerProperty length = new SimpleIntegerProperty(Configuration.DEFAULT_VALUE_FIXATION_LENGTH);
        final Configuration config = mock(Configuration.class);
        when(config.getFixationLength()).thenAnswer(invocation -> length.get());
        doAnswer(invocation -> {
            length.set(invocation.getArgument(0));
            return null;
        }).when(config).setFixationLength(anyInt());
        final Slider slider = WaitForAsyncUtils.asyncFx(() -> FixationLengthControl.getInstance()
            .createFixationLengthSlider(config, new Label())).get();
        final double[][] dragged = {{20000, 10000}, {-5, 0}, {2000, 2000}, {1990, 2000}, {1260, 1300}, {9960, 10000}};
        for (final double[] each : dragged) {
            drag(slider, each[0]);
            assertEquals((int) each[1], length.get(), "dragged to " + each[0]);
        }
    }

    @Test
    void shouldReachEverySizeOfTheElements() throws Exception {
        final SimpleIntegerProperty size = new SimpleIntegerProperty(Configuration.DEFAULT_VALUE_ELEMENT_SIZE);
        final Configuration config = mock(Configuration.class);
        when(config.getElementSize()).thenAnswer(invocation -> size.get());
        when(config.getElementSizeProperty()).thenReturn(size);
        final Slider slider = WaitForAsyncUtils.asyncFx(() -> ElementSizeControl.getInstance()
            .createElementSizeSlider(config, new Label())).get();
        final double[][] dragged = {{1000, 200}, {0, 5}, {100, 100}, {99.6, 100}, {37.2, 37}};
        for (final double[] each : dragged) {
            drag(slider, each[0]);
            assertEquals((int) each[1], size.get(), "dragged to " + each[0]);
        }
    }

    @Test
    void shouldReachEveryThicknessOfTheRing() throws Exception {
        final SimpleIntegerProperty thickness = new SimpleIntegerProperty(Configuration.DEFAULT_VALUE_RING_THICKNESS);
        final Configuration config = mock(Configuration.class);
        when(config.getRingThickness()).thenAnswer(invocation -> thickness.get());
        when(config.getRingThicknessProperty()).thenReturn(thickness);
        final Slider slider = WaitForAsyncUtils.asyncFx(() -> ProgressBarControl.getInstance()
            .createRingThicknessSlider(config, new Label())).get();
        final double[][] dragged = {{1000, 200}, {0, 50}, {100, 100}, {97, 100}, {143, 140}};
        for (final double[] each : dragged) {
            drag(slider, each[0]);
            assertEquals((int) each[1], thickness.get(), "dragged to " + each[0]);
        }
    }
}
