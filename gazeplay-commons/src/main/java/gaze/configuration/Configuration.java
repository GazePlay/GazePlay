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

    private static String GAZEMODE = "GAZEMODE";
    private static String EYETRACKER = "EYETRACKER";
    private static String LANGUAGE = "LANGUAGE";
    private static String FILEDIR = "FILEDIR";
    private static String CONFIGPATH = Utils.getGazePlayFolder() + "GazePlay.properties";
    private static String FIXATIONLENGTH = "FixationLength";
    private static String CSSFILE ="CssFile";


    public String gazeMode = "true";
    public String eyetracker = "none";
    public String language = "fra";
    public String filedir = Utils.getGazePlayFolder() + "files" + Utils.FILESEPARATOR;
    private double fixationlength=0.5;
    private String cssfile="";

    public Configuration() {

        final Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(CONFIGPATH);

            // load a properties file
            prop.load(input);

            String buffer = prop.getProperty(GAZEMODE);
            if (buffer != null)
                gazeMode = buffer.toLowerCase();

            buffer = prop.getProperty(EYETRACKER);
            if (buffer != null)
                eyetracker = buffer.toLowerCase();

            buffer = prop.getProperty(LANGUAGE);
            if (buffer != null)
                language = buffer.toLowerCase();

            buffer = prop.getProperty(FILEDIR);
            if (buffer != null)
                filedir = buffer;

            buffer = prop.getProperty(FIXATIONLENGTH);
            if (buffer != null) {
                try {
                    fixationlength = Double.valueOf(buffer);
                } catch (NumberFormatException e) {
                    log.info("NumberFormatException " + buffer);
                }

            }

            buffer = prop.getProperty(CSSFILE);
            if (buffer != null)
                cssfile = buffer;

        } catch (final IOException ex) {
            log.info(CONFIGPATH + " not found");
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

    public Configuration(String eyetracker, String LANGUAGE) {

        this.eyetracker = eyetracker;
        this.language = LANGUAGE;
    }

    public Configuration(String eyetracker) {

        this.eyetracker = eyetracker;
    }

    public void saveConfig() {

        StringBuilder sb = new StringBuilder(1000);
        sb.append(EYETRACKER);
        sb.append('=');
        sb.append(eyetracker);
        sb.append(Utils.LINESEPARATOR);

        sb.append(LANGUAGE);
        sb.append('=');
        sb.append(language);
        sb.append(Utils.LINESEPARATOR);

        sb.append(FILEDIR);
        sb.append('=');
        sb.append(filedir);
        sb.append(Utils.LINESEPARATOR);

        sb.append(FIXATIONLENGTH);
        sb.append('=');
        sb.append(fixationlength);
        sb.append(Utils.LINESEPARATOR);

        sb.append(CSSFILE);
        sb.append('=');
        sb.append(cssfile);
        sb.append(Utils.LINESEPARATOR);

        Utils.save(sb.toString(), new File(CONFIGPATH));
    }

}
