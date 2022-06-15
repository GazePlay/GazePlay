package net.gazeplay.games.ninja;

import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.RandomPositionGenerator;

/**
 * Created by schwab on 26/12/2016.
 */
public class Ninja implements GameLifeCycle {

    private final IGameContext gameContext;

    private final Stats stats;

    private Target portrait;

    private final NinjaGameVariant gameVariant;

    private final ReplayablePseudoRandom randomGenerator;

    private final String variantType;

    public Ninja(final IGameContext gameContext, final Stats stats, final NinjaGameVariant gameVariant) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setCurrentGameSeed(randomGenerator.getSeed());
        this.variantType = gameVariant.getLabel();
    }

    public Ninja(final IGameContext gameContext, final Stats stats, final NinjaGameVariant gameVariant, double gameSeed) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
        this.variantType = gameVariant.getLabel();
    }

    @Override
    public void launch() {
        final RandomPositionGenerator randomPositionGenerator = gameContext.getRandomPositionGenerator();
        randomPositionGenerator.setRandomGenerator(randomGenerator);

        portrait = new Target(gameContext, randomPositionGenerator, stats,
            Portrait.createImageLibrary(randomGenerator), gameVariant, this, randomGenerator, stats.getRoundsDurationReport(), stats.getLevelsReport(), 3000);
        gameContext.setLimiterAvailable();
        gameContext.getChildren().add(portrait);
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.incrementNumberOfGoalsToReach();
    }

    @Override
    public void dispose() {
        gameContext.clear();
        portrait.currentTranslation.stop();
        // portrait = null; <- it introduced a NullPointerException
    }
}
