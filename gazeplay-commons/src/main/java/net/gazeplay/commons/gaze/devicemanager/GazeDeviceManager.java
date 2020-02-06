package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import net.gazeplay.commons.gaze.GazeMotionListener;

import java.util.function.Supplier;

public interface GazeDeviceManager {

    void init(Supplier<Dimension2D> currentScreenDimensionSupplier);

    void destroy();

    void addGazeMotionListener(GazeMotionListener listener);

    void removeGazeMotionListener(GazeMotionListener listener);

    void addEventFilter(Node gs);

    void addEventHandler(Node gs);

    void removeEventFilter(Node gs);

    void removeEventHandler(Node gs);

    void clear();
}
