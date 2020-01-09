package net.gazeplay.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
class MusicControlTest {

    @Mock
    GazePlay mockGazePlay;

    @Mock
    Translator mockTranslator;

    Slider mockVolumeSlider;

    private MusicControl musicControl;

    @BeforeEach
    void setup() {
        initMocks();
        musicControl = new MusicControl(mockGazePlay);
        musicControl.createMusicControlPane();
        mockVolumeSlider = new Slider();
    }

    void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockTranslator.translate()).thenReturn("translation");
    }

    @Test
    void shouldCreateUnmutedMuteSwitch() {
        mockVolumeSlider.setValue(0.5);
        StackPane result = (StackPane) musicControl.createMuteSwitchButton(mockVolumeSlider);

        Button mute = (Button) result.getChildren().get(0);
        Button unmute = (Button) result.getChildren().get(1);

        assertTrue(mute.isVisible());
        assertFalse(unmute.isVisible());
    }

    @Test
    void shouldCreateMutedMuteSwitch() {
        mockVolumeSlider.setValue(0);
        StackPane result = (StackPane) musicControl.createMuteSwitchButton(mockVolumeSlider);

        Button mute = (Button) result.getChildren().get(0);
        Button unmute = (Button) result.getChildren().get(1);

        assertFalse(mute.isVisible());
        assertTrue(unmute.isVisible());
    }

    @Test
    void shouldMuteVolumeWhenMuteButtonPressed() {
        mockVolumeSlider.setValue(0.5);
        StackPane result = (StackPane) musicControl.createMuteSwitchButton(mockVolumeSlider);

        Button mute = (Button) result.getChildren().get(0);
        mute.fire();

        assertEquals(0, mockVolumeSlider.getValue());
    }

    @Test
    void shouldUnmuteVolumeWhenUnmuteButtonPressed() {
        mockVolumeSlider.setValue(0);
        StackPane result = (StackPane) musicControl.createMuteSwitchButton(mockVolumeSlider);

        Button unmute = (Button) result.getChildren().get(1);
        unmute.fire();

        assertNotEquals(0, mockVolumeSlider.getValue());
    }

    @Test
    void shouldShowMuteButtonWhenVolumeIsIncreased() {
        mockVolumeSlider.setValue(0);
        StackPane result = (StackPane) musicControl.createMuteSwitchButton(mockVolumeSlider);

        Button mute = (Button) result.getChildren().get(0);
        Button unmute = (Button) result.getChildren().get(1);

        mockVolumeSlider.setValue(0.5);

        assertFalse(unmute.isVisible());
        assertTrue(mute.isVisible());
    }

    @Test
    void shouldShowUnmuteButtonWhenVolumeIsSetToZero() {
        mockVolumeSlider.setValue(0.5);
        StackPane result = (StackPane) musicControl.createMuteSwitchButton(mockVolumeSlider);

        Button mute = (Button) result.getChildren().get(0);
        Button unmute = (Button) result.getChildren().get(1);

        mockVolumeSlider.setValue(0);

        assertFalse(mute.isVisible());
        assertTrue(unmute.isVisible());
    }
}
