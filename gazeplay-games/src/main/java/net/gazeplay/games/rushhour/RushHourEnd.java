package net.gazeplay.games.rushhour;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Embouteillage" ends (maintainer's choice of 9/11/2026): after 1, 3 or 5 parkings left by the red car, or never.
 */
@Getter
@RequiredArgsConstructor
public enum RushHourEnd {
    SOLVED_1("RushHourSolved1", 1),
    SOLVED_3("RushHourSolved3", 3),
    SOLVED_5("RushHourSolved5", 5),
    ENDLESS("RushHourEndless", 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The parkings to solve, 0 for a game without end.
     */
    private final int parkings;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static RushHourEnd of(final String key) {
        for (final RushHourEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
