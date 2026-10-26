package net.gazeplay.games.gazeRace;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "GazeRace", chosen in the menu.
 */
@RequiredArgsConstructor
public enum GazeRaceVariant {

    HORIZONTAL("Play horizontally"),
    VERTICAL("Play vertically");
    @Getter
    private final String label;
}
