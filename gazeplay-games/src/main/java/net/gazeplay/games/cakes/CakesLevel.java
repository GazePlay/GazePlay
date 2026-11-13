package net.gazeplay.games.cakes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "La Fabrique à gâteaux" (maintainer's choices of 12/11/2026): the layers of the model, and whether it
 * hides after a while; the free mode has a single value, up to three layers.
 */
@Getter
@RequiredArgsConstructor
public enum CakesLevel {
    EASY("CakesEasy", 1, false),
    NORMAL("CakesNormal", 3, false),
    HARD("CakesHard", 3, true),
    FREE("CakesFreeLayers", 3, false);

    /**
     * Key of translation, and value of the variants (the buttons).
     */
    private final String key;

    /**
     * The layers of the model, or the most layers in the free mode.
     */
    private final int layers;

    /**
     * Whether the model hides after a while (it is shown again by a button).
     */
    private final boolean hidden;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static CakesLevel of(final String key) {
        for (final CakesLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
