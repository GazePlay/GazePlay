package net.gazeplay.ui.scenes.ingame;

import javafx.event.Event;
import javafx.event.EventDispatcher;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.soundsmanager.SoundsManagerFactory;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.Bravo;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static net.gazeplay.ui.scenes.ingame.GameContext.updateConfigPane;

/**
 * Creates a new {@link GameContext} (game scene with its control panel) each time a game is launched.
 */
@Slf4j
public class GameContextFactory implements Supplier<GameContext> {

    /**
     * Smallest room kept in the menu for its close button.
     */
    static final double BUTTON_MIN_HEIGHT = 64;

    /**
     * With the gaze only, how long the mouse stays still before its pointer is hidden again.
     */
    private static final Duration CURSOR_IDLE_TIME = Duration.seconds(2);

    /**
     * With both inputs, how long the mouse keeps the lead after it moved, on top of the length of a fixation: something can
     * be selected with a still mouse.
     */
    private static final long MOUSE_LEAD_AFTER_FIXATION_MILLIS = 2000;

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

    private final GazePlay gazePlay;

    private final Translator translator;

    private final GazeDeviceManagerFactory gazeDeviceManagerFactory;

    private final SoundsManagerFactory soundsManagerFactory;

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
     * setting themselves (CasseBrique, Follow, gazeRace.Player). A node marked with
     * {@link IGameContext#MOUSE_ALLOWED_PROPERTY} (e.g. the participant form of GazePlay Eval) still gets the mouse.
     */
    static void applyInputMode(final Pane gamingRoot, final EyeTracker eyeTracker) {
        if (!eyeTracker.acceptsMouse()) {
            gamingRoot.addEventFilter(MouseEvent.ANY, event -> {
                if (!allowsMouse(event.getTarget(), gamingRoot)) {
                    event.consume();
                }
            });
        }
    }

    /**
     * @return whether the target of a mouse event, or one of its parents in the game area, is marked with
     *   {@link IGameContext#MOUSE_ALLOWED_PROPERTY}
     */
    private static boolean allowsMouse(final Object target, final Pane gamingRoot) {
        Node node = target instanceof Node targetNode ? targetNode : null;
        while (node != null && node != gamingRoot) {
            if (Boolean.TRUE.equals(node.getProperties().get(IGameContext.MOUSE_ALLOWED_PROPERTY))) {
                return true;
            }
            node = node.getParent();
        }
        return false;
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

        final HBox configPane = new HBox(2);
        configPane.setAlignment(Pos.CENTER_LEFT);
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> updateConfigPane(configPane, primaryStage));
        configPane.heightProperty().addListener((observable) -> updateConfigPane(configPane, primaryStage));

        final GazeDeviceManager gazeDeviceManager = gazeDeviceManagerFactory.get();

        final Scene scene = gazePlay.getPrimaryScene();
        final GameCursor cursor = gameCursor(scene, EyeTracker.fromConfigValue(config.getEyeTracker()),
            () -> config.getFixationLength() + MOUSE_LEAD_AFTER_FIXATION_MILLIS);

        final GameMenuPanel menu = new GameMenuPanel(configPane, controlPanel, open -> {
            setMenuOpen(open, gazeDeviceManager);
            if (cursor != null) {
                cursor.setMenuOpen(open);
            }
        });

        final I18NButton closeButton = new I18NButton(translator, "Close");
        closeButton.setMinWidth(Region.USE_PREF_SIZE);
        closeButton.setOnAction(event -> menu.close());
        controlPanel.getChildren().add(closeButton);

        if (scene != null) {
            connectMenuToScene(scene, root, menu, configPane);
        }
        if (cursor != null) {
            followTheScreenAndTheGaze(scene, root, cursor);
            keepTheHiddenMouseOutOfTheGame(gamingRoot, cursor);
        }

        log.info("the value of the control bar is : =" + controlPanel.getPrefWidth());
        controlPanel.setPrefWidth(primaryStage.getWidth() / 2.5);
        setMenuOpen(false, gazeDeviceManager);

        root.getChildren().add(gamingRoot);
        root.getChildren().add(configPane);

        return new GameContext(gazePlay, translator, root, gamingRoot, bravo, controlPanel, gazeDeviceManager, soundsManagerFactory.get(), configPane);
    }

