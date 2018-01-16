package net.gazeplay.games.creampie;

/**
 * Created by schwab on 12/08/2016.
 */

import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.stats.ShootGamesStats;
import net.gazeplay.commons.utils.stats.Stats;

public class CreamPie implements GameLifeCycle {

    private final GameContext gameContext;

    private final Stats stats;

    public CreamPie(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
    }

    @Override
    public void launch() {
        Hand hand = new Hand();

        Target portrait = new Target(gameContext.getRandomPositionGenerator(), hand, stats, Portrait.loadAllImages());

        gameContext.getChildren().add(portrait);
        gameContext.getChildren().add(hand);

        hand.recomputePosition();
    }

    @Override
    public void dispose() {

    }
}
