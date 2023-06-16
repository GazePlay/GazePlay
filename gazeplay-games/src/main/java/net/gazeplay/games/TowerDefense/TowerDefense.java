package net.gazeplay.games.TowerDefense;

import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class TowerDefense implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;

    TowerDefense(final IGameContext gameContext, final Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
    }

    @Override
    public void launch() {
        gameContext.start();
        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {
        gameContext.clear();
    }

}
