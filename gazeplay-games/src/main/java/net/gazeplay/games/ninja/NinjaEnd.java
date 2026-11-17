package net.gazeplay.games.ninja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Ninja" ends (maintainer's choices of 4/11 and 17/11/2026): after 10, 20 or 30 targets cut, then "Bravo" and
 * the statistics, or never. There is no life and no penalty: a target missed just goes on. The order is that of the
 * window of the variants, "without end" always last (on the right).
 */
@Getter
@RequiredArgsConstructor
public enum NinjaEnd {
    CUT_10("NinjaEnd10", 10),
    CUT_20("NinjaEnd20", 20),
    CUT_30("NinjaEnd30", 30),
    ENDLESS("NinjaEndless", 0);

    /**
     * Key of translation, and second switch of the variants.
     */
    private final String key;

    /**
     * The targets to cut, 0 without end.
     */
    private final int targets;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static NinjaEnd of(final String key) {
        for (final NinjaEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
