package net.gazeplay.games.ticTacToe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Who plays against the player in the Morpion (maintainer's choices of 10/11/2026): the computer at 3 levels, or a
 * second player on the same screen.
 */
@Getter
@RequiredArgsConstructor
public enum TicTacToeOpponent {
    EASY("TicTacToeEasy"),
    NORMAL("TicTacToeNormal"),
    HARD("TicTacToeHard"),
    DUO("TicTacToeDuo");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    boolean isComputer() {
        return this != DUO;
    }

    /**
     * @return the opponent of a key, the medium computer for an unknown one
     */
    public static TicTacToeOpponent of(final String key) {
        for (final TicTacToeOpponent opponent : values()) {
            if (opponent.key.equals(key)) {
                return opponent;
            }
        }
        return NORMAL;
    }
}
