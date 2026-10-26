package net.gazeplay.ui.scenes.ingame;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.event.Event;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.soundsmanager.SoundsManagerFactory;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.Bravo;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Objects;
import java.util.function.Supplier;

import static net.gazeplay.ui.scenes.ingame.GameContext.updateConfigPane;

/**
 * Creates a new {@link GameContext} (game scene with its control panel) each time a game is launched.
 */
@Slf4j
public class GameContextFactory implements Supplier<GameContext> {

    static final double BUTTON_MIN_HEIGHT = 64;

    /**
     * @return the size of the configuration button: a tenth of the width of the window
     */
    private static double computeButtonSize(Stage primaryStage) {
        return primaryStage.getWidth() / 10;
    }

    /**
     * @return the empty side panel of the game screen
     */
    private static HBox createControlPanel() {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(hbox);

        Configuration config = ActiveConfigurationContext.getInstance();
        if (config.isDarkThemeEnabled()) {
            hbox.setStyle("-fx-background-color: rgba(0,0,0,1); " + "-fx-background-radius: 8px; "
                + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);" + "-fx-text-fill: white;");
        }
        else {
            hbox.setStyle("-fx-background-color: #fffaf0; " + "-fx-background-radius: 8px; "
                + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: white;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);"+"-fx-text-fill: black;");
        }
        return hbox;
    }

    /**
     * Sizes the configuration button for the width of the window (at least {@link #BUTTON_MIN_HEIGHT}).
     */
    private static void updateConfigButton(Button button, ImageView btnImg, Stage primaryStage) {
        double buttonSize = primaryStage.getWidth() / 10;

        if (buttonSize < BUTTON_MIN_HEIGHT) {
            buttonSize = BUTTON_MIN_HEIGHT;
        }

        btnImg.setFitWidth(buttonSize);
        btnImg.setFitHeight(buttonSize);

        button.setPrefHeight(buttonSize);
        button.setPrefWidth(buttonSize);
    }

    private final GazePlay gazePlay;

    private final Translator translator;

    private final GazeDeviceManagerFactory gazeDeviceManagerFactory;

    private final SoundsManagerFactory soundsManagerFactory;

    private boolean menuOpen = false;

    public GameContextFactory(final GazePlay gazePlay, final Translator translator,
                              final GazeDeviceManagerFactory gazeDeviceManagerFactory,
                              final SoundsManagerFactory soundsManagerFactory) {
        this.gazePlay = gazePlay;
        this.translator = translator;
        this.gazeDeviceManagerFactory = gazeDeviceManagerFactory;
        this.soundsManagerFactory = soundsManagerFactory;
    }

    /**
     * Gaze only (EYE_TRACKER=tobii): the mouse no longer reaches the game, e.g. its progress indicators. The
     * configuration panel, next to the game area, still works with the mouse, and the statistics, recorded on the root
     * of the scene, still record the mouse movements. Three games listen to the mouse on the whole scene and check the
     * setting themselves (CasseBrique, Follow, gazeRace.Player).
     */
    static void applyInputMode(final Pane gamingRoot, final EyeTracker eyeTracker) {
        if (!eyeTracker.acceptsMouse()) {
            gamingRoot.addEventFilter(MouseEvent.ANY, MouseEvent::consume);
        }
    }

