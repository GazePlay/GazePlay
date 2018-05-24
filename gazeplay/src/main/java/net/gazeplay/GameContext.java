package net.gazeplay;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.utils.*;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.IOException;

@Slf4j
public class GameContext extends GraphicalContext<Pane> {

    public static boolean menuOpen = false;

    @Setter
    private static boolean runAsynchronousStatsPersist = false;

    public static GameContext newInstance(GazePlay gazePlay) {

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, gazePlay.getPrimaryStage().getWidth(), gazePlay.getPrimaryStage().getHeight(),
                Color.BLACK);

        final Configuration config = Configuration.getInstance();
        CssUtil.setPreferredStylesheets(config, scene);

        Bravo bravo = new Bravo();

        Pane gamingRoot = new Pane();
        gamingRoot.setStyle("-fx-background-color: black;");

        HBox controlPanel = createControlPanel();
        // Adapt the size and position of buttons to screen width
        controlPanel.maxWidthProperty().bind(root.widthProperty());
        controlPanel.toFront();

        double buttonSize = gazePlay.getPrimaryStage().getWidth() / 10;

        // Button bt = new Button();
        ImageView bt = new ImageView(new Image("data/common/images/configuration-button-alt4.png"));
        bt.setFitWidth(buttonSize);
        bt.setFitHeight(buttonSize);
        /*
         * bt.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; " +
         * "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; " + "-fx-max-height: " +
         * buttonSize + "px;");
         */

        gazePlay.getPrimaryStage().heightProperty().addListener((obs, oldVal, newVal) -> {
            bt.setLayoutY(0);
            bt.toFront();
        });
        gazePlay.getPrimaryStage().widthProperty().addListener((obs, oldVal, newVal) -> {
            bt.setLayoutX(-buttonSize / 2);
            bt.toFront();
        });

        EventHandler<MouseEvent> mouseEnterControlPanelEventHandler = mouseEvent -> {
            double from = 0;
            double to = 1;
            double angle = 360;
            if (menuOpen) {
                from = 1;
                to = 0;
                angle = -1 * angle;
            }
            RotateTransition rt = new RotateTransition(Duration.millis(500), bt);
            rt.setByAngle(angle);
            FadeTransition ft = new FadeTransition(Duration.millis(500), controlPanel);
            ft.setFromValue(from);
            ft.setToValue(to);
            ParallelTransition pt = new ParallelTransition();
            pt.getChildren().addAll(rt, ft);
            controlPanel.setDisable(menuOpen);
            menuOpen = !menuOpen;
            pt.play();
        };

        log.info("the value of the control bar is : =" + controlPanel.getPrefWidth());
        controlPanel.setPrefWidth(gazePlay.getPrimaryStage().getWidth() / 2.5);
        controlPanel.setOpacity(0);
        controlPanel.setDisable(true);
        menuOpen = false;

