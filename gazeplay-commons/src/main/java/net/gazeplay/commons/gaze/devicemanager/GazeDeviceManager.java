package net.gazeplay.commons.gaze.devicemanager;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import net.gazeplay.commons.gaze.GazeMotionListener;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.function.Supplier;

public interface GazeDeviceManager {

    void init(Supplier<Dimension2D> currentScreenDimensionSupplier, Supplier<Point2D> currentScreenPositionSupplier);

    void destroy();

    void addGazeMotionListener(GazeMotionListener listener);

    void removeGazeMotionListener(GazeMotionListener listener);

    void addEventFilter(Node gs);

    void addEventHandler(Node gs);

    void removeEventFilter(Node gs);

    void addStats(Stats stats);

    void removeEventHandler(Node gs);

    void clear();

    void onSavedMovementsUpdate(Point2D gazePositionOnScreen, String event);

    void setInReplayMode(boolean b);

    boolean isInReplayMode();
}
