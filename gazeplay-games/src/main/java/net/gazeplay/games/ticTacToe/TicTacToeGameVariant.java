package net.gazeplay.games.ticTacToe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TicTacToeGameVariant {
    P2("TwoPlayers"),
    IA("VsComputer");

    @Getter
    private final String label;
}
