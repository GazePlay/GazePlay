package net.gazeplay.ui.scenes.configuration;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.extern.slf4j.Slf4j;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import net.gazeplay.GazePlay;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class ConfigurationContextTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Translator mockTranslator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
    }

    @Test
    void shouldReturnToMenuOnHomeButtonPress() {
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        HomeButton button = context.createHomeButtonInConfigurationManagementScreen(mockGazePlay);
        button.fireEvent(TestingUtils.clickOnTarget(button));

        verify(mockGazePlay).onReturnToMenu();
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
    void canChangeTheMusicFolderAndPlayIfWasPlaying(@Mocked BackgroundMusicManager mockMusicManager,
                                                    @Mocked Configuration mockConfiguration) {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();

        new Expectations() {{
            mockMusicManager.isPlaying();
            result = true;

            BackgroundMusicManager.getInstance();
            result = mockMusicManager;

            mockConfiguration.getMusicFolderProperty();
            result = mockMusicFolderProperty;
        }};

        ConfigurationContext.changeMusicFolder("mockFolder", mockConfiguration);

        new Verifications() {{
            mockMusicManager.play();
            times = 1;
        }};
    }

    @Test
    void canChangeTheMusicFolderAndNotPlayIfWasntPlaying(@Mocked BackgroundMusicManager mockMusicManager,
                                                         @Mocked Configuration mockConfiguration) {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();

        new Expectations() {{
            mockMusicManager.isPlaying();
            result = false;

            BackgroundMusicManager.getInstance();
            result = mockMusicManager;

            mockConfiguration.getMusicFolderProperty();
            result = mockMusicFolderProperty;
        }};

        ConfigurationContext.changeMusicFolder("mockFolder", mockConfiguration);

        new Verifications() {{
            mockMusicManager.play();
            times = 0;
        }};
    }

    @Test
    void canChangeTheMusicFolderWithBlankFolder(@Mocked BackgroundMusicManager mockMusicManager,
                                                @Mocked Configuration mockConfiguration) {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();
        String expectedFolder = System.getProperty("user.home") + File.separator + "GazePlay" + File.separator + "music";

        new Expectations() {{
            mockMusicManager.isPlaying();
            result = false;

            BackgroundMusicManager.getInstance();
            result = mockMusicManager;

            mockConfiguration.getMusicFolderProperty();
            result = mockMusicFolderProperty;
        }};

        ConfigurationContext.changeMusicFolder("", mockConfiguration);

        new Verifications() {{
            mockMusicManager.getAudioFromFolder(expectedFolder);
        }};
    }
}
