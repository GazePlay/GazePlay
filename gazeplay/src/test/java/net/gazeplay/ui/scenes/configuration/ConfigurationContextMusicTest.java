package net.gazeplay.ui.scenes.configuration;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
@RunWith(MockitoJUnitRunner.class)
class ConfigurationContextMusicTest {

    @Mock
    private Configuration mockConfig;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void canChangeMusicFolderToNewFolder() {
        StringProperty musicFolder = new SimpleStringProperty();
        String newFolder = "new/folder";
        when(mockConfig.getMusicFolderProperty()).thenReturn(musicFolder);
        ConfigurationContext.changeMusicFolder(newFolder, mockConfig);

        assertEquals(musicFolder.getValue(), newFolder);
    }

    @Test
    void canChangeMusicFolderToDefaultFolder() {
        StringProperty musicFolder = new SimpleStringProperty();
        String newFolder = "";
        String expectedFolder = GazePlayDirectories.getGazePlayFolder() + File.separator + "music";
        when(mockConfig.getMusicFolderProperty()).thenReturn(musicFolder);
        ConfigurationContext.changeMusicFolder(newFolder, mockConfig);

        assertEquals(musicFolder.getValue(), expectedFolder);
    }
}
