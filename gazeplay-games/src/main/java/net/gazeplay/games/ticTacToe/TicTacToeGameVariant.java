package net.gazeplay.games.ticTacToe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TicTacToeGameVariant {
    P2("P2"),
    IA("IA");

    @Getter
    private final String label;
}
