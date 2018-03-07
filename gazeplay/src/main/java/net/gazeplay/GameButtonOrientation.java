package net.gazeplay;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;

import java.util.Random;

@Slf4j
public enum GameButtonOrientation {
    HORIZONTAL, VERTICAL;

    public static GameButtonOrientation getDefault() {
        return HORIZONTAL;
    }

    public static GameButtonOrientation random() {
        GameButtonOrientation[] values = values();
        int randomIndex = new Random().nextInt(values.length);
        return values[randomIndex];
    }

    public static GameButtonOrientation fromConfig(Configuration config) {
        String configValue = config.getMenuButtonsOrientation();
        if (configValue == null) {
            return getDefault();
        }
        try {
            return valueOf(configValue);
        } catch (IllegalArgumentException e) {
            log.warn("IllegalArgumentException : config value is invalid : {}", configValue, e);
            return getDefault();
        }
    }

}
