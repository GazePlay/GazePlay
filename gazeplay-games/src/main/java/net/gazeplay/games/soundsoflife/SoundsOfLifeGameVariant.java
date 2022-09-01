package net.gazeplay.games.soundsoflife;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SoundsOfLifeGameVariant {
    FARM("Farm"),
    JUNGLE("Jungle"),
    SAVANNA("Savanna"),
    SEA("Sea");

    @Getter
    private final String Label;
}
