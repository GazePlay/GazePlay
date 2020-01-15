package net.gazeplay.ui.scenes.configuration;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BackgroundMusicManager.class)
class ConfigurationContextTest {

    private BackgroundMusicManager mockMusicManager;
    private Configuration mockConfiguration;

    @BeforeEach
    void setup() {
        mockMusicManager = mock(BackgroundMusicManager.class);
        mockConfiguration = mock(Configuration.class);
        mockStatic(BackgroundMusicManager.class);
        when(BackgroundMusicManager.getInstance()).thenReturn(mockMusicManager);
    }

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

    @Test
    void canChangeTheMusicFolderAndPlayIfWasPlaying() {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();

        when(mockMusicManager.isPlaying()).thenReturn(true);
        when(mockConfiguration.getMusicFolderProperty()).thenReturn(mockMusicFolderProperty);

        ConfigurationContext.changeMusicFolder("mockFolder", mockConfiguration);

        verify(mockMusicManager).play();
    }
}
