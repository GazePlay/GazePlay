package net.gazeplay.games.gazeplayEval;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.games.gazeplayEval.config.*;
import net.gazeplay.games.gazeplayEval.round.EvalRound;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Function;


@Slf4j
public class GazeplayEval implements GameLifeCycle {
    @Getter
    private final ReplayablePseudoRandom random;

    private Iterator<EvalRound> rounds = null;
    private final EvalResults results = new EvalResults();
    @Getter
    private final EvalConfig config;
    private final KeyboardEventHandler keyboardHandler = new KeyboardEventHandler();

    @Getter
    private EvalRound currentRound;
    private final Timeline timeLimiter = new Timeline(); // Time limit for the round
    private final Function<Void, Void> onRoundFinishDummy;

    public GazeplayEval(double gameSeed) {
        random = new ReplayablePseudoRandom(gameSeed);

        log.info("New instance, with seed " + gameSeed);

        GameState.context.startScoreLimiter();
        GameState.context.startTimeLimiter();
        GameState.stats.setGameSeed(gameSeed);
        GameState.stats.setTargetAOIList(new ArrayList<>());

        onRoundFinishDummy = (aVoid) -> {
            this.onRoundFinish();
            return null;
        };

        try {
            config = new EvalConfig();  // Load config from context
        } catch (Exception e) {
            log.error("Error while loading the configuration file for the game " + GameState.variant.getNameGame(), e);
            throw new RuntimeException(e);
        }

        GameState.context.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
    }

    @Override
    public void launch() {
        if (currentRound != null) {
            log.warn("Trying to launch a new round while the current one is still running");
            return;
        }
        log.info("Starting new round");

        if (rounds == null || !rounds.hasNext()) {  // Play the evaluation, if not started yet or restarting
            rounds = config.getItems().map(item -> new EvalRound(item, onRoundFinishDummy)).iterator();
            results.clear();
            results.startTime = System.currentTimeMillis();
        }

        currentRound = rounds.next();

        GameState.context.setLimiterAvailable();
        GameState.context.getGazeDeviceManager().addStats(GameState.stats);
        GameState.context.firstStart();

        if (currentRound.getConfig().getTimeLimit() > 0) {
            timeLimiter.getKeyFrames().clear();  // Make sure there is no other Key Frame
            timeLimiter.getKeyFrames().add(new KeyFrame(new Duration(currentRound.getConfig().getTimeLimit()), (event) -> onRoundFinish()));
            timeLimiter.playFromStart();
        }

        keyboardHandler.enable();
        currentRound.launch();
    }

    @Override
    public void dispose() {
        if (currentRound == null) {
            log.warn("Trying to dispose the game twice");
            return;
        }
        log.info("Disposing current round, and retrieving its results");
        keyboardHandler.disable();
        timeLimiter.stop();
        results.add(currentRound.retrieveResults());
        currentRound.dispose();
        currentRound = null;
    }

    private void onRoundFinish() {  // Maybe something to show that the round is finished?
        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(ActiveConfigurationContext.getInstance().getTransitionTime())));
        transition.setOnFinished((event) -> this.afterRoundFinish());
        this.dispose();
        transition.playFromStart();
    }

    private void afterRoundFinish() {
        GameState.context.clear();
        if (rounds.hasNext()) {
            this.launch();
        } else {
            this.finalizeStats();
            GameState.context.updateScore(GameState.stats, this);
            GameState.context.showRoundStats(GameState.stats, this);
        }
    }

    private void finalizeStats() {
        GameState.stats.timeGame = System.currentTimeMillis() - results.startTime;
        GameState.stats.nameScores = new ArrayList<>();  // We cannot leave them 2 to null
        GameState.stats.scores = new ArrayList<>();      // Should maybe change that in stats
        GameState.stats.totalItemsAddedManually = 0;
        results.autoExport();
    }

    private class KeyboardEventHandler implements EventHandler<KeyEvent> {

        private boolean ignoreAnyInput = false;

        @Override
        public void handle(KeyEvent key) {
            if (ignoreAnyInput)
                return;

            if (key.getCode().isWhitespaceKey()) {
                dispose();
                afterRoundFinish();
            }
        }

        public void disable() {
            ignoreAnyInput = true;
        }

        public void enable() {
            ignoreAnyInput = false;
        }
    }
}
