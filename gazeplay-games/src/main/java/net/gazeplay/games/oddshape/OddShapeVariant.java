package net.gazeplay.games.oddshape;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OddShapeVariant {
    NORMAL("Normal"),
    HARDER("Harder");

    @Getter
    private final String label;
}
