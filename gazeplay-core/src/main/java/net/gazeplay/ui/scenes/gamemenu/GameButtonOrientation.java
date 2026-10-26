package net.gazeplay.ui.scenes.gamemenu;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

/**
 * Layout of the cards of the games in the menu: image beside the name (horizontal) or above it (vertical).
 */
@Slf4j
public enum GameButtonOrientation {
    HORIZONTAL, VERTICAL;

    public static GameButtonOrientation getDefault() {
        return HORIZONTAL;
    }

    /**
     * @return an orientation drawn at random
     */
    public static GameButtonOrientation random() {
        GameButtonOrientation[] values = values();
        int randomIndex = new ReplayablePseudoRandom().nextInt(values.length);
        return values[randomIndex];
    }

    /**
     * @return the orientation of the settings, the default one when missing or unknown
     */
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
