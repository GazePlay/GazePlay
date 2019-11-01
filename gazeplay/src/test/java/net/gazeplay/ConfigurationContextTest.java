package net.gazeplay;

import org.junit.jupiter.api.Test;

import java.io.File;

class ConfigurationContextTest {

    @Test
    void canSetupANewMusicFolder() {
        String songName = "songidea(copycat)_0.mp3";
        File testFolder = new File("music_test");
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assert (testFolder.isDirectory());
        assert (expectedFile.exists());

        expectedFile.delete();
        testFolder.delete();
    }

    @Test
    void canSetupANewMusicFolderIfTheFolderExists() {
        String songName = "songidea(copycat)_0.mp3";
        File testFolder = new File("music_test");
        assert (testFolder.mkdir());
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assert (testFolder.isDirectory());
        assert (expectedFile.exists());

        expectedFile.delete();
        testFolder.delete();
    }

    @Test
    void canSetupANewMusicFolderIfTheSongDoesntExist() {
        String songName = "fakesong.mp3";
        File testFolder = new File("music_test");
        assert (testFolder.mkdir());
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assert (testFolder.isDirectory());
        assert (!expectedFile.exists());

        testFolder.delete();
    }
}
