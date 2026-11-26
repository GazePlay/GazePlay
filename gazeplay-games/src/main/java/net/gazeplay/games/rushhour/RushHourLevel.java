package net.gazeplay.games.rushhour;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Embouteillage" (maintainer's choices of 9/11/2026): the fewest moves of its parkings (a move: one vehicle
 * slid by as many boxes as wanted), made by gradle/images/make_trafficjam_levels.py. Very easy since 26/11/2026 (tester's
 * feedback of 23/11/2026, point 63: the easy level was not): a single vehicle in the way, among 3 to 6, the first level,
 * chosen when the window of the variants opens.
 */
@Getter
@RequiredArgsConstructor
public enum RushHourLevel {
    VERY_EASY("RushHourVeryEasy", "veryeasy"),
    EASY("RushHourEasy", "easy"),
    NORMAL("RushHourNormal", "normal"),
    HARD("RushHourHard", "hard");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * Its name in the file of the parkings.
     */
    private final String fileName;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static RushHourLevel of(final String key) {
        for (final RushHourLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
