package net.gazeplay.games.ninja;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.gazeplay.GameContext;
import net.gazeplay.StatsContext;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

/**
 * Created by schwab on 26/12/2016.
 */
public class Ninja extends Application {

    public static void main(String[] args) {
        Application.launch(net.gazeplay.games.ninja.Ninja.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        StatsContext statsContext = StatsContext.newInstance(null);
        ShootGamesStats stats = new ShootGamesStats(statsContext.getScene());

        GameContext gameContext = GameContext.newInstance(null);
        gameContext.createHomeButtonInGameScreen(null, null);

        launch(gameContext, stats);

        gameContext.setUpOnStage(primaryStage);
    }

    public static void launch(GameContext gameContext, ShootGamesStats stats) {
        Scene scene = gameContext.getScene();

        Portrait.RandomPositionGenerator randomPositionGenerator = new Portrait.RandomPositionGenerator(scene);

        Target portrait = new Target(gameContext, randomPositionGenerator, stats, Portrait.loadAllImages());

        gameContext.getChildren().add(portrait);
    }
}
