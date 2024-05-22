package net.gazeplay.games.cups2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.games.cups2.strategy.StrategyBuilder;
import net.gazeplay.games.cups2.utils.*;
import net.gazeplay.games.cups2.action.*;

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
    private final StrategyBuilder strategy;
    private final List<Action> actions;
    private final List<Cup> cups;

    public CupsAndBalls(IGameContext gameContext, Stats stats, int nbCups, double gameSeed) {
        super();
        Config.nbCups = nbCups;
        CupsAndBalls.gameContext = gameContext;
        CupsAndBalls.stats = stats;
        this.strategy = StrategyBuilder.newInstanceOf(Config.STRATEGY_TYPE);
        this.actions = new ArrayList<>();
        this.cups = new ArrayList<>(nbCups);
        for (int i = 0; i < nbCups; i++)
            this.cups.add(new Cup(i));
        random.setSeed(gameSeed);
        this.ball = new Ball(cups.get(random.nextInt(nbCups)));

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
        this.strategy = StrategyBuilder.newInstanceOf(Config.STRATEGY_TYPE);
        this.actions = new ArrayList<>();
        this.cups = new ArrayList<>(nbCups);
        for (int i = 0; i < nbCups; i++)
            this.cups.add(new Cup(i));
        random.setSeed(System.currentTimeMillis());
        stats.setGameSeed(random.getSeed());
        this.ball = new Ball(cups.get(random.nextInt(nbCups)));

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

        if (actions.isEmpty())
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

    private Void onActionFinished(Void unused) {
        for (Cup cup : cups)
            cup.setCurrentIndex(cups.indexOf(cup));

        if (actions.isEmpty()) {
//            dispose();
//            gameContext.clear();
            launch();
        } else {
            actions.remove(0).execute(this::onActionFinished);
        }

        return null;
    }
}
