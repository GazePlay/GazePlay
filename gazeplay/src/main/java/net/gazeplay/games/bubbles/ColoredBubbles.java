package net.gazeplay.games.bubbles;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.SecondScreen;

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
        Scene scene = new Scene(root, 1200, 700);
        scene.setFill(new ImagePattern(new Image("data/bubble/images/underwater-treasures.jpg")));
        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));
        primaryStage.setScene(scene);

        GameContext gameContext = GameContext.newInstance(null);

        new Bubble(gameContext, Bubble.COLOR, null, true);
        primaryStage.show();

        SecondScreen.launch();

    }

}
