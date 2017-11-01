package gaze.configuration;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import utils.games.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by schwab on 24/10/2016.
 */
@ToString
@Slf4j
public class Configuration {

    private static String GazeMode = "GazeMode";
    private static String EyeTracker = "EyeTracker";
    private static String Language = "Language";
    private static String FileDir = "FileDir";
    private static String configPath = Utils.getGazePlayFolder() + "GazePlay.properties";

    public String gazeMode = "true";
    public String eyetracker = "none";
    public String language = "fra";
    public String filedir = Utils.getGazePlayFolder() + "files" + Utils.FILESEPARATOR;

    public Configuration() {

        final Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(configPath);

            // load a properties file
            prop.load(input);

            String buffer = prop.getProperty(GazeMode);
            if (buffer != null)
                gazeMode = buffer.toLowerCase();

            buffer = prop.getProperty(EyeTracker);
            if (buffer != null)
                eyetracker = buffer.toLowerCase();

            buffer = prop.getProperty(Language);
            if (buffer != null)
                language = buffer.toLowerCase();

            buffer = prop.getProperty(FileDir);
            if (buffer != null)
                filedir = buffer;

        } catch (final IOException ex) {
            log.info(configPath + " not found");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (final IOException e) {
                    log.error("Exception", e);
                }
            }
        }
    }

    public Configuration(String eyetracker, String language) {

        this.eyetracker = eyetracker;
        this.language = language;
    }

    public Configuration(String eyetracker) {

        this.eyetracker = eyetracker;
    }

    public void saveConfig() {

        StringBuilder sb = new StringBuilder(1000);
        sb.append(EyeTracker);
        sb.append('=');
        sb.append(eyetracker);
        sb.append(Utils.LINESEPARATOR);

        sb.append(Language);
        sb.append('=');
        sb.append(language);
        sb.append(Utils.LINESEPARATOR);

        sb.append(FileDir);
        sb.append('=');
        sb.append(filedir);
        sb.append(Utils.LINESEPARATOR);

        Utils.save(sb.toString(), new File(configPath));
    }

}
