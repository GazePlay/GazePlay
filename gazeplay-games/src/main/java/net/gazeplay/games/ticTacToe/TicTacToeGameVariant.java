package net.gazeplay.games.ticTacToe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A variant of the game "TicTacToe", chosen in the menu.
 */
@RequiredArgsConstructor
public enum TicTacToeGameVariant {
    P2("TwoPlayers"),
    IA("VsComputer");

    @Getter
    private final String label;
}
