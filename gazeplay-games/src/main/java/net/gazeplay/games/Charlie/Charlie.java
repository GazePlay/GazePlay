package net.gazeplay.games.Charlie;

import javafx.geometry.Dimension2D;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

public class Charlie implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final DimensionGameVariant gameVariant;

    private final ReplayablePseudoRandom random;

    private final Dimension2D dimension2D;

    Charlie(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant){

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom();
        this.stats.setGameSeed(random.getSeed());

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
    }

    Charlie(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant, double gameSeed){

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom(gameSeed);

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
    }

    public void launch(){

    }

    public void dispose(){

    }
}
