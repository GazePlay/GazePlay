package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.NonNull;
import net.gazeplay.commons.configuration.AnimationSpeedRatioSource;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.soundsmanager.SoundManager;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.RandomPositionGenerator;

import java.util.function.Supplier;

/**
 * What the game screen offers to a game: its drawing area ({@link #getChildren()}), the settings, the gaze, the sounds,
 * the translations, the end of round transition and statistics screen, and the score and time limits of the settings.
 */
public interface IGameContext {

    /**
     * Key of {@link Node#getProperties()}: a node of the game area marked with it (value {@code true}) receives the
     * mouse, with its children, even with the eye tracker alone (EYE_TRACKER=tobii), where the mouse is otherwise
     * stopped. For a screen used by someone else than the player, e.g. the participant form of GazePlay Eval, filled
     * by the examiner (maintainer's request of 30/10/2026).
     */
    String MOUSE_ALLOWED_PROPERTY = "gazeplay.mouseAllowed";

    /**
     * @return the settings of the active profile
     */
    @NonNull
    Configuration getConfiguration();

    /**
     * @return the animation speed chosen by the user, which can change during the game
     */
    AnimationSpeedRatioSource getAnimationSpeedRatioSource();

    /**
     * @return a generator of random positions in the game area
     */
    @NonNull
    RandomPositionGenerator getRandomPositionGenerator();

    /**
     * @return the size of the game area
     */
    @NonNull
    GamePanelDimensionProvider getGamePanelDimensionProvider();

    /**
     * @return where the game registers the nodes that react to the gaze
     */
    @NonNull
    GazeDeviceManager getGazeDeviceManager();

    /**
     * @return the queue of the sounds of the game
     */
    @NonNull
    SoundManager getSoundManager();

    /**
     * Pauses the background music of GazePlay, e.g. for a game whose sounds must be heard alone. The games used to call
     * the music manager of the application directly.
     */
    void pauseBackgroundMusic();

    /**
     * @return the translations of the interface in the language of the settings
     */
    @NonNull
    Translator getTranslator();

    /**
     * @return the window of GazePlay
     */
    @NonNull
    Stage getPrimaryStage();

    /**
     * @return the scene of the window
     */
    @NonNull
    Scene getPrimaryScene();

    /**
     * @return the nodes of the game area: a game adds its nodes there
     */
    @NonNull
    ObservableList<Node> getChildren();

    /**
     * @return the game area
     */
    @NonNull
    Pane getRoot();

    /**
     * Removes all the nodes of the game area.
     */
    void clear();

    /**
     * Ends the game: stops and saves the statistics, then shows the statistics screen, whose "continue" button launches
     * a new round of the game.
     */
    void showRoundStats(Stats stats, GameLifeCycle currentGame);

    /**
     * Does nothing (kept for the games that call it).
     */
    void onGameStarted();

    /**
     * Disables the game area for {@code delay} ms, so that nothing can be selected while the round appears.
     */
    void onGameStarted(int delay);

    /**
     * Removes the fixation length control from the side panel, for the games that do not use it.
     */
    void setOffFixationLengthControl();

    /**
     * Shows the "bravo" animation with confetti over the game, then calls {@code onFinishedEventHandler}.
     */
    void playWinTransition(long delay, EventHandler<ActionEvent> onFinishedEventHandler);

    /**
     * Removes the "bravo" animation.
     */
    void endWinTransition();

    /**
     * @return the size of the screen showing GazePlay
     */
    Supplier<Dimension2D> getCurrentScreenDimensionSupplier();

    /**
     * Enables the score limit of the settings (LIMITER_SCORE) for this game, if enabled there: checked by
     * {@link #updateScore}.
     */
    void startScoreLimiter();

    /**
     * Enables the time limit of the settings (LIMITER_TIME) for this game, if enabled there: checked by
     * {@link #updateScore}.
     */
    void startTimeLimiter();

    /**
     * Allows the limits to end the game again (each limit ends it once).
     */
    void setLimiterAvailable();

    /**
     * Starts the timer of the time limit.
     */
    void start();

    /**
     * Starts the timer of the time limit, unless it is already running.
     */
    void firstStart();

    /**
     * Stops the timer of the time limit.
     */
    void stop();

    /**
     * To be called by a game after each goal: when the score or time limit of the settings is reached, calls the
     * matching handler, plays the win transition and shows the statistics.
     */
    void updateScore(Stats stats, GameLifeCycle currentGame, EventHandler<ActionEvent> onTimeLimiterEndEventHandler, EventHandler<ActionEvent> onScoreLimiterEndEventHandler);

    /**
     * Same as {@link #updateScore(Stats, GameLifeCycle, EventHandler, EventHandler)}, with no handlers.
     */
    void updateScore(Stats stats, GameLifeCycle currentGame);

}
