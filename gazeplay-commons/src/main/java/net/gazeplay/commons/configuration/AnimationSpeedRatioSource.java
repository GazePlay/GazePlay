package net.gazeplay.commons.configuration;

import javafx.beans.property.DoubleProperty;

public interface AnimationSpeedRatioSource {

    /**
     * use getSpeedRatioProperty() instead to bind to animation rate
     */
    @Deprecated
    double getDurationRatio();

    DoubleProperty getSpeedRatioProperty();

}
