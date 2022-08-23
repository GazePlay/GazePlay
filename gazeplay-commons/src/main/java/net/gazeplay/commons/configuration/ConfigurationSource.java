package net.gazeplay.commons.configuration;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.aeonbits.owner.ConfigFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

@Slf4j
public class ConfigurationSource {
    private static final File defaultProfileConfigFile = new File(GazePlayDirectories.getGazePlayFolder(), "GazePlay.properties");

    private static File getSpecificProfileConfigPath(final String profileId) {
        return new File(GazePlayDirectories.getUserProfileDirectory(profileId), "GazePlay.properties");
    }

    public static Configuration createFromDefaultProfile() {
        return createFromPropertiesResource(defaultProfileConfigFile);
    }

    public static Configuration createFromProfile(final String profileId) {
        return createFromPropertiesResource(getSpecificProfileConfigPath(profileId));
    }

    public static Configuration createFromPropertiesResource(final File propertiesFile) {
        Properties properties;

        try {
            log.info("Loading Properties from : {}", propertiesFile);
            properties = loadProperties(propertiesFile);
            log.info("Properties loaded : {}", properties);
        } catch (final IOException e) {
            log.error("Failure while loading Properties file {}", propertiesFile, e);
            properties = new Properties();
        }

        final ApplicationConfig applicationConfig = ConfigFactory.create(ApplicationConfig.class, properties);
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
