package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How the Piano is played (maintainer's choices of 13/11/2026): a song, the next key lit, or the keys freely.
 */
@Getter
@RequiredArgsConstructor
public enum PianoMode {
    SONG("PianoSongs"),
    FREE("PianoFree");

    /**
     * Key of translation, and group of the variants.
     */
    private final String key;

    /**
     * @return the mode of a key, the songs for an unknown one
     */
    public static PianoMode of(final String key) {
        for (final PianoMode mode : values()) {
            if (mode.key.equals(key)) {
                return mode;
            }
        }
        return SONG;
    }
}
