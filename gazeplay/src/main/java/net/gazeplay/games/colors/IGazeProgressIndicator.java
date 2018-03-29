package net.gazeplay.games.colors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 *
 * @author medard
 */
public interface IGazeProgressIndicator {

    public void setOnFinish(EventHandler<ActionEvent> handler);

    public void start();

    public void stop();
}
