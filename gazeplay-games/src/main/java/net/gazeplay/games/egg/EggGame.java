package net.gazeplay.games.egg;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;


@Slf4j
public class EggGame implements GameLifeCycle {

    private final IGameContext gameContext;

    private final Stats stats;

    private final int numberOfTurns;

    private final String type;
    private final String modeImage;

    public EggGame(final IGameContext gameContext, final Stats stats, final int numOfTurns, final String type, final String modeImage) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.numberOfTurns = numOfTurns;
        this.type = type;
        this.modeImage = modeImage;
        gameContext.startTimeLimiter();
    }

    public EggGame(final IGameContext gameContext, final Stats stats, final int numOfTurns, final String type, double gameSeed, final String modeImage) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.numberOfTurns = numOfTurns;
        this.type = type;
        this.modeImage = modeImage;
    }

    @Override
    public void launch() {
        gameContext.start();
        gameContext.setLimiterAvailable();
        final Configuration config = gameContext.getConfiguration();

        final Egg egg = createEgg(config);

        gameContext.getChildren().add(egg);

        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    private Egg createEgg(final Configuration config) {
        return new Egg(gameContext, stats, this, gameContext.getConfiguration().getFixationLength(), numberOfTurns, type, modeImage);
    }

}
