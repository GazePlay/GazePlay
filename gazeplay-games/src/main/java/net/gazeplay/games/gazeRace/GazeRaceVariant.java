package net.gazeplay.games.gazeRace;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GazeRaceVariant {

    HORIZONTAL("Play horizontally"),
    VERTICAL("Play vertically");
    @Getter
    private final String label;
}
