package net.gazeplay;

import net.gazeplay.ui.scenes.configuration.ConfigurationContext;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationContextTest {

    @Test
    void canSetupANewMusicFolder() {
        String songName = "songidea(copycat)_0.mp3";
        File testFolder = new File("music_test");
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assertTrue(testFolder.isDirectory());
        assertTrue(expectedFile.exists());

        assertTrue(expectedFile.delete());
        assertTrue(testFolder.delete());
    }

    @Test
    void canSetupANewMusicFolderIfTheFolderExists() {
        String songName = "songidea(copycat)_0.mp3";
        File testFolder = new File("music_test");
        assertTrue(testFolder.mkdir());
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assertTrue(testFolder.isDirectory());
        assertTrue(expectedFile.exists());

        assertTrue(expectedFile.delete());
        assertTrue(testFolder.delete());
    }

    @Test
    void canSetupANewMusicFolderIfTheSongDoesntExist() {
        String songName = "fakesong.mp3";
        File testFolder = new File("music_test");
        assertTrue(testFolder.mkdir());
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assertTrue(testFolder.isDirectory());
        assertFalse(expectedFile.exists());

        assertTrue(testFolder.delete());
    }
}
