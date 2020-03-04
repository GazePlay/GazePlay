package net.gazeplay.commons.soundsmanager;

import net.gazeplay.TestingUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SoundManagerTest {

    @Test
    void shouldAddMP3FileNameToTheList() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/hand_sound1.mp3");
        
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
    }

    @Test
    void shouldAddWAVFileNameToTheList() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/mvmt0.wav");
        
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
    }

    @Test
    void shouldAddSeveralFileNameToTheList() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/mvmt0.wav");
        
        soundsManager.add("music/hand_sound1.mp3");
        
        soundsManager.add("music/song.mp3");
        
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
        Assertions.assertTrue(soundsManager.getListOfMusicToPlay().contains("music/song.mp3"));
    }

    @Test
    void shouldRemoveMP3FileFromListWhenPlayed() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/hand_sound1.mp3");
        
        soundsManager.playRequestedSounds();
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
    }

    @Test
    void shouldRemoveWAVFileFromListWhenPlayed() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/mvmt0.wav");
        
        soundsManager.playRequestedSounds();
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
    }

    @Test
    void shouldRemoveFilesFromListWhenPlayed() throws InterruptedException {
        SoundsManager soundsManager = new SoundsManager();
        soundsManager.add("music/mvmt0.wav");
        
        soundsManager.add("music/hand_sound1.mp3");
        
        soundsManager.playRequestedSounds();
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/mvmt0.wav"));
        soundsManager.playRequestedSounds();
        Assertions.assertFalse(soundsManager.getListOfMusicToPlay().contains("music/hand_sound1.mp3"));
    }

}
