package net.gazeplay.games.connect4;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Quatre en ligne" ends (maintainer's choice of 10/11/2026): after 1, 3 or 5 games, or never.
 */
@Getter
@RequiredArgsConstructor
public enum Connect4End {
    GAMES_1("Connect4Games1", 1),
    GAMES_3("Connect4Games3", 3),
    GAMES_5("Connect4Games5", 5),
    ENDLESS("Connect4Endless", 0);

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
    public static Connect4End of(final String key) {
        for (final Connect4End end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return GAMES_3;
    }
}
