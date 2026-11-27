package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * What is played of a song of the Piano (tester's feedback, point 38, maintainer's decision of 26/11/2026): its start
 * only, then the whole song played again as a reward, or the whole song.
 */
@Getter
@RequiredArgsConstructor
public enum PianoLength {
    START("PianoStart"),
    WHOLE("PianoWhole");

    /**
     * Key of translation, and value of a switch of the variants.
     */
    private final String key;

    /**
     * @return the length of a key, the start for an unknown one
     */
    public static PianoLength of(final String key) {
        for (final PianoLength length : values()) {
            if (length.key.equals(key)) {
                return length;
            }
        }
        return START;
    }
}
