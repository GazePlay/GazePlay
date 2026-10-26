package net.gazeplay.games.soundsoflife;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "SoundsOfLife", chosen in the menu.
 */
@RequiredArgsConstructor
public enum SoundsOfLifeGameVariant {
    FARM("Farm"),
    JUNGLE("Jungle"),
    SAVANNA("Savanna"),
    SEA("Sea");

    @Getter
    private final String Label;
}
