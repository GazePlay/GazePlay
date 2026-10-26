package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.Stats;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads the notes of a MIDI file one after the other, for the Piano game.
 */
@Slf4j
public class MidiReader {

    private static final int NOTE_ON = 0x90;

    private Track track;

    private long prevTick;

    @Getter
    private int key;

    private int tickIndex;

    private Stats stats;

    MidiReader(InputStream inputStream, Stats stats) {
        this.stats = stats;
        try {
            Sequence sequence = MidiSystem.getSequence(inputStream);
            int maxIndex = 0;
            int max = 0;
            for (int i = 0; i < sequence.getTracks().length; i++) {
                if (max < sequence.getTracks()[i].size()) {
                    maxIndex = i;
                }
            }
            // TODO problem here
            track = sequence.getTracks()[maxIndex];
            tickIndex = -1;
            prevTick = -1;
            key = -1;
        } catch (InvalidMidiDataException | IOException e) {
            log.error("Cannot read the MIDI file", e);
        }

    }

    /**
     * @return the number of notes of the track
     */
    public int getTrackSize(){
        int nbOfNotes = 0;
        int index = 0;
        long previousTick = -1;
        while (index + 1 < track.size()) {
            MidiEvent event = track.get(index);
            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                if (sm.getCommand() == NOTE_ON) {
                    if ((sm.getChannel() == 0) && (previousTick != event.getTick())) {
                        nbOfNotes++;
                        previousTick = event.getTick();
                    }
                }
            }
            index++;
        }
        return nbOfNotes;
    }

    /**
     * @return the next note of the track, -1 at its end
     */
    int nextNote() {
        int note = -1;
        if (tickIndex + 1 < track.size()) {
            tickIndex++;
            MidiEvent event = track.get(tickIndex);
            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                if (sm.getCommand() == NOTE_ON) {
                    // TODO problem here
                    if ((sm.getChannel() == 0) && (prevTick != event.getTick())) {
                        prevTick = event.getTick();
                        key = sm.getData1();
                        note = key % 12;
                        return note;
                    } else {
                        return nextNote();
                    }
                } else {
                    return nextNote();
                }
            } else {
                return nextNote();
            }
        }
        return note;
    }
}
