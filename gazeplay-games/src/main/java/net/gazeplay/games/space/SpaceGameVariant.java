package net.gazeplay.games.space;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "SpaceGame", chosen in the menu.
 */
@RequiredArgsConstructor
public enum SpaceGameVariant {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard");
    @Getter
    private final String label;
}
