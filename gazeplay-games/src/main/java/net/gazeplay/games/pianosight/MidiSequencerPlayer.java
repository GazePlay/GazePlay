package net.gazeplay.games.pianosight;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.sound.midi.*;

public class MidiSequencerPlayer {

    private Sequencer sequencer;
    PianoReceiver pianoReceiver;

    public MidiSequencerPlayer(Sequence sequence, ObjectProperty<Note> ip) {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(sequence);
            sequencer.open();
              Transmitter transmitter = sequencer.getTransmitter();
              pianoReceiver = new PianoReceiver(sequencer, ip);
              transmitter.setReceiver(pianoReceiver);
        } catch (InvalidMidiDataException | MidiUnavailableException ignored) {


        }
    }

    public void start(){
        if(!sequencer.isRunning()) {
            sequencer.start();
        }
    }

    public void stop() throws MidiUnavailableException {
        if (sequencer.isRunning()) {
            //sequencer.stop();
        }
    }

    public void playPause() throws MidiUnavailableException {
        if(!sequencer.isRunning()) {
            start();
        } else {
            stop();
        }
    }

}
