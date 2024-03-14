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
import net.gazeplay.components.SaveData;

import java.util.function.Supplier;

public interface IGameContext {

    @NonNull
    Configuration getConfiguration();

    AnimationSpeedRatioSource getAnimationSpeedRatioSource();

    @NonNull
    RandomPositionGenerator getRandomPositionGenerator();

    @NonNull
    GamePanelDimensionProvider getGamePanelDimensionProvider();

    @NonNull
    GazeDeviceManager getGazeDeviceManager();

    @NonNull
    SoundManager getSoundManager();

    @NonNull
    Translator getTranslator();

    @NonNull
    Stage getPrimaryStage();

    @NonNull
    Scene getPrimaryScene();

    @NonNull
    ObservableList<Node> getChildren();

    @NonNull
    Pane getRoot();

    void clear();

    void showRoundStats(Stats stats, GameLifeCycle currentGame);

    void onGameStarted();

    void onGameStarted(int delay);

    void setOffFixationLengthControl();

    void playWinTransition(long delay, EventHandler<ActionEvent> onFinishedEventHandler);

    void endWinTransition();

    void resetBordersToFront();

    Supplier<Dimension2D> getCurrentScreenDimensionSupplier();

    void startScoreLimiter();

    void startTimeLimiter();

    void startTimeLimiterEmmanuel(SaveData saveData);

    void setLimiterAvailable();

    void start();

    void firstStart();

    void stop();

    void updateScore(Stats stats, GameLifeCycle currentGame, EventHandler<ActionEvent> onTimeLimiterEndEventHandler, EventHandler<ActionEvent> onScoreLimiterEndEventHandler);

    void updateScore(Stats stats, GameLifeCycle currentGame);

}
