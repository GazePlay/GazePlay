package net.gazeplay.games.simon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When "Répète après moi" ends (maintainer's choice of 10/11/2026): when the melody has 5, 8 or 12 notes, or never
 * (until the lives are lost, or a mistake with two players).
 */
@Getter
@RequiredArgsConstructor
public enum SimonEnd {
    NOTES_5("SimonNotes5", 5),
    NOTES_8("SimonNotes8", 8),
    NOTES_12("SimonNotes12", 12),
    ENDLESS("SimonEndless", 0);

    /**
     * Key of translation, and value of the variants.
     */
    private final String key;

    /**
     * The notes of the melody to reach, 0 without end.
     */
    private final int notes;

    /**
     * @return the end of a key, 8 notes for an unknown one
     */
    public static SimonEnd of(final String key) {
        for (final SimonEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return NOTES_8;
    }
}
