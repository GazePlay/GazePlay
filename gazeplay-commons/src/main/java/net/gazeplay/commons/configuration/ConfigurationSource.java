package net.gazeplay.commons.configuration;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.*;
import java.util.Properties;

@Slf4j
public class ConfigurationSource {

    private static final String defaultProfileConfigPath = GazePlayDirectories.getGazePlayFolder() + "GazePlay.properties";

    private static String getSpecificProfileConfigPath(String profileId) {
        return GazePlayDirectories.getUserProfileDirectory(profileId) + GazePlayDirectories.FILESEPARATOR + "GazePlay.properties";
    }

    public static Configuration createFromDefaultProfile() {
        return createFromPropertiesResource(new File(defaultProfileConfigPath));
    }

    public static Configuration createFromProfile(String profileId) {
        return createFromPropertiesResource(new File(getSpecificProfileConfigPath(profileId)));
    }

    public static Configuration createFromPropertiesResource(File propertiesFile) {
        Properties properties = null;
        try {
            log.info("loading new properties from : {}", propertiesFile);
            properties = loadProperties(propertiesFile);
            log.info("Properties loaded : {}", properties);
        } catch (FileNotFoundException e) {
            log.warn("Config file not found : {}", propertiesFile);
        } catch (IOException e) {
            log.error("Failure while loading config file {}", propertiesFile, e);
        }
        final Configuration config = new Configuration(propertiesFile);
        if (properties != null) {
            config.populateFromProperties(properties);
        }
        return config;
    }

    private static Properties loadProperties(File propertiesFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(propertiesFile)) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }

}
