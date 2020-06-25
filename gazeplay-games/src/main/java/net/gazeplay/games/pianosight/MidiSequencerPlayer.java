package net.gazeplay.games.pianosight;

import javafx.beans.property.ObjectProperty;
import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.*;

@Slf4j
public class MidiSequencerPlayer {

    Sequencer sequencer;
    PianoReceiver pianoReceiver;
    Sequence sequence;
    ObjectProperty<Note> ip;
    float currentBPM;

    public MidiSequencerPlayer(Sequence sequence, ObjectProperty<Note> ip) {
        this.sequence = sequence;
        this.ip = ip;
        try {
            if (sequencer != null && sequencer.isOpen()) {
                sequencer.close();
            }
            sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(sequence);
            sequencer.open();
            Transmitter transmitter = sequencer.getTransmitter();
            pianoReceiver = new PianoReceiver(sequencer, ip);
            transmitter.setReceiver(pianoReceiver);
        } catch (InvalidMidiDataException | MidiUnavailableException ignored) {
        }
    }


    public void start() {
        if (!sequencer.isRunning()) {
            sequencer.start();
        }
        sequencer.setTempoInBPM(currentBPM);
        /*sequencer.setTrackMute(0, false);
        sequencer.setTrackSolo(0, false);*/
    }

    public void stop() {
        sequencer.setTempoInBPM(0);
        /*.setTrackSolo(0, true);
        sequencer.setTrackMute(0, true);*/
    }

    public void setChanel(int c, boolean b) {
        pianoReceiver.setChanel(c, b);
    }

    public void setTempo(float tempo){
        currentBPM = tempo;
    }
}
