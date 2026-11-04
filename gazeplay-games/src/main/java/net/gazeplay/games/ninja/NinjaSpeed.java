package net.gazeplay.games.ninja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The speed of the fruit of "Ninja" (maintainer's choice of 4/11/2026): the seconds it takes to go through the height of
 * the screen, the same whatever the length of its way, at the normal animation speed. The adaptive speed starts at the
 * normal one and follows the player (see {@link Ninja}).
 */
@Getter
@RequiredArgsConstructor
public enum NinjaSpeed {
    SLOW("NinjaSlow", 5),
    NORMAL("NinjaNormal", 3),
    FAST("NinjaFast", 2),
    ADAPTIVE("NinjaAdaptive", 3);

    /**
     * Key of translation, and sub-group of the variants. The statistics screen looks for the key of the adaptive speed
     * to show its chart of the levels.
     */
    private final String key;

    /**
     * The seconds to go through the height of the screen (at the start, for the adaptive speed).
     */
    private final double crossingSeconds;

    /**
     * @return the speed of a key, normal for an unknown one
     */
    public static NinjaSpeed of(final String key) {
        for (final NinjaSpeed speed : values()) {
            if (speed.key.equals(key)) {
                return speed;
            }
        }
        return NORMAL;
    }
}
