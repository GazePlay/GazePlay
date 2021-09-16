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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.RandomPositionGenerator;
import net.gazeplay.ui.*;
import net.gazeplay.ui.scenes.stats.StatsContext;
import net.gazeplay.ui.scenes.stats.StatsContextFactory;

import java.io.IOException;
import java.util.function.Supplier;

@Slf4j
public class GameContext extends GraphicalContext<Pane> implements IGameContext {

    @Getter
    private HomeButton homeButton;

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

    @Setter
    private static boolean runAsynchronousStatsPersist = false;

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

    public void resetBordersToFront() {
        // rootBorderPane.setBottom(null);
        // rootBorderPane.setBottom(bottomPane);
    }

    @Override
    public Supplier<Dimension2D> getCurrentScreenDimensionSupplier() {
        return getGazePlay().getCurrentScreenDimensionSupplier();
    }

    @Override
    public void startScoreLimiter() {
        limiterS = getConfiguration().isLimiterS();
    }

    @Override
    public void startTimeLimiter() {
        limiterT = getConfiguration().isLimiterT();
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

    private double time(double start, double end) {
        return (end - start) / 1000;
    }

    public void createQuitShortcut(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame) {
        Configuration config = ActiveConfigurationContext.getInstance();
        final Scene scene = gazePlay.getPrimaryScene();

        EventHandler<KeyEvent> buttonHandler = new EventHandler<>() {

            public void handle(KeyEvent event) {

                exitGame(stats, gazePlay, currentGame);
                scene.removeEventHandler(KeyEvent.KEY_PRESSED, this);
                scene.removeEventHandler(KeyEvent.KEY_RELEASED, this);
            }
        };

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode().getChar().equals(config.getQuitKey())) {
                scene.addEventHandler(KeyEvent.KEY_RELEASED, buttonHandler);
            }
        });
    }

    public void createControlPanel(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame) {
        Configuration config = ActiveConfigurationContext.getInstance();
        MusicControl musicControl = getMusicControl();
        AnimationSpeedRatioControl animationSpeedRatioControl = AnimationSpeedRatioControl.getInstance();
        FixationLengthControl fixationLengthControl = FixationLengthControl.getInstance();
        ElementSizeControl elementSizeControl = ElementSizeControl.getInstance();


        Stage primaryStage = gazePlay.getPrimaryStage();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);

        leftControlPane = new GridPane();
        leftControlPane.setHgap(5);
        leftControlPane.setVgap(5);
        leftControlPane.setAlignment(Pos.TOP_CENTER);
        leftControlPane.add(musicControl.createMusicControlPane(), 0, 0);
        leftControlPane.add(musicControl.createVolumeLevelControlPane(config, gazePlay.getTranslator()), 1, 0);
        leftControlPane.add(animationSpeedRatioControl.createSpeedEffectsPane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene()), 2, 0);
        leftControlPane.add(elementSizeControl.createElementSizePane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene()), 3, 0);
        fixPan = fixationLengthControl.createfixationLengthPane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene());
        leftControlPane.add(fixPan, 4, 0);
        leftControlPane.getChildren().forEach(node -> {
            GridPane.setVgrow(node, Priority.ALWAYS);
            GridPane.setHgrow(node, Priority.ALWAYS);
        });

        scrollPane.setContent(leftControlPane);
        menuHBox.getChildren().add(scrollPane);

        I18NButton toggleFullScreenButtonInGameScreen = createToggleFullScreenButtonInGameScreen(gazePlay);
        menuHBox.getChildren().add(toggleFullScreenButtonInGameScreen);

        homeButton = createHomeButtonInGameScreen(gazePlay, stats, currentGame);
        menuHBox.getChildren().add(homeButton);

        double buttonSize = primaryStage.getWidth() / 10;

        if (buttonSize < GameContextFactoryBean.BUTTON_MIN_HEIGHT) {
            buttonSize = GameContextFactoryBean.BUTTON_MIN_HEIGHT;
        }

        double offset = buttonSize + homeButton.getBoundsInLocal().getWidth() + toggleFullScreenButtonInGameScreen.getBoundsInLocal().getWidth();

        scrollPane.setPrefWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMinWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMaxWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> updateControllPanel(scrollPane, toggleFullScreenButtonInGameScreen, primaryStage));
    }

    public void updateControllPanel(ScrollPane scrollPane, I18NButton toggleFullScreenButtonInGameScreen, Stage primaryStage) {
        double buttonSize = primaryStage.getWidth() / 10;

        if (buttonSize < GameContextFactoryBean.BUTTON_MIN_HEIGHT) {
            buttonSize = GameContextFactoryBean.BUTTON_MIN_HEIGHT;
        }

        double offset = buttonSize + homeButton.getBoundsInLocal().getWidth() + toggleFullScreenButtonInGameScreen.getBoundsInLocal().getWidth();

        scrollPane.setPrefWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMinWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMaxWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
    }

    public void createControlPanel(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame, String replayMode) {
        Configuration config = ActiveConfigurationContext.getInstance();
        MusicControl musicControl = getMusicControl();
        AnimationSpeedRatioControl animationSpeedRatioControl = AnimationSpeedRatioControl.getInstance();
        FixationLengthControl fixationLengthControl = FixationLengthControl.getInstance();
        ElementSizeControl elementSizeControl = ElementSizeControl.getInstance();

        Stage primaryStage = gazePlay.getPrimaryStage();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);

        leftControlPane = new GridPane();
        leftControlPane.setHgap(5);
        leftControlPane.setVgap(5);
        leftControlPane.setAlignment(Pos.TOP_CENTER);
        leftControlPane.add(musicControl.createMusicControlPane(), 0, 0);
        leftControlPane.add(musicControl.createVolumeLevelControlPane(config, gazePlay.getTranslator()), 1, 0);
        leftControlPane.add(animationSpeedRatioControl.createSpeedEffectsPane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene()), 2, 0);
        leftControlPane.add(elementSizeControl.createElementSizePane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene()), 3, 0);
        fixPan = fixationLengthControl.createfixationLengthPane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene());
        leftControlPane.add(fixPan, 4, 0);
        leftControlPane.getChildren().forEach(node -> {
            GridPane.setVgrow(node, Priority.ALWAYS);
            GridPane.setHgrow(node, Priority.ALWAYS);
        });


        scrollPane.setContent(leftControlPane);
        menuHBox.getChildren().add(scrollPane);

        I18NButton toggleFullScreenButtonInGameScreen = createToggleFullScreenButtonInGameScreen(gazePlay);
        menuHBox.getChildren().add(toggleFullScreenButtonInGameScreen);

        homeButton = createHomeButtonInGameScreenWithoutHandler(gazePlay);
        menuHBox.getChildren().add(homeButton);

        double buttonSize = primaryStage.getWidth() / 10;

        if (buttonSize < GameContextFactoryBean.BUTTON_MIN_HEIGHT) {
            buttonSize = GameContextFactoryBean.BUTTON_MIN_HEIGHT;
        }

        double offset = buttonSize + homeButton.getBoundsInLocal().getWidth() + toggleFullScreenButtonInGameScreen.getBoundsInLocal().getWidth();
        scrollPane.setPrefWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMinWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);
        scrollPane.setMaxWidth(9.9d * primaryStage.getWidth() / 10d - offset - 100);

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> updateControllPanel(scrollPane, toggleFullScreenButtonInGameScreen, primaryStage));

    }

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

        Runnable asynchronousStatsPersistTask = () -> {
            try {
                stats.saveStats();
            } catch (IOException e) {
                log.error("Failed to save stats file", e);
            }
        };

        if (runAsynchronousStatsPersist) {
            AsyncUiTaskExecutor.getInstance().getExecutorService().execute(asynchronousStatsPersistTask);
        } else {
            asynchronousStatsPersistTask.run();
        }

        StatsContext statsContext = StatsContextFactory.newInstance(gazePlay, stats);

        this.clear();

        gazePlay.onDisplayStats(statsContext);
    }

    public HomeButton createHomeButtonInGameScreenWithoutHandler(@NonNull GazePlay gazePlay) {
        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();
        return new HomeButton(screenDimension);
    }

    public void exitReplayGame(@NonNull Stats stats, @NonNull GazePlay gazePlay, @NonNull GameLifeCycle currentGame) {

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

        Runnable asynchronousStatsPersistTask = () -> {
            try {
                stats.saveStats();
            } catch (IOException e) {
                log.error("Failed to save stats file", e);
            }
        };

        if (runAsynchronousStatsPersist) {
            AsyncUiTaskExecutor.getInstance().getExecutorService().execute(asynchronousStatsPersistTask);
        } else {
            asynchronousStatsPersistTask.run();
        }

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

    /*
     * When the game starts,
     * the player can't select an element
     * until the delay is over
     */
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
