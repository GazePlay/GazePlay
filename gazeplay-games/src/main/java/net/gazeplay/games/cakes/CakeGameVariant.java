package net.gazeplay.games.cakes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CakeGameVariant {
    ONELAYER("Easy"),
    THREELAYERS("Normal"),
    THREELAYERSHIDEN("Hard");

    @Getter
    private final String label;
}
