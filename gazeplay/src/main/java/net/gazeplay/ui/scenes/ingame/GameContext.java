package net.gazeplay.ui.scenes.ingame;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
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
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.*;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.RandomPositionGenerator;
import net.gazeplay.ui.AnimationSpeedRatioControl;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.MusicControl;
import net.gazeplay.ui.scenes.stats.StatsContext;

import java.io.IOException;

@Slf4j
public class GameContext extends GraphicalContext<Pane> implements IGameContext {

    public static void updateConfigPane(final Pane configPane, Stage primaryStage) {
        double mainHeight = primaryStage.getHeight();

        final double newY = mainHeight - configPane.getHeight() - 30;
        log.debug("translated config pane to y : {}, height : {}", newY, configPane.getHeight());
        configPane.setTranslateY(newY);
    }

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

    private final Pane configPane;

    private final Pane gamingRoot;

    private EventHandler<Event> pointerEvent;
    private Circle mousePointer;
    private Circle gazePointer;

    protected GameContext(
        @NonNull GazePlay gazePlay,
        @NonNull Translator translator,
        @NonNull Pane root,
        @NonNull Pane gamingRoot,
        @NonNull Bravo bravo,
        @NonNull HBox menuHBox,
        @NonNull GazeDeviceManager gazeDeviceManager,
        @NonNull Pane configPane
    ) {
        super(gazePlay, root);
        this.translator = translator;
        this.gamingRoot = gamingRoot;
        this.bravo = bravo;
        this.menuHBox = menuHBox;
        this.gazeDeviceManager = gazeDeviceManager;
        this.configPane = configPane;

        this.gamePanelDimensionProvider = new GamePanelDimensionProvider(() -> root, gazePlay::getPrimaryScene);
        this.randomPositionGenerator = new RandomPanePositionGenerator(gamePanelDimensionProvider);

        if (this.getConfiguration().isVideoRecordingEnabled()) {
            this.pointersInit();
        }
    }

    public void pointersInit() {
        mousePointer = new Circle(10, Color.RED);
        mousePointer.setMouseTransparent(true);
        mousePointer.setOpacity(0.5);
        root.getChildren().add(mousePointer);


        gazePointer = new Circle(10, Color.BLUE);
        gazePointer.setMouseTransparent(true);
        gazePointer.setOpacity(0.5);
        root.getChildren().add(gazePointer);
        getGazeDeviceManager().addEventFilter(root);

        pointerEvent = e -> {
            if (e.getEventType() == MouseEvent.MOUSE_MOVED) {
                mousePointer.setCenterX(((MouseEvent) e).getX());
                mousePointer.setCenterY(((MouseEvent) e).getY());
                mousePointer.toFront();
            } else if (e.getEventType() == GazeEvent.GAZE_MOVED) {
                gazePointer.setCenterX(((GazeEvent) e).getX());
                gazePointer.setCenterY(((GazeEvent) e).getY());
                gazePointer.toFront();
            }
        };
        root.addEventFilter(MouseEvent.ANY, pointerEvent);
        root.addEventFilter(GazeEvent.ANY, pointerEvent);
    }


    public void pointersClear() {
        root.removeEventFilter(MouseEvent.ANY, pointerEvent);
        root.removeEventFilter(GazeEvent.ANY, pointerEvent);
        root.getChildren().remove(gazePointer);
        root.getChildren().remove(mousePointer);
        getGazeDeviceManager().removeEventFilter(root);
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

    public void createQuitShortcut(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame) {
        Configuration config = ActiveConfigurationContext.getInstance();
        final Scene scene = gazePlay.getPrimaryScene();

        // gamingRoot.getChildren().add(scene);
        EventHandler buttonHandler = new EventHandler<KeyEvent>() {

            public void handle(KeyEvent event) {

                exitGame(stats, gazePlay, currentGame);
                scene.removeEventHandler(KeyEvent.KEY_PRESSED, this);
                scene.removeEventHandler(KeyEvent.KEY_RELEASED, this);
            }
        };
        // scene.addEventHandler(KeyEvent.KEY_PRESSED, buttonHandler);
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

        GridPane leftControlPane = new GridPane();
        leftControlPane.setHgap(5);
        leftControlPane.setVgap(5);
        leftControlPane.setAlignment(Pos.TOP_CENTER);
        leftControlPane.add(musicControl.createMusicControlPane(), 0, 0);
        leftControlPane.add(musicControl.createVolumeLevelControlPane(config, gazePlay.getTranslator()), 1, 0);
        leftControlPane.add(animationSpeedRatioControl.createSpeedEffectsPane(config, gazePlay.getTranslator(), gazePlay.getPrimaryScene()), 2, 0);
        leftControlPane.getChildren().forEach(node -> {
            GridPane.setVgrow(node, Priority.ALWAYS);
            GridPane.setHgrow(node, Priority.ALWAYS);
        });


        menuHBox.getChildren().add(leftControlPane);

        I18NButton toggleFullScreenButtonInGameScreen = createToggleFullScreenButtonInGameScreen(gazePlay);
        menuHBox.getChildren().add(toggleFullScreenButtonInGameScreen);

        HomeButton homeButton = createHomeButtonInGameScreen(gazePlay, stats, currentGame);
        menuHBox.getChildren().add(homeButton);
    }

    public HomeButton createHomeButtonInGameScreen(@NonNull GazePlay gazePlay, @NonNull Stats stats,
                                                   @NonNull GameLifeCycle currentGame) {

        EventHandler<Event> homeEvent = e -> {
            root.setCursor(Cursor.WAIT); // Change cursor to wait style
            exitGame(stats, gazePlay, currentGame);
            root.setCursor(Cursor.DEFAULT); // Change cursor to default style
        };

        HomeButton homeButton = new HomeButton();
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);
        return homeButton;
    }

    private void exitGame(@NonNull Stats stats, @NonNull GazePlay gazePlay, @NonNull GameLifeCycle currentGame) {

        if (this.getConfiguration().isVideoRecordingEnabled()) {
            pointersClear();
        }

        currentGame.dispose();
        ForegroundSoundsUtils.stopSound(); // to stop playing the sound of Bravo
        stats.stop();
        gazeDeviceManager.clear();
        gazeDeviceManager.destroy();

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

        StatsContext statsContext = StatsContext.newInstance(gazePlay, stats);

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

        CustomButton continueButton = new CustomButton("data/common/images/continue.png");
        continueButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            getGazePlay().onGameLaunch(this);
            stats.reset();
            currentGame.launch();
        });

        StatsContext statsContext = StatsContext.newInstance(getGazePlay(), stats, continueButton);

        this.clear();
        getGazePlay().onDisplayStats(statsContext);

    }

    @Override
    public void playWinTransition(long delay, EventHandler<ActionEvent> onFinishedEventHandler) {
        getChildren().add(bravo);
        bravo.toFront();
        bravo.setConfetiOnStart(this);
        bravo.playWinTransition(root, delay, onFinishedEventHandler);
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

}
