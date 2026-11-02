package net.gazeplay.games.bubbles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How a bubble of "Bubbles" bursts (maintainer's choice of 2/11/2026: both, chosen in the variants): as soon as the
 * gaze enters it, or after a fixation (a ring goes round it meanwhile).
 */
@Getter
@RequiredArgsConstructor
public enum BubblesBurst {
    AT_ONCE("BubblesBurstAtOnce"),
    FIXATION("BubblesBurstFixation");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the way of a key, at once for an unknown one
     */
    public static BubblesBurst of(final String key) {
        for (final BubblesBurst burst : values()) {
            if (burst.key.equals(key)) {
                return burst;
            }
        }
        return AT_ONCE;
    }
}
