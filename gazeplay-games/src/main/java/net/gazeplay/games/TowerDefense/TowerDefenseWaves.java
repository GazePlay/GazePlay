package net.gazeplay.games.TowerDefense;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the waves of "Tower Defense" start (maintainer's rules of 3/11/2026): on demand, by a fixation on the button,
 * so that the player is never overwhelmed and plays at their own pace (as before); or by themselves,
 * {@value TowerDefense#AUTOMATIC_WAVE_DELAY} s after the end of the last one, the button still sending a wave sooner.
 */
@Getter
@RequiredArgsConstructor
public enum TowerDefenseWaves {
    ON_DEMAND("TowerDefenseWavesOnDemand", false),
    AUTOMATIC("TowerDefenseWavesAutomatic", true);

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * Whether a wave starts by itself after a delay.
     */
    private final boolean automatic;

    /**
     * @return the way of a key, on demand for an unknown one
     */
    public static TowerDefenseWaves of(final String key) {
        for (final TowerDefenseWaves waves : values()) {
            if (waves.key.equals(key)) {
                return waves;
            }
        }
        return ON_DEMAND;
    }
}
