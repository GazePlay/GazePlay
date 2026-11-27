package net.gazeplay.games.gazeRace;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of GazeRace (maintainer's choice of 9/11/2026): how many cars come each second, and how fast the road goes at
 * the start and at most. The normal level is the game of before the review (2 or 3 cars a second; a 1920 pixels wide road
 * crossed in 6.4 s at the start, 2.1 s at most). The road reaches its top speed after {@link GazeRace#SECONDS_TO_TOP}
 * seconds without a hit, and goes back to its starting speed after each one.
 * <p>
 * Tester's feedback of 23/11/2026, point 54: a very easy level to learn to drive with the gaze before avoiding: a car
 * every 2.5 seconds, all overtaking the player (none coming the other way), slowly, and no barrier. The easiest first.
 */
@Getter
@RequiredArgsConstructor
public enum GazeRaceLevel {
    VERY_EASY("GazeRaceVeryEasy", 1, 1, 1 / 10.0, 1 / 6.0, 2.5, true, false),
    EASY("GazeRaceEasy", 1, 2, 1 / 8.0, 1 / 4.0, 1, false, true),
    NORMAL("GazeRaceNormal", 2, 3, 1 / 6.4, 1 / 2.13, 1, false, true),
    HARD("GazeRaceHard", 3, 4, 1 / 4.5, 1 / 1.6, 1, false, true);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The cars that come at each arrival (each second, see {@link #carInterval}): between these two numbers.
     */
    private final int fewestCars;
    private final int mostCars;

    /**
     * The speed of the road, in lengths of the road per second: at the start, and at most.
     */
    private final double startSpeed;
    private final double topSpeed;

    /**
     * The seconds between two arrivals of cars.
     */
    private final double carInterval;

    /**
     * Whether all the cars overtake the player (none comes the other way).
     */
    private final boolean oneWay;

    /**
     * Whether barriers of works come on the line in the middle.
     */
    private final boolean barriers;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static GazeRaceLevel of(final String key) {
        for (final GazeRaceLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
