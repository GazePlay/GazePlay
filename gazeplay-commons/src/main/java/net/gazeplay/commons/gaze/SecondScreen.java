package net.gazeplay.commons.gaze;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
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

    private SecondScreen(final Stage stage2, final Lighting[][] lightings) {
        this.stage2 = stage2;
        this.lightings = lightings;
    }

    public static SecondScreen launch() {
        final ObservableList<Screen> screens = Screen.getScreens();

        final Screen screen1 = screens.get(0);

        final Screen screen2;
        if (screens.size() == 1) {
            screen2 = screens.get(0);
        } else {
            screen2 = screens.get(1);
        }

        log.debug("screen1.getBounds() = " + screen1.getBounds());
        log.debug("screen2.getBounds() = " + screen2.getBounds());

        final Stage stage2 = new Stage();
        stage2.setScene(new Scene(new Label("primary")));
        stage2.setX(screen2.getVisualBounds().getMinX());
        stage2.setY(screen2.getVisualBounds().getMinY());
        stage2.setWidth(screen1.getBounds().getWidth());
        stage2.setHeight(screen1.getBounds().getHeight());
        final Group root = new Group();
        final Scene scene = new Scene(root, screen1.getBounds().getWidth(), screen1.getBounds().getHeight(), Color.BLACK);

        final Lighting[][] lightings = makeLighting(root, screen2.getBounds());

        stage2.setScene(scene);

        stage2.show();

        return new SecondScreen(stage2, lightings);
    }

    private static Lighting[][] makeLighting(final Group root, final Rectangle2D screen2Bounds) {

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

    public void light(final javafx.geometry.Point2D rawCoordinates) {
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
    public void gazeMoved(final javafx.geometry.Point2D position) {
        light(position);
    }
}
