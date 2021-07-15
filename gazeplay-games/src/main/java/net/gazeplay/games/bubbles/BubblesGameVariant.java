package net.gazeplay.games.bubbles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum BubblesGameVariant {
    TOP(Bubble.DIRECTION_TOP),
    BOTTOM(Bubble.DIRECTION_BOTTOM),
    RIGHT(Bubble.DIRECTION_RIGHT),
    LEFT(Bubble.DIRECTION_LEFT),
    TOP_FIX(Bubble.DIRECTION_TOP_FIX),
    BOTTOM_FIX(Bubble.DIRECTION_BOTTOM_FIX),
    RIGHT_FIX(Bubble.DIRECTION_RIGHT_FIX),
    LEFT_FIX(Bubble.DIRECTION_LEFT_FIX);

    @Getter
    private final String direction;
}
