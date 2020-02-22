package net.gazeplay.commons.configuration;

import mockit.Mock;
import mockit.MockUp;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationSourceTest {

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep;

    @Test
    void shouldCreateFromDefaultProfile() {
        new MockUp<GazePlayDirectories>() {
            @Mock
            public File getGazePlayFolder() {
                return new File(localDataFolder);
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
                return new File(localDataFolder);
            }
        };

        Configuration result = ConfigurationSource.createFromProfile("test");

        assertEquals("eng", result.getLanguage());
    }
}
