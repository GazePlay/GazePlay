package net.gazeplay;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.scene.control.Button;
import net.gazeplay.commons.utils.games.SystemInfo;

@Slf4j
public class GameContext extends GraphicalContext<Pane> {

    public static boolean menuOpen = false;

    @Setter
    private static boolean runAsynchronousStatsPersist = false;

    private static final double BUTTON_MIN_HEIGHT = 64;

    public static GameContext newInstance(GazePlay gazePlay) {

        Pane root = new Pane();

        final Stage primaryStage = gazePlay.getPrimaryStage();

        final Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight(), Color.BLACK);

        root.prefWidthProperty().bind(primaryStage.widthProperty());
        root.prefHeightProperty().bind(primaryStage.heightProperty());
        root.minWidthProperty().bind(primaryStage.widthProperty());
        root.minHeightProperty().bind(primaryStage.heightProperty());

        final Configuration config = Configuration.getInstance();
        CssUtil.setPreferredStylesheets(config, scene);

        Bravo bravo = new Bravo();

        Pane gamingRoot = new Pane();
        gamingRoot.setStyle("-fx-background-color: black;");
        gamingRoot.prefWidthProperty().bind(primaryStage.widthProperty());
        gamingRoot.prefHeightProperty().bind(primaryStage.heightProperty());
        gamingRoot.minWidthProperty().bind(primaryStage.widthProperty());
        gamingRoot.minHeightProperty().bind(primaryStage.heightProperty());

        HBox controlPanel = createControlPanel();
        // Adapt the size and position of buttons to screen width
        controlPanel.maxWidthProperty().bind(root.widthProperty());
        controlPanel.toFront();

        double buttonSize = getButtonSize();

        // Button bt = new Button();
        ImageView buttonImg = new ImageView(new Image("data/common/images/configuration-button-alt4.png"));
        buttonImg.setFitWidth(buttonSize);
        buttonImg.setFitHeight(buttonSize);

        final Button bt = new Button();
        bt.setMinHeight(BUTTON_MIN_HEIGHT);
        bt.setGraphic(buttonImg);
        bt.setStyle("-fx-background-color: transparent;");
        updateConfigButton(bt, buttonImg);
        /*
         * bt.setStyle("-fx-background-radius: " + buttonSize + "em; " + "-fx-min-width: " + buttonSize + "px; " +
         * "-fx-min-height: " + buttonSize + "px; " + "-fx-max-width: " + buttonSize + "px; " + "-fx-max-height: " +
         * buttonSize + "px;");
         */

        final HBox root2 = new HBox(2);
        root2.setAlignment(Pos.CENTER_LEFT);
        // Pane root2 = new Pane();
        gazePlay.getPrimaryStage().heightProperty().addListener((obs, oldVal, newVal) -> {
            updateConfigPane(root2);
        });
        gazePlay.getPrimaryStage().widthProperty().addListener((obs, oldVal, newVal) -> {
            updateConfigButton(bt, buttonImg);
        });
        root2.heightProperty().addListener((observable) -> {
            updateConfigPane(root2);
        });

