package net.gazeplay.games.ticTacToe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the Morpion ends (maintainer's choice of 10/11/2026): after 1, 3 or 5 games, or never.
 */
@Getter
@RequiredArgsConstructor
public enum TicTacToeEnd {
    GAMES_1("TicTacToeGames1", 1),
    GAMES_3("TicTacToeGames3", 3),
    GAMES_5("TicTacToeGames5", 5),
    ENDLESS("TicTacToeEndless", 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The games of the match, 0 without end.
     */
    private final int games;

    /**
     * @return the end of a key, 3 games for an unknown one
     */
    public static TicTacToeEnd of(final String key) {
        for (final TicTacToeEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return GAMES_3;
    }
}
