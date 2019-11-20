package net.gazeplay.commons.configuration;

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
        return configuration.getAnimationSpeedRatioProperty().getValue();
    }

}
