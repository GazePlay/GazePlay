package net.gazeplay.games.race;

import lombok.Getter;

/**
 * Variants of the game "FrogsRace": how fast the other frogs are (maintainer's request of 30/10/2026: a race the
 * player can win, easy by default). Each other frog takes a random time between the two bounds to reach the finish;
 * the player needs {@link Race#HOPS_TO_FINISH} bugs. The bounds are a first guess, to be adjusted after trying the game
 * with the eye tracker.
 */
public enum RaceVariant {
    EASY("Easy", 90, 110),
    NORMAL("Normal", 60, 75),
    HARD("Hard", 40, 50);

    /**
     * Key of the translation of the name of the variant.
     */
    @Getter
    private final String label;

    /**
     * Shortest and longest time of another frog to reach the finish, in seconds.
     */
    @Getter
    private final int minRaceSeconds;

    @Getter
    private final int maxRaceSeconds;

    RaceVariant(final String label, final int minRaceSeconds, final int maxRaceSeconds) {
        this.label = label;
        this.minRaceSeconds = minRaceSeconds;
        this.maxRaceSeconds = maxRaceSeconds;
    }
}
