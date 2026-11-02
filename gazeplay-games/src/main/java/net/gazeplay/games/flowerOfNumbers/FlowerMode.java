package net.gazeplay.games.flowerOfNumbers;

import lombok.Getter;

/**
 * The rules of Flower Of Numbers (maintainer's decision of 2/11/2026). In every mode, the values of all the petals
 * together must make the number of the heart, and each value can be put only once.
 */
public enum FlowerMode {
    /**
     * The game shares the number among the 5 petals (at random, at least 1 each) and shows the part of each petal:
     * each petal must make its part.
     */
    GUIDED("FlowerGuided", 5),
    /**
     * Only the total counts: some petals can stay empty.
     */
    FREE("FlowerFree", 1),
    /**
     * The total, with at least one value in each petal.
     */
    ALL_PETALS("FlowerAllPetals", 5);

    /**
     * Key of the translation of the name of the mode, and its name in the persistent form of the variants.
     */
    @Getter
    private final String key;

    /**
     * The smallest number of the heart in this mode.
     */
    @Getter
    private final int smallestNumber;

    FlowerMode(final String key, final int smallestNumber) {
        this.key = key;
        this.smallestNumber = smallestNumber;
    }

    /**
     * @return the mode of this key, guided if none
     */
    public static FlowerMode of(final String key) {
        for (final FlowerMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return GUIDED;
    }
}
