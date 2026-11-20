package net.gazeplay.games.scratchcard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The size of the scraper of "Cartes à gratter" (maintainer's choice of 12/11/2026), as a part of the height of the
 * screen (its radius). In the order of the window of the variants, from the easiest to the hardest (20/11/2026: large
 * first, less to scratch).
 */
@Getter
@RequiredArgsConstructor
public enum ScratchCardBrush {
    LARGE("ScratchBrushLarge", 0.1),
    MEDIUM("ScratchBrushMedium", 0.07),
    SMALL("ScratchBrushSmall", 0.045);

    /**
     * Key of translation, and value of the variants (the buttons).
     */
    private final String key;

    /**
     * The radius of the scraper, as a part of the height of the screen.
     */
    private final double radius;

    /**
     * @return the scraper of a key, the medium one for an unknown one
     */
    public static ScratchCardBrush of(final String key) {
        for (final ScratchCardBrush brush : values()) {
            if (brush.key.equals(key)) {
                return brush;
            }
        }
        return MEDIUM;
    }
}
