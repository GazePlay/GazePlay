package net.gazeplay.ui.scenes.ingame;

import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GamePanelDimensionProvider;
import net.gazeplay.GazePlay;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.AnimationSpeedRatioSource;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.DefaultAnimationSpeedRatioSource;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.soundsmanager.SoundManager;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.*;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.stats.StatDisplayUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.RandomPositionGenerator;
import net.gazeplay.ui.*;
import net.gazeplay.ui.scenes.stats.StatsContext;
import net.gazeplay.ui.scenes.stats.StatsContextFactory;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * The game screen: the game area ({@link #getRoot()}, where the game draws), the configuration button and its side
 * panel (music, volumes, speed, fixation length, element size, progress indicators, background color, full screen, home
 * and replay buttons), the "bravo" animation, and the gaze and sound managers of the game.
 * <p>
 * It implements {@link IGameContext}, the interface the games see. Created by {@link GameContextFactory}.
 */
@Slf4j
public class GameContext extends GraphicalContext<Pane> implements IGameContext {

    @Getter
    private HomeButton homeButton;

    @Getter
    private ReplayButton replayButton;

    /**
     * Keeps the configuration button at the bottom of the window.
     */
    public static void updateConfigPane(final Pane configPane, Stage primaryStage) {
        double mainHeight = primaryStage.getHeight();

        final double newY = mainHeight - configPane.getHeight() - 30;
        log.debug("translated config pane to y : {}, height : {}", newY, configPane.getHeight());
        configPane.setTranslateY(newY);
    }

    boolean limiterS = false;
    boolean limiterT = false;
    long startTime = 0;
    long endTime = 0;
    boolean limiteUsed = false;

    @Getter
    private final Translator translator;

    private final Bravo bravo;

    @Getter
    private final HBox menuHBox;

    @Getter
    private final RandomPositionGenerator randomPositionGenerator;

    @Getter
    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    @Getter
    private final GazeDeviceManager gazeDeviceManager;

    @Getter
    private final SoundManager soundManager;

    private final Pane configPane;

    private final Pane gamingRoot;

    private VideoRecordingContext videoRecordingContext;

    private TitledPane fixPan;

    private GridPane leftControlPane;

    /**
     * Use {@link GameContextFactory} to create a game screen.
     */
    protected GameContext(
        @NonNull GazePlay gazePlay,
        @NonNull Translator translator,
        @NonNull Pane root,
        @NonNull Pane gamingRoot,
        @NonNull Bravo bravo,
        @NonNull HBox menuHBox,
        @NonNull GazeDeviceManager gazeDeviceManager,
        @NonNull SoundManager soundManager,
        @NonNull Pane configPane
    ) {
        super(gazePlay, root);
        this.translator = translator;
        this.gamingRoot = gamingRoot;
        this.bravo = bravo;
        this.menuHBox = menuHBox;
        this.gazeDeviceManager = gazeDeviceManager;
        this.soundManager = soundManager;
        this.configPane = configPane;

        this.gamePanelDimensionProvider = new GamePanelDimensionProvider(() -> root, gazePlay::getPrimaryScene);
        this.randomPositionGenerator = new RandomPanePositionGenerator(gamePanelDimensionProvider, new ReplayablePseudoRandom());

        if (this.getConfiguration().isVideoRecordingEnabled()) {
            videoRecordingContext = new VideoRecordingContext(root, this);
        }
    }


    @Override
    public void setUpOnStage(final Scene scene) {

        super.setUpOnStage(scene);

        log.info("SETTING UP");
        updateConfigPane(configPane, getGazePlay().getPrimaryStage());
    }

    @Override
    public Supplier<Dimension2D> getCurrentScreenDimensionSupplier() {
        return getGazePlay().getCurrentScreenDimensionSupplier();
    }

    @Override
    public void startScoreLimiter() {
        limiterS = getConfiguration().isLimiterScoreEnabled();
    }

    @Override
    public void startTimeLimiter() {
        limiterT = getConfiguration().isLimiterTimeEnabled();
        setLimiterAvailable();
    }

    @Override
    public void setLimiterAvailable() {
        limiteUsed = false;
    }

    @Override
    public void start() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void firstStart() {
        if (startTime == 0) {
            start();
        }
    }

    @Override
    public void stop() {
        endTime = System.currentTimeMillis();
    }

    @Override
    public void updateScore(Stats stats, GameLifeCycle currentGame) {
        updateScore(stats, currentGame, e -> {
        }, e -> {
        });
    }

    @Override
    public void updateScore(Stats stats, GameLifeCycle currentGame, EventHandler<ActionEvent> onTimeLimiterEndEventHandler, EventHandler<ActionEvent> onScoreLimiterEndEventHandler) {
        if (limiterS && !limiteUsed) {
            if (stats.getNbGoalsReached() == getConfiguration().getLimiterScore()) {
                onScoreLimiterEndEventHandler.handle(null);
                playWinTransition(0, event -> showRoundStats(stats, currentGame));
                limiteUsed = true;
            }
        }
        if (limiterT && !limiteUsed) {
            stop();
            if (time(startTime, endTime) >= getConfiguration().getLimiterTime()) {
                onTimeLimiterEndEventHandler.handle(null);
                playWinTransition(0, event -> showRoundStats(stats, currentGame));
                startTime = 0;
                limiteUsed = true;
            }
        }

    }

    /**
     * @return the time between two instants in ms, in seconds
     */
    private double time(double start, double end) {
        return (end - start) / 1000;
    }

    /**
     * Fills the side panel with the controls; during a replay, without the replay button.
     */
    public void createControlPanel(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame, boolean inReplayMode) {
        Configuration config = getConfiguration();
        MusicControl musicControl = getMusicControl();
        AnimationSpeedRatioControl animationSpeedRatioControl = AnimationSpeedRatioControl.getInstance();
        FixationLengthControl fixationLengthControl = FixationLengthControl.getInstance();
        ElementSizeControl elementSizeControl = ElementSizeControl.getInstance();
        ProgressBarControl progressBarControl = ProgressBarControl.getInstance();
        BackgroundColorControl backgroundColorControl = BackgroundColorControl.getInstance();

        Stage primaryStage = gazePlay.getPrimaryStage();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);

        leftControlPane = new GridPane();
        leftControlPane.setHgap(5);
        leftControlPane.setVgap(5);
        leftControlPane.setAlignment(Pos.TOP_CENTER);
        leftControlPane.add(musicControl.createMusicControlPane(), 0, 0);
        leftControlPane.add(musicControl.createVolumeLevelControlPane(config, gazePlay.getTranslator()), 1, 0);
        if (!inReplayMode) {
            leftControlPane.add(fixationLengthControl.createfixationLengthPane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene()), 2, 0);
            leftControlPane.add(animationSpeedRatioControl.createSpeedEffectsPane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene()), 3, 0);
            leftControlPane.add(elementSizeControl.createElementSizePane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene()), 4, 0);
            leftControlPane.add(progressBarControl.createProgressControlPane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene()), 5, 0);
            leftControlPane.add(backgroundColorControl.createBackgroundColorControlPane(config, gazePlay.getTranslator(), gamingRoot), 6, 0);
        }
        leftControlPane.getChildren().forEach(node -> {
            GridPane.setVgrow(node, Priority.ALWAYS);
            GridPane.setHgrow(node, Priority.ALWAYS);
        });

        scrollPane.setContent(leftControlPane);
        menuHBox.getChildren().add(scrollPane);

        I18NButton toggleFullScreenButtonInGameScreen = createToggleFullScreenButtonInGameScreen(gazePlay);
        menuHBox.getChildren().add(toggleFullScreenButtonInGameScreen);

        homeButton = inReplayMode ? createHomeButtonInGameScreenWithoutHandler(gazePlay) : createHomeButtonInGameScreen(gazePlay, stats, currentGame);
        menuHBox.getChildren().add(homeButton);

        replayButton = inReplayMode ? createReplayButtonInGameScreenWithoutHandler(gazePlay) : createReplayButtonInGameScreen(gazePlay, stats, currentGame);
        menuHBox.getChildren().add(replayButton);

        double buttonSize = primaryStage.getWidth() / 10;

        if (buttonSize < GameContextFactory.BUTTON_MIN_HEIGHT) {
            buttonSize = GameContextFactory.BUTTON_MIN_HEIGHT;
        }

        double offset = buttonSize + homeButton.getBoundsInLocal().getWidth() + replayButton.getBoundsInLocal().getWidth() + toggleFullScreenButtonInGameScreen.getBoundsInLocal().getWidth();

        scrollPane.setPrefWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMinWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMaxWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> updateControllPanel(scrollPane, toggleFullScreenButtonInGameScreen, primaryStage));
    }

    /**
     * Sizes the buttons of the side panel for the width of the window.
     */
    public void updateControllPanel(ScrollPane scrollPane, I18NButton toggleFullScreenButtonInGameScreen, Stage primaryStage) {
        double buttonSize = primaryStage.getWidth() / 10;

        if (buttonSize < GameContextFactory.BUTTON_MIN_HEIGHT) {
            buttonSize = GameContextFactory.BUTTON_MIN_HEIGHT;
        }

        double offset = buttonSize + homeButton.getBoundsInLocal().getWidth() + replayButton.getBoundsInLocal().getWidth() + toggleFullScreenButtonInGameScreen.getBoundsInLocal().getWidth();

        scrollPane.setPrefWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMinWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMaxWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
    }

    /**
     * @return the home button: it ends the game, saves its results and shows its statistics
     */
    public HomeButton createHomeButtonInGameScreen(@NonNull GazePlay gazePlay, @NonNull Stats stats,
                                                   @NonNull GameLifeCycle currentGame) {

        EventHandler<Event> homeEvent = e -> {
            root.setCursor(Cursor.WAIT); // Change cursor to wait style
            exitGame(stats, gazePlay, currentGame);
            root.setCursor(Cursor.DEFAULT); // Change cursor to default style
        };

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        HomeButton homeButton = new HomeButton(screenDimension);
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);
        return homeButton;
    }

    /**
     * @return the replay button: it ends the game and replays it at once
     */
    public ReplayButton createReplayButtonInGameScreen(@NonNull GazePlay gazePlay, @NonNull Stats stats,
                                                       @NonNull GameLifeCycle currentGame) {

        EventHandler<Event> replayEvent = e -> {
            exitReplayGame(stats, gazePlay, currentGame);
            getGazePlay().onGameLaunch(this);
            stats.reset();
            currentGame.launch();
            soundManager.init();
        };

        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();

        ReplayButton replayButton = new ReplayButton(screenDimension);
        replayButton.addEventHandler(MouseEvent.MOUSE_CLICKED, replayEvent);
        return replayButton;
    }

    /**
     * Ends the game for its replay: saves the results and shows the statistics (the gaze manager is kept for the
     * replay).
     */
    public void exitReplayGame(@NonNull Stats stats, @NonNull GazePlay gazePlay, @NonNull GameLifeCycle currentGame) {

        if (videoRecordingContext != null) {
            videoRecordingContext.pointersClear();
        }

        currentGame.dispose();
        ForegroundSoundsUtils.stopSound(); // to stop playing the sound of Bravo
        stats.stop();

        soundManager.clear();
        soundManager.destroy();

        saveStats(stats);

        StatsContext statsContext = StatsContextFactory.newInstance(gazePlay, stats);

        this.clear();

        gazePlay.onDisplayStats(statsContext);
    }


    /**
     * Ends the game: stops the game, its sounds, the gaze and the statistics, saves the results and shows the
     * statistics screen.
     */
    public void exitGame(@NonNull Stats stats, @NonNull GazePlay gazePlay, @NonNull GameLifeCycle currentGame) {

        if (videoRecordingContext != null) {
            videoRecordingContext.pointersClear();
        }

        currentGame.dispose();
        ForegroundSoundsUtils.stopSound(); // to stop playing the sound of Bravo
        stats.stop();
        gazeDeviceManager.clear();
        gazeDeviceManager.destroy();

        soundManager.clear();
        soundManager.destroy();

        if (stats.isSavingDisabled()) {
            // Nothing saved (GazePlay Eval Test): no statistics screen, which shows the files written; the JVM of the
            // game ends, as with the home button of that screen
            StatDisplayUtils.closeStatsWindow();
            return;
        }
        saveStats(stats);

        StatsContext statsContext = StatsContextFactory.newInstance(gazePlay, stats);

        this.clear();

        gazePlay.onDisplayStats(statsContext);
    }

    /**
     * Saves the results of the game. It used to be run in the background when a static flag was set, which nothing
     * did: the results were always saved here, before the statistics screen.
     */
    private static void saveStats(final Stats stats) {
        try {
            stats.saveStats();
        } catch (IOException e) {
            log.error("Failed to save stats file", e);
        }
    }

    /**
     * @return a home button without action
     */
    public HomeButton createHomeButtonInGameScreenWithoutHandler(@NonNull GazePlay gazePlay) {
        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();
        return new HomeButton(screenDimension);
    }

    /**
     * @return a replay button without action
     */
    public ReplayButton createReplayButtonInGameScreenWithoutHandler(@NonNull GazePlay gazePlay) {
        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();
        return new ReplayButton(screenDimension);
    }

    /**
     * Ends the replay of a replayData.json file and shows the statistics of the recorded game (nothing is saved again).
     */
    public void exitReplayJsonGame(@NonNull Stats stats, @NonNull GazePlay gazePlay, @NonNull GameLifeCycle currentGame) {

        if (videoRecordingContext != null) {
            videoRecordingContext.pointersClear();
        }

        currentGame.dispose();
        ForegroundSoundsUtils.stopSound(); // to stop playing the sound of Bravo
        gazeDeviceManager.clear();
        gazeDeviceManager.destroy();

        soundManager.clear();
        soundManager.destroy();

        StatsContext statsContext = StatsContextFactory.newInstance(gazePlay, stats);

        this.clear();

        gazePlay.onDisplayStats(statsContext);
    }

    @Override
    public void pauseBackgroundMusic() {
        BackgroundMusicManager.getInstance().pause();
    }

    /**
     * The configuration of the active profile, read at each call: a profile change (menu) is seen by the next call.
     * The games read it through this method rather than through the static accessor.
     */
    @Override
    public @NonNull Configuration getConfiguration() {
        return ActiveConfigurationContext.getInstance();
    }

    @Override
    public AnimationSpeedRatioSource getAnimationSpeedRatioSource() {
        DefaultAnimationSpeedRatioSource defaultAnimationSpeedRatioSource = new DefaultAnimationSpeedRatioSource();
        defaultAnimationSpeedRatioSource.setConfiguration(ActiveConfigurationContext.getInstance());
        return defaultAnimationSpeedRatioSource;
    }

    @Override
    public Stage getPrimaryStage() {
        return getGazePlay().getPrimaryStage();
    }

    @Override
    public Scene getPrimaryScene() {
        return getGazePlay().getPrimaryScene();
    }

    @Override
    public void showRoundStats(Stats stats, GameLifeCycle currentGame) {
        stats.stop();

        if (stats.isSavingDisabled()) {
            StatDisplayUtils.closeStatsWindow();
            return;
        }
        saveStats(stats);

        Dimension2D screenDimension = getGazePlay().getCurrentScreenDimensionSupplier().get();

        CustomButton continueButton = new CustomButton("data/common/images/continue.png", screenDimension);
        continueButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            getGazePlay().onGameLaunch(this);
            stats.reset();
            currentGame.launch();
        });

        StatsContext statsContext = StatsContextFactory.newInstance(getGazePlay(), stats, continueButton);

        this.clear();
        getGazePlay().onDisplayStats(statsContext);

    }

    @Override
    public void playWinTransition(long delay, EventHandler<ActionEvent> onFinishedEventHandler) {
        if (!getChildren().contains(bravo)) {
            getChildren().add(bravo);
            bravo.toFront();
            bravo.setConfettiOnStart(this);
            bravo.playWinTransition(root, delay, onFinishedEventHandler);
        }
    }

    @Override
    public void endWinTransition() {
        getChildren().remove(bravo);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return gamingRoot.getChildren();
    }

    @Override
    public Pane getRoot() {
        return gamingRoot;
    }

    public void onGameStarted() {
    }

    public void setOffFixationLengthControl() {
        leftControlPane.getChildren().remove(fixPan);
    }

    public void onGameStarted(int delay) {
        gamingRoot.setDisable(true);
        PauseTransition wait = new PauseTransition(Duration.millis(delay));
        wait.setOnFinished(waitEvent -> {
            gamingRoot.setDisable(false);
        });
        wait.play();
    }

}
