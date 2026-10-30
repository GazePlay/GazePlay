package net.gazeplay.games.spotthedifferences;

import lombok.Getter;

/**
 * Variants of the game "SpotDifference" (maintainer's request of 30/10/2026): how many differences each picture has,
 * smaller ones in Hard. The differences of each level are made by gradle/images/make_spotdifferences_images.py and
 * listed in instances.json under the name of the value.
 */
public enum SpotTheDifferencesVariant {
    EASY("Easy"),
    NORMAL("Normal"),
    HARD("Hard");

    /**
     * Key of the translation of the name of the variant.
     */
    @Getter
    private final String label;

    SpotTheDifferencesVariant(final String label) {
        this.label = label;
    }
}
