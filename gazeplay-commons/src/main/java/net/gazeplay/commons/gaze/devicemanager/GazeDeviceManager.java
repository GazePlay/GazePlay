package net.gazeplay.commons.gaze.devicemanager;

import javafx.scene.Node;
import net.gazeplay.commons.utils.stats.Stats;

public interface GazeDeviceManager {

    void init();

    void destroy();

    void addStats(Stats newStats);

    void addEventFilter(Node gs);

    void addEventHandler(Node gs);

    void removeEventFilter(Node gs);

    void removeEventHandler(Node gs);

    void clear();
}
