package net.gazeplay.games.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Ordre" (maintainer's choices of 10/11/2026): 3, 5 or 7 targets, or progressive (3 targets, one more
 * after each round won, up to {@link #MOST}).
 */
@Getter
@RequiredArgsConstructor
public enum OrderLevel {
    THREE("OrderTargets3", 3),
    FIVE("OrderTargets5", 5),
    SEVEN("OrderTargets7", 7),
    PROGRESSIVE("OrderProgressive", 3);

    /**
     * The most targets of the progressive level.
     */
    static final int MOST = 10;

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The targets of the first round.
     */
    private final int targets;

    /**
     * @return the level of a key, 5 targets for an unknown one
     */
    public static OrderLevel of(final String key) {
        for (final OrderLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return FIVE;
    }

    /**
     * @return the level of a number of targets of the former variants (3, 5 or 7)
     */
    public static OrderLevel ofTargets(final int targets) {
        return targets <= 3 ? THREE : targets <= 5 ? FIVE : SEVEN;
    }
}
