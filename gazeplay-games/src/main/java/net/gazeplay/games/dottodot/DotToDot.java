package net.gazeplay.games.dottodot;

import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class DotToDot implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    private final DotToDotGameVariant gameVariant;


    public DotToDot(final IGameContext gameContext, final DotToDotGameVariant gameVariant, final Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
    }

    @Override
    public void launch() {
        gameContext.getChildren().clear();
        //gameContext.getChildren().add(portrait);
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose() {
        gameContext.clear();
    }
}
