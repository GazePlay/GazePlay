package net.gazeplay.games.pianosight;

import javafx.beans.property.ObjectProperty;

import javax.sound.midi.*;

import static javax.sound.midi.ShortMessage.NOTE_ON;

public class PianoReceiver implements Receiver {

    Sequencer sequencer;
    ObjectProperty<Note> ip;
    long prevTick = 0;
    boolean[] chanel = new boolean[16];

    public PianoReceiver(Sequencer sequencer, ObjectProperty<Note> ip) {
        super();
        this.ip = ip;
        this.sequencer = sequencer;
        this.initChanel();
    }

    public void initChanel() {
        for (int i = 0; i < 16; i++) {
            chanel[i] = false;
        }
        prevTick = 0;
    }

    public void send(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            int key = sm.getData1();
            int velocity = sm.getData2();
            if (sm.getCommand() == NOTE_ON) {
                long newTick = sequencer.getTickPosition();
                if (prevTick + 10 < newTick && prevTick != -1 && chanelHaveToBePlayed(sm.getChannel())) {
                    prevTick = newTick;
                    Note n = new Note(key, velocity, newTick);
                    ip.setValue(new Note(-1, -1, -1));
                    ip.setValue(n);
                } else {
                    Note n = new Note(key, velocity, newTick);
                }
            }
        }
    }

    private boolean chanelHaveToBePlayed(int chanelIndex) {
        return chanel[chanelIndex];
    }

    @Override
    public void close() {
    }

    public void setChanel(int c, boolean b) {
        chanel[c] = b;
    }

    public void setAllChanelOn() {
        for (int i = 0; i < 16; i++) {
            chanel[i] = true;
        }
    }
}
