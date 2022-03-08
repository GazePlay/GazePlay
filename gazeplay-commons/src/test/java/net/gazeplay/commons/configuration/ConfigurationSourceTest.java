package net.gazeplay.commons.configuration;

import mockit.Mock;
import mockit.MockUp;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationSourceTest {

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep;

    private final File resourcesFolder = new File(localDataFolder);

//    @Test
//    void shouldCreateFromPropertiesResource() {
//        Configuration result = ConfigurationSource.createFromPropertiesResource(new File(resourcesFolder, "GazePlay.properties"));

//        assertEquals("eng", result.getLanguage());
//    }

    @Test
    void givenPropertiesFileNotFound_shouldCreateDefaultProperties() {
        Configuration result = ConfigurationSource.createFromPropertiesResource(new File(localDataFolder, "wrong.properties"));

        assertEquals(2000, result.getFixationLength());
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

        assertEquals(900, result.getFixationLength());
    }
}
