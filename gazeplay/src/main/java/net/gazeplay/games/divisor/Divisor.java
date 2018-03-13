package net.gazeplay.games.divisor;

import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * Created by givaudan on 15/02/2018.
 */
public class Divisor implements GameLifeCycle {
    private final GameContext gameContext;
    private final Stats stats;

    public Divisor(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
    }

    @Override
    public void launch() {
        Target target = new Target(gameContext, gameContext.getRandomPositionGenerator(), stats,
                Portrait.loadAllImages(), 0, System.currentTimeMillis(), this);
        gameContext.getChildren().add(target);
    }

    @Override
    public void dispose() {
        this.gameContext.getChildren().removeAll();
    }

}
