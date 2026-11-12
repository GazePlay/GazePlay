package net.gazeplay.games.literacy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the letters of "Identifier les lettres" are written (maintainer's choice of 12/11/2026): capitals, small letters
 * (in the font Andika, made for beginning readers: an "a" and a "g" as at school), or both (the "A" asked is found as
 * "A" and as "a").
 */
@Getter
@RequiredArgsConstructor
public enum LettersCase {
    CAPITALS("LettersCapitals"),
    SMALL("LettersSmall"),
    BOTH("LettersBoth");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, capitals for an unknown one
     */
    public static LettersCase of(final String key) {
        for (final LettersCase letterCase : values()) {
            if (letterCase.key.equals(key)) {
                return letterCase;
            }
        }
        return CAPITALS;
    }
}
