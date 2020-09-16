
package net.gazeplay.games.pianosight;

import javafx.beans.property.ObjectProperty;
import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.*;

@Slf4j
public class MidiSequencerPlayer {

    Sequencer sequencer;
    PianoReceiver pianoReceiver;
    Sequence sequence;
    ObjectProperty<Note> noteProperty;
    float currentBPM;

    public MidiSequencerPlayer(Sequence sequence, ObjectProperty<Note> noteProperty) {
        this.sequence = sequence;
        this.noteProperty = noteProperty;
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(sequence);
            sequencer.open();
            Transmitter transmitter = sequencer.getTransmitter();
            pianoReceiver = new PianoReceiver(sequencer, noteProperty);
            transmitter.setReceiver(pianoReceiver);
            /*
            on parcours toute la sequence une première fois pour trouver tous les ticks où on va stop, en créant un objet qui contient le tick et la track du truc qu'on veux jouer
            sequencer.getSequence().getTracks()[0].get(0).getTick()
             */
        } catch (InvalidMidiDataException | MidiUnavailableException ignored) {
        }
    }


    public void start() {
        if (!sequencer.isRunning()) {
            sequencer.start();
        }
        sequencer.setTempoInBPM(currentBPM);
    }

    public void stop() {
        sequencer.setTempoInBPM(0);
    }

    public void setChannel(int c, boolean b) {
        pianoReceiver.channelHasToBePlayed[c] = b;
    }

    public void setTempo(float tempo) {
        currentBPM = tempo;
    }
}
