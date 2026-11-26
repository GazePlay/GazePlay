package net.gazeplay.games.race;

import lombok.Getter;

/**
 * Variants of the game "FrogsRace": how fast the other frogs are (maintainer's request of 30/10/2026: a race the
 * player can win, easy by default). Each other frog takes a random time between the two bounds to reach the finish;
 * the player needs {@link Race#HOPS_TO_FINISH} bugs. Faster since 26/11/2026 (tester's feedback of 23/11/2026, point 55:
 * no rival even at the hard level; easy was 90 to 110 s, normal 60 to 75 s, hard 40 to 50 s). The bounds are still a
 * guess, to be adjusted after trying the game with the eye tracker.
 */
public enum RaceVariant {
    EASY("Easy", 60, 75),
    NORMAL("Normal", 35, 45),
    HARD("Hard", 20, 26);

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

    /**
     * @return the speed of a label, easy for an unknown one
     */
    public static RaceVariant of(final String label) {
        for (final RaceVariant variant : values()) {
            if (variant.label.equals(label)) {
                return variant;
            }
        }
        return EASY;
    }
}
