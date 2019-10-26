package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.RandomPositionGenerator;

import java.io.IOException;

public interface IGameContext {

    RandomPositionGenerator getRandomPositionGenerator();

    GamePanelDimensionProvider getGamePanelDimensionProvider();

    GazeDeviceManager getGazeDeviceManager();
    
    Translator getTranslator();
    
    Stage getPrimaryStage();

    Scene getPrimaryScene();
    
    void clear();

    void showRoundStats(Stats stats, GameLifeCycle currentGame) throws IOException;

    void onGameStarted();
    
    void playWinTransition(long delay, EventHandler<ActionEvent> onFinishedEventHandler);

    void endWinTransition();

    ObservableList<Node> getChildren();

    Pane getRoot();

    void resetBordersToFront();
    
}
