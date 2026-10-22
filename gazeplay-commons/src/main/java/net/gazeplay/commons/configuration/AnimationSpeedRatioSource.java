package net.gazeplay.commons.configuration;

import javafx.beans.property.DoubleProperty;

public interface AnimationSpeedRatioSource {

    /**
     * Current ratio of the durations (inverse of the speed ratio), read by the games that compute their movements in
     * their own loop (e.g. Biboule Jump, Space Game). To make a JavaFX animation follow the speed, bind its rate to
     * {@link #getSpeedRatioProperty()} instead.
     */
    double getDurationRatio();

    DoubleProperty getSpeedRatioProperty();

}
