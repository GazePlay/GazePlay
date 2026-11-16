package net.gazeplay.games.whereisit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The end of a game of the family "Where is it?" (maintainer's request of 16/11/2026, when the time and score limiters
 * of the settings were removed: they were its only end): after 5, 10 or 15 screens found, "Bravo" then the statistics,
 * or without end (leave by the menu). A screen is found when its right picture is chosen.
 */
@Getter
@RequiredArgsConstructor
public enum WhereIsItEnd {
    SCREENS_5("WhereIsItEnd5", 5),
    SCREENS_10("WhereIsItEnd10", 10),
    SCREENS_15("WhereIsItEnd15", 15),
    ENDLESS("WhereIsItEndless", 0);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The screens to find, 0 without end.
     */
    private final int screens;

    /**
     * @return the end of a key; without end for an unknown one (as the variants of before 16/11/2026)
     */
    public static WhereIsItEnd of(final String key) {
        for (final WhereIsItEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
