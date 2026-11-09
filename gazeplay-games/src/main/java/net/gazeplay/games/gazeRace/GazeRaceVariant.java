package net.gazeplay.games.gazeRace;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The way the road of GazeRace goes on the screen: from left to right, or from the bottom to the top. The group of the
 * variants since the review of 9/11/2026; before, the only variant of the game (its name is kept so that the old
 * variants are still read).
 */
@Getter
@RequiredArgsConstructor
public enum GazeRaceVariant {

    HORIZONTAL("GazeRaceHorizontal"),
    VERTICAL("GazeRaceVertical");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the road of a key, horizontal for an unknown one
     */
    public static GazeRaceVariant of(final String key) {
        for (final GazeRaceVariant road : values()) {
            if (road.key.equals(key)) {
                return road;
            }
        }
        return HORIZONTAL;
    }
}
