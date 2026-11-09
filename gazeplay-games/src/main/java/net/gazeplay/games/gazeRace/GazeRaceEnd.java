package net.gazeplay.games.gazeRace;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When GazeRace ends (maintainer's choice of 9/11/2026): when the three lives are lost, or after 1 or 3 minutes if they
 * are not.
 */
@Getter
@RequiredArgsConstructor
public enum GazeRaceEnd {
    ENDLESS("GazeRaceEndless", 0),
    TIME_1("GazeRaceTime1", 60),
    TIME_3("GazeRaceTime3", 180);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The time of the game, in seconds, 0 when it has no limit of time.
     */
    private final int seconds;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static GazeRaceEnd of(final String key) {
        for (final GazeRaceEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