    @Override
    public GameContext get() {
        final Stage primaryStage = Objects.requireNonNull(gazePlay.getPrimaryStage(), "primaryStage is required");

        Pane root = new Pane();

        root.prefWidthProperty().bind(primaryStage.widthProperty());
        root.prefHeightProperty().bind(primaryStage.heightProperty());
        root.minWidthProperty().bind(primaryStage.widthProperty());
        root.minHeightProperty().bind(primaryStage.heightProperty());

        Bravo bravo = new Bravo();

        Pane gamingRoot = new Pane();
        gamingRoot.prefWidthProperty().bind(primaryStage.widthProperty());
        gamingRoot.prefHeightProperty().bind(primaryStage.heightProperty());
        gamingRoot.minWidthProperty().bind(primaryStage.widthProperty());
        gamingRoot.minHeightProperty().bind(primaryStage.heightProperty());

        Configuration config = ActiveConfigurationContext.getInstance();

        applyInputMode(gamingRoot, EyeTracker.fromConfigValue(config.getEyeTracker()));

        Color color = Color.web(config.getBackgroundColorProperty().getValue());

        gamingRoot.setBackground(new Background(new BackgroundFill(color, null, null)));

        HBox controlPanel = createControlPanel();
        // Adapt the size and position of buttons to screen width
        controlPanel.maxWidthProperty().bind(root.widthProperty());
        controlPanel.toFront();

        double buttonSize = computeButtonSize(primaryStage);

        ImageView buttonImg = new ImageView(new Image("data/common/images/configuration-button-alt4.png"));
        buttonImg.setFitWidth(buttonSize);
        buttonImg.setFitHeight(buttonSize);

        final Button bt = new Button();
        bt.setMinHeight(BUTTON_MIN_HEIGHT);
        bt.setGraphic(buttonImg);
        bt.setStyle("-fx-background-color: transparent;");
        updateConfigButton(bt, buttonImg, primaryStage);

        final HBox configPane = new HBox(2);
        configPane.setAlignment(Pos.CENTER_LEFT);
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> updateConfigPane(configPane, primaryStage));
        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> updateConfigButton(bt, buttonImg, primaryStage));
        configPane.heightProperty().addListener((observable) -> updateConfigPane(configPane, primaryStage));

        final GazeDeviceManager gazeDeviceManager = gazeDeviceManagerFactory.get();

        EventHandler<MouseEvent> mousePressedControlPanelEventHandler = mouseEvent -> {
            double from = 0;
            double to = 1;
            double angle = 360;

            if (menuOpen) {
                from = 1;
                to = 0;
                angle = -1 * angle;
            } else if (!configPane.getChildren().contains(controlPanel)) {
                configPane.getChildren().add(controlPanel);
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
            setMenuOpen(!menuOpen, gazeDeviceManager);
            pt.setOnFinished(actionEvent -> {
                if (!menuOpen) {
                    configPane.getChildren().remove(controlPanel);
                }
            });
            pt.play();
        };

        // The menu closes when the mouse leaves it. This handler used to be added again at each opening of the menu,
        // and ran even when the menu was closed
        configPane.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (!menuOpen) {
                return;
            }
            RotateTransition rt = new RotateTransition(Duration.millis(500), bt);
            rt.setByAngle(-360);
            FadeTransition ft = new FadeTransition(Duration.millis(500), controlPanel);
            ft.setFromValue(1);
            ft.setToValue(0);
            ParallelTransition pt = new ParallelTransition();
            pt.getChildren().addAll(rt, ft);
            controlPanel.setPrefWidth(primaryStage.getWidth() / 2.5);
            controlPanel.setVisible(false);
            controlPanel.setDisable(true);
            controlPanel.setMouseTransparent(true);
            setMenuOpen(false, gazeDeviceManager);
            pt.setOnFinished(actionEvent -> {
                if (!menuOpen) {
                    configPane.getChildren().remove(controlPanel);
                }
            });
            pt.play();
            buttonTransparentHandler(bt);
        });

        final Scene scene = gazePlay.getPrimaryScene();
        if (scene != null) {
            blockGameInputWhileMenuOpen(scene, root, configPane, config);
        }

        log.info("the value of the control bar is : =" + controlPanel.getPrefWidth());
        controlPanel.setPrefWidth(primaryStage.getWidth() / 2.5);
        controlPanel.setVisible(false);
        controlPanel.setDisable(true);
        controlPanel.setMouseTransparent(true);
        setMenuOpen(false, gazeDeviceManager);

        bt.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedControlPanelEventHandler);
        bt.getStyleClass().add("button");

        buttonTransparentHandler(bt);

        configPane.getChildren().add(bt);
        root.getChildren().add(gamingRoot);
        root.getChildren().add(configPane);

        return new GameContext(gazePlay, translator, root, gamingRoot, bravo, controlPanel, gazeDeviceManager, soundsManagerFactory.get(), configPane);
    }

    /**
     * Opens or closes the configuration menu of the game screen. While it is open, the game is left alone: the gaze is
     * no longer dispatched to the game (see {@link GazeDeviceManager#setPaused(boolean)}), the mouse and the keyboard no
     * longer reach it (see {@link #blocksGameInput}), and nothing is recorded in the statistics.
     */
    private void setMenuOpen(final boolean open, final GazeDeviceManager gazeDeviceManager) {
        menuOpen = open;
        Stats.setConfigMenuOpen(open);
        gazeDeviceManager.setPaused(open);
    }

    /**
     * Stops, at the entrance of the scene, the mouse and keyboard events meant for the game while the configuration
     * menu is open. The scene is the only place where all the games are covered: some of them listen to the mouse or
     * the keyboard on the whole scene (GazeRace, SprintFinish, SurviveAgainstRobots), before any filter of the game
     * screen.
     */
    private void blockGameInputWhileMenuOpen(final Scene scene, final Pane root, final Node menu,
                                             final Configuration config) {
        final EventDispatcher sceneDispatcher = scene.getEventDispatcher();
        scene.setEventDispatcher((event, tail) -> {
            // Only while this game screen is shown: the statistics screen, shown on the same scene, is not concerned
            if (scene.getRoot() == root && blocksGameInput(event, menuOpen, menu, config.getQuitKey())) {
                return null;
            }
            return sceneDispatcher.dispatchEvent(event, tail);
        });
    }

    /**
     * @param menuOpen whether the configuration menu is open
     * @param menu     the configuration menu (its button and panel)
     * @param quitKey  the key leaving the game (QUIT_KEY setting), which keeps working
     * @return whether an event must be stopped: while the menu is open, the mouse events outside of the menu and the
     *   keyboard events other than the quit key
     */
    static boolean blocksGameInput(final Event event, final boolean menuOpen, final Node menu, final String quitKey) {
        if (!menuOpen) {
            return false;
        }
        if (event instanceof KeyEvent keyEvent) {
            return !keyEvent.getCode().getChar().equals(quitKey);
        }
        if (event instanceof MouseEvent || event instanceof ScrollEvent) {
            Node node = event.getTarget() instanceof Node target ? target : null;
            while (node != null) {
                if (node == menu) {
                    return false;
                }
                node = node.getParent();
            }
            return true;
        }
        return false;
    }

    /**
     * Makes the configuration button fade out 2 seconds after it appears and when the mouse leaves it, and come back
     * when the mouse is on it (unless the panel is open).
     */
    private void buttonTransparentHandler(Button bt) {
        FadeTransition fd = new FadeTransition(Duration.millis(500), bt);
        fd.setFromValue(1);
        fd.setToValue(0.1);

        FadeTransition initialFd = new FadeTransition(Duration.seconds(1), bt);
        initialFd.setFromValue(1);
        initialFd.setToValue(0.1);
        initialFd.setDelay(Duration.seconds(2));

        EventHandler<MouseEvent> mouseEnterControlPanelEventHandler = mouseEvent -> {
            if (!menuOpen) {
                fd.stop();
                initialFd.stop();
                bt.setOpacity(1);
            }
        };

        bt.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEnterControlPanelEventHandler);

        EventHandler<MouseEvent> mouseExitControlPanelEventHandler = mouseEvent -> {
            if (!menuOpen) {
                fd.play();
            }
        };

        bt.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitControlPanelEventHandler);

        initialFd.play();

    }


}
