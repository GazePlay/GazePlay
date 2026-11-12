package net.gazeplay.games.literacy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Identifier les lettres" ends (maintainer's choice of 12/11/2026): after 3, 5 or 10 pictures uncovered, or never.
 */
@Getter
@RequiredArgsConstructor
public enum LettersEnd {
    PICTURES_3("LettersEnd3", 3),
    PICTURES_5("LettersEnd5", 5),
    PICTURES_10("LettersEnd10", 10),
    ENDLESS("LettersEndless", 0);

    /**
     * Key of translation, and other switch of the variants.
     */
    private final String key;

    /**
     * The pictures to uncover, 0 without end.
     */
    private final int pictures;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static LettersEnd of(final String key) {
        for (final LettersEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
