package net.gazeplay.commons.configuration;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.utils.games.Utils;

import java.io.*;
import java.util.Properties;

import static net.gazeplay.commons.themes.BuiltInUiTheme.DEFAULT_THEME;

@Slf4j
public class ConfigurationBuilder implements Cloneable {

    private static String PROPERTY_NAME_GAZEMODE = "GAZEMODE";
    private static String PROPERTY_NAME_EYETRACKER = "EYETRACKER";
    private static String PROPERTY_NAME_LANGUAGE = "LANGUAGE";
    private static String PROPERTY_NAME_FILEDIR = "FILEDIR";
    private static String PROPERTY_NAME_FIXATIONLENGTH = "FIXATIONLENGTH";
    private static String PROPERTY_NAME_CSSFILE = "CSSFILE";
    private static String PROPERTY_NAME_WHEREISIT_DIR = "WHEREISITDIR";
    private static String PROPERTY_NAME_QUESTION_LENGTH = "QUESTIONLENGTH";
    private static final String PROPERTY_NAME_ENABLE_REWARD_SOUND = "ENABLE_REWARD_SOUND";
    private static final String PROPERTY_NAME_MENU_BUTTONS_ORIENTATION = "MENU_BUTTONS_ORIENTATION";

    private static String CONFIGPATH = Utils.getGazePlayFolder() + "GazePlay.properties";

    private static boolean DEFAULT_VALUE_GAZEMODE = true;
    private static String DEFAULT_VALUE_EYETRACKER = EyeTracker.mouse_control.toString();
    private static String DEFAULT_VALUE_LANGUAGE = "fra";
    private static int DEFAULT_VALUE_FIXATION_LENGTH = 500;
    private static String DEFAULT_VALUE_CSS_FILE = DEFAULT_THEME.getPreferredConfigPropertyValue();
    private static String DEFAULT_VALUE_WHEREISIT_DIR = "";
    private static int DEFAULT_VALUE_QUESTION_LENGTH = 5000;
    public static final boolean DEFAULT_VALUE_ENABLE_REWARD_SOUND = true;

    private static String getFileDirectoryDefaultValue() {
        return Utils.getGazePlayFolder() + "files" /* + Utils.FILESEPARATOR */;
    }

