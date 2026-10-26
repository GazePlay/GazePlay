package net.gazeplay.games.connect4;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "Connect4", chosen in the menu.
 */
@RequiredArgsConstructor
public enum Connect4GameVariant {
    PLAYERSTART("PlayerStart"),
    AISTART("AiStart");
    @Getter
    private final String label;
}
