package net.gazeplay.games.creampie;

/**
 * Created by schwab on 12/08/2016.
 */

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

public class CreamPie implements GameLifeCycle {

    private final GameContext gameContext;

    private final ShootGamesStats stats;

    public CreamPie(GameContext gameContext, ShootGamesStats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
    }

    @Override
    public void launch() {
        Scene scene = gameContext.getScene();

        Hand hand = new Hand(scene);

        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(scene);

        Target portrait = new Target(randomPositionGenerator, hand, stats, Portrait.loadAllImages());

        gameContext.getChildren().add(portrait);
        gameContext.getChildren().add(hand);
    }

    @Override
    public void dispose() {

    }
}
