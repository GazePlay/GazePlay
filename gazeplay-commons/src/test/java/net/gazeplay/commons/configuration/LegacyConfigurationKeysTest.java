package net.gazeplay.commons.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class LegacyConfigurationKeysTest {

    @Test
    void shouldCopyOldKeysToMissingNewKeys() {
        Properties properties = new Properties();
        properties.setProperty("EYETRACKER", "mouse_control");
        properties.setProperty("LIMITERT", "true");

        int migrated = LegacyConfigurationKeys.migrate(properties);

        assertEquals(2, migrated);
        assertEquals("mouse_control", properties.getProperty("EYE_TRACKER"));
        assertEquals("true", properties.getProperty("LIMITER_TIME_ENABLED"));
        // Kept for older versions of GazePlay
        assertEquals("mouse_control", properties.getProperty("EYETRACKER"));
    }

    @Test
    void shouldKeepTheNewKeyWhenBothArePresent() {
        Properties properties = new Properties();
        properties.setProperty("EYETRACKER", "eyetribe");
        properties.setProperty("EYE_TRACKER", "mouse_control");

        assertEquals(0, LegacyConfigurationKeys.migrate(properties));
        assertEquals("mouse_control", properties.getProperty("EYE_TRACKER"));
    }

    @Test
    void shouldMapEveryOldKeyToAnExistingDistinctKey() {
        assertEquals(17, LegacyConfigurationKeys.NEW_KEY_BY_OLD_KEY.size());
        assertEquals(17, LegacyConfigurationKeys.NEW_KEY_BY_OLD_KEY.values().stream().distinct().count());
    }

    /**
     * A GazePlay.properties written by a version older than 1.11.1 used to be read as if it were empty.
     */
    @Test
    void shouldReadTheSettingsOfAnOldConfigurationFile(@TempDir File tempDir) throws IOException {
        File file = new File(tempDir, "GazePlay.properties");
        Files.writeString(file.toPath(), String.join("\n",
            "EYETRACKER=mouse_control",
            "FILEDIR=/Users/admin/GazePlay/files",
            "FIXATIONLENGTH=900",
            "CSSFILE=builtin\\:BLUE",
            "FORCE_DISPLAY_NEWS=true",
            "QUESTIONLENGTH=4000",
            ""), StandardCharsets.ISO_8859_1);

        Configuration configuration = ConfigurationSource.createFromPropertiesResource(file);

        assertEquals("mouse_control", configuration.getEyeTracker());
        assertEquals("/Users/admin/GazePlay/files", configuration.getFileDir());
        assertEquals(900, configuration.getFixationLength());
        assertEquals("builtin:BLUE", configuration.getCssFile());
        assertTrue(configuration.isLatestNewsDisplayForced());
        assertEquals(4000, configuration.getQuestionLength());

        configuration.setUserName("someone");

        Properties saved = new Properties();
        try (InputStream is = Files.newInputStream(file.toPath())) {
            saved.load(is);
        }
        assertEquals("mouse_control", saved.getProperty("EYE_TRACKER"));
        assertEquals("mouse_control", saved.getProperty("EYETRACKER"));
        assertEquals("someone", saved.getProperty("USER_NAME"));
    }
}
