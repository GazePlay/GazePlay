package net.gazeplay.games.creampie;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The size of the targets of "Tartes à la crème" (maintainer's choice of 12/11/2026), their radius as a part of the
 * height of the screen (before the review: the setting of the size of the elements). In the order of the window of the
 * variants, from the easiest to the hardest (tester's feedback of 20/11/2026: large first).
 */
@Getter
@RequiredArgsConstructor
public enum CreamPieSize {
    LARGE("PieLarge", 0.18),
    MEDIUM("PieMedium", 0.13),
    SMALL("PieSmall", 0.09);

    /**
     * Key of translation, and value of the variants (the buttons).
     */
    private final String key;

    private final double radius;

    /**
     * @return the size of a key, the medium one for an unknown one
     */
    public static CreamPieSize of(final String key) {
        for (final CreamPieSize size : values()) {
            if (size.key.equals(key)) {
                return size;
            }
        }
        return MEDIUM;
    }
}
