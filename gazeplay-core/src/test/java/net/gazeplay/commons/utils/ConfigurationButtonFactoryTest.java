package net.gazeplay.commons.utils;

import javafx.geometry.Dimension2D;
import net.gazeplay.GazePlay;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.utils.screen.ScreenDimensionSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
class ConfigurationButtonFactoryTest {

    @Mock
    private GazePlay gazePlay;

    @Mock
    private ScreenDimensionSupplier screenDimensionSupplier;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(gazePlay.getCurrentScreenDimensionSupplier()).thenReturn(screenDimensionSupplier);
        when(screenDimensionSupplier.get()).thenReturn(new Dimension2D(1024, 768));
    }

    @Test
    void shouldCreateConfigurationButton() {
        ConfigurationButton button = ConfigurationButtonFactory.createConfigurationButton(gazePlay);
        button.fireEvent(TestingUtils.clickOnTarget(button));

        verify(gazePlay).onDisplayConfigurationManagement();
    }

}
