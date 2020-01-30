package net.gazeplay.commons.utils;

import net.gazeplay.GazePlay;
import net.gazeplay.TestingUtils;
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
        button.fireEvent(TestingUtils.clickOnTarget(button));

        verify(mockGazePlay).onDisplayConfigurationManagement();
    }

}
