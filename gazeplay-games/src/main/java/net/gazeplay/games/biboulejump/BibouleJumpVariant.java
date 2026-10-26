package net.gazeplay.games.biboulejump;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "BibJump", chosen in the menu.
 */
@RequiredArgsConstructor
public enum BibouleJumpVariant {
    MOVING("With moving platforms"),
    NOT_MOVING("Without moving platforms");

    @Getter
    private final String label;

}
