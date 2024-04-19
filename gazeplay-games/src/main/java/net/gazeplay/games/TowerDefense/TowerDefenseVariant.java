package net.gazeplay.games.TowerDefense;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TowerDefenseVariant {
    MAP_ONE("Carte 1"),
    MAP_TWO("Carte 2");
    @Getter
    private final String label;
}

