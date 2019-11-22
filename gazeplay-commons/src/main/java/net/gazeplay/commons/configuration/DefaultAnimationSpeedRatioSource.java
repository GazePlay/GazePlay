package net.gazeplay.commons.configuration;

import javafx.beans.property.DoubleProperty;
import lombok.Setter;

public class DefaultAnimationSpeedRatioSource implements AnimationSpeedRatioSource {

    @Setter
    private Configuration configuration;

    @Override
    public double getDurationRatio() {
        return 1d / getSpeedRatio();
    }

    @Override
    public double getSpeedRatio() {
        return getSpeedRatioProperty().getValue();
    }

    @Override
    public DoubleProperty getSpeedRatioProperty() {
        return configuration.getAnimationSpeedRatioProperty();
    }

}
