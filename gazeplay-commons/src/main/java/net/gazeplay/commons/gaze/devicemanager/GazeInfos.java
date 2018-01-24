package net.gazeplay.commons.gaze.devicemanager;

import javafx.scene.Node;
import lombok.Getter;
import lombok.Setter;

/**
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
    private boolean on;

    GazeInfos(Node node) {
        this(node, 0, false);
    }

    private GazeInfos(Node node, long time, boolean on) {
        this.node = node;
        this.time = time;
        this.on = on;
    }

}
