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
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.games.gazeplayEval.config.*;
import net.gazeplay.games.gazeplayEval.round.EvalRound;
import net.gazeplay.games.gazeplayEval.round.RoundResults;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static net.gazeplay.games.gazeplayEval.config.QuestionType.AUDIO;

@Slf4j
public class GazeplayEval implements GameLifeCycle {
    @Getter
    private final ReplayablePseudoRandom random;

    private Iterator<EvalRound> rounds = null;
    private final List<RoundResults> resultsList = new ArrayList<>();
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
            log.error("Trying to launch a new round while the current one is still running");
            return;
        }
        log.info("Starting new round");

        if (rounds == null || !rounds.hasNext()) {  // Play the evaluation, if not started yet or restarting
            rounds = config.getItems().map(item -> new EvalRound(item, onRoundFinishDummy)).iterator();
            resultsList.clear();
            startTime = System.currentTimeMillis();
        }

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
        log.info("Disposing current round, and retrieving its results");
        keyboardHandler.disable();
        resultsList.add(currentRound.retrieveResults());
        currentRound.dispose();
        currentRound = null;
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
            GameState.context.showRoundStats(GameState.stats, this);
        }
    }

    private void finalizeStats() {
        GameState.stats.timeGame = System.currentTimeMillis() - startTime;
        GameState.stats.nameScores = new ArrayList<>();  // We cannot leave them 2 to null
        GameState.stats.scores = new ArrayList<>();      // Should maybe change that in stats
        GameState.stats.totalItemsAddedManually = 0;
        try {
            switch (config.getOutputType()) {
                case CSV -> this.exportToCSV();
                case XLS -> this.exportToExcel();
                case ALL -> {
                    this.exportToCSV();
                    this.exportToExcel();
                }
                default -> log.warn("No Output set or wrong statement");
            }
        } catch (Exception e) {
            log.error("Exception while exporting the results: ", e);
        }
    }

    private void exportToExcel() {
    }

    private void exportToCSV() throws Exception {
        File outputPath = new File(GameState.stats.getGameStatsOfTheDayDirectory() + "\\" + config.getName() + "-" + DateUtils.dateTimeNow() + ".csv");
        GameState.stats.actualFile = outputPath.getPath();

        // Setting up export.line() where each argument is a string to be written in a different column
        PrintWriter out = new PrintWriter(outputPath, StandardCharsets.UTF_16);
        interface CSVexporter {
            void line(String... values);
        }
        CSVexporter export = (String... values) -> {
            for (int i = 0; i < values.length; i++) {
                if (i > 0)
                    out.append(", ");
                out.append(values[i].translateEscapes());
            }
            out.append("\r\n");
        };

        export.line();
        export.line("Nom de l'évaluation", config.getName());
        export.line("ID du patient", config.getPatientId());
        export.line("Fait le", new SimpleDateFormat("dd/MM/yyyy").format(new Date(startTime)));
        export.line("Heure de l'évaluation", new SimpleDateFormat("HH:mm").format(new Date(startTime)));
        export.line("Durée de l'évaluation", Math.round(GameState.stats.timeGame / 100f) * 10 + "s");
        export.line("Nombre d'items", String.valueOf(config.getItemsCount()));
        export.line("Nombre total d'images", String.valueOf(resultsList.stream().mapToInt(RoundResults::getPicturesCount).sum()));
        export.line("Nombre total de sons", String.valueOf(config.getItems().mapToInt(it -> it.getQuestionType() == AUDIO ? 1 : 0).sum()));

        export.line();
        export.line(
            "Numéro d'item",
            "Nombre d'images",
            "Nombres d'images sélectionnées",
            "Question posée (audio ou texte)",
            "Durée limite",
            "Durée de réponse",
            "Images sélectionnées"
        );
        for (int i = 0; i < config.getItemsCount(); i++) {
            final ItemConfig iConfig = config.getItem(i);
            export.line(
                String.valueOf(i + 1), // Numéro d'item
                String.valueOf(resultsList.get(i).getPicturesCount()), // Nombre d'images
                String.valueOf(iConfig.getSelectionsRequired()), // Nombres d'images à sélectionner
                config.getItem(i).getQuestionText(), // Question posée
                Math.round(iConfig.getTimeLimit() / 100f) * 10 + "s", // Temps limite, 2 chiffres après la virgule
                Math.round(resultsList.get(i).getTimeRound() / 100f) * 10 + "s", // Durée de réponse
                String.valueOf(resultsList.get(i).getSelectedPictures().stream().map(
                    pictureCoord -> iConfig.getGrid(pictureCoord.getKey(), pictureCoord.getValue()).getName()
                ).toList()) // Images sélectionnées
            );
        }
        out.close();
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
