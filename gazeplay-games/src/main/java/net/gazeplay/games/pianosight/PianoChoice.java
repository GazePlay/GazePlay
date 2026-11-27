package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Who chooses the songs of the Piano (tester's feedback, point 39, maintainer's decision of 26/11/2026): the person, on
 * the screen "Choisis ta chanson" at the start and after each song, or the game, at random.
 */
@Getter
@RequiredArgsConstructor
public enum PianoChoice {
    CHOOSE("PianoChoose"),
    RANDOM("PianoRandom");

    /**
     * Key of translation, and value of a switch of the variants.
     */
    private final String key;

    /**
     * @return the choice of a key, the person's choice for an unknown one
     */
    public static PianoChoice of(final String key) {
        for (final PianoChoice choice : values()) {
            if (choice.key.equals(key)) {
                return choice;
            }
        }
        return CHOOSE;
    }
}
