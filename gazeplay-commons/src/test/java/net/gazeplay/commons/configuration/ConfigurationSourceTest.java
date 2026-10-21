package net.gazeplay.commons.configuration;

import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void givenPropertiesFileNotFoundShouldCreateDefaultProperties() {
        Configuration result = ConfigurationSource.createFromPropertiesResource(new File(localDataFolder, "wrong.properties"));

        assertEquals(2000, result.getFixationLength());
    }

    @Test
    void shouldCreateFromProfile() throws IOException {
        // user.home is a temporary folder during tests (see build.gradle)
        File profileDirectory = GazePlayDirectories.getUserProfileDirectory("test");
        assertTrue(profileDirectory.mkdirs() || profileDirectory.isDirectory());
        Files.copy(new File(resourcesFolder, "GazePlay.properties").toPath(),
            new File(profileDirectory, "GazePlay.properties").toPath(), StandardCopyOption.REPLACE_EXISTING);

        Configuration result = ConfigurationSource.createFromProfile("test");

        assertEquals(900, result.getFixationLength());
    }
}
