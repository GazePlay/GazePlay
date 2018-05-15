package net.gazeplay.games.pianosight;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import net.gazeplay.commons.utils.games.Utils;

public class midiReader {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static void main(String[] args) throws Exception {
    	
    	String fileName = "RIVER.mid";
        InputStream inputStream = Utils.getInputStream("data/pianosight/songs/" + fileName);
    	
        Sequence sequence = MidiSystem.getSequence(inputStream);

        Instru instru =  new Instru();
        long prevtick = -1;
        LinkedList<Integer> notes = new LinkedList<Integer>();
        for (Track track :  sequence.getTracks()) {
            System.out.println();
            for (int i=0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                long tick = event.getTick();
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    System.out.print("Channel: " + sm.getChannel() + " ");
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        /*if(tick==prevtick) {
                        	notes.add(key);
                        }else {
                        	for (int note : notes) {
                                instru.note_on(note);
                    		}
                            Thread.sleep(500);
                        	notes.clear();
                        	notes.add(key);
                        }*/
                        prevtick=tick;
                        int octave = (key / 12)-1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                        if (velocity ==0) {
                        	instru.note_on(key);
                            Thread.sleep(500);
                        }
                    }
                }
            }

            System.out.println();
        }

    }
}