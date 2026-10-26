package net.gazeplay.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;

/**
 * A progress indicator filled by a fixation on one or several nodes.
 */
public interface IGazeProgressIndicator {

    /**
     * Sets what happens when the fixation lasted long enough.
     */
    void setOnFinish(EventHandler<ActionEvent> handler);

    /**
     * Starts filling the indicator.
     */
    void start();

    /**
     * Stops and hides the indicator.
     */
    void stop();

    /**
     * Starts the indicator when the gaze or the mouse enters the node, and stops it when it leaves; the node is
     * registered for the gaze.
     */
    boolean addNodeToListen(final Node node, final GazeDeviceManager gazeDeviceManager);

    /**
     * Stops listening to the node, and unregisters it for the gaze.
     */
    boolean removeNodeToListen(final Node node, final GazeDeviceManager gazeDeviceManager);
}
