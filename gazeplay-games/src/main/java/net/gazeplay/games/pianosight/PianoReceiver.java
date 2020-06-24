package net.gazeplay.games.pianosight;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;

import javax.sound.midi.*;

import java.util.LinkedList;

import static javax.sound.midi.ShortMessage.*;

public class PianoReceiver implements Receiver {

    Sequencer sequencer;
    ObjectProperty<Note> ip;
    long prevTick = 0;
    int trackIndex = 0;
    boolean[] chanel = new boolean[16];

    public PianoReceiver(Sequencer sequencer, ObjectProperty<Note> ip ) {
        super();
        this.ip = ip;
        this.sequencer = sequencer;
        this.initChanel();
        Track[] ts = sequencer.getSequence().getTracks();
        int minLength = 0;
        int maxLength = 0;
        for (int i = 0; i< ts.length; i++){
            if(ts[i].size()> maxLength){
                maxLength = ts[i].size();
            }
            if(ts[i].size()< minLength){
                minLength = ts[i].size();
            }
        }
        int distance = maxLength;
        for (int i = 0; i< ts.length; i++){
            int res = (maxLength - ts[i].size() - ts[i].size() + minLength) ;
            if(distance > res*res && ts[i].size() != 0){
                trackIndex = i;
            }
        }
        //this.sequencer.setTrackSolo(trackIndex,true);
    }

    public void initChanel(){
        for(int i = 0; i <16; i++){
            chanel[i] = false;
        }
    }

    public void send(MidiMessage message, long timeStamp) {
        if(message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            int key = sm.getData1();
            int velocity = sm.getData2();
            if (sm.getCommand() == NOTE_ON) {
                long newTick = sequencer.getTickPosition();
                if(prevTick != newTick && prevTick != -1 && chanelHaveToBePlayed(sm.getChannel())) {
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

    private boolean chanelHaveToBePlayed(int chanelIndex){
        return chanel[chanelIndex];
    }

    @Override
    public void close() {
    }

    public void setChanel(int c, boolean b){
        chanel[c]=b;
    }
}
