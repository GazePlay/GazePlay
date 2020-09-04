package net.gazeplay.games.creampie;

import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Portrait;
import net.gazeplay.components.RandomPositionGenerator;

/**
 * Created by schwab on 12/08/2016.
 */
public class CreamPie implements GameLifeCycle {

    private final IGameContext gameContext;

    private final Stats stats;

    private final Hand hand;

    private final Target target;

    public CreamPie(IGameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;

        final ImageLibrary imageLibrary = Portrait.createImageLibrary();
        final RandomPositionGenerator randomPositionGenerator = gameContext.getRandomPositionGenerator();

        hand = new Hand();
        target = new Target(randomPositionGenerator, hand, stats, gameContext, imageLibrary, this);

    }

    @Override
    public void launch() {
        gameContext.getChildren().clear();

        gameContext.getChildren().add(target);
        gameContext.getChildren().add(hand);
        gameContext.setLimiterAvailable();
        hand.recomputePosition();

        gameContext.getRoot().widthProperty().addListener((obs, oldVal, newVal) -> hand.recomputePosition());
        gameContext.getRoot().heightProperty().addListener((obs, oldVal, newVal) -> hand.recomputePosition());

        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {
        stats.setTargetAOIList(target.getTargetAOIList());
    }
}
