package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "Piano", chosen in the menu.
 */
@RequiredArgsConstructor
public enum PianoGameVariant {
    FREEPLAY("Freeplay"),
    NORMAL("PlayWithInstructions");
    @Getter
    private final String label;
}