    /**
     * @param mouseLeadMillis with both inputs, how long the mouse keeps the lead after it moved
     * @return what manages the pointer of the mouse; null with the mouse alone, where it is always visible, or without
     *   scene
     */
    static GameCursor gameCursor(final Scene scene, final EyeTracker eyeTracker, final LongSupplier mouseLeadMillis) {
        if (scene == null || !eyeTracker.usesGaze()) {
            return null;
        }
        return eyeTracker.acceptsMouse() ? GameCursor.forLastInput(scene, mouseLeadMillis)
            : GameCursor.forGazeOnly(scene, CURSOR_IDLE_TIME);
    }

    /**
     * The pointer is managed only on this game screen: the statistics screen, on the same scene, is used with the mouse.
     * The positions of the gaze come as one event each on the root of the game (see {@link Stats}).
     */
    static void followTheScreenAndTheGaze(final Scene scene, final Pane root, final GameCursor cursor) {
        scene.rootProperty().addListener((observable, oldRoot, newRoot) -> {
            if (newRoot == root) {
                cursor.start();
            } else {
                cursor.stop();
            }
        });
        root.addEventFilter(GazeEvent.ANY, event -> {
            if (event.getTarget() == root && event.getEventType() != GazeEvent.GAZE_EXITED) {
                cursor.gazeUsed();
            }
        });
    }

    /**
     * With both inputs, a hidden mouse does not play: something passing under it must not start a fixation (remark of the
     * maintainer, 5/11/2026). The statistics, recorded on the root of the scene, still get its movements.
     */
    static void keepTheHiddenMouseOutOfTheGame(final Pane gamingRoot, final GameCursor cursor) {
        gamingRoot.addEventFilter(MouseEvent.ANY, event -> {
            if (cursor.isMouseOutOfTheGame()) {
                event.consume();
            }
        });
    }

    /**
     * Follows the opening or closing of the menu of the game screen. While it is open, the game is left alone: the gaze
     * is no longer dispatched to the game (see {@link GazeDeviceManager#setPaused(boolean)}), the mouse and the keyboard
     * no longer reach it (see {@link #blocksGameInput}), and nothing is recorded in the statistics.
     */
    private static void setMenuOpen(final boolean open, final GazeDeviceManager gazeDeviceManager) {
        Stats.setConfigMenuOpen(open);
        gazeDeviceManager.setPaused(open);
    }

    /**
     * While this game screen is shown, gives the Esc key to the menu, and stops, at the entrance of the scene, the mouse
     * and keyboard events meant for the game while the menu is open. The scene is the only place where all the games are
     * covered: some of them listen to the mouse or the keyboard on the whole scene (GazeRace, SprintFinish), before any
     * filter of the game screen. The menu closes when another screen replaces this one (home and replay buttons, end of
     * the game), which used to be done by the mouse leaving the menu.
     */
    static void connectMenuToScene(final Scene scene, final Pane root, final GameMenuPanel menu,
                                   final Node menuHolder) {
        final EventDispatcher sceneDispatcher = scene.getEventDispatcher();
        scene.setEventDispatcher((event, tail) -> {
            // Only while this game screen is shown: the statistics screen, shown on the same scene, is not concerned
            if (scene.getRoot() == root
                && (menu.handleKey(event) || blocksGameInput(event, menu.isOpen(), menuHolder))) {
                return null;
            }
            return sceneDispatcher.dispatchEvent(event, tail);
        });
        scene.rootProperty().addListener((observable, oldRoot, newRoot) -> {
            if (newRoot != root) {
                menu.close();
            }
        });
    }

    /**
     * @param menuOpen whether the configuration menu is open
     * @param menu     the holder of the configuration menu
     * @return whether an event must be stopped: while the menu is open, the mouse events outside of the menu and all
     *   the keyboard events
     */
    static boolean blocksGameInput(final Event event, final boolean menuOpen, final Node menu) {
        if (!menuOpen) {
            return false;
        }
        if (event instanceof KeyEvent) {
            return true;
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

}
