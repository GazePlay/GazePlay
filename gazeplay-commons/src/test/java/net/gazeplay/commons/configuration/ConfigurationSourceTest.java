package net.gazeplay.commons.configuration;

import lombok.extern.slf4j.Slf4j;
import mockit.Mock;
import mockit.MockUp;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ConfigurationSourceTest {

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep;

    private final File resourcesFolder = new File(localDataFolder);

    @Test
    void shouldCreateFromDefaultProfile() {
        log.info(resourcesFolder.getAbsolutePath());
        new MockUp<GazePlayDirectories>() {
            @Mock
            public File getGazePlayFolder() {
                return resourcesFolder;
            }
        };

        Configuration result = ConfigurationSource.createFromDefaultProfile();

        assertEquals("eng", result.getLanguage());
    }

    @Test
    void shouldCreateDefaultPropertiesIfFileNotFound() {
        Configuration result = ConfigurationSource.createFromPropertiesResource(new File(localDataFolder, "wrong.properties"));

        assertEquals("fra", result.getLanguage());
    }

    @Test
    void shouldCreateFromProfile() {
        new MockUp<GazePlayDirectories>() {
            @Mock
            public File getUserProfileDirectory(String id) {
                return resourcesFolder;
            }
        };

        Configuration result = ConfigurationSource.createFromProfile("test");

        assertEquals("eng", result.getLanguage());
    }
}
