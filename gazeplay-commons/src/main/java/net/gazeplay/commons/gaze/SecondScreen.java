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

    private SecondScreen(Stage stage2, Lighting[][] lightings) {
        this.stage2 = stage2;
        this.lightings = lightings;
    }

    public static SecondScreen launch() {
        ObservableList<Screen> screens = Screen.getScreens();

        final Screen screen1 = screens.get(0);

        final Screen screen2;
        if (screens.size() == 1) {
            screen2 = screens.get(0);
        } else {
            screen2 = screens.get(1);
        }

        log.info("screen1.getBounds() = " + screen1.getBounds());
        log.info("screen2.getBounds() = " + screen2.getBounds());

        Stage stage2 = new Stage();
        stage2.setScene(new Scene(new Label("primary")));
        stage2.setX(screen2.getVisualBounds().getMinX());
        stage2.setY(screen2.getVisualBounds().getMinY());
        stage2.setWidth(screen1.getBounds().getWidth());
        stage2.setHeight(screen1.getBounds().getHeight());
        Group root = new Group();
        Scene scene = new Scene(root, screen1.getBounds().getWidth(), screen1.getBounds().getHeight(), Color.BLACK);

        Lighting[][] lightings = makeLighting(root, screen2.getBounds());

        stage2.setScene(scene);

        stage2.show();

        SecondScreen sc = new SecondScreen(stage2, lightings);
        return sc;
    }

    private static Lighting[][] makeLighting(Group root, Rectangle2D screen2Bounds) {

        int width = (int) screen2Bounds.getWidth();
        int height = (int) screen2Bounds.getHeight();

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

    public void light(javafx.geometry.Point2D rawCoordinates) {
        int x = (int) (rawCoordinates.getX() / pixelWidth);
        int y = (int) (rawCoordinates.getY() / pixelWidth);
        if (x < 0 || x >= lightings.length) {
            return;
        }
        if (y < 0 || y >= lightings[x].length) {
            return;
        }
        lightings[x][y].enter();
    }

    @Override
    public void gazeMoved(javafx.geometry.Point2D position) {
        light(position);
    }
}
