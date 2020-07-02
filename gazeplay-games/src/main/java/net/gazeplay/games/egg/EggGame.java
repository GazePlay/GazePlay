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

    private boolean limiterT;
    private long startTime = 0;
    private long endTime = 0;

    public EggGame(final IGameContext gameContext, final Stats stats, final int numOfTurns) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.numberOfTurns = numOfTurns;
        this.limiterT = gameContext.getConfiguration().isLimiterT();
    }

    @Override
    public void launch() {
        start();
        final Configuration config = gameContext.getConfiguration();

        final Egg egg = createEgg(config);

        gameContext.getChildren().add(egg);

        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    private Egg createEgg(final Configuration config) {
        final javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final double eggHeight = gameDimension2D.getHeight() / 2;
        final double eggWidth = 3. * eggHeight / 4.;

        final int fixationlength = config.getFixationLength();

        final double positionX = gameDimension2D.getWidth() / 2 - eggWidth / 2;
        final double positionY = gameDimension2D.getHeight() / 2 - eggHeight / 2;

        return new Egg(positionX, positionY, eggWidth, eggHeight, gameContext, stats, this, fixationlength, numberOfTurns);
    }

    void updateScore() {
        if (limiterT) {
            stop();
            if (time(startTime, endTime) >= gameContext.getConfiguration().getLimiterTime()) {
                gameContext.playWinTransition(0, event1 -> gameContext.showRoundStats(stats, this));
            }
        }
    }

    private void start() {
        startTime = System.currentTimeMillis();
    }

    private void stop() {
        endTime = System.currentTimeMillis();
    }

    public double time(double start, double end) {
        return (end - start) / 1000;
    }

}
