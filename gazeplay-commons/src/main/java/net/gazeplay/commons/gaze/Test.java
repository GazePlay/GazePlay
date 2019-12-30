package net.gazeplay.commons.gaze;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;

/**
 * Created by schwab on 16/08/2016.
 */
public class Test extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Test");

        primaryStage.setFullScreen(true);

        Group root = new Group();

        Scene scene = new Scene(root, 1200, 700, Color.BLACK);

        Circle circle = new Circle(200, 300, 100, Color.YELLOW);

        root.getChildren().add(circle);

        GazeDeviceManagerFactory gazeDeviceManagerFactory = new GazeDeviceManagerFactory();
        GazeDeviceManager gazeDeviceManager = gazeDeviceManagerFactory.get();

        gazeDeviceManager.addEventFilter(circle);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
