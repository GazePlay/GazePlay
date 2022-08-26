package net.gazeplay.games.cakes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CakeGameVariant {
    ONELAYER("OneLayer"),
    THREELAYERS("ThreeLayers"),
    THREELAYERSHIDEN("ThreeLayersHiden");

    @Getter
    private final String label;
}
