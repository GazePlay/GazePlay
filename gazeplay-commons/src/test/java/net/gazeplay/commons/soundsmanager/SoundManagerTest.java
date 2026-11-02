package net.gazeplay.commons.soundsmanager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SoundManagerTest {

    private SoundManager soundManager;

    @BeforeEach
    void initSoundManager() {
        soundManager = new SoundManager();
    }

    @Test
    void shouldAddSeveralFilesNameToTheList() {
        soundManager.add("music/mvmt0.wav");
        soundManager.add("music/hand_sound1.mp3");
        soundManager.add("music/song.mp3");

        Assertions.assertTrue(soundManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
        Assertions.assertTrue(soundManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
        Assertions.assertTrue(soundManager.getListOfMusicToPlay().contains("music/song.mp3"));
    }

    @Test
    void shouldRemoveFilesFromListWhenPlayed() {
        soundManager.add("music/mvmt0.wav");
        soundManager.add("music/hand_sound1.mp3");

        soundManager.playRequestedSounds();
        Assertions.assertFalse(soundManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
        soundManager.playRequestedSounds();
        Assertions.assertFalse(soundManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
    }

    /**
     * A sound that replaces the others: the sounds waiting are forgotten (and the one playing is stopped, with no
     * sound played here).
     */
    @Test
    void shouldForgetTheSoundsWaitingWhenASoundReplacesThem() {
        soundManager.add("music/mvmt0.wav");
        soundManager.add("music/hand_sound1.mp3");

        soundManager.replace("music/song.mp3");

        Assertions.assertEquals(java.util.List.of("music/song.mp3"),
            new java.util.ArrayList<>(soundManager.getListOfMusicToPlay()));
    }

}
