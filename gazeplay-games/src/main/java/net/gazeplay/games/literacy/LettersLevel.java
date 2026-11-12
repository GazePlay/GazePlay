package net.gazeplay.games.literacy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The levels of "Identifier les lettres" (maintainer's choice of 12/11/2026): the size of the grid, how many other
 * letters, and whether they look like the letter asked. About a third of the cards show the letter asked.
 * <ul>
 * <li>Easy: 6 cards (2 x 3), the letter asked and one letter which does not look like it;</li>
 * <li>Normal: 12 cards (3 x 4), 3 other letters;</li>
 * <li>Hard: 20 cards (4 x 5), 3 other letters, those which look like the letter asked first (b, d, p, q...).</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum LettersLevel {
    EASY("Easy", 2, 3, 1),
    NORMAL("Normal", 3, 4, 3),
    HARD("Hard", 4, 5, 3);

    /**
     * Key of translation, and value (the button) of the variants.
     */
    private final String key;
    private final int lines;
    private final int columns;
    /**
     * The other letters on the cards.
     */
    private final int others;

    public int getCards() {
        return lines * columns;
    }

    /**
     * @return the cards showing the letter asked: a third of them, rounded
     */
    public int getTargets() {
        return Math.round(getCards() / 3f);
    }

    /**
     * @return the level of a key, easy for an unknown one
     */
    public static LettersLevel of(final String key) {
        for (final LettersLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return EASY;
    }
}