        EventHandler<MouseEvent> mouseEnterControlPanelEventHandler = mouseEvent -> {
            double from = 0;
            double to = 1;
            double angle = 360;
            if (menuOpen) {
                from = 1;
                to = 0;
                angle = -1 * angle;
            } else {
                root2.getChildren().add(controlPanel);
            }
            RotateTransition rt = new RotateTransition(Duration.millis(500), bt);
            rt.setByAngle(angle);
            FadeTransition ft = new FadeTransition(Duration.millis(500), controlPanel);
            ft.setFromValue(from);
            ft.setToValue(to);
            ParallelTransition pt = new ParallelTransition();
            pt.getChildren().addAll(rt, ft);
            controlPanel.setDisable(menuOpen);
            controlPanel.setMouseTransparent(menuOpen);
            controlPanel.setVisible(true);
            menuOpen = !menuOpen;
            pt.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if (!menuOpen) {
                        root2.getChildren().remove(controlPanel);
                    }
                }
            });
            pt.play();
        };

        log.info("the value of the control bar is : =" + controlPanel.getPrefWidth());
        controlPanel.setPrefWidth(gazePlay.getPrimaryStage().getWidth() / 2.5);
        controlPanel.setVisible(false);
        controlPanel.setDisable(true);
        controlPanel.setMouseTransparent(true);
        menuOpen = false;

        bt.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEnterControlPanelEventHandler);
        bt.getStyleClass().add("button");

        root2.getChildren().add(bt);
        root2.getChildren().add(controlPanel);
        root.getChildren().add(gamingRoot);
        root.getChildren().add(root2);

        root2.getChildren().remove(controlPanel);

        GamePanelDimensionProvider gamePanelDimensionProvider = new GamePanelDimensionProvider(gamingRoot, scene);

        RandomPositionGenerator randomPositionGenerator = new RandomPanePositionGenerator(gamePanelDimensionProvider);

        GazeDeviceManager gazeDeviceManager = GazeDeviceManagerFactory.getInstance().createNewGazeListener();

        return new GameContext(gazePlay, gamingRoot, scene, bravo, controlPanel, gamePanelDimensionProvider,
                randomPositionGenerator, gazeDeviceManager, root2);
    }

    private static void updateConfigButton(Button button, ImageView btnImg) {

        final GazePlay gazePlay = GazePlay.getInstance();
        double buttonSize = gazePlay.getPrimaryStage().getWidth() / 10;

        if (buttonSize < BUTTON_MIN_HEIGHT) {
            buttonSize = BUTTON_MIN_HEIGHT;
        }

        btnImg.setFitWidth(buttonSize);
        btnImg.setFitHeight(buttonSize);

        button.setPrefHeight(buttonSize);
        button.setPrefWidth(buttonSize);
    }

    private static void updateConfigPane(final Pane configPane) {

        final GazePlay gazePlay = GazePlay.getInstance();

        double mainHeight = gazePlay.getPrimaryStage().getHeight();

        final double newY = mainHeight - configPane.getHeight() - 30;
        log.info("translated config pane to y : {}, height : {}", newY, configPane.getHeight());
        configPane.setTranslateY(newY);
    }

    private static double getButtonSize() {
        final GazePlay gazePlay = GazePlay.getInstance();
        double buttonSize = gazePlay.getPrimaryStage().getWidth() / 10;
        return buttonSize;
    }

    public static HBox createControlPanel() {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(hbox);

        hbox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        hbox.setStyle("-fx-background-color: rgba(0, 0, 0, 1);" + " -fx-background-radius: 8px;"
                + " -fx-border-radius: 8px;" + " -fx-border-width: 5px;" + " -fx-border-color: rgba(60, 63, 65, 0.7);"
                + " -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

        return hbox;
    }

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

    private GameContext(GazePlay gazePlay, Pane gamingRoot, Scene scene, Bravo bravo, HBox menuHBox,
            GamePanelDimensionProvider gamePanelDimensionProvider, RandomPositionGenerator randomPositionGenerator,
            GazeDeviceManager gazeDeviceManager, final Pane configPane) {
        super(gazePlay, gamingRoot, scene);
        this.bravo = bravo;
        this.menuHBox = menuHBox;
        this.gamePanelDimensionProvider = gamePanelDimensionProvider;
        this.randomPositionGenerator = randomPositionGenerator;
        this.gazeDeviceManager = gazeDeviceManager;
        this.configPane = configPane;

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

        super.setUpOnStage(stage);

        updateConfigPane(configPane);
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

        HomeButton homeButton = createHomeButtonInGameScreen(gazePlay, stats, currentGame);
        menuHBox.getChildren().add(homeButton);
    }

    public HomeButton createHomeButtonInGameScreen(@NonNull GazePlay gazePlay, @NonNull Stats stats,
            @NonNull GameLifeCycle currentGame) {

        EventHandler<Event> homeEvent = e -> {
            scene.setCursor(Cursor.WAIT); // Change cursor to wait style
            homeButtonClicked(stats, gazePlay, currentGame);
            // BackgroundMusicManager.getInstance().pause();
            scene.setCursor(Cursor.DEFAULT); // Change cursor to default style
        };

        HomeButton homeButton = new HomeButton();
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);
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

        this.dispose();
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
