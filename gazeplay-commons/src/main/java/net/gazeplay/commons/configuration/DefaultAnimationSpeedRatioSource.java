package net.gazeplay.commons.configuration;

import javafx.beans.property.DoubleProperty;
import lombok.Setter;

/**
 * The animation speed of a configuration, set with {@link #setConfiguration(Configuration)} before use.
 */
public class DefaultAnimationSpeedRatioSource implements AnimationSpeedRatioSource {

    @Setter
    private Configuration configuration;

    @Override
    public double getDurationRatio() {
        return 1d / getSpeedRatioProperty().getValue();
    }

    @Override
    public DoubleProperty getSpeedRatioProperty() {
        return configuration.getAnimationSpeedRatioProperty();
    }

}
