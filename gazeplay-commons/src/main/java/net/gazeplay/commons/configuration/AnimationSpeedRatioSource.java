package net.gazeplay.commons.configuration;

import javafx.beans.property.DoubleProperty;

/**
 * Gives the animation speed chosen in the settings (ANIMATION_SPEED_RATIO): 1 is the normal speed, 2 twice as fast.
 */
public interface AnimationSpeedRatioSource {

    /**
     * Current ratio of the durations (inverse of the speed ratio), read by the games that compute their movements in
     * their own loop (e.g. Biboule Jump, Space Game). To make a JavaFX animation follow the speed, bind its rate to
     * {@link #getSpeedRatioProperty()} instead.
     */
    double getDurationRatio();

    /**
     * @return the speed ratio, as a property that changes when the user changes the speed during a game
     */
    DoubleProperty getSpeedRatioProperty();

}
