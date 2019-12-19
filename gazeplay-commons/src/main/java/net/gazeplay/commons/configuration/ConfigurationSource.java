package net.gazeplay.commons.configuration;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.aeonbits.owner.ConfigFactory;

import java.io.*;
import java.util.Properties;

@Slf4j
public class ConfigurationSource {

    private static final File defaultProfileConfigFile = new File(GazePlayDirectories.getGazePlayFolder(), "GazePlay.properties");

    private static File getSpecificProfileConfigPath(String profileId) {
        return new File(GazePlayDirectories.getUserProfileDirectory(profileId), "GazePlay.properties");
    }

    public static Configuration createFromDefaultProfile() {
        log.info(defaultProfileConfigFile.getAbsolutePath());
        return createFromPropertiesResource(defaultProfileConfigFile);
    }

    public static Configuration createFromProfile(String profileId) {
        return createFromPropertiesResource(getSpecificProfileConfigPath(profileId));
    }

    public static Configuration createFromPropertiesResource(File propertiesFile) {
        Properties properties;
        try {
            log.info("Loading Properties from : {}", propertiesFile);
            properties = loadProperties(propertiesFile);
            log.info("Properties loaded : {}", properties);
        } catch (FileNotFoundException e) {
            log.warn("Properties file not found : {}", propertiesFile);
            properties = new Properties();
        } catch (IOException e) {
            log.error("Failure while loading Properties file {}", propertiesFile, e);
            properties = new Properties();
        }
        ApplicationConfig applicationConfig = ConfigFactory.create(ApplicationConfig.class, properties);
        log.info("*************************************************************");
        return new Configuration(propertiesFile, applicationConfig);
    }

    private static Properties loadProperties(File propertiesFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(propertiesFile)) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }

}
