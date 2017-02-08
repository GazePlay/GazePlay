package gaze.Configuration;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by schwab on 24/10/2016.
 */

public class Configuration {

    public String gazeMode = "true";

    public Configuration() {

        final Properties prop = new Properties();
        InputStream input = null;

        try {

            String fileSeparator = System.getProperties().getProperty("file.separator");

            String configPath = System.getProperties().getProperty("user.home")+ fileSeparator +"GazePlay"+fileSeparator+"TET.properties";

            System.out.println(configPath);

            input = new FileInputStream(configPath);

            // load a properties file
            prop.load(input);

            String buffer = prop.getProperty("GazeMode");
            if(buffer!=null)
                gazeMode = buffer.toLowerCase();


        } catch (final IOException ex) {
            ex.printStackTrace();
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