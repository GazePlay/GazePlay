package net.gazeplay.games.ninja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Ninja" ends (maintainer's choice of 4/11/2026): never, or after 20 or 50 targets cut; then "Bravo" and the
 * statistics. There is no life and no penalty: a target missed just goes on.
 */
@Getter
@RequiredArgsConstructor
public enum NinjaEnd {
    ENDLESS("NinjaEndless", 0),
    CUT_20("NinjaEnd20", 20),
    CUT_50("NinjaEnd50", 50);

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
