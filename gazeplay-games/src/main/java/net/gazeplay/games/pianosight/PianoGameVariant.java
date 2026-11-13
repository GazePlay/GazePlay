package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The variants of the Piano before its review (13/11/2026), kept to read the saved games and the shortcuts: "Jeu libre"
 * is the free mode, "Jouer en suivant les instructions" the songs without end (see {@link PianoGameLauncher}).
 */
@RequiredArgsConstructor
public enum PianoGameVariant {
    FREEPLAY("Freeplay"),
    NORMAL("PlayWithInstructions");
    @Getter
    private final String label;
}
