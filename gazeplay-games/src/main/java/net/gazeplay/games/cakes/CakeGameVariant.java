package net.gazeplay.games.cakes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CakeGameVariant {
    ONELAYER("easy"),
    THREELAYERS("normal"),
    THREELAYERSHIDEN("extreme");

    @Getter
    private final String label;
}
