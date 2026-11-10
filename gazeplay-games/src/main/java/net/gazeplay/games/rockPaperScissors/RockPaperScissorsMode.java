package net.gazeplay.games.rockPaperScissors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The modes of "Pierre papier ciseaux" (maintainer's choices of 10/11/2026): against the computer (its sign hidden
 * until both are shown), or learning the rule (its sign shown: choose the one that beats it).
 */
@Getter
@RequiredArgsConstructor
public enum RockPaperScissorsMode {
    VERSUS("RpsVersus"),
    LEARN("RpsLearn");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, against the computer for an unknown one
     */
    public static RockPaperScissorsMode of(final String key) {
        for (final RockPaperScissorsMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return VERSUS;
    }
}
