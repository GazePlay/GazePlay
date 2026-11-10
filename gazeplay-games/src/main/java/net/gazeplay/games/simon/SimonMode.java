package net.gazeplay.games.simon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The modes of "Répète après moi" (maintainer's choices of 10/11/2026):
 * <ul>
 * <li>classic: the device plays a melody, one note longer at each turn, and the player repeats it; 3 lives;</li>
 * <li>two players: each one in turn repeats the whole melody, then adds a note of their choice; a mistake loses;</li>
 * <li>free: the player adds a note of their choice, the device plays the whole melody again; no mistake possible.</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum SimonMode {
    CLASSIC("SimonClassic"),
    DUO("SimonDuo"),
    FREE("SimonFree");

    /**
     * Key of translation, and sub-group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, classic for an unknown one
     */
    public static SimonMode of(final String key) {
        for (final SimonMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return CLASSIC;
    }
}
