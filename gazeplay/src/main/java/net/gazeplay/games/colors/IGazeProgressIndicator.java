package net.gazeplay.games.colors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

public interface IGazeProgressIndicator {

    public void setOnFinish(EventHandler<ActionEvent> handler);

    public void start();

    public void stop();

    public boolean addNodeToListen(final Node node);

    public boolean removeNodeToListen(final Node node);
}
