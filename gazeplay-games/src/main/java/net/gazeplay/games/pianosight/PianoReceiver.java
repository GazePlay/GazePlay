package net.gazeplay.games.pianosight;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import static javax.sound.midi.ShortMessage.NOTE_ON;

public class PianoReceiver implements Receiver {

    ObjectProperty<Note> currentNoteProperty;
    ObjectProperty<Long> currentTickProperty;

    Sequencer sequencer;
    long previousTick = 0;
    boolean[] channelHasToBePlayed = new boolean[16];
    boolean isSliderInUse = false;

    public PianoReceiver(Sequencer sequencer, ObjectProperty<Note> currentNoteProperty) {
        super();

        this.currentNoteProperty = currentNoteProperty;
        this.currentTickProperty = new SimpleObjectProperty<Long>(0L);

        this.sequencer = sequencer;
        this.initPianorReceiverParameters();
    }

    public void initPianorReceiverParameters() {
        for (int i = 0; i < 16; i++) {
            channelHasToBePlayed[i] = true;
        }

        previousTick = 0;
        currentTickProperty.setValue(0L);
    }

    public void send(MidiMessage message, long timeStamp) {
        if (!isSliderInUse) {
            long newTick = sequencer.getTickPosition();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                if (sm.getCommand() == NOTE_ON) {
                    if (previousTick + 10 < newTick && previousTick != -1 && channelHasToBePlayed[sm.getChannel()]) {
                        previousTick = newTick;
                        Note noteToPlay = new Note(/*key*/sm.getData1(),/*velocity*/sm.getData2(), newTick);
                        currentNoteProperty.setValue(new Note(-1, -1, -1));
                        currentNoteProperty.setValue(noteToPlay);
                    }
                }
            }
            currentTickProperty.setValue(newTick);
        }
    }

    @Override
    public void close() {
    }

}
