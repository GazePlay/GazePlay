package net.gazeplay.games.creampie;

/**
 * Created by schwab on 12/08/2016.
 */

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.SecondScreen;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.stats.ShootGamesStats;
import net.gazeplay.commons.utils.stats.Stats;

public class CreamPie extends Application {

    public static void main(String[] args) {
        Application.launch(net.gazeplay.games.creampie.CreamPie.class, args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("CreamPie");

        primaryStage.setFullScreen(true);

        Group root = new Group();

        Scene scene = new Scene(root, com.sun.glass.ui.Screen.getScreens().get(0).getWidth(),
                com.sun.glass.ui.Screen.getScreens().get(0).getHeight(), Color.BLACK);

        Stats stats = new ShootGamesStats(scene);

        GameContext gameContext = GameContext.newInstance(null);

        launch(gameContext, (ShootGamesStats) stats);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen.launch();
    }

    public static void launch(GameContext gameContext, ShootGamesStats stats) {

        Scene scene = gameContext.getScene();

        Hand hand = new Hand(scene);

        Portrait.RandomPositionGenerator randomPositionGenerator = new Portrait.RandomPositionGenerator(scene);

        Target portrait = new Target(randomPositionGenerator, hand, stats, Portrait.loadAllImages());

        gameContext.getChildren().add(portrait);
        gameContext.getChildren().add(hand);
    }
}
