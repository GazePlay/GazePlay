package net.gazeplay.games.ladder;

import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

public class Ladder implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;

    Ladder(IGameContext gameContext, Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
        random = new ReplayablePseudoRandom();
    }

    Ladder(IGameContext gameContext, Stats stats, double gameSeed){
        this.gameContext = gameContext;
        this.stats = stats;
        random = new ReplayablePseudoRandom(gameSeed);
    }

    @Override
    public void launch(){

    }

    @Override
    public void dispose(){

    }
}
