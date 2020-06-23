package net.gazeplay.games.pianosight;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.sound.midi.*;

public class MidiSequencerPlayer {

    Sequencer sequencer;
    PianoReceiver pianoReceiver;
    Sequence sequence;
    ObjectProperty<Note> ip;

    public MidiSequencerPlayer(Sequence sequence, ObjectProperty<Note> ip) {
       this.sequence = sequence;
       this.ip = ip;
       this.init();
    }

    public void initSequencer() throws InvalidMidiDataException, MidiUnavailableException {
        if(sequencer !=null && sequencer.isOpen()){
            sequencer.close();
        }
        sequencer = MidiSystem.getSequencer();
        sequencer.setSequence(sequence);
        sequencer.open();
    }

    public void initReceiver() throws MidiUnavailableException {
        Transmitter transmitter = sequencer.getTransmitter();
        pianoReceiver = new PianoReceiver(sequencer, ip);
        transmitter.setReceiver(pianoReceiver);
    }

    public void init(){
        try {
           initSequencer();
           initReceiver();
        } catch (InvalidMidiDataException | MidiUnavailableException ignored) {
        }
    }

    public void start() {
        if(!sequencer.isRunning()) {
            sequencer.start();
        }
        sequencer.setTempoInBPM(120);
    }

    public void stop() {
        sequencer.setTempoInBPM(0);
    }

    public void playPause() {
        if(sequencer.getTempoInMPQ() != 0) {
            start();
        } else {
            stop();
        }
    }

    public void setChanel(int c){
        pianoReceiver.setChanel(c);
    }

}
