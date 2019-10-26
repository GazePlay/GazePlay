package net.gazeplay.games.colors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;

public interface IGazeProgressIndicator {

    public void setOnFinish(EventHandler<ActionEvent> handler);

    public void start();

    public void stop();

    public boolean addNodeToListen(final Node node, final GazeDeviceManager gazeDeviceManager);

    public boolean removeNodeToListen(final Node node, final GazeDeviceManager gazeDeviceManager);
}
