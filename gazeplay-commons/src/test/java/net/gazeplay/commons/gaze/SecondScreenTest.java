package net.gazeplay.commons.gaze;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class SecondScreenTest {

    @Mock
    private Stage mockStage;

    private Group group;
    private Lighting[][] lightingArray;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        group = new Group();
        Rectangle2D screenBounds = new Rectangle2D(0, 0, 1920, 1080);
        lightingArray = SecondScreen.makeLighting(group, screenBounds);
    }

    @Test
    void shouldMakeLighting() {
        assertEquals(1920 / 20, lightingArray.length);
        assertEquals(1080 / 20, lightingArray[0].length);
        assertEquals((1920 / 20) * (1080 / 20), group.getChildren().size());
    }

    @Test
    void shouldCloseTheStage() {
        SecondScreen secondScreen = new SecondScreen(mockStage, lightingArray);
        secondScreen.close();

        verify(mockStage).close();
    }

    @Test
    void shouldNotLightCoordinatesOutOfBounds() {
        List<Point2D> answerList = List.of(
            new Point2D(-1, 1),
            new Point2D(1, -1),
            new Point2D(lightingArray.length * 20 + 1, 0),
            new Point2D(0, lightingArray[0].length * 20 + 1)
        );

        for (Point2D point2D : answerList) {
            SecondScreen secondScreen = new SecondScreen(mockStage, lightingArray);
            assertDoesNotThrow(() -> secondScreen.light(point2D));
        }
    }

    @Test
    void shouldLightCoordinates() {
        List<Point2D> answerList = List.of(
            new Point2D(1, 1),
            new Point2D(lightingArray.length * 20 - 1, 0),
            new Point2D(0, lightingArray[0].length * 20 - 1)
        );

        Lighting[][] mockLighting = lightingArray;

        for (int i = 0; i < mockLighting.length; i++) {
            for (int j = 0; j < mockLighting[i].length; j++) {
                mockLighting[i][j] = mock(Lighting.class);
            }
        }

        SecondScreen secondScreen = new SecondScreen(mockStage, mockLighting);

        for (Point2D point2D : answerList) {
            secondScreen.light(point2D);

            verify(mockLighting[(int) point2D.getX() / 20][(int) point2D.getY() / 20]).enter();
        }
    }

    @Test
    void shouldLightWhenGazeMoved() {
        Lighting[][] mockLighting = lightingArray;

        for (int i = 0; i < mockLighting.length; i++) {
            for (int j = 0; j < mockLighting[i].length; j++) {
                mockLighting[i][j] = mock(Lighting.class);
            }
        }

        SecondScreen secondScreen = new SecondScreen(mockStage, mockLighting);
        secondScreen.gazeMoved(new Point2D(1, 1));

        verify(mockLighting[1 / 20][1 / 20]).enter();
    }
}
