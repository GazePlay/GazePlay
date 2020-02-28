package net.gazeplay.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;

public interface IGazeProgressIndicator {

    void setOnFinish(EventHandler<ActionEvent> handler);

    void start();

    void stop();

    boolean addNodeToListen(final Node node, final GazeDeviceManager gazeDeviceManager);

    boolean removeNodeToListen(final Node node, final GazeDeviceManager gazeDeviceManager);
}
