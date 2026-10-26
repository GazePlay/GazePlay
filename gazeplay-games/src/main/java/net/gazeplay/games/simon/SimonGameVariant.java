package net.gazeplay.games.simon;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "Simon", chosen in the menu.
 */
@RequiredArgsConstructor
public enum SimonGameVariant {

    EASY_CLASSIC("Easy"),
    NORMAL_CLASSIC("Normal"),
    HARD_CLASSIC("Hard"),
    MODE2("Simon copy you"),
    MODE3("Multiplayer");

    @Getter
    private final String label;
}
