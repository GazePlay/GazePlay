package net.gazeplay.commons.gaze.devicemanager;

import javafx.scene.Node;
import lombok.Getter;
import lombok.Setter;

/**
 * State of a registered node for the dispatch: whether the gaze, and the mouse, are on it, and since when ({@code
 * time}, -1 after they left it).
 * <p>
 * Created by schwab on 24/08/2016.
 */
class GazeInfos {

    @Getter
    private final Node node;

    @Getter
    @Setter
    private long time;

    @Getter
    @Setter
    private boolean onGaze;

    @Getter
    @Setter
    private boolean onMouse;

    GazeInfos(Node node) {
        this(node, 0, false);
    }

    private GazeInfos(Node node, long time, boolean on) {
        this.node = node;
        this.time = time;
        this.onGaze = on;
    }

    public String toString() {
        return "At " + time + " on " + onGaze + "Node " + node;
    }

}
