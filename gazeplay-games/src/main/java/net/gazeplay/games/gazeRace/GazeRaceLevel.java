package net.gazeplay.games.gazeRace;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of GazeRace (maintainer's choice of 9/11/2026): how many cars come each second, and how fast the road goes at
 * the start and at most. The normal level is the game of before the review (2 or 3 cars a second; a 1920 pixels wide road
 * crossed in 6.4 s at the start, 2.1 s at most). The road reaches its top speed after {@link GazeRace#SECONDS_TO_TOP}
 * seconds without a hit, and goes back to its starting speed after each one.
 */
@Getter
@RequiredArgsConstructor
public enum GazeRaceLevel {
    EASY("GazeRaceEasy", 1, 2, 1 / 8.0, 1 / 4.0),
    NORMAL("GazeRaceNormal", 2, 3, 1 / 6.4, 1 / 2.13),
    HARD("GazeRaceHard", 3, 4, 1 / 4.5, 1 / 1.6);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The cars that come each second: between these two numbers.
     */
    private final int fewestCars;
    private final int mostCars;

    /**
     * The speed of the road, in lengths of the road per second: at the start, and at most.
     */
    private final double startSpeed;
    private final double topSpeed;

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
