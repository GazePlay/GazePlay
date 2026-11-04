package net.gazeplay.ui;

import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import net.gazeplay.commons.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The control of the fixation length in the menu of the gear.
 */
@ExtendWith(ApplicationExtension.class)
class FixationLengthControlTest {

    /**
     * A click on the value gives the default length of the settings (it used to give 0.5 s, the default being 2 s).
     */
    @Test
    void shouldComeBackToTheDefaultLengthWithAClickOnTheValue() throws Exception {
        final Configuration config = mock(Configuration.class);
        when(config.getFixationLength()).thenReturn(700);
        final Label label = new Label();
        final Slider slider = WaitForAsyncUtils.asyncFx(() -> {
            final Slider s = FixationLengthControl.getInstance().createFixationLengthSlider(config, label);
            Event.fireEvent(label, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false,
                false, false, false, true, false, false, false, false, true, null));
            return s;
        }).get();

        assertEquals(Configuration.DEFAULT_VALUE_FIXATION_LENGTH, slider.getValue());
        verify(config).setFixationLength(Configuration.DEFAULT_VALUE_FIXATION_LENGTH);
    }
}
