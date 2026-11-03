package net.gazeplay.games.surviveAgainstRobots;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Survive Against Robots" ends, besides the three lives lost (maintainer's rules of 3/11/2026): never, after 1 or
 * 3 minutes, or after 20 or 50 robots destroyed. Then "Bravo" and the statistics.
 */
@Getter
@RequiredArgsConstructor
public enum SurviveEnd {
    ENDLESS("SurviveEndless", 0, 0),
    TIME_1("SurviveTime1", 0, 60),
    TIME_3("SurviveTime3", 0, 180),
    ROBOTS_20("SurviveRobots20", 20, 0),
    ROBOTS_50("SurviveRobots50", 50, 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The robots to destroy, 0 when the end is not a number of robots.
     */
    private final int robots;

    /**
     * The time of the game, in seconds, 0 when the end is not a time.
     */
    private final int seconds;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static SurviveEnd of(final String key) {
        for (final SurviveEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
