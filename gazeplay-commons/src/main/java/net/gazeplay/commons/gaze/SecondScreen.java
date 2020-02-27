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

    private final Lighting[][] lightingArray;

    SecondScreen(final Stage stage2, final Lighting[][] lightingArray) {
        this.stage2 = stage2;
        this.lightingArray = lightingArray;
    }

    static Lighting[][] makeLighting(final Group root, final Rectangle2D screen2Bounds) {

        final int width = (int) screen2Bounds.getWidth();
        final int height = (int) screen2Bounds.getHeight();

        final Lighting[][] lightingArray = new Lighting[width / pixelWidth][height / pixelWidth];

        for (int i = 0; i < lightingArray.length; i++) {
            for (int j = 0; j < lightingArray[i].length; j++) {
                lightingArray[i][j] = new Lighting(
                    i * pixelWidth,
                    j * pixelWidth,
                    pixelWidth,
                    lightingLength,
                    lightingColor
                );
                root.getChildren().add(lightingArray[i][j]);
            }
        }

        return lightingArray;
    }

    public void close() {
        stage2.close();
    }

    public void light(final Point2D rawCoordinates) {
        final int x = (int) (rawCoordinates.getX() / pixelWidth);
        final int y = (int) (rawCoordinates.getY() / pixelWidth);

        if ((x < 0 || x >= lightingArray.length) ||
            (y < 0 || y >= lightingArray[x].length)) {
            return;
        }

        lightingArray[x][y].enter();
    }

    @Override
    public void gazeMoved(final Point2D position) {
        light(position);
    }
}
