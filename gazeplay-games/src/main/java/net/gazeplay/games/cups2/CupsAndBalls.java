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
    private static PlayerModel playerModel;  // is in charge of estimating the player's skill level

    private static void setPlayerModel(PlayerModel playerModel) {
        CupsAndBalls.playerModel = playerModel;
    }

    private final Ball ball;
    private final StrategyBuilder strategy = StrategyBuilder.newInstanceOf(Config.STRATEGY_TYPE);  // Will build the list of actions in each round
    private final List<Action> actions = new ArrayList<>();
    private final List<Cup> cups = new ArrayList<>();

    @Getter
    private static Phase currentPhase = Phase.OBSERVATION;

    private static void setCurrentPhase(Phase newPhase) {
        currentPhase = newPhase;
    }

    @Getter
    private static Action currentAction = null;  // Invalid when currentPhase == Phase.SELECTION

    private static Action setCurrentAction(Action newAction) {
        return (currentAction = newAction);
    }

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
        // TODO: cups are set up once on their Y coordinate, only changing the X coordinate, but the window could be resized...
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

        Config.nbCupsSubscribe(this::onNbCupsChanged);
    }

    @Override
    public void launch() {
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.setLimiterAvailable();
        stats.notifyNewRoundReady();

        setCurrentPhase(Phase.OBSERVATION);
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

    private Void onNbCupsChanged(Void unused) {
        // Executed when the number of cups changes
        // Create or remove cups to match the new number of cups
        // and if that's the case, interchange the ball with the whole new set of cups,
        // and reveal its location to the player
        if (cups.size() < Config.getNbCups()) {
            for (int i = cups.size(); i < Config.getNbCups(); i++) {
                cups.add(new Cup(i, this::onCupSelected));
                gameContext.getChildren().add(cups.get(i));
            }
            Cup.swapBall(ball.getContainer(), cups.get(random.nextInt(Config.getNbCups())));
        } else if (cups.size() > Config.getNbCups()) {
            Cup.swapBall(ball.getContainer(), cups.get(random.nextInt(Config.getNbCups())));
            for (int i = cups.size() - 1; i >= Config.getNbCups(); i--) {
                gameContext.getChildren().remove(cups.get(i));
                cups.remove(i).dispose();
            }
        } else
            return null;
        actions.add(0, new RevealAll(cups));
        return null;
    }

    private Void onCupSelected(Cup cup) {
        // Executed during the selection phase when the player picked a cup
        if (cup.hasBall())
            // Set the new current action and execute it, then call the method corresponding to whether the ball is inside or not
            setCurrentAction(new Reveal(cup)).execute(this::onRightRevealFinished);
        else
            setCurrentAction(new Reveal(cup)).execute(this::onWrongRevealFinished);
        for (Cup c : cups)
            c.disableSelection();
        return null;
    }

    private Void onRightRevealFinished(Void unused) {
        // Start a new round and inform the player model
        getPlayerModel().selectedRightCup();
        launch();
        return null;
    }

    private Void onWrongRevealFinished(Void unused) {
        // Start again the selection phase and inform the player model
        for (Cup c : cups)
            c.enableSelection();
        getPlayerModel().selectedWrongCup();
        return null;
    }

    private Void onActionFinished(Void unused) {
        for (Cup cup : cups)  // Update the index of all cups in case some were moved
            cup.setCurrentIndex(cups.indexOf(cup));

        setCurrentAction(null);
        if (actions.isEmpty()) {
            // When there is no more action then the round is finished, so we set up a new selection phase
            setupSelection();
        } else {
            new Timeline(new KeyFrame(
                Duration.millis(Config.INTER_ROUND_DELAY),
                e -> setCurrentAction(actions.remove(0)).execute(this::onActionFinished)
                // Calls itself after the end of the executed action
            )).play();
        }

        return null;
    }

    private void setupSelection() {
        setCurrentPhase(Phase.SELECTION);
        for (Cup cup : cups)
            cup.enableSelection();
    }

    public enum Phase {
        SELECTION, OBSERVATION
    }
}
