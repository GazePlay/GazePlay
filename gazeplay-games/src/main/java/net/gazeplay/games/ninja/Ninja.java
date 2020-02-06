package net.gazeplay.games.ninja;

import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;

/**
 * Created by schwab on 26/12/2016.
 */
public class Ninja implements GameLifeCycle {

    private final IGameContext gameContext;

    private final Stats stats;

    private Target portrait;

    private final NinjaGameVariant gameVariant;

    public Ninja(final IGameContext gameContext, final Stats stats, final NinjaGameVariant gameVariant) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
    }

    @Override
    public void launch() {
        portrait = new Target(gameContext, gameContext.getRandomPositionGenerator(), stats,
            Portrait.createImageLibrary(), gameVariant);

        gameContext.getChildren().add(portrait);
    }

    @Override
    public void dispose() {
        gameContext.clear();
        portrait.currentTranslation.stop();
        // portrait = null; <- it introduced a NullPointerException
    }
}
