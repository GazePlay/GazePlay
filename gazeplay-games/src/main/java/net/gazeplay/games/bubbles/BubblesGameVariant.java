package net.gazeplay.games.bubbles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum BubblesGameVariant {
    TOP(Bubble.DIRECTION_TOP),
    BOTTOM(Bubble.DIRECTION_BOTTOM),
    RIGHT(Bubble.DIRECTION_RIGHT),
    LEFT(Bubble.DIRECTION_LEFT),
    FIX(Bubble.FIX);

    @Getter
    private final String direction;
}
