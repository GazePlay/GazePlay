package net.gazeplay.games.gazeplayEval;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.EvalOptions;
import net.gazeplay.commons.eval.EvalDefinition;
import net.gazeplay.commons.eval.EvalDefinition.MediaFolder;
import net.gazeplay.commons.eval.EvalImporter;
import net.gazeplay.commons.eval.EvalLibrary;
import net.gazeplay.commons.eval.EvalProblem;
import net.gazeplay.commons.eval.EvalReader;
import net.gazeplay.commons.eval.EvalScreen;
import net.gazeplay.commons.eval.EvalScreen.Cell;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.HeatMap;
import net.gazeplay.commons.utils.stats.HeatMapGrid;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.gazeplayEval.EvalSessionRecorder.EndReason;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * GazePlay Eval: plays an evaluation created with the GazePlay Eval website and imported in the settings (one variant
 * per imported evaluation), and writes its results (see {@link EvalResultsWriter}).
 * <p>
 * A session: the participant form (fields of the evaluation, filled by the examiner), then the screens in their order:
 * <ul>
 *     <li>transition: a pause, possibly with a fixation cross, which may have to be fixated;</li>
 *     <li>instruction: a text, an image, a video or a sound, possibly with a start button to fixate;</li>
 *     <li>stimuli: a grid of images and/or sounds, selected by fixating them.</li>
 * </ul>
 * In the modes of EVAL_OPTIONS other than the classic one, a screen after each grid offers to do it again or to
 * continue. Keys of the examiner: space (next screen), P (play the sound again), R or left arrow (do the grid again),
 * C or right arrow or enter (continue).
 * <p>
 * Each screen has a number; a screen ends in one place only ({@link #endScreen}), which ignores the timers and
 * callbacks of the screens already ended. The timers, the media and the selection are paused while the configuration
 * menu of the game screen is open.
 */
@Slf4j
public class GazeplayEval implements GameLifeCycle {

    /** Folder of the heatmaps of the screens, in the folder of the session. */
    static final String HEATMAPS_FOLDER = "heatmaps";

    /** Columns of the heatmaps, as those of GazePlay (Stats.HEAT_MAP_COLUMNS, format version 4). */
    private static final int HEAT_MAP_COLUMNS = 400;

    /** Pause after the last selection of a grid without sound, so that the selection is seen. */
    private static final double VALIDATION_PAUSE_MILLIS = 500;

    private enum Phase { MESSAGE, FORM, SCREEN, OPTIONS, END }

    private enum Choice { RESTART, CONTINUE }

    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;
    private final EvalDefinition definition;
    private final List<EvalProblem> loadProblems;
    private final boolean noEvaluation;

    private Phase phase = Phase.MESSAGE;
    private EvalSessionRecorder recorder;
    private Path sessionFolder;
    private ExecutorService screenshotWriter;
    private boolean resultsWritten;

    private int screenIndex;
    private int screenToken;
    private final Map<Integer, Integer> attempts = new HashMap<>();
    private final Map<Integer, List<Cell>> layouts = new HashMap<>();

    // Content of the current screen
    private final List<EvalTarget> targets = new ArrayList<>();
    private final Map<EvalTarget, GridCell> gridCells = new HashMap<>();
    private final List<Animation> timers = new ArrayList<>();
    private final List<MediaPlayer> players = new ArrayList<>();
    private Pane indicatorLayer;
    private Runnable replaySound;
    private boolean soundBlocksSelection;
    private boolean gridDone;

    // Choice screen
    private final Map<EvalTarget, Choice> choiceTargets = new HashMap<>();
    private Choice proposedChoice;
    private final Map<Choice, Rectangle> choiceButtons = new HashMap<>();

    // Configuration menu
    private Timeline menuWatcher;
    private boolean menuOpen;
    private final List<Animation> pausedTimers = new ArrayList<>();
    private final List<MediaPlayer> pausedPlayers = new ArrayList<>();
    private final List<EvalTarget> deactivatedTargets = new ArrayList<>();

    // Recording of the gaze and the mouse, and keys of the examiner
    private Parent sceneRoot;
    private EventHandler<GazeEvent> gazeRecorder;
    private EventHandler<MouseEvent> mouseRecorder;
    private EventHandler<KeyEvent> keyHandler;

    private final List<TextField> participantFields = new ArrayList<>();

    /**
     * A cell of the grid on screen.
     */
    private record GridCell(Cell cell, String position, List<Node> visuals) {
    }

    public GazeplayEval(final IGameContext gameContext, final GazeplayEvalGameVariant gameVariant, final Stats stats) {
        this(gameContext, gameVariant, stats, new ReplayablePseudoRandom());
    }

    public GazeplayEval(final IGameContext gameContext, final GazeplayEvalGameVariant gameVariant, final Stats stats,
                        final double gameSeed) {
        this(gameContext, gameVariant, stats, new ReplayablePseudoRandom(gameSeed));
    }

    private GazeplayEval(final IGameContext gameContext, final GazeplayEvalGameVariant gameVariant, final Stats stats,
                         final ReplayablePseudoRandom random) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.random = random;
        this.stats.setGameSeed(random.getSeed());
        if (gameVariant == null) {
            noEvaluation = true;
            definition = null;
            loadProblems = List.of();
        } else {
            noEvaluation = false;
            EvalImporter.cleanLeftovers(EvalLibrary.folder());
            EvalReader.Result result = EvalReader.read(EvalLibrary.evaluationFolder(gameVariant.getNameGame()));
            definition = result.definition();
            loadProblems = result.problems();
            if (definition == null) {
                log.warn("Evaluation {} cannot be played: {}", gameVariant.getNameGame(), result.errors());
            }
        }
    }

    @Override
    public void launch() {
        cleanUp();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();

        if (definition == null) {
            showMessage();
            return;
        }
        resultsWritten = false;
        attempts.clear();
        layouts.clear();
        installHandlers();
        if (definition.participantFields().isEmpty()) {
            startSession(new LinkedHashMap<>());
        } else {
            showForm();
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Message, form, session

    private void showMessage() {
        phase = Phase.MESSAGE;
        StringBuilder message = new StringBuilder(translate(noEvaluation ? "EvalNoEvaluation" : "EvalCannotBePlayed"));
        loadProblems.stream().filter(EvalProblem::error).limit(10)
            .forEach(problem -> message.append("\n• ").append(problem.format(gameContext.getTranslator())));
        Text text = centeredText(message.toString(), 28);
        gameContext.getChildren().add(text);
    }

    private void showForm() {
        phase = Phase.FORM;
        Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();

        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));
        form.setPrefSize(dimension.getWidth(), dimension.getHeight());

        Text title = new Text(definition.name());
        title.setFont(Font.font(null, FontWeight.BOLD, 36));
        title.setFill(foreground());

        GridPane fields = new GridPane();
        fields.setHgap(20);
        fields.setVgap(15);
        fields.setAlignment(Pos.CENTER);
        participantFields.clear();
        int row = 0;
        for (String label : definition.participantFields()) {
            Label fieldLabel = new Label(label);
            fieldLabel.setFont(Font.font(22));
            fieldLabel.setTextFill(foreground());
            TextField field = new TextField();
            field.setPrefColumnCount(25);
            field.setFont(Font.font(22));
            // The letters typed must not reach the game screen (Q leaves the game)
            EventHandler<KeyEvent> keepKeys = e -> {
                if (e.getCode() != KeyCode.TAB && e.getCode() != KeyCode.ENTER) {
                    e.consume();
                }
            };
            field.addEventHandler(KeyEvent.KEY_PRESSED, keepKeys);
            field.addEventHandler(KeyEvent.KEY_RELEASED, keepKeys);
            field.setOnAction(e -> submitForm());
            fieldLabel.setLabelFor(field);
            fields.add(fieldLabel, 0, row);
            fields.add(field, 1, row);
            participantFields.add(field);
            row++;
        }

        Text hint = new Text(translate("EvalFormHint"));
        hint.setFont(Font.font(20));
        hint.setFill(foreground());

        form.getChildren().addAll(title, fields, hint);
        gameContext.getChildren().add(form);
        if (!participantFields.isEmpty()) {
            Platform.runLater(() -> participantFields.get(0).requestFocus());
        }
    }

    private void submitForm() {
        if (phase != Phase.FORM) {
            return;
        }
        Map<String, String> participant = new LinkedHashMap<>();
        for (int i = 0; i < participantFields.size(); i++) {
            participant.put(definition.participantFields().get(i), participantFields.get(i).getText().trim());
        }
        gameContext.clear();
        startSession(participant);
    }

    private void startSession(Map<String, String> participant) {
        Configuration config = gameContext.getConfiguration();
        recorder = new EvalSessionRecorder(definition.name(), System::currentTimeMillis);
        recorder.getParticipant().putAll(participant);
        Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Map<String, Object> context = recorder.getContext();
        context.put("application", "GazePlay Learning");
        context.put("dossierEvaluation", definition.folder().toAbsolutePath().toString());
        context.put("nombreEcransEvaluation", definition.screens().size());
        context.put("formatResultats", definition.resultFormat().name());
        context.put("modePassation", config.getEvalOptions().name());
        context.put("modeEntree", config.getEyeTracker());
        context.put("delaiAvantSelectionMs", config.getDelayBeforeSelectionTime());
        context.put("sonBloqueSelection", config.isEvalSoundBlocksSelection());
        context.put("dureeFixationChoixMs", config.getFixationLength());
        context.put("graine", random.getSeed());
        context.put("largeurZoneJeu", Math.round(dimension.getWidth()));
        context.put("hauteurZoneJeu", Math.round(dimension.getHeight()));

        try {
            sessionFolder = createSessionFolder(GazePlayDirectories.getEvaluationResultsFolder().toPath(),
                LocalDate.now());
        } catch (IOException e) {
            // The results are tried again at the end, in the same place
            log.error("Cannot create the folder of the results of the evaluation", e);
            sessionFolder = GazePlayDirectories.getEvaluationResultsFolder().toPath()
                .resolve("Eval_" + EvalResultsWriter.sessionFolderName(recorder.getStartTime()));
        }
        context.put("dossierResultats", sessionFolder.toAbsolutePath().toString());
        screenshotWriter = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "GazePlayEval-screenshots");
            thread.setDaemon(true);
            return thread;
        });
        log.info("Session of the evaluation {} started, results in {}", definition.name(), sessionFolder);

        screenIndex = 0;
        showScreen();
    }

    /**
     * Creates the folder of a session in the results folder (created if needed): "Eval" + number + "_" + yyyy-MM-dd,
     * the number starting from 1 each day and increasing with the sessions of the day (decision of the maintainer),
     * e.g. Eval1_2026-10-27 then Eval2_2026-10-27. Same names as the version of the GazePlayLearning branch, whose
     * folders are in the same place.
     */
    static Path createSessionFolder(Path resultsFolder, LocalDate day) throws IOException {
        Files.createDirectories(resultsFolder);
        String suffix = "_" + day.format(DateTimeFormatter.ISO_LOCAL_DATE);
        for (int number = 1; ; number++) {
            Path folder = resultsFolder.resolve("Eval" + number + suffix);
            try {
                // Atomic: two sessions started at the same time get two different numbers
                return Files.createDirectory(folder);
            } catch (FileAlreadyExistsException e) {
                // next number
            }
        }
    }

    private void finishSession(boolean completed) {
        if (recorder == null || resultsWritten) {
            return;
        }
        if (phase == Phase.SCREEN && recorder.getCurrentScreen() != null && recorder.getCurrentScreen().getEnd() < 0) {
            // Game left during a screen: its heatmap too
            saveHeatmaps();
        }
        recorder.finish(completed);
        writeResults();
    }

    private void writeResults() {
        resultsWritten = true;
        if (screenshotWriter != null) {
            screenshotWriter.shutdown();
            try {
                if (!screenshotWriter.awaitTermination(20, TimeUnit.SECONDS)) {
                    log.warn("Screenshots of the evaluation still being written");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            EvalResultsWriter.write(recorder, definition.resultFormat(), sessionFolder);
            log.info("Results of the evaluation {} written in {}", definition.name(), sessionFolder);
        } catch (IOException e) {
            log.error("Cannot write the results of the evaluation in {}", sessionFolder, e);
        }
    }

    private void showEnd() {
        phase = Phase.END;
        finishSession(true);
        gameContext.getChildren().add(centeredText(translate("EvalFinished"), 40));
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> gameContext.showRoundStats(stats, this));
        pause.play();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Screens

    private void showScreen() {
        clearScreen();
        if (screenIndex >= definition.screens().size()) {
            showEnd();
            return;
        }
        phase = Phase.SCREEN;
        int token = ++screenToken;
        EvalScreen screen = definition.screens().get(screenIndex);
        int attempt = attempts.merge(screenIndex, 1, Integer::sum);
        recorder.beginScreen(screenIndex + 1, attempt, screen.typeName());
        switch (screen) {
            case EvalScreen.Transition transition -> showTransition(transition, token);
            case EvalScreen.Instruction instruction -> showInstruction(instruction, token);
            case EvalScreen.Stimuli stimuli -> showStimuli(stimuli, token);
        }
        if (screen.autoAdvanceSeconds() != null) {
            EndReason reason = screen instanceof EvalScreen.Stimuli ? EndReason.TIME_ELAPSED : EndReason.DELAY_ELAPSED;
            schedule(screen.autoAdvanceSeconds() * 1000, token, () -> endScreen(reason, token));
        }
        activateAfterDelay(token);
    }

    /**
     * Ends the current screen, once: the later calls with the same token, and those of the screens already ended, are
     * ignored.
     */
    private void endScreen(EndReason reason, int token) {
        if (phase != Phase.SCREEN || token != screenToken) {
            return;
        }
        screenToken++;
        EvalScreen screen = definition.screens().get(screenIndex);
        saveHeatmaps();
        recorder.endScreen(reason);
        clearScreen();
        if (screen instanceof EvalScreen.Stimuli && gameContext.getConfiguration().getEvalOptions() != EvalOptions.Classique) {
            showChoice();
        } else {
            screenIndex++;
            showScreen();
        }
    }

    private void showTransition(EvalScreen.Transition transition, int token) {
        if (!transition.fixationCross()) {
            return;
        }
        Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double size = Math.min(dimension.getWidth(), dimension.getHeight()) / 10;
        double centerX = dimension.getWidth() / 2;
        double centerY = dimension.getHeight() / 2;
        Rectangle horizontal = new Rectangle(centerX - size / 2, centerY - size / 14, size, size / 7);
        Rectangle vertical = new Rectangle(centerX - size / 14, centerY - size / 2, size / 7, size);
        horizontal.setFill(foreground());
        vertical.setFill(foreground());
        horizontal.setMouseTransparent(true);
        vertical.setMouseTransparent(true);
        gameContext.getChildren().addAll(horizontal, vertical);
        if (transition.crossFixationSeconds() != null) {
            Rectangle area = transparentArea(centerX - size, centerY - size, 2 * size, 2 * size);
            gameContext.getChildren().add(area);
            addTarget(area, "croix", transition.crossFixationSeconds() * 1000,
                (target, source) -> {
                    recorder.event("fixation_validee", source, target.getName(), null, null, "");
                    endScreen(EndReason.FIXATION, token);
                });
        }
    }

    private void showInstruction(EvalScreen.Instruction instruction, int token) {
        Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        boolean withButton = instruction.startButtonFixationSeconds() != null;
        // Room left at the bottom for the start button
        double mediaHeight = dimension.getHeight() * (withButton ? 0.7 : 0.85);
        double mediaWidth = dimension.getWidth() * 0.85;
        double mediaTop = dimension.getHeight() * (withButton ? 0.05 : 0.075);

        if (instruction.mediaType() != null) {
            switch (instruction.mediaType()) {
                case TEXT -> {
                    Text text = new Text(instruction.content());
                    text.setFont(Font.font(40));
                    text.setFill(foreground());
                    text.setTextAlignment(TextAlignment.CENTER);
                    text.setWrappingWidth(mediaWidth);
                    Bounds bounds = text.getLayoutBounds();
                    text.setLayoutX((dimension.getWidth() - mediaWidth) / 2 - bounds.getMinX());
                    text.setLayoutY(mediaTop + (mediaHeight - bounds.getHeight()) / 2 - bounds.getMinY());
                    gameContext.getChildren().add(text);
                }
                case IMAGE -> {
                    ImageView view = imageView(MediaFolder.IMAGES, instruction.content(), mediaWidth, mediaHeight);
                    if (view != null) {
                        view.setLayoutX((dimension.getWidth() - view.getImage().getWidth()) / 2);
                        view.setLayoutY(mediaTop + (mediaHeight - view.getImage().getHeight()) / 2);
                        gameContext.getChildren().add(view);
                    }
                }
                case VIDEO -> {
                    MediaPlayer player = play(MediaFolder.VIDEOS, instruction.content(), "video", token, null);
                    if (player != null) {
                        MediaView view = new MediaView(player);
                        view.setPreserveRatio(true);
                        view.setFitWidth(mediaWidth);
                        view.setFitHeight(mediaHeight);
                        view.setMouseTransparent(true);
                        // Centred once the size of the video is known
                        view.layoutBoundsProperty().addListener((observable, before, bounds) -> {
                            view.setLayoutX((dimension.getWidth() - bounds.getWidth()) / 2);
                            view.setLayoutY(mediaTop + (mediaHeight - bounds.getHeight()) / 2);
                        });
                        gameContext.getChildren().add(view);
                        replaySound = () -> {
                            player.seek(Duration.ZERO);
                            player.play();
                        };
                    }
                }
                case SOUND -> {
                    Text note = centeredText("♪", 120);
                    gameContext.getChildren().add(note);
                    replaySound = () -> play(MediaFolder.AUDIO, instruction.content(), "son", token, null);
                    replaySound.run();
                }
            }
        }

        if (withButton) {
            double width = Math.min(dimension.getWidth() * 0.3, 420);
            double height = Math.min(dimension.getHeight() * 0.16, 160);
            double x = (dimension.getWidth() - width) / 2;
            double y = dimension.getHeight() * 0.8 - height / 2 + dimension.getHeight() * 0.05;
            Rectangle button = new Rectangle(x, y, width, height);
            button.setArcWidth(40);
            button.setArcHeight(40);
            button.setFill(Color.web(gameContext.getConfiguration().getProgressBarColor(), 0.35));
            button.setStroke(foreground());
            button.setStrokeWidth(4);
            Text label = new Text("▶  " + translate("EvalStartButton"));
            label.setFont(Font.font(null, FontWeight.BOLD, 34));
            label.setFill(foreground());
            label.setMouseTransparent(true);
            Bounds bounds = label.getLayoutBounds();
            label.setLayoutX(x + (width - bounds.getWidth()) / 2 - bounds.getMinX());
            label.setLayoutY(y + (height - bounds.getHeight()) / 2 - bounds.getMinY());
            gameContext.getChildren().addAll(button, label);
            addTarget(button, "bouton", instruction.startButtonFixationSeconds() * 1000,
                (target, source) -> {
                    recorder.event("fixation_validee", source, target.getName(), null, null, "");
                    endScreen(EndReason.FIXATION, token);
                });
        }
    }

    private void showStimuli(EvalScreen.Stimuli stimuli, int token) {
        List<Cell> layout = layouts.computeIfAbsent(screenIndex, index -> arrange(stimuli));
        Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double cellWidth = dimension.getWidth() / stimuli.columns();
        double cellHeight = dimension.getHeight() / stimuli.rows();
        double margin = Math.min(cellWidth, cellHeight) * 0.06;

        List<String> labels = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        for (int index = 0; index < layout.size(); index++) {
            Cell cell = layout.get(index);
            int row = index / stimuli.columns();
            int column = index % stimuli.columns();
            String position = "L" + (row + 1) + "C" + (column + 1);
            if (cell.emptySlot()) {
                labels.add(position + ":-");
                continue;
            }
            labels.add(position + ":" + cell.label());
            if (cell.correct()) {
                expected.add(position + ":" + cell.label());
            }
            double x0 = column * cellWidth + margin;
            double y0 = row * cellHeight + margin;
            double width = cellWidth - 2 * margin;
            double height = cellHeight - 2 * margin;

            List<Node> visuals = new ArrayList<>();
            Rectangle area = null;
            ImageView view = cell.image() == null ? null : imageView(MediaFolder.IMAGES, cell.image(), width, height);
            if (view != null) {
                double imageWidth = view.getImage().getWidth();
                double imageHeight = view.getImage().getHeight();
                view.setLayoutX(x0 + (width - imageWidth) / 2);
                view.setLayoutY(y0 + (height - imageHeight) / 2);
                view.setMouseTransparent(true);
                visuals.add(view);
                // The target is the image shown, with a small margin
                double pad = margin / 2;
                area = transparentArea(view.getLayoutX() - pad, view.getLayoutY() - pad, imageWidth + 2 * pad,
                    imageHeight + 2 * pad);
            }
            if (area == null) {
                // A cell with a sound only (or whose image cannot be read): a frame with a note
                area = transparentArea(x0, y0, width, height);
                area.setStroke(foreground());
                area.getStrokeDashArray().addAll(12d, 8d);
                area.setStrokeWidth(3);
                Text note = new Text("♪");
                note.setFont(Font.font(Math.min(width, height) / 3));
                note.setFill(foreground());
                note.setMouseTransparent(true);
                Bounds bounds = note.getLayoutBounds();
                note.setLayoutX(x0 + (width - bounds.getWidth()) / 2 - bounds.getMinX());
                note.setLayoutY(y0 + (height - bounds.getHeight()) / 2 - bounds.getMinY());
                visuals.add(note);
            }
            gameContext.getChildren().addAll(visuals);
            gameContext.getChildren().add(area);
            visuals.add(area);
            EvalTarget target = addTarget(area, position + ":" + cell.label(), stimuli.fixationSeconds() * 1000,
                (selected, source) -> onCellSelected(stimuli, selected, source, token));
            gridCells.put(target, new GridCell(cell, position, visuals));
        }
        recorder.describeGrid(stimuli.rows(), stimuli.columns(), stimuli.rule().siteValue(), stimuli.toSelect(),
            labels, expected);
        stats.incrementNumberOfGoalsToReach(stimuli.toSelect());

        if (stimuli.screenSound() != null) {
            replaySound = () -> play(MediaFolder.AUDIO, stimuli.screenSound(), "son_ecran", token, null);
            replaySound.run();
        }
    }

    /**
     * @return the cells in the order shown: mixed among the non-empty places when the positions are random (the empty
     * slots stay in place). Kept for the attempts after the first one.
     */
    private List<Cell> arrange(EvalScreen.Stimuli stimuli) {
        List<Cell> cells = new ArrayList<>(stimuli.cells());
        if (!stimuli.randomPositions()) {
            return cells;
        }
        List<Integer> places = new ArrayList<>();
        List<Cell> shown = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            if (!cells.get(i).emptySlot()) {
                places.add(i);
                shown.add(cells.get(i));
            }
        }
        for (int i = shown.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Cell swapped = shown.get(i);
            shown.set(i, shown.get(j));
            shown.set(j, swapped);
        }
        for (int i = 0; i < places.size(); i++) {
            cells.set(places.get(i), shown.get(i));
        }
        return cells;
    }

    private void onCellSelected(EvalScreen.Stimuli stimuli, EvalTarget target, String source, int token) {
        if (token != screenToken || gridDone) {
            return;
        }
        GridCell gridCell = gridCells.get(target);
        target.close();
        Cell cell = gridCell.cell();
        boolean counted = stimuli.rule() == EvalScreen.SelectionRule.ALL || cell.correct();
        Bounds bounds = target.getArea().getBoundsInParent();
        recorder.select(gridCell.position(), cell.label(), cell.correct(), source, counted, bounds.getCenterX(),
            bounds.getCenterY());
        stats.incrementNumberOfGoalsReached();

        if (stimuli.hideAfterSelection()) {
            gridCell.visuals().forEach(node -> node.setVisible(false));
        } else {
            Rectangle frame = new Rectangle(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
            frame.setFill(Color.TRANSPARENT);
            frame.setStroke(Color.web(gameContext.getConfiguration().getProgressBarColor()));
            frame.setStrokeWidth(6);
            frame.setMouseTransparent(true);
            gameContext.getChildren().add(frame);
        }

        gridDone = recorder.getCurrentScreen().getCountedSelections() >= stimuli.toSelect();
        if (gridDone) {
            setTargetsActive(false);
        }
        if (cell.sound() != null) {
            boolean block = gameContext.getConfiguration().isEvalSoundBlocksSelection();
            if (block && !gridDone) {
                soundBlocksSelection = true;
                setTargetsActive(false);
            }
            play(MediaFolder.AUDIO, cell.sound(), "son_case", token, () -> {
                if (gridDone) {
                    endScreen(EndReason.VALIDATED, token);
                } else if (soundBlocksSelection) {
                    soundBlocksSelection = false;
                    setTargetsActive(true);
                }
            });
        } else if (gridDone) {
            schedule(VALIDATION_PAUSE_MILLIS, token, () -> endScreen(EndReason.VALIDATED, token));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Choice after a grid (EVAL_OPTIONS)

    private void showChoice() {
        phase = Phase.OPTIONS;
        int token = ++screenToken;
        EvalOptions mode = gameContext.getConfiguration().getEvalOptions();
        proposedChoice = null;
        recorder.event("ecran_choix", EvalSessionRecorder.SYSTEM, "", null, null, mode.name());

        Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double width = dimension.getWidth() * 0.3;
        double height = dimension.getHeight() * 0.4;
        double y = (dimension.getHeight() - height) / 2;
        double fixation = gameContext.getConfiguration().getFixationLength();
        addChoiceButton(Choice.RESTART, "↺  " + translate("EvalRestart"), dimension.getWidth() * 0.12, y, width,
            height, mode, fixation, token);
        addChoiceButton(Choice.CONTINUE, translate("EvalContinue") + "  →",
            dimension.getWidth() * 0.88 - width, y, width, height, mode, fixation, token);
        activateAfterDelay(token);
    }

    private void addChoiceButton(Choice choice, String text, double x, double y, double width, double height,
                                 EvalOptions mode, double fixationMillis, int token) {
        Rectangle button = new Rectangle(x, y, width, height);
        button.setArcWidth(50);
        button.setArcHeight(50);
        button.setFill(Color.web(gameContext.getConfiguration().getProgressBarColor(), 0.3));
        button.setStroke(foreground());
        button.setStrokeWidth(4);
        Text label = new Text(text);
        label.setFont(Font.font(null, FontWeight.BOLD, 36));
        label.setFill(foreground());
        label.setMouseTransparent(true);
        Bounds bounds = label.getLayoutBounds();
        label.setLayoutX(x + (width - bounds.getWidth()) / 2 - bounds.getMinX());
        label.setLayoutY(y + (height - bounds.getHeight()) / 2 - bounds.getMinY());
        gameContext.getChildren().addAll(button, label);
        choiceButtons.put(choice, button);

        // The examiner chooses with the mouse (when the input mode accepts it) or the keyboard
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> choose(choice, EvalSessionRecorder.MOUSE, token));

        String name = choice == Choice.RESTART ? "recommencer" : "continuer";
        // With the oral support, the gaze on the buttons is only recorded: the examiner chooses
        double fixation = mode == EvalOptions.Autonomie_Soutien_Oral ? -1 : fixationMillis;
        EvalTarget target = addTarget(button, name, fixation, (selected, source) -> {
            if (mode == EvalOptions.Autonomie_Total) {
                choose(choice, source, token);
            } else if (mode == EvalOptions.Autonomie_Soutien_Visuel) {
                propose(choice, source);
            }
        });
        choiceTargets.put(target, choice);
    }

    private void propose(Choice choice, String source) {
        proposedChoice = choice;
        recorder.event("proposition", source, choice == Choice.RESTART ? "recommencer" : "continuer", null, null, "");
        choiceButtons.forEach((buttonChoice, button) -> {
            button.setStroke(buttonChoice == choice ? Color.RED : foreground());
            button.setStrokeWidth(buttonChoice == choice ? 10 : 4);
        });
    }

    private void choose(Choice choice, String source, int token) {
        if (phase != Phase.OPTIONS || token != screenToken) {
            return;
        }
        screenToken++;
        recorder.event("choix_option", source, choice == Choice.RESTART ? "recommencer" : "continuer", null, null, "");
        clearScreen();
        if (choice == Choice.CONTINUE) {
            screenIndex++;
        }
        showScreen();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Targets, timers, media

    /**
     * Adds a target on a shape of the game area; it becomes active after the delay before selection.
     */
    private EvalTarget addTarget(Rectangle area, String name, double fixationMillis,
                                 BiConsumer<EvalTarget, String> onSelected) {
        Pane indicators = indicatorLayer();
        EvalTarget target = new EvalTarget(gameContext, area, name, fixationMillis, indicators, new EvalTarget.Listener() {
            @Override
            public void onCrossing(EvalTarget crossed, String event, String source, double x, double y, long sampleTime) {
                if (recorder != null) {
                    recorder.eventAt(recorder.elapsedAt(sampleTime), event, source, crossed.getName(), x, y, "");
                }
            }

            @Override
            public void onSelected(EvalTarget selected, String source) {
                onSelected.accept(selected, source);
            }
        });
        targets.add(target);
        return target;
    }

    /**
     * @return the layer of the progress indicators, above everything of the screen
     */
    private Pane indicatorLayer() {
        if (indicatorLayer == null || !gameContext.getChildren().contains(indicatorLayer)) {
            indicatorLayer = new Pane();
            indicatorLayer.setMouseTransparent(true);
            indicatorLayer.setPickOnBounds(false);
            gameContext.getChildren().add(indicatorLayer);
        }
        indicatorLayer.toFront();
        return indicatorLayer;
    }

    private void activateAfterDelay(int token) {
        int delay = gameContext.getConfiguration().getDelayBeforeSelectionTime();
        Runnable activate = () -> {
            if (token == screenToken) {
                setTargetsActive(true);
            }
        };
        if (delay > 0) {
            schedule(delay, token, activate);
        } else {
            activate.run();
        }
    }

    private void setTargetsActive(boolean active) {
        if (active && (gridDone || soundBlocksSelection)) {
            return;
        }
        if (menuOpen) {
            // Restored when the menu closes
            deactivatedTargets.clear();
            if (active) {
                targets.stream().filter(target -> !target.isClosed()).forEach(deactivatedTargets::add);
            }
            return;
        }
        for (EvalTarget target : targets) {
            target.setActive(active);
        }
    }

    private void schedule(double millis, int token, Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.millis(Math.max(1, millis)));
        pause.setOnFinished(e -> {
            timers.remove(pause);
            if (token == screenToken) {
                action.run();
            }
        });
        timers.add(pause);
        if (menuOpen) {
            pausedTimers.add(pause);
        } else {
            pause.play();
        }
    }

    /**
     * Plays a sound or a video of the evaluation.
     *
     * @param kind  name of the media in the events ("son", "video", "son_case"...)
     * @param onEnd called at the end of the media, or at once when it cannot be played
     * @return the player, null when the media cannot be played
     */
    private MediaPlayer play(MediaFolder folder, String fileName, String kind, int token, Runnable onEnd) {
        Runnable ended = () -> {
            if (token == screenToken && onEnd != null) {
                onEnd.run();
            }
        };
        try {
            Media media = new Media(definition.media(folder, fileName).toUri().toString());
            MediaPlayer player = new MediaPlayer(media);
            players.add(player);
            player.setOnEndOfMedia(() -> {
                recorder.event("fin_" + kind, EvalSessionRecorder.SYSTEM, fileName, null, null, "");
                ended.run();
            });
            player.setOnError(() -> {
                recorder.event("erreur_media", EvalSessionRecorder.SYSTEM, fileName, null, null,
                    String.valueOf(player.getError()));
                ended.run();
            });
            recorder.event("debut_" + kind, EvalSessionRecorder.SYSTEM, fileName, null, null, "");
            if (menuOpen) {
                pausedPlayers.add(player);
            } else {
                player.play();
            }
            return player;
        } catch (RuntimeException e) {
            log.warn("Cannot play {}", fileName, e);
            recorder.event("erreur_media", EvalSessionRecorder.SYSTEM, fileName, null, null, e.toString());
            Platform.runLater(ended);
            return null;
        }
    }

    /**
     * @return the image of the evaluation reduced to fit in the size, null when it cannot be read
     */
    private ImageView imageView(MediaFolder folder, String fileName, double maxWidth, double maxHeight) {
        Image image = new Image(definition.media(folder, fileName).toUri().toString(), maxWidth, maxHeight, true,
            true, false);
        if (image.isError()) {
            log.warn("Cannot read the image {}", fileName, image.getException());
            recorder.event("erreur_media", EvalSessionRecorder.SYSTEM, fileName, null, null,
                String.valueOf(image.getException()));
            return null;
        }
        return new ImageView(image);
    }

    private Rectangle transparentArea(double x, double y, double width, double height) {
        Rectangle area = new Rectangle(x, y, width, height);
        area.setFill(Color.TRANSPARENT);
        return area;
    }

    private Text centeredText(String content, double size) {
        Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Text text = new Text(content);
        text.setFont(Font.font(size));
        text.setFill(foreground());
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(dimension.getWidth() * 0.8);
        text.setMouseTransparent(true);
        Bounds bounds = text.getLayoutBounds();
        text.setLayoutX(dimension.getWidth() * 0.1 - bounds.getMinX());
        text.setLayoutY((dimension.getHeight() - bounds.getHeight()) / 2 - bounds.getMinY());
        return text;
    }

    /**
     * @return black on a light background, white on a dark one
     */
    private Color foreground() {
        try {
            Color background = Color.web(gameContext.getConfiguration().getBackgroundColorProperty().getValue());
            return background.getBrightness() > 0.6 ? Color.BLACK : Color.WHITE;
        } catch (RuntimeException e) {
            return Color.WHITE;
        }
    }

    private String translate(String key) {
        return gameContext.getTranslator().translate(key);
    }

    /**
     * Removes the content of the screen: targets, timers, media, nodes.
     */
    private void clearScreen() {
        targets.forEach(EvalTarget::close);
        targets.clear();
        gridCells.clear();
        choiceTargets.clear();
        choiceButtons.clear();
        timers.forEach(Animation::stop);
        timers.clear();
        pausedTimers.clear();
        players.forEach(player -> {
            player.stop();
            player.dispose();
        });
        players.clear();
        pausedPlayers.clear();
        deactivatedTargets.clear();
        replaySound = null;
        indicatorLayer = null;
        gridDone = false;
        soundBlocksSelection = false;
        gameContext.clear();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Recording, keys, configuration menu

    private void installHandlers() {
        sceneRoot = gameContext.getPrimaryScene().getRoot();
        gazeRecorder = e -> {
            // One event per sample, fired on the root (see Stats)
            if (e.getSource() == sceneRoot && e.getTarget() == sceneRoot && isRecording()) {
                Point2D scene = sceneRoot.localToScene(e.getX(), e.getY());
                recordSample(EvalSessionRecorder.GAZE, scene, recorder.elapsedAt(e.getSampleTime()));
            }
        };
        mouseRecorder = e -> {
            if ((e.getEventType() == MouseEvent.MOUSE_MOVED || e.getEventType() == MouseEvent.MOUSE_DRAGGED)
                && isRecording() && !menuOpen) {
                recordSample(EvalSessionRecorder.MOUSE, new Point2D(e.getSceneX(), e.getSceneY()), recorder.elapsed());
            }
        };
        sceneRoot.addEventFilter(GazeEvent.ANY, gazeRecorder);
        sceneRoot.addEventFilter(MouseEvent.ANY, mouseRecorder);

        keyHandler = this::onKey;
        gameContext.getPrimaryScene().addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);

        menuWatcher = new Timeline(new KeyFrame(Duration.millis(150), e -> watchMenu()));
        menuWatcher.setCycleCount(Animation.INDEFINITE);
        menuWatcher.play();
    }

    private boolean isRecording() {
        return recorder != null && (phase == Phase.SCREEN || phase == Phase.OPTIONS);
    }

    private void recordSample(String source, Point2D scenePosition, long time) {
        Point2D position = gameContext.getRoot().sceneToLocal(scenePosition);
        if (position == null) {
            return;
        }
        String under = "-";
        for (EvalTarget target : targets) {
            if (target.getArea().isVisible() && target.getArea().getBoundsInParent().contains(position)) {
                under = target.getName();
                break;
            }
        }
        recorder.sample(time, source, position.getX(), position.getY(), under);
    }

    private void onKey(KeyEvent event) {
        KeyCode code = event.getCode();
        switch (phase) {
            case FORM -> {
                if (code == KeyCode.ENTER) {
                    event.consume();
                    submitForm();
                }
            }
            case SCREEN -> {
                if (code == KeyCode.SPACE) {
                    event.consume();
                    recorder.event("touche", EvalSessionRecorder.KEYBOARD, "espace", null, null, "écran suivant");
                    endScreen(EndReason.EXAMINER, screenToken);
                } else if (code == KeyCode.P && replaySound != null) {
                    recorder.event("touche", EvalSessionRecorder.KEYBOARD, "P", null, null, "son rejoué");
                    replaySound.run();
                }
            }
            case OPTIONS -> {
                if (code == KeyCode.R || code == KeyCode.LEFT) {
                    choose(Choice.RESTART, EvalSessionRecorder.KEYBOARD, screenToken);
                } else if (code == KeyCode.C || code == KeyCode.RIGHT) {
                    choose(Choice.CONTINUE, EvalSessionRecorder.KEYBOARD, screenToken);
                } else if (code == KeyCode.ENTER) {
                    choose(proposedChoice == null ? Choice.CONTINUE : proposedChoice, EvalSessionRecorder.KEYBOARD,
                        screenToken);
                }
            }
            default -> {
                // nothing
            }
        }
    }

    /**
     * Pauses the screen while the configuration menu is open, and resumes it when it closes.
     */
    private void watchMenu() {
        boolean open = Stats.isConfigMenuOpen();
        if (open == menuOpen || recorder == null) {
            return;
        }
        if (open) {
            recorder.event("menu_ouvert", EvalSessionRecorder.SYSTEM, "", null, null, "");
            pausedTimers.clear();
            for (Animation timer : timers) {
                if (timer.getStatus() == Animation.Status.RUNNING) {
                    timer.pause();
                    pausedTimers.add(timer);
                }
            }
            pausedPlayers.clear();
            for (MediaPlayer player : players) {
                if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                    player.pause();
                    pausedPlayers.add(player);
                }
            }
            deactivatedTargets.clear();
            for (EvalTarget target : targets) {
                if (target.isActive()) {
                    target.setActive(false);
                    deactivatedTargets.add(target);
                }
            }
            menuOpen = true;
        } else {
            menuOpen = false;
            recorder.event("menu_ferme", EvalSessionRecorder.SYSTEM, "", null, null, "");
            pausedTimers.forEach(Animation::play);
            pausedTimers.clear();
            pausedPlayers.forEach(MediaPlayer::play);
            pausedPlayers.clear();
            deactivatedTargets.stream().filter(target -> !target.isClosed()).forEach(target -> target.setActive(true));
            deactivatedTargets.clear();
        }
    }

    /**
     * Saves the heatmaps of the current screen in the heatmaps folder of the session: its image with the heatmap of the
     * gaze, and the one of the mouse, drawn as those of GazePlay (grid of {@link #HEAT_MAP_COLUMNS} columns, colors and
     * opacity of the settings, legend on the right). A source without position on the screen has no heatmap; a screen
     * without any position gets its image alone. The images are taken here, written in the background.
     */
    private void saveHeatmaps() {
        if (sessionFolder == null || screenshotWriter == null || screenshotWriter.isShutdown()) {
            return;
        }
        try {
            EvalSessionRecorder.ScreenRecord screen = recorder.getCurrentScreen();
            if (screen == null) {
                return;
            }
            Configuration config = gameContext.getConfiguration();
            BufferedImage screenshot = SwingFXUtils.fromFXImage(gameContext.getRoot().snapshot(null, null), null);
            List<EvalSessionRecorder.Sample> samples = recorder.samplesOf(screen.getScreen(), screen.getAttempt());
            Path folder = sessionFolder.resolve(HEATMAPS_FOLDER);
            String prefix = String.format("ecran-%02d-%s-essai-%d", screen.getScreen(), screen.getType(),
                screen.getAttempt());
            boolean written = false;
            for (String source : List.of(EvalSessionRecorder.GAZE, EvalSessionRecorder.MOUSE)) {
                List<EvalSessionRecorder.Sample> ofSource = samples.stream()
                    .filter(sample -> sample.source().equals(source)).toList();
                if (ofSource.isEmpty()) {
                    continue;
                }
                HeatMapGrid grid = new HeatMapGrid(screenshot.getWidth(), screenshot.getHeight(),
                    screenshot.getWidth() / (double) HEAT_MAP_COLUMNS);
                ofSource.forEach(sample -> grid.add((int) sample.x(), (int) sample.y()));
                HeatMap heatMap = new HeatMap(grid.counts(), heatMapOpacity(config), heatMapColors(config));
                BufferedImage heat = SwingFXUtils.fromFXImage(heatMap.getImage(), null);
                BufferedImage key = SwingFXUtils.fromFXImage(
                    heatMap.getColorKey(Math.max(20, screenshot.getWidth() / 20), Math.max(40, screenshot.getHeight() / 2)),
                    null);
                Path file = folder.resolve(prefix + "-" + source + ".png");
                screenshotWriter.execute(() -> writeImage(withHeatMap(screenshot, heat, key), file));
                written = true;
            }
            if (!written) {
                screenshotWriter.execute(() -> writeImage(screenshot, folder.resolve(prefix + ".png")));
            }
        } catch (RuntimeException e) {
            log.warn("Cannot save the heatmaps of the screen", e);
        }
    }

    /**
     * @return the image of the screen with the heatmap, smoothed, over it and its legend on the right (as
     * {@code Stats.addHeatMapToMetrics})
     */
    private static BufferedImage withHeatMap(BufferedImage screenshot, BufferedImage heat, BufferedImage key) {
        BufferedImage result = new BufferedImage(screenshot.getWidth(), screenshot.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        graphics.drawImage(screenshot, 0, 0, null);
        Kernel kernel = new Kernel(3, 3,
            new float[]{1 / 16f, 1 / 8f, 1 / 16f, 1 / 8f, 1 / 4f, 1 / 8f, 1 / 16f, 1 / 8f, 1 / 16f});
        BufferedImage smoothed = new ConvolveOp(kernel).filter(heat, null);
        graphics.drawImage(smoothed, 0, 0, screenshot.getWidth(), screenshot.getHeight(), null);
        graphics.drawImage(key, result.getWidth() - key.getWidth(), (result.getHeight() - key.getHeight()) / 2, null);
        graphics.dispose();
        return result;
    }

    private static void writeImage(BufferedImage image, Path file) {
        try {
            Files.createDirectories(file.getParent());
            ImageIO.write(image, "png", file.toFile());
        } catch (IOException e) {
            log.warn("Cannot write {}", file, e);
        }
    }

    private static double heatMapOpacity(Configuration config) {
        Double opacity = config.getHeatMapOpacity();
        return opacity == null ? 0.7 : opacity;
    }

    /**
     * @return the colors of the heatmaps of the settings (from the coldest), default ones when fewer than two
     */
    private static List<Color> heatMapColors(Configuration config) {
        List<Color> colors = config.getHeatMapColors();
        if (colors == null || colors.size() < 2) {
            return List.of(Color.BLUE, Color.CYAN, Color.LIME, Color.YELLOW, Color.RED);
        }
        return colors;
    }

    private void cleanUp() {
        clearScreen();
        if (menuWatcher != null) {
            menuWatcher.stop();
            menuWatcher = null;
        }
        if (sceneRoot != null) {
            sceneRoot.removeEventFilter(GazeEvent.ANY, gazeRecorder);
            sceneRoot.removeEventFilter(MouseEvent.ANY, mouseRecorder);
            sceneRoot = null;
        }
        if (keyHandler != null) {
            gameContext.getPrimaryScene().removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
            keyHandler = null;
        }
        menuOpen = false;
    }

    /**
     * Ends the session when the game is left before its end (its results are written, marked as interrupted), and
     * frees the screen.
     */
    @Override
    public void dispose() {
        if (phase != Phase.END && phase != Phase.MESSAGE) {
            finishSession(false);
        }
        cleanUp();
    }
}
