package net.gazeplay.games.rushhour;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RushHourEmmanuelGameVariant {
    Level1("level1"),
    Level2("level2"),
    Level3("level3");

    @Getter
    private final String label;
}
