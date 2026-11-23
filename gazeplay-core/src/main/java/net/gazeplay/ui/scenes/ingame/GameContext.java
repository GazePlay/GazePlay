package net.gazeplay.ui.scenes.ingame;

import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
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
 * The game screen: the game area ({@link #getRoot()}, where the game draws), its menu opened with Esc (a window in the
 * middle of the darkened game, {@link GameMenuView}, refonte of 17/11/2026: resume, restart, quit, sound, settings of
 * the games, progress ring), the "bravo" animation, and the gaze and sound managers of the game.
 * <p>
 * It implements {@link IGameContext}, the interface the games see. Created by {@link GameContextFactory}.
 */
@Slf4j
public class GameContext extends GraphicalContext<Pane> implements IGameContext {

    /**
     * The button of the menu quitting the game (the replay of a file adds its own action to it).
     */
    @Getter
    private Button homeButton;

    /**
     * The button of the menu restarting the game.
     */
    @Getter
    private Button replayButton;

    @Getter
    private final Translator translator;

    private final Bravo bravo;

    /**
     * The darkened layer of the menu, over the whole screen, which holds its window while it is open.
     */
    @Getter
    private final Pane menuOverlay;

    @Getter
    private final RandomPositionGenerator randomPositionGenerator;

    @Getter
    private final GamePanelDimensionProvider gamePanelDimensionProvider;

    @Getter
    private final GazeDeviceManager gazeDeviceManager;

    @Getter
    private final SoundManager soundManager;

    private final Pane gamingRoot;

    private VideoRecordingContext videoRecordingContext;

    @Getter
    private GameMenuView menuView;

    /**
     * Closes the menu (set by {@link GameContextFactory}): the Resume button.
     */
    @lombok.Setter
    private Runnable menuCloser = () -> { };

    /**
     * Use {@link GameContextFactory} to create a game screen.
     */
    protected GameContext(
        @NonNull GazePlay gazePlay,
        @NonNull Translator translator,
        @NonNull Pane root,
        @NonNull Pane gamingRoot,
        @NonNull Bravo bravo,
        @NonNull Pane menuOverlay,
        @NonNull GazeDeviceManager gazeDeviceManager,
        @NonNull SoundManager soundManager,
        @NonNull Pane menuLayer
    ) {
        super(gazePlay, root);
        this.translator = translator;
        this.gamingRoot = gamingRoot;
        this.bravo = bravo;
        this.menuOverlay = menuOverlay;
        this.gazeDeviceManager = gazeDeviceManager;
        this.soundManager = soundManager;

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
    }

    @Override
    public Supplier<Dimension2D> getCurrentScreenDimensionSupplier() {
        return getGazePlay().getCurrentScreenDimensionSupplier();
    }

    /**
     * Fills the menu (Esc) of this game: everything for a game; the sound and Quit for the replay of a file (which
     * gives Quit its action); the volume, Resume and Quit for an evaluation ({@link GameLifeCycle#offersGameSettings()}).
     */
    public void createControlPanel(@NonNull GazePlay gazePlay, @NonNull Stats stats, GameLifeCycle currentGame, boolean inReplayMode) {
        final GameMenuView.Kind kind = inReplayMode ? GameMenuView.Kind.REPLAY
            : currentGame != null && !currentGame.offersGameSettings() ? GameMenuView.Kind.EVALUATION
            : GameMenuView.Kind.GAME;
        // Started by the former side panel of the music: the background music of the game screen, once per JVM
        getMusicControl().startTheMusicOnce();

        final Translator menuTranslator = gazePlay.getTranslator();
        menuView = new GameMenuView(menuTranslator, menuLocale(menuTranslator), getConfiguration(),
            gazePlay.getPrimaryScene(), kind,
            currentGame == null ? java.util.EnumSet.allOf(net.gazeplay.MenuSetting.class) : currentGame.menuSettings(),
            () -> menuCloser.run());
        homeButton = menuView.getQuitButton();
        replayButton = menuView.getRestartButton();
        if (!inReplayMode) {
            homeButton.setOnAction(e -> {
                root.setCursor(Cursor.WAIT);
                exitGame(stats, gazePlay, currentGame);
                root.setCursor(Cursor.DEFAULT);
            });
            replayButton.setOnAction(e -> {
                exitReplayGame(stats, gazePlay, currentGame);
                getGazePlay().onGameLaunch(this);
                stats.reset();
                currentGame.launch();
                soundManager.init();
            });
        }
        menuOverlay.getChildren().setAll(menuView.getCard());
    }

    private static java.util.Locale menuLocale(final Translator translator) {
        final java.util.Locale locale = translator == null ? null : translator.currentLocale();
        if (locale == null || locale.getLanguage().isEmpty()) {
            return java.util.Locale.FRENCH;
        }
        final String language = locale.getLanguage();
        for (final String iso2 : java.util.Locale.getISOLanguages()) {
            try {
                if (java.util.Locale.of(iso2).getISO3Language().equals(language)) {
                    return java.util.Locale.of(iso2);
                }
            } catch (final java.util.MissingResourceException e) {
                // no ISO 3 code for this language
            }
        }
        return locale;
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

        if (stats.isSavingDisabled()) {
            // Nothing saved (GazePlay Eval Test, or an evaluation left on its participant form): no statistics to show
            this.clear();
            return;
        }
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

        // No button going on with the game any more (maintainer's request of 16/11/2026, done on 17/11/2026): one goes
        // back to the menu and launches the game again from it
        StatsContext statsContext = StatsContextFactory.newInstance(getGazePlay(), stats);

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

    /**
     * The delay before a selection running, null otherwise (see {@link #onGameStarted()}).
     */
    private volatile PauseTransition selectionDelay;

    @Override
    public void onGameStarted() {
        final int delay = getConfiguration().getGameStartDelay();
        if (delay <= 0) {
            return;
        }
        // One delay at a time, started again at each call (23/11/2026, point 22: the games call it at each new choice;
        // two delays used to run side by side, the first one enabling the game area too early)
        if (selectionDelay != null) {
            selectionDelay.stop();
        }
        gamingRoot.setDisable(true);
        final PauseTransition wait = new PauseTransition(Duration.millis(delay));
        wait.setOnFinished(waitEvent -> {
            selectionDelay = null;
            gamingRoot.setDisable(false);
        });
        selectionDelay = wait;
        wait.play();
    }

    @Override
    public boolean isSelectionBlocked() {
        return selectionDelay != null;
    }

    /**
     * Takes the fixation length out of the menu, for a game that does not use it (Race).
     */
    public void setOffFixationLengthControl() {
        if (menuView != null && menuView.getFixationRow() != null) {
            menuView.getFixationRow().setVisible(false);
            menuView.getFixationRow().setManaged(false);
        }
    }

}
