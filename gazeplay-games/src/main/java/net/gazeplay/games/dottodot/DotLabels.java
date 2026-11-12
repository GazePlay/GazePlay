package net.gazeplay.games.dottodot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What the dots of "Point par point" show (maintainer's choice of 12/11/2026): numbers (1, 2, 3...) or letters (A, B,
 * C...), joined in that order.
 */
@Getter
@RequiredArgsConstructor
public enum DotLabels {
    NUMBERS("DotNumbers"),
    LETTERS("DotLetters");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the label of the dot at this place in the order, from 0
     */
    public String label(final int index) {
        return this == LETTERS ? String.valueOf((char) ('A' + index)) : Integer.toString(index + 1);
    }

    /**
     * @return the labels of a key, the numbers for an unknown one
     */
    public static DotLabels of(final String key) {
        for (final DotLabels labels : values()) {
            if (labels.key.equals(key)) {
                return labels;
            }
        }
        return NUMBERS;
    }
}
