package net.gazeplay.games.pianosight;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.Stats;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class MidiReader {

    private static final int NOTE_ON = 0x90;

    private static final int NOTE_OFF = 0x80;

    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private Track track;

    private long prevTick;

    @Getter
    private int key;

    private int tickIndex;

    private final Instru instru = new Instru();

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
            e.printStackTrace();
        }

    }

    public int getTrackSize() {
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
