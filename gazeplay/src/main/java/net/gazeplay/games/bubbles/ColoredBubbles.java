package net.gazeplay.games.bubbles;

import gaze.SecondScreen;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by schwab on 28/08/2016.
 */
public class ColoredBubbles extends Application {

    public static void main(String[] args) {
        Application.launch(ColoredBubbles.class, args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Colored Bubbles");
        primaryStage.setFullScreen(true);

        Group root = new Group();
        Scene scene = new Scene(root, 1200, 700, Color.BLACK);
        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
        primaryStage.setScene(scene);
        Bubble bubble = new Bubble(scene, root, Bubble.COLOR, null);
        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();

    }

}
