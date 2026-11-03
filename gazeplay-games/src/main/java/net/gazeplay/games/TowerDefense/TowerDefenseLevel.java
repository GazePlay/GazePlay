package net.gazeplay.games.TowerDefense;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The level of "Tower Defense" (maintainer's rules of 3/11/2026): the money and the lives at the start, and how much
 * stronger the enemies get every two waves.
 */
@Getter
@RequiredArgsConstructor
public enum TowerDefenseLevel {
    EASY("TowerDefenseEasy", 100, 15, 0.15),
    NORMAL("TowerDefenseNormal", 50, 10, 0.25),
    HARD("TowerDefenseHard", 40, 5, 0.40);

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * The money at the start.
     */
    private final int money;

    /**
     * The lives at the start (an enemy reaching the castle costs one).
     */
    private final int lives;

    /**
     * The life the enemies gain every two waves (0.25: 25 % more).
     */
    private final double healthGrowth;

    /**
     * @return the level of a key, normal for an unknown one
     */
    public static TowerDefenseLevel of(final String key) {
        for (final TowerDefenseLevel level : values()) {
            if (level.key.equals(key)) {
                return level;
            }
        }
        return NORMAL;
    }
}
