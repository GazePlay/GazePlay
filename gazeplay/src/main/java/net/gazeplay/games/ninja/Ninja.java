package net.gazeplay.games.ninja;

import javafx.scene.Scene;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

/**
 * Created by schwab on 26/12/2016.
 */
public class Ninja implements GameLifeCycle {

    private final GameContext gameContext;

    private final ShootGamesStats stats;

    public Ninja(GameContext gameContext, ShootGamesStats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
    }

    @Override
    public void launch() {
        Scene scene = gameContext.getScene();

        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(scene);

        Target portrait = new Target(gameContext, randomPositionGenerator, stats, Portrait.loadAllImages());

        gameContext.getChildren().add(portrait);
    }

    @Override
    public void dispose() {

    }
}
