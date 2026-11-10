package net.gazeplay.games.ticTacToe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The variants of the Morpion before its review of 10/11/2026, kept so that the old variants (saved statistics,
 * shortcuts) are still read: two players are played with two players, against the computer against the easy computer
 * (the closest to the former one, which played at random), in 1 game (the former game ended at the first win of the
 * player).
 */
@RequiredArgsConstructor
public enum TicTacToeGameVariant {
    P2("TwoPlayers"),
    IA("VsComputer");

    @Getter
    private final String label;
}
