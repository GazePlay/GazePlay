package net.gazeplay.games.magiccards;

import javafx.application.Application;
import javafx.stage.Stage;
import net.gazeplay.GameContext;
import net.gazeplay.StatsContext;
import net.gazeplay.commons.utils.stats.HiddenItemsGamesStats;

/**
 * Created by schwab on 17/09/2016.
 */
public class MagicCards extends Application {

    @Override
    public void start(Stage primaryStage) {
        StatsContext statsContext = StatsContext.newInstance(null);
        HiddenItemsGamesStats stats = new HiddenItemsGamesStats(statsContext.getScene());

        GameContext gameContext = GameContext.newInstance(null);

        Card.addCards(gameContext, 2, 2, stats);

        gameContext.setUpOnStage(primaryStage);
    }
}
