package net.gazeplay.commons.configuration;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Reads the GazePlay.properties files of the profiles: the default profile (in the GazePlay folder) and the user
 * profiles (profiles/&lt;id&gt;/GazePlay.properties).
 */
@Slf4j
public class ConfigurationSource {

    private static final File defaultProfileConfigFile = new File(GazePlayDirectories.getGazePlayFolder(), "GazePlay.properties");

    private static File getSpecificProfileConfigPath(final String profileId) {
        return new File(GazePlayDirectories.getUserProfileDirectory(profileId), "GazePlay.properties");
    }

    /**
     * @return the configuration of the default profile
     */
    public static Configuration createFromDefaultProfile() {
        return createFromPropertiesResource(defaultProfileConfigFile);
    }

    /**
     * @return the configuration of a user profile
     */
    public static Configuration createFromProfile(final String profileId) {
        return createFromPropertiesResource(getSpecificProfileConfigPath(profileId));
    }

    /**
     * Reads a GazePlay.properties file. A missing file gives the default settings (first launch, new profile); an
     * unreadable one too, with an error in the log. The keys of older versions are migrated, and the paths that pointed
     * into the former GazePlay folder are moved to the new one (in memory: the file is only written when a setting
     * changes).
     *
     * @return the configuration, which writes the file back at each change of a setting
     */
    public static Configuration createFromPropertiesResource(final File propertiesFile) {
        Properties properties;

        // First launch, or new profile: not an error (it used to be logged as one, with a stack trace)
        if (!propertiesFile.exists()) {
            log.info("No Properties file {} yet: default settings", propertiesFile);
            return new Configuration(propertiesFile, new ApplicationConfig(new Properties()));
        }

        try {
            log.info("Loading Properties from : {}", propertiesFile);
            properties = loadProperties(propertiesFile);
            LegacyConfigurationKeys.migrate(properties);
            // Paths saved when the GazePlay folder was still in the home directory (moved to Documents in 2026)
            GazePlayDirectories.relocateMovedPaths(properties);
            log.info("Properties loaded : {}", properties);
        } catch (final IOException e) {
            log.error("Failure while loading Properties file {}", propertiesFile, e);
            properties = new Properties();
            log.info("Properties loaded : {}", properties);
        }

        final ApplicationConfig applicationConfig = new ApplicationConfig(properties);
        return new Configuration(propertiesFile, applicationConfig);
    }

    private static Properties loadProperties(final File propertiesFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(propertiesFile.toPath())) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }
}
