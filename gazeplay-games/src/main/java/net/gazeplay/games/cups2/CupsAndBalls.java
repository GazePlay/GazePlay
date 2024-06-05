package net.gazeplay.games.cups2;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.games.cups2.action.Reveal;
import net.gazeplay.games.cups2.action.RevealAll;
import net.gazeplay.games.cups2.model.PlayerModel;
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

    private static void setGameContext(IGameContext gameContext) {
        CupsAndBalls.gameContext = gameContext;
    }

    @Getter
    private static Stats stats;

    private static void setStats(Stats stats) {
        CupsAndBalls.stats = stats;
    }

    @Getter
    private static PlayerModel playerModel;

    private static void setPlayerModel(PlayerModel playerModel) {
        CupsAndBalls.playerModel = playerModel;
    }

    private final Ball ball;
    private final StrategyBuilder strategy = StrategyBuilder.newInstanceOf(Config.STRATEGY_TYPE);
    private final List<Action> actions = new ArrayList<>();
    private final List<Cup> cups = new ArrayList<>();

    @Getter
    private static Phase currentPhase = Phase.OBSERVATION;

    @Getter
    private static Action currentAction = null;  // Invalid when currentPhase == Phase.SELECTION

    public CupsAndBalls(IGameContext gameContext, Stats stats, int nbCups, double gameSeed) {
        super();
        Config.setNbCups(nbCups);
        setGameContext(gameContext);
        setStats(stats);
        if (Config.STRATEGY_TYPE == StrategyBuilder.Type.ADAPTIVE)
            setPlayerModel(new PlayerModel());
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
        Config.setNbCups(nbCups);
        setGameContext(gameContext);
        setStats(stats);
        if (Config.STRATEGY_TYPE == StrategyBuilder.Type.ADAPTIVE)
            setPlayerModel(new PlayerModel());
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

        currentPhase = Phase.OBSERVATION;
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
        if (playerModel != null)
            playerModel.dispose();
    }

    private Void onCupSelected(Cup cup) {
        if (cup.hasBall())
            (currentAction = new Reveal(cup)).execute(this::onRightRevealFinished);
        else
            (currentAction = new Reveal(cup)).execute(this::onWrongRevealFinished);
        for (Cup c : cups)
            c.disableSelection();
        return null;
    }

    private Void onRightRevealFinished(Void unused) {
//        dispose();
//        gameContext.clear();
//        if (Config.getNbCups() < Config.MAX_NB_CUPS) {
//            Config.setNbCups(Config.getNbCups() + 1);
//            cups.add(new Cup(cups.size(), this::onCupSelected));
//            gameContext.getChildren().add(cups.get(cups.size() - 1));
//            Cup.swapBall(ball.getContainer(), cups.get(random.nextInt(cups.size())));
//            actions.add(new RevealAll(cups));
//        }
        launch();
        return null;
    }

    private Void onWrongRevealFinished(Void unused) {
        for (Cup c : cups)
            c.enableSelection();
        return null;
    }

    private Void onActionFinished(Void unused) {
        for (Cup cup : cups)
            cup.setCurrentIndex(cups.indexOf(cup));

        if (actions.isEmpty()) {
            setupSelection();
        } else {
            (currentAction = actions.remove(0)).execute(this::onActionFinished);
        }

        return null;
    }

    private void setupSelection() {
        currentPhase = Phase.SELECTION;
        for (Cup cup : cups)
            cup.enableSelection();
    }

    public enum Phase {
        SELECTION, OBSERVATION
    }
}
