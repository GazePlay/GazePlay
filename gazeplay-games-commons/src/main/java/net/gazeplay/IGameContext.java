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
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.RandomPositionGenerator;

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

    void playWinTransition(long delay, EventHandler<ActionEvent> onFinishedEventHandler);

    void endWinTransition();

    void resetBordersToFront();

    Supplier<Dimension2D> getCurrentScreenDimensionSupplier();

}
