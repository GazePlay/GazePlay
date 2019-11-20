package net.gazeplay.commons.configuration;

import lombok.Setter;

public class DefaultAnimationSpeedRatioSource implements AnimationSpeedRatioSource {

    @Setter
    private Configuration configuration;

    @Override
    public double getSpeedEffects() {
        return 1d / configuration.getSpeedEffectsProperty().getValue();
    }

}
