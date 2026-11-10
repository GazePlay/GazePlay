package net.gazeplay.games.connect4;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Who plays against the player in "Quatre en ligne" (maintainer's choices of 10/11/2026): the computer at 3 levels, or
 * a second player on the same screen.
 */
@Getter
@RequiredArgsConstructor
public enum Connect4Opponent {
    EASY("Connect4Easy"),
    NORMAL("Connect4Normal"),
    HARD("Connect4Hard"),
    DUO("Connect4Duo");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    boolean isComputer() {
        return this != DUO;
    }

    /**
     * @return the opponent of a key, the normal computer for an unknown one
     */
    public static Connect4Opponent of(final String key) {
        for (final Connect4Opponent opponent : values()) {
            if (opponent.key.equals(key)) {
                return opponent;
            }
        }
        return NORMAL;
    }
}
