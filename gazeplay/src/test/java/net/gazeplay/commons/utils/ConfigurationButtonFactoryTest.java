package net.gazeplay.commons.utils;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import net.gazeplay.GazePlay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
class ConfigurationButtonFactoryTest {

    @Mock
    private GazePlay mockGazePlay;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldCreateConfigurationButton() {
        ConfigurationButton button = ConfigurationButtonFactory.createConfigurationButton(mockGazePlay);
        button.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false, new PickResult(button, 0, 0)));

        verify(mockGazePlay).onDisplayConfigurationManagement();
    }

}
