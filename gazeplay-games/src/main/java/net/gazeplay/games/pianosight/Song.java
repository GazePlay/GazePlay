package net.gazeplay.games.pianosight;

import lombok.Getter;

/**
 * The songs of the Piano (maintainer's choice of 13/11/2026): four tunes of the public domain, written note by note,
 * all within the octave of the keyboard, from the middle C to the C above. Each note: its semitones above the middle C
 * (0 = do, 2 = ré, 4 = mi, 5 = fa, 7 = sol, 9 = la, 10 = si bémol, 11 = si, 12 = do) and its length in beats, used when
 * the song is played again at its end.
 * <p>
 * "Au clair de la lune" is in G, with an F sharp (a black key): in C its third phrase goes under the middle C.
 * "Joyeux anniversaire" is in F, with a B flat (a black key): its tune (Mildred and Patty Hill, "Good Morning to All",
 * 1893) is in the public domain in Europe since 2017. "L'Ode à la joie": its first two phrases (Beethoven, 1824).
 */
@Getter
public enum Song {
    AU_CLAIR_DE_LA_LUNE("PianoSongMoon",
        new int[]{7, 7, 7, 9, 11, 9, 7, 11, 9, 9, 7,
            7, 7, 7, 9, 11, 9, 7, 11, 9, 9, 7,
            9, 9, 9, 9, 4, 4, 9, 7, 6, 4, 2,
            7, 7, 7, 9, 11, 9, 7, 11, 9, 9, 7},
        new double[]{1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 4,
            1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 4,
            1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 4,
            1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 4}),
    AH_VOUS_DIRAI_JE_MAMAN("PianoSongTwinkle",
        new int[]{0, 0, 7, 7, 9, 9, 7, 5, 5, 4, 4, 2, 2, 0,
            7, 7, 5, 5, 4, 4, 2, 7, 7, 5, 5, 4, 4, 2,
            0, 0, 7, 7, 9, 9, 7, 5, 5, 4, 4, 2, 2, 0},
        new double[]{1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 2,
            1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 2,
            1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 2}),
    ODE_A_LA_JOIE("PianoSongJoy",
        new int[]{4, 4, 5, 7, 7, 5, 4, 2, 0, 0, 2, 4, 4, 2, 2,
            4, 4, 5, 7, 7, 5, 4, 2, 0, 0, 2, 4, 2, 0, 0},
        new double[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1.5, 0.5, 2,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1.5, 0.5, 2}),
    JOYEUX_ANNIVERSAIRE("PianoSongBirthday",
        new int[]{0, 0, 2, 0, 5, 4,
            0, 0, 2, 0, 7, 5,
            0, 0, 12, 9, 5, 4, 2,
            10, 10, 9, 5, 7, 5},
        new double[]{0.75, 0.25, 1, 1, 1, 2,
            0.75, 0.25, 1, 1, 1, 2,
            0.75, 0.25, 1, 1, 1, 1, 2,
            0.75, 0.25, 1, 1, 1, 2});

    /**
     * Key of translation of its title.
     */
    private final String key;
    private final int[] notes;
    private final double[] beats;

    Song(final String key, final int[] notes, final double[] beats) {
        if (notes.length != beats.length) {
            throw new IllegalArgumentException(key + ": " + notes.length + " notes, " + beats.length + " lengths");
        }
        this.key = key;
        this.notes = notes;
        this.beats = beats;
    }

    int size() {
        return notes.length;
    }
}
