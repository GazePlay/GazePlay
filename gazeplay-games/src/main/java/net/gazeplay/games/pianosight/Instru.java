package net.gazeplay.games.pianosight;

import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

/**
 * Created by schwab on 28/08/2016.
 */
@Slf4j
public class Instru {

    private final MidiChannel canal;

    Instru() {
        // On récupère le synthétiseur, on l'ouvre et on obtient un canal
        final Synthesizer synthetiseur;
        try {
            synthetiseur = MidiSystem.getSynthesizer();
            synthetiseur.open();
        } catch (final MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
        canal = synthetiseur.getChannels()[0];

        // On initialise l'instrument 0 (le piano) pour le canal
        canal.programChange(0);
    }

    /**
     * Joue la note dont le numéro est en paramètre
     */
    void noteOn(final int note) {
        final int volume = 100;
        canal.noteOn(note, volume);
    }

    /**
     * Arrête de jouer la note dont le numéro est en paramètre
     */
    public void noteOff(final int note) {
        canal.noteOff(note);
    }

    /**
     * Set le type d'instrument dont le numéro MIDI est précisé en paramètre
     */
    public void setInstrument(final int instru) {
        canal.programChange(instru);
    }

}
