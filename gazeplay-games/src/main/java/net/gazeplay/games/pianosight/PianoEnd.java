package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * When the songs of the Piano end (maintainer's choice of 13/11/2026): after 1, 3 or 5 songs, or never; the free mode
 * has a single value, without end.
 */
@Getter
@RequiredArgsConstructor
public enum PianoEnd {
    SONGS_1("PianoEnd1", 1),
    SONGS_3("PianoEnd3", 3),
    SONGS_5("PianoEnd5", 5),
    ENDLESS("PianoEndless", 0),
    FREE("PianoAllKeys", 0);

    /**
     * Key of translation, and value of the variants (the buttons).
     */
    private final String key;

    /**
     * The songs to play, 0 without end.
     */
    private final int songs;

    /**
     * @return the end of a key, without end for an unknown one
     */
    public static PianoEnd of(final String key) {
        for (final PianoEnd end : values()) {
            if (end.key.equals(key)) {
                return end;
            }
        }
        return ENDLESS;
    }
}