        bt.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEnterControlPanelEventHandler);

        Pane root2 = new Pane();
        root2.getChildren().add(bt);
        root2.getChildren().add(controlPanel);
        root.setCenter(gamingRoot);
        root.getChildren().add(root2);

        GamePanelDimensionProvider gamePanelDimensionProvider = new GamePanelDimensionProvider(gamingRoot, scene);

        RandomPositionGenerator randomPositionGenerator = new RandomPanePositionGenerator(gamePanelDimensionProvider);

        GazeDeviceManager gazeDeviceManager = GazeDeviceManagerFactory.getInstance().createNewGazeListener();

        return new GameContext(gazePlay, gamingRoot, scene, bravo, root2, controlPanel, gamePanelDimensionProvider,
                randomPositionGenerator, root, gazeDeviceManager);
    }

    public static HBox createControlPanel() {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(hbox);

        hbox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        hbox.setStyle("-fx-background-color: rgba(0, 0, 0, 1);" + " -fx-background-radius: 8px;"
                + " -fx-border-radius: 8px;" + " -fx-border-width: 5px;" + " -fx-border-color: rgba(60, 63, 65, 0.7);"
                + " -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

        return hbox;
    }

    private final Bravo bravo;

    private final Pane bottomPane;

    @Getter
    private final HBox menuHBox;

    @Getter
    private final RandomPositionGenerator randomPositionGenerator;

    @Getter
    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    private final BorderPane rootBorderPane;

    @Getter
    private final GazeDeviceManager gazeDeviceManager;

    private GameContext(GazePlay gazePlay, Pane gamingRoot, Scene scene, Bravo bravo, Pane bottomPane, HBox menuHBox,
            GamePanelDimensionProvider gamePanelDimensionProvider, RandomPositionGenerator randomPositionGenerator,
            BorderPane rootBorderPane, GazeDeviceManager gazeDeviceManager) {
        super(gazePlay, gamingRoot, scene);
        this.bravo = bravo;
        this.bottomPane = bottomPane;
        this.menuHBox = menuHBox;
        this.gamePanelDimensionProvider = gamePanelDimensionProvider;
        this.randomPositionGenerator = randomPositionGenerator;
        this.rootBorderPane = rootBorderPane;
        this.gazeDeviceManager = gazeDeviceManager;

        /*
         * double initW = gazePlay.getPrimaryStage().getWidth(); double initH = gazePlay.getPrimaryStage().getHeight();
         * gazePlay.getPrimaryStage().widthProperty().addListener((obs, oldVal, newVal) -> { if (root instanceof Pane) {
         * log.info(""+newVal.doubleValue()+"-"+initW+"/2= "+ (newVal.doubleValue()-initW)/2);
         * ((Pane)root).setLayoutX((newVal.doubleValue()-initW)/2); ((Pane)root).setScaleX(newVal.doubleValue()/initW);
         * } });
         * 
         * gazePlay.getPrimaryStage().heightProperty().addListener((obs, oldVal, newVal) -> { if (root instanceof Pane)
         * { log.info(""+newVal.doubleValue()+"-"+initH+"/2= "+ (newVal.doubleValue()-initH)/2);
         * ((Pane)root).setLayoutY((newVal.doubleValue()-initH)/2); ((Pane)root).setScaleY(newVal.doubleValue()/initH);
         * } });
         */
    }

    @Override
    public void setUpOnStage(Stage stage) {
        // BackgroundMusicManager.getInstance().pause();
        super.setUpOnStage(stage);
    }

    public void resetBordersToFront() {
        // rootBorderPane.setBottom(null);
        // rootBorderPane.setBottom(bottomPane);
    }

    public void createControlPanel(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame) {
        menuHBox.getChildren().add(createMusicControlPane());
        menuHBox.getChildren().add(createEffectsVolumePane());

        I18NButton toggleFullScreenButtonInGameScreen = createToggleFullScreenButtonInGameScreen(gazePlay);
        menuHBox.getChildren().add(toggleFullScreenButtonInGameScreen);

        ProgressHomeButton homeButton = createHomeButtonInGameScreen(gazePlay, stats, currentGame);
        menuHBox.getChildren().add(homeButton);

        Dimension2D dimension2D = getGamePanelDimensionProvider().getDimension2D();
        bottomPane.setLayoutY(dimension2D.getHeight() * 0.90 - menuHBox.getHeight());
        bottomPane.setMinWidth(dimension2D.getWidth());

        gazePlay.getPrimaryStage().widthProperty().addListener((obs, oldVal, newVal) -> {
            bottomPane.setMinWidth(newVal.doubleValue());
        });
        gazePlay.getPrimaryStage().heightProperty().addListener((obs, oldVal, newVal) -> {
            if (gazePlay.isFullScreen()) {
                bottomPane.setLayoutY(newVal.doubleValue() - 2 * 8 - menuHBox.getHeight());
            } else {
                bottomPane.setLayoutY(newVal.doubleValue() - 50 - menuHBox.getHeight());
            }
        });

    }

    public ProgressHomeButton createHomeButtonInGameScreen(@NonNull GazePlay gazePlay, @NonNull Stats stats,
            @NonNull GameLifeCycle currentGame) {

        EventHandler<Event> homeEvent = e -> {
            scene.setCursor(Cursor.WAIT); // Change cursor to wait style
            homeButtonClicked(stats, gazePlay, currentGame);
            // BackgroundMusicManager.getInstance().pause();
            scene.setCursor(Cursor.DEFAULT); // Change cursor to default style
        };

        ProgressHomeButton homeButton = new ProgressHomeButton();
        homeButton.button.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);
        homeButton.assignIndicator(homeEvent);
        return homeButton;
    }

    private void homeButtonClicked(@NonNull Stats stats, @NonNull GazePlay gazePlay,
            @NonNull GameLifeCycle currentGame) {
        currentGame.dispose();

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

        gazePlay.onDisplayStats(statsContext);
    }

    public void playWinTransition(long delay, EventHandler<ActionEvent> onFinishedEventHandler) {
        getChildren().add(bravo);
        bravo.toFront();
        bravo.playWinTransition(scene, delay, onFinishedEventHandler);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

}
