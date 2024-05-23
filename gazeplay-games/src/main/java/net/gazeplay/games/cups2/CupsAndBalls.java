package net.gazeplay.games.cups2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.games.cups2.action.Reveal;
import net.gazeplay.games.cups2.action.RevealAll;
import net.gazeplay.games.cups2.strategy.StrategyBuilder;
import net.gazeplay.games.cups2.utils.*;
import net.gazeplay.games.cups2.action.Action;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CupsAndBalls implements GameLifeCycle {

    public final static ReplayablePseudoRandom random = new ReplayablePseudoRandom();

    @Getter
    private static IGameContext gameContext;
    @Getter
    private static Stats stats;

    private final Ball ball;
    private final StrategyBuilder strategy = StrategyBuilder.newInstanceOf(Config.STRATEGY_TYPE);
    private final List<Action> actions = new ArrayList<>();
    private final List<Cup> cups = new ArrayList<>();

    public CupsAndBalls(IGameContext gameContext, Stats stats, int nbCups, double gameSeed) {
        super();
        Config.nbCups = nbCups;
        CupsAndBalls.gameContext = gameContext;
        CupsAndBalls.stats = stats;
        for (int i = 0; i < nbCups; i++)
            this.cups.add(new Cup(i, this::onCupSelected));
        random.setSeed(gameSeed);
        this.ball = new Ball(cups.get(random.nextInt(nbCups)));
        actions.add(new RevealAll(cups));

        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();

        gameContext.getChildren().addAll(cups);
        gameContext.getChildren().add(ball);
    }

    public CupsAndBalls(IGameContext gameContext, Stats stats, int nbCups) {
        super();
        Config.nbCups = nbCups;
        CupsAndBalls.gameContext = gameContext;
        CupsAndBalls.stats = stats;
        for (int i = 0; i < nbCups; i++)
            this.cups.add(new Cup(i, this::onCupSelected));
        random.setSeed(System.currentTimeMillis());
        stats.setGameSeed(random.getSeed());
        this.ball = new Ball(cups.get(random.nextInt(nbCups)));
        actions.add(new RevealAll(cups));

        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();

        gameContext.getChildren().addAll(cups);
        gameContext.getChildren().add(ball);
    }

    @Override
    public void launch() {
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.setLimiterAvailable();
        stats.notifyNewRoundReady();

        strategy.computeActions(actions, cups, ball.getContainer().getCurrentIndex());

        new Timeline(new KeyFrame(
            Duration.millis(Config.ROUND_DELAY),
            e -> this.onActionFinished(null)
        )).play();
    }

    @Override
    public void dispose() {
        for (Cup cup : cups)
            cup.dispose();
        ball.dispose();
    }

    private Void onCupSelected(Cup cup) {
        (new Reveal(cup)).execute(this::onRevealFinished);
        if (!cup.hasBall())
            actions.add(new RevealAll(cups));
        for (Cup c : cups)
            c.disableSelection();
        return null;
    }

    private Void onRevealFinished(Void unused) {
//        dispose();
//        gameContext.clear();
        launch();
        return null;
    }

    private Void onActionFinished(Void unused) {
        for (Cup cup : cups)
            cup.setCurrentIndex(cups.indexOf(cup));

        if (actions.isEmpty()) {
            setupSelection();
        } else {
            actions.remove(0).execute(this::onActionFinished);
        }

        return null;
    }

    private void setupSelection() {
        for (Cup cup : cups)
            cup.enableSelection();
    }
}
