package net.gazeplay.games.pianosight;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.Stats;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

@Slf4j
public class MidiReader {

    private static final int NOTE_ON = 0x90;

    private static final int NOTE_OFF = 0x80;

    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private Track track;

    private long prevTick;
    LinkedList<Integer> prevKeyList = new LinkedList<>();
    private long tickLength;

    @Getter
    private int key;

    private int tickIndex;

    private final Instru instru = new Instru();

    private Stats stats;

    MidiReader(InputStream inputStream, Stats stats) {
        this.stats = stats;
        try {
            Sequence sequence = MidiSystem.getSequence(inputStream);
            tickLength = (1000 / 60);
            track = sequence.getTracks()[0];
            for (int i = 0; i < sequence.getTracks().length; i++) {
                for (int j = 0; j < sequence.getTracks()[i].size(); j++) {
                    track.add(sequence.getTracks()[i].get(j));
                }
            }
            tickIndex = 0;
            prevTick = -1;
            key = -1;
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }

    }

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
                    if ((previousTick != event.getTick())) {
                        nbOfNotes++;
                        previousTick = event.getTick();
                    }
                }
            }
            index++;
        }
        return nbOfNotes;
    }

    public int nextNote(){
        try {
            if (tickIndex < track.size()) {
                MidiEvent event = track.get(tickIndex);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (tickIndex == 0 && (sm.getCommand() == ShortMessage.NOTE_ON)) {
                        prevTick = event.getTick();
                        instru.noteOn(sm.getData1(), sm.getData2());
                        tickIndex++;
                        return sm.getData1()%12;
                    } else if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        long waitPeriod = (event.getTick() - prevTick);
                        if (waitPeriod != 0) {
                            return playKey( event, sm, false);
                        } else {
                            return playKey( event, sm, true);
                        }
                    } else {
                        tickIndex++;
                        return nextNote();
                    }
                } else {
                    tickIndex++;
                    return nextNote();
                }
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return -1;
        }
        return -1;
    }

    private int playKey(MidiEvent event, ShortMessage sm, boolean isPartofChord) throws InterruptedException {
        prevTick = event.getTick();
        if(!isPartofChord){
            prevKeyList.clear();
        }
        prevKeyList.add(sm.getData1());
        instru.noteOn(sm.getData1(), sm.getData2());
        tickIndex++;
        if(isPartofChord || sm.getData2() == 0) {
            return nextNote();
        } else {
            return sm.getData1()%12;
        }
    }
}
