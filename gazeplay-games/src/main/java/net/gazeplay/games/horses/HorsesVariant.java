package net.gazeplay.games.horses;

import lombok.Getter;

/**
 * Variants of the game "Horses" (30/10/2026): one player against 1, 2 or 3 teams played by the computer, or 2, 3 or 4
 * players taking turns in front of the same screen; on the normal board (4 pawns per team) or on the simplified one
 * (1 pawn, a shorter track), chosen by the switches at the bottom of the window of the variants (the simplified board
 * used to be a game of its own, "Horses Simplified").
 */
public enum HorsesVariant {
    VS_COMPUTER_1("HorsesVsComputer1", 2, 1, false),
    VS_COMPUTER_2("HorsesVsComputer2", 3, 1, false),
    VS_COMPUTER_3("HorsesVsComputer3", 4, 1, false),
    PLAYERS_2("HorsesPlayers2", 2, 2, false),
    PLAYERS_3("HorsesPlayers3", 3, 3, false),
    PLAYERS_4("HorsesPlayers4", 4, 4, false),
    SIMPLIFIED_VS_COMPUTER_1("HorsesVsComputer1", 2, 1, true),
    SIMPLIFIED_VS_COMPUTER_2("HorsesVsComputer2", 3, 1, true),
    SIMPLIFIED_VS_COMPUTER_3("HorsesVsComputer3", 4, 1, true),
    SIMPLIFIED_PLAYERS_2("HorsesPlayers2", 2, 2, true),
    SIMPLIFIED_PLAYERS_3("HorsesPlayers3", 3, 3, true),
    SIMPLIFIED_PLAYERS_4("HorsesPlayers4", 4, 4, true);

    /**
     * Key of the translation of the name of the variant (the same on both boards).
     */
    @Getter
    private final String label;

    /**
     * Teams in the game, and how many of them are played by people (the others by the computer).
     */
    @Getter
    private final int teams;

    @Getter
    private final int humans;

    /**
     * Whether the game is played on the simplified board.
     */
    @Getter
    private final boolean simplified;

    HorsesVariant(final String label, final int teams, final int humans, final boolean simplified) {
        this.label = label;
        this.teams = teams;
        this.humans = humans;
        this.simplified = simplified;
    }
}
