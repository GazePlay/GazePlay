package net.gazeplay.commons.configuration;

import javafx.beans.property.DoubleProperty;

public interface AnimationSpeedRatioSource {

    double getDurationRatio();

    double getSpeedRatio();

    DoubleProperty getSpeedRatioProperty();

}
