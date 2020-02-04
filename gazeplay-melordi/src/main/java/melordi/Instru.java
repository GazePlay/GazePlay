package melordi;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Instru {

    public final int volume = 100;

    private Synthesizer synthetiseur;
    private final MidiChannel canal;

    public Instru() {

        try {
            // On récupère le synthétiseur, on l'ouvre et on obtient un canal
            synthetiseur = MidiSystem.getSynthesizer();
            synthetiseur.open();
        } catch (final MidiUnavailableException ex) {
            Logger.getLogger(Instru.class.getName()).log(Level.SEVERE, null, ex);
        }
        canal = synthetiseur.getChannels()[0];

        // On initialise l'instrument 0 (le piano) pour le canal
        canal.programChange(0);
    }

    // Joue la note dont le numéro est en paramètre
    public void note_on(final int note) {
        canal.noteOn(note, volume);
    }

    // Arrête de jouer la note dont le numéro est en paramètre
    public void note_off(final int note) {
        canal.noteOff(note);
    }

    // Set le type d'instrument dont le numéro MIDI est précisé en paramètre
    public void set_instrument(final int instru) {
        canal.programChange(instru);
    }
}
