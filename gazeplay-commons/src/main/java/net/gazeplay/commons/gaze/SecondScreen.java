package net.gazeplay.commons.gaze;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by schwab on 10/09/2016.
 */
@Slf4j
public class SecondScreen implements GazeMotionListener {

    private static final int pixelWidth = 20;
    private static final int lightingLength = 20;
    private static final Color lightingColor = Color.BLUE;

    private final Stage stage2;

    private final Lighting[][] lightings;

    SecondScreen(final Stage stage2, final Lighting[][] lightings) {
        this.stage2 = stage2;
        this.lightings = lightings;
    }

    static Lighting[][] makeLighting(final Group root, final Rectangle2D screen2Bounds) {

        final int width = (int) screen2Bounds.getWidth();
        final int height = (int) screen2Bounds.getHeight();

        final Lighting[][] lightings = new Lighting[width / pixelWidth][height / pixelWidth];

        for (int i = 0; i < lightings.length; i++) {
            for (int j = 0; j < lightings[i].length; j++) {
                lightings[i][j] = new Lighting(i * pixelWidth, j * pixelWidth, pixelWidth, lightingLength,
                    lightingColor);
                root.getChildren().add(lightings[i][j]);
            }
        }

        return lightings;
    }

    public void close() {
        stage2.close();
    }

    public void light(final Point2D rawCoordinates) {
        final int x = (int) (rawCoordinates.getX() / pixelWidth);
        final int y = (int) (rawCoordinates.getY() / pixelWidth);
        if (x < 0 || x >= lightings.length) {
            return;
        }
        if (y < 0 || y >= lightings[x].length) {
            return;
        }
        lightings[x][y].enter();
    }

    @Override
    public void gazeMoved(final Point2D position) {
        light(position);
    }
}
