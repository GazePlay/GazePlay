package net.gazeplay.games.pianosight;

import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

/**
 * The MIDI synthesizer of Java playing the notes of the Piano with its piano sound (no file needed). Without a
 * synthesizer or a sound output, the game goes on silently (a warning in the log).
 * <p>
 * Created by schwab on 28/08/2016; tolerant and closed at the end since 13/11/2026.
 */
@Slf4j
class Instru implements NotePlayer {

    private static final int VOLUME = 100;

    private Synthesizer synthesizer;
    private MidiChannel channel;

    Instru() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            channel = synthesizer.getChannels()[0];
            channel.programChange(0);
        } catch (final MidiUnavailableException | RuntimeException e) {
            log.warn("No MIDI synthesizer: the Piano plays without sound", e);
            channel = null;
        }
    }

    @Override
    public void play(final int midiNote) {
        if (channel != null) {
            // The same note again: start it anew
            channel.noteOff(midiNote);
            channel.noteOn(midiNote, VOLUME);
        }
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.allNotesOff();
        }
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }
        channel = null;
    }
}
