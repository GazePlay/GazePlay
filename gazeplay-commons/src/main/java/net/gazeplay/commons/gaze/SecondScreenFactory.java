package net.gazeplay.commons.gaze;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecondScreenFactory {

    public static SecondScreen launch() {
        final ObservableList<Screen> screens = Screen.getScreens();

        final Screen screen1 = screens.get(0);
        final Screen screen2 = screens.size() == 1 ? screens.get(0) : screens.get(1);

        log.debug("screen1.getBounds() = " + screen1.getBounds());
        log.debug("screen2.getBounds() = " + screen2.getBounds());

        final Stage stage2 = new Stage();
        stage2.setScene(new Scene(new Label("primary")));
        stage2.setX(screen2.getVisualBounds().getMinX());
        stage2.setY(screen2.getVisualBounds().getMinY());
        stage2.setWidth(screen1.getBounds().getWidth());
        stage2.setHeight(screen1.getBounds().getHeight());

        final Group root = new Group();
        final Scene scene = new Scene(
            root,
            screen1.getBounds().getWidth(),
            screen1.getBounds().getHeight(),
            Color.BLACK
        );

        final Lighting[][] lightingArray = SecondScreen.makeLighting(root, screen2.getBounds());

        stage2.setScene(scene);

        stage2.show();

        return new SecondScreen(stage2, lightingArray);
    }
}