    private static Properties loadProperties(String propertiesFilePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(propertiesFilePath)) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }

    public static ConfigurationBuilder createFromPropertiesResource() {
        Properties properties;
        try {
            properties = loadProperties(CONFIGPATH);
        } catch (FileNotFoundException e) {
            log.warn("Config file not found : {}", CONFIGPATH);
            properties = null;
        } catch (IOException e) {
            log.error("Failure while loading config file {}", CONFIGPATH, e);
            properties = null;
        }
        ConfigurationBuilder builder = new ConfigurationBuilder();
        if (properties != null) {
            log.info("Properties loaded : {}", properties);
            builder.populateFromProperties(properties);
        }
        return builder;
    }

    protected boolean gazeMode = DEFAULT_VALUE_GAZEMODE;

    protected String eyetracker = DEFAULT_VALUE_EYETRACKER;

    protected String language = DEFAULT_VALUE_LANGUAGE;

    protected String filedir = getFileDirectoryDefaultValue();

    protected Integer fixationlength = DEFAULT_VALUE_FIXATION_LENGTH;

    protected String cssfile = DEFAULT_VALUE_CSS_FILE;

    protected String whereIsItDir = DEFAULT_VALUE_WHEREISIT_DIR;

    protected int questionLength = DEFAULT_VALUE_QUESTION_LENGTH;

    protected boolean enableRewardSound = DEFAULT_VALUE_ENABLE_REWARD_SOUND;

    protected String menuButtonsOrientation;

    public ConfigurationBuilder() {

    }

    private ConfigurationBuilder copy() {
        /*
         * try { return (ConfigurationBuilder) super.clone(); } catch (CloneNotSupportedException e) { throw new
         * RuntimeException(e); }
         */
        return this;
    }

    public ConfigurationBuilder withGazeMode(Boolean value) {
        ConfigurationBuilder copy = copy();
        copy.gazeMode = value;
        return copy;
    }

    public ConfigurationBuilder withEyeTracker(String value) {
        ConfigurationBuilder copy = copy();
        copy.eyetracker = value;
        return copy;
    }

    public ConfigurationBuilder withLanguage(String value) {
        ConfigurationBuilder copy = copy();
        copy.language = value;
        return copy;
    }

    public ConfigurationBuilder withFileDir(String value) {
        ConfigurationBuilder copy = copy();
        copy.filedir = value;
        return copy;
    }

    public ConfigurationBuilder withFixationLength(Integer value) {
        ConfigurationBuilder copy = copy();
        copy.fixationlength = value;
        return copy;
    }

    public ConfigurationBuilder withCssFile(String value) {
        ConfigurationBuilder copy = copy();
        copy.cssfile = value;
        return copy;
    }

    public ConfigurationBuilder withWhereIsItDir(String value) {
        ConfigurationBuilder copy = copy();
        copy.whereIsItDir = value;
        return copy;
    }

    public ConfigurationBuilder withQuestionLength(int value) {
        ConfigurationBuilder copy = copy();
        copy.questionLength = value;
        return copy;
    }

    public ConfigurationBuilder withEnableRewardSound(Boolean value) {
        ConfigurationBuilder copy = copy();
        copy.enableRewardSound = value;
        return copy;
    }

    public ConfigurationBuilder withMenuButtonsOrientation(String value) {
        ConfigurationBuilder copy = copy();
        copy.menuButtonsOrientation = value;
        return copy;
    }

    public void populateFromProperties(Properties prop) {
        String buffer;

        buffer = prop.getProperty(PROPERTY_NAME_GAZEMODE);
        if (buffer != null) {
            gazeMode = Boolean.parseBoolean(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_EYETRACKER);
        if (buffer != null) {
            eyetracker = buffer;
        }

        buffer = prop.getProperty(PROPERTY_NAME_LANGUAGE);
        if (buffer != null) {
            language = buffer.toLowerCase();
        }

        buffer = prop.getProperty(PROPERTY_NAME_FILEDIR);
        if (buffer != null) {
            filedir = buffer;
        }

        buffer = prop.getProperty(PROPERTY_NAME_FIXATIONLENGTH);
        if (buffer != null) {
            try {
                fixationlength = Integer.parseInt(buffer);
            } catch (NumberFormatException e) {
                log.warn("NumberFormatException while parsing value '{}' for property {}", buffer,
                        PROPERTY_NAME_FIXATIONLENGTH);
            }
        }

        buffer = prop.getProperty(PROPERTY_NAME_CSSFILE);
        if (buffer != null) {
            cssfile = buffer;
        }

        buffer = prop.getProperty(PROPERTY_NAME_WHEREISIT_DIR);
        if (buffer != null) {
            whereIsItDir = buffer.toLowerCase();
        }

        buffer = prop.getProperty(PROPERTY_NAME_QUESTION_LENGTH);
        if (buffer != null) {
            try {
                questionLength = Integer.parseInt(buffer);
            } catch (NumberFormatException e) {
                log.warn("NumberFormatException while parsing value '{}' for property {}", buffer,
                        PROPERTY_NAME_QUESTION_LENGTH);
            }
        }

        buffer = prop.getProperty(PROPERTY_NAME_ENABLE_REWARD_SOUND);
        if (buffer != null) {
            enableRewardSound = Boolean.parseBoolean(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_MENU_BUTTONS_ORIENTATION);
        if (buffer != null) {
            menuButtonsOrientation = buffer;
        }

    }

    private Properties toProperties() {
        Properties properties = new Properties() {

            @Override
            public Object setProperty(String key, String value) {
                if (value == null) {
                    return this.remove(key);
                }
                return super.setProperty(key, value);
            }

        };

        // FIXME why is this not saved to file ? -> Certainly no longer usefull (see issue #102)
        // properties.setProperty(PROPERTY_NAME_GAZEMODE, this.gazeMode);

        properties.setProperty(PROPERTY_NAME_EYETRACKER, this.eyetracker);
        properties.setProperty(PROPERTY_NAME_LANGUAGE, this.language);
        properties.setProperty(PROPERTY_NAME_FILEDIR, this.filedir);
        properties.setProperty(PROPERTY_NAME_FIXATIONLENGTH, Integer.toString(this.fixationlength));
        properties.setProperty(PROPERTY_NAME_CSSFILE, this.cssfile);
        properties.setProperty(PROPERTY_NAME_WHEREISIT_DIR, this.whereIsItDir);
        properties.setProperty(PROPERTY_NAME_QUESTION_LENGTH, Integer.toString(this.questionLength));
        properties.setProperty(PROPERTY_NAME_ENABLE_REWARD_SOUND, Boolean.toString(this.enableRewardSound));
        properties.setProperty(PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, this.menuButtonsOrientation);

        return properties;
    }

    public Configuration build() {
        Configuration configuration = new Configuration(this);
        return configuration;
    }

    public void saveConfig() throws IOException {
        Properties properties = toProperties();
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(CONFIGPATH))) {
            String fileComment = "Automatically generated by GazePlay";
            properties.store(fileOutputStream, fileComment);
        }
        log.info("Properties saved : {}", properties);
    }

    public void saveConfigIgnoringExceptions() {
        try {
            saveConfig();
        } catch (IOException e) {
            log.error("Exception while writing configuration to file {}", CONFIGPATH, e);
        }
    }
}
