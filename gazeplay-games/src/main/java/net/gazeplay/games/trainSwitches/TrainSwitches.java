package net.gazeplay.games.trainSwitches;

import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class TrainSwitches implements GameLifeCycle {

    private final IGameContext gameContext;
    private final TrainSwitchesGameVariant gameVariant;
    private final Stats stats;

    TrainSwitches(final IGameContext gameContext, TrainSwitchesGameVariant gameVariant, final Stats stats){
        this.gameContext = gameContext;
        this.gameVariant = gameVariant;
        this.stats = stats;
    }

    @Override
    public void launch() {
        stats.notifyNewRoundReady();
        gameContext.start();
    }

    @Override
    public void dispose() {
        gameContext.clear();
    }


}
