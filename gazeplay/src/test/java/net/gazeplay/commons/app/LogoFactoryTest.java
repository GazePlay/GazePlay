package net.gazeplay.commons.app;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class LogoFactoryTest {

    @Mock
    private Stage mockStage;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldCreateAnAnimatedLogo() {
        when(mockStage.getHeight()).thenReturn(100d);

        LogoFactory logoFactory = LogoFactory.getInstance();
        Pane result = (Pane) logoFactory.createLogoAnimated(mockStage);

        assertEquals(9, result.getChildren().size());
    }

    @Test
    void shouldCreateStaticLogo() {
        ReadOnlyDoubleProperty mockHeightProperty = new SimpleDoubleProperty();

        when(mockStage.getHeight()).thenReturn(100d);
        when(mockStage.getWidth()).thenReturn(100d);
        when(mockStage.heightProperty()).thenReturn(mockHeightProperty);

        LogoFactory logoFactory = LogoFactory.getInstance();
        ImageView result = (ImageView) logoFactory.createLogoStatic(mockStage);

        assertTrue(result.getImage().getUrl().contains(LogoFactory.staticLogoImagePath));
    }

    @Test
    void shouldResizeStaticLogo() {
        DoubleProperty mockHeightProperty = new SimpleDoubleProperty();

        when(mockStage.getHeight()).thenReturn(100d).thenReturn(50d);
        when(mockStage.getWidth()).thenReturn(100d);
        when(mockStage.heightProperty()).thenReturn(mockHeightProperty);

        LogoFactory logoFactory = LogoFactory.getInstance();
        ImageView result = (ImageView) logoFactory.createLogoStatic(mockStage);

        assertEquals(10d, result.getFitHeight());
        assertEquals(50d, result.getFitWidth());

        mockHeightProperty.set(50d);

        assertEquals(5d, result.getFitHeight());
        assertEquals(50d, result.getFitWidth());
    }
}
