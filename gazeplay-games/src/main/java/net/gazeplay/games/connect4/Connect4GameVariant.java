package net.gazeplay.games.connect4;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The variants of "Puissance 4" before its review of 10/11/2026 (the player or the computer playing first), kept so
 * that the old variants (saved statistics, shortcuts) are still read: they are played against the easy computer, the
 * closest to the former one (it played at random), without end.
 */
@RequiredArgsConstructor
public enum Connect4GameVariant {
    PLAYERSTART("PlayerStart"),
    AISTART("AiStart");

    @Getter
    private final String label;
}
