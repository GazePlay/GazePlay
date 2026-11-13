package net.gazeplay.games.pianosight;

/**
 * What plays the notes of the Piano: the MIDI synthesizer of Java in the game (see {@link Instru}), a recorder in the
 * tests.
 */
interface NotePlayer {

    /**
     * Plays a note.
     *
     * @param midiNote its MIDI number (60 is the middle C)
     */
    void play(int midiNote);

    /**
     * Stops every note and frees the synthesizer.
     */
    void close();
}
