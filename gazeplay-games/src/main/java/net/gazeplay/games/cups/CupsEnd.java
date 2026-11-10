package net.gazeplay.games.cups;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Gobelets Pass Pass" ends (maintainer's choice of 10/11/2026): after 3, 5 or 10 balls found, or never (until the
 * lives are lost).
 */
@Getter
@RequiredArgsConstructor
public enum CupsEnd {
    FOUND_3("CupsFound3", 3),
    FOUND_5("CupsFound5", 5),
    FOUND_10("CupsFound10", 10),
    ENDLESS("CupsEndless", 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The balls to find, 0 without end.
     */
    private final int found;

    /**
     * @return the end of a key, 5 balls for an unknown one
     */
    public static CupsEnd of(final String key) {
        for (final CupsEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return FOUND_5;
    }
}
