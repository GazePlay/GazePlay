package gaze.Configuration;


import utils.games.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by schwab on 24/10/2016.
 */

public class Configuration {

    public String gazeMode = "true";
    public String eyetracker = "tobii";
    public String language = "eng";

    public Configuration() {

        final Properties prop = new Properties();
        InputStream input = null;

        String configPath = Utils.getGazePlayFolder()+"GazePlay.properties";

        try {
            input = new FileInputStream(configPath);

            // load a properties file
            prop.load(input);

            String buffer = prop.getProperty("GazeMode");
            if(buffer!=null)
                gazeMode = buffer.toLowerCase();

            buffer = prop.getProperty("EyeTracker");
            if(buffer!=null)
                eyetracker = buffer.toLowerCase();

            buffer = prop.getProperty("Language");
            if(buffer!=null)
                language = buffer.toLowerCase();

        } catch (final IOException ex) {
            System.out.println(configPath + " not found");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}