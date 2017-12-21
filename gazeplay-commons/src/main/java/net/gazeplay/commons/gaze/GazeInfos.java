package net.gazeplay.commons.gaze;

import javafx.scene.Node;

/**
 * Created by schwab on 24/08/2016.
 */
public class GazeInfos {

    private Node node;
    private long time;
    private boolean on;

    public GazeInfos(Node node) {
        this.node = node;
    }

    public GazeInfos(Node node, long time, boolean on) {

        this.node = node;
        this.time = time;
        this.on = on;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }
}
