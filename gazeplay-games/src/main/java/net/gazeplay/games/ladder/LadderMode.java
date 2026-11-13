package net.gazeplay.games.ladder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How "Échelle" is played (maintainer's choices of 13/11/2026): choose a start and follow the Biboule, or find the start
 * that leads to the picture asked.
 */
@Getter
@RequiredArgsConstructor
public enum LadderMode {
    WATCH("LadderWatch"),
    AIM("LadderAim");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, watching for an unknown one (the game had no variant before its review)
     */
    public static LadderMode of(final String key) {
        for (final LadderMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return WATCH;
    }
}
