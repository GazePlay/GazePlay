package net.gazeplay.games.bubbles;

import javafx.application.Application;
import javafx.stage.Stage;
import net.gazeplay.GameContext;

/**
 * Created by schwab on 28/08/2016.
 */
public class PortraitBubbles extends Application {

    public static void main(String[] args) {

        Application.launch(PortraitBubbles.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        GameContext gameContext = GameContext.newInstance(null);

        new Bubble(gameContext, Bubble.PORTRAIT, null, false);

        gameContext.setUpOnStage(primaryStage);
    }
}
