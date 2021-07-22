package net.gazeplay.games.ninja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NinjaGameVariant {
    RANDOM("Random"),
    VERTICAL("Vertical"),
    HORIZONTAL("Horizontal"),
    DIAGONAL_UPPER_LEFT_TO_LOWER_RIGHT("Diagonal from upper left to lower right"),
    DIAGONAL_UPPER_RIGHT_TO_LOWER_LEFT("Diagonal from upper right to lower left"),

    DYNAMIC_RANDOM("Random Dynamic"),
    DYNAMIC_VERTICAL("Vertical Dynamic"),
    DYNAMIC_HORIZONTAL("Horizontal Dynamic"),
    DYNAMIC_DIAGONAL_UPPER_LEFT_TO_LOWER_RIGHT("Diagonal from upper left to lower right Dynamic"),
    DYNAMIC_DIAGONAL_UPPER_RIGHT_TO_LOWER_LEFT("Diagonal from upper right to lower left Dynamic");
    @Getter
    private final String label;
}
