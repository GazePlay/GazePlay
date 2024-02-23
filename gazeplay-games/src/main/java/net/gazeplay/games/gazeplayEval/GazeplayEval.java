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
    private final EvalConfig config;

    private final Function<Void, Void> onRoundFinishDummy;
    private final KeyboardEventHandler keyboardHandler = new KeyboardEventHandler();

    @Getter
    private EvalRound currentRound;

    private long startTime = 0;

    public GazeplayEval(double gameSeed) {
        random = new ReplayablePseudoRandom(gameSeed);

        GameState.context.startScoreLimiter();
        GameState.context.startTimeLimiter();
        GameState.stats.setGameSeed(gameSeed);
        GameState.stats.setTargetAOIList(new ArrayList<>());

        onRoundFinishDummy = (aVoid) -> {
            this.onRoundFinish();
            return null;
        };

        try {
            config = new EvalConfig();
        } catch (Exception e) {
            log.error("Error while loading the configuration file for the game " + GameState.variant.getNameGame(), e);
            throw new RuntimeException(e);
        }

        GameState.context.getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, keyboardHandler);
    }

    @Override
    public void launch() {
        if (currentRound != null) {
            log.error("Trying to launch a new round while the current one is still running");
            return;
        }
        log.info("Starting new round");

        if (startTime == 0)
            startTime = System.currentTimeMillis();

        if (rounds == null || !rounds.hasNext())  // Play the evaluation, if not started yet or restarting
            rounds = config.getItems().map(item -> new EvalRound(item, onRoundFinishDummy)).iterator();

        currentRound = rounds.next();

        GameState.context.setLimiterAvailable();
        GameState.context.getGazeDeviceManager().addStats(GameState.stats);
        GameState.context.firstStart();

        keyboardHandler.enable();
        currentRound.launch();
    }

    @Override
    public void dispose() {
        if (currentRound == null) {
            log.warn("Trying to dispose the game twice");
            return;
        }
        log.info("Disposing current round");
        keyboardHandler.disable();
        currentRound.dispose();
        currentRound = null;
    }

    private void finalizeStats() {
        GameState.stats.timeGame = System.currentTimeMillis() - startTime;
        GameState.stats.nameScores = new ArrayList<>();
        GameState.stats.scores = new ArrayList<>();
        GameState.stats.totalItemsAddedManually = 0;
        switch (config.getOutputType()) {
            case CSV -> this.exportToCSV();
            case XLS -> this.exportToExcel();
            case ALL -> {
                this.exportToCSV();
                this.exportToExcel();
            }
            default -> log.warn("No Output set or wrong statement");
        }
    }

    private void exportToExcel() {
    }

    private void exportToCSV() {
    }

    private void onRoundFinish() {
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
//            this.resetFromReplay();
            GameState.context.showRoundStats(GameState.stats, this);
        }
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
