package net.gazeplay.games.cakes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "Cakes", chosen in the menu.
 */
@RequiredArgsConstructor
public enum CakeGameVariant {
    ONELAYER("Easy"),
    THREELAYERS("Normal"),
    THREELAYERSHIDEN("Hard");

    @Getter
    private final String label;
}
