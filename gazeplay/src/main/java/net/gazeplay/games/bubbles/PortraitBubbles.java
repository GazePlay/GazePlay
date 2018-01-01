package net.gazeplay.games.bubbles;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.SecondScreen;

/**
 * Created by schwab on 28/08/2016.
 */
public class PortraitBubbles extends Application {

    public static void main(String[] args) {

        Application.launch(PortraitBubbles.class, args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Portrait Bubbles");
        primaryStage.setFullScreen(true);

        Group root = new Group();
        Scene scene = new Scene(root, 1200, 700, Color.BLACK);
        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        GameContext gameContext = new GameContext(null, root, scene);

        new Bubble(gameContext, Bubble.PORTRAIT, null, false);

        primaryStage.show();

        SecondScreen.launch();
    }
}
