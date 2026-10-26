package net.gazeplay.games.TowerDefense;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "TowerDefense", chosen in the menu.
 */
@RequiredArgsConstructor
public enum TowerDefenseVariant {
    MAP_ONE("Map1"),
    MAP_TWO("Map2");
    @Getter
    private final String label;
}

