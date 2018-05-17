package net.gazeplay;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.utils.*;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.IOException;

@Slf4j
public class GameContext extends GraphicalContext<Pane> {

    @Setter
    private static boolean runAsynchronousStatsPersist = false;

    public static GameContext newInstance(GazePlay gazePlay) {

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, gazePlay.getPrimaryStage().getWidth(), gazePlay.getPrimaryStage().getHeight(),
                Color.BLACK);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        CssUtil.setPreferredStylesheets(config, scene);

        Bravo bravo = new Bravo();

        Pane gamingRoot = new Pane();
        gamingRoot.setStyle("-fx-background-color: black;");

        HBox controlPanel = createControlPanel();
        // Adapt the size and position of buttons to screen width
        controlPanel.maxWidthProperty().bind(root.widthProperty());
        controlPanel.toFront();

        EventHandler<MouseEvent> mouseEnterControlPanelEventHandler = mouseEvent -> {
            controlPanel.setOpacity(1);
            // root.setBottom(autoHiddingControlPanel);
        };
        EventHandler<MouseEvent> mouseExitControlPanelEventHandler = mouseEvent -> {
            controlPanel.setOpacity(0);
            // root.setBottom(null);
        };

        controlPanel.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnterControlPanelEventHandler);
        controlPanel.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitControlPanelEventHandler);

        mouseEnterControlPanelEventHandler.handle(null);
        BorderPane root2 = new BorderPane();
        root2.setBottom(controlPanel);
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
    }

    @Override
    public void setUpOnStage(Stage stage) {
        BackgroundMusicManager.getInstance().pauseAll();
        super.setUpOnStage(stage);
    }

    public void resetBordersToFront() {
        // rootBorderPane.setBottom(null);
        // rootBorderPane.setBottom(bottomPane);
    }

    public void createControlPanel(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame) {
        menuHBox.getChildren().add(createSoundControlPane());

        I18NButton toggleFullScreenButtonInGameScreen = createToggleFullScreenButtonInGameScreen(gazePlay);
        menuHBox.getChildren().add(toggleFullScreenButtonInGameScreen);

        ProgressHomeButton homeButton = createHomeButtonInGameScreen(gazePlay, stats, currentGame);
        menuHBox.getChildren().add(homeButton);

        Dimension2D dimension2D = getGamePanelDimensionProvider().getDimension2D();
        bottomPane.setLayoutY(dimension2D.getHeight() * 0.92 - menuHBox.getHeight());
        bottomPane.setMinWidth(dimension2D.getWidth());
    }

    public ProgressHomeButton createHomeButtonInGameScreen(@NonNull GazePlay gazePlay, @NonNull Stats stats,
            @NonNull GameLifeCycle currentGame) {

        EventHandler<Event> homeEvent = e -> {
            scene.setCursor(Cursor.WAIT); // Change cursor to wait style
            homeButtonClicked(stats, gazePlay, currentGame);
            BackgroundMusicManager.getInstance().pauseAll();
            scene.setCursor(Cursor.DEFAULT); // Change cursor to default style
        };

        ProgressHomeButton homeButton = new ProgressHomeButton();
        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);
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
        bravo.playWinTransition(scene, delay, onFinishedEventHandler);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

}
