package net.gazeplay.games.TowerDefense;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Tower Defense" is won (maintainer's rules of 3/11/2026): never, or when 10 or 20 waves are over with lives
 * left ("Bravo" and the statistics). The lives lost: "Lost!" and the statistics.
 */
@Getter
@RequiredArgsConstructor
public enum TowerDefenseEnd {
    ENDLESS("TowerDefenseEndless", 0),
    WAVES_10("TowerDefenseWaves10", 10),
    WAVES_20("TowerDefenseWaves20", 20);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The waves to hold, 0 without end.
     */
    private final int waves;

    /**
     * @return the end of a key, never for an unknown one
     */
    public static TowerDefenseEnd of(final String key) {
        for (final TowerDefenseEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
