package net.gazeplay.commons.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.EyeTracker;

import java.util.Properties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import static net.gazeplay.commons.themes.BuiltInUiTheme.DEFAULT_THEME;
import net.gazeplay.commons.utils.games.Utils;

@Slf4j
public class Configuration implements Cloneable {

    private static final String PROPERTY_NAME_GAZEMODE = "GAZEMODE";
    private static final String PROPERTY_NAME_EYETRACKER = "EYETRACKER";
    private static final String PROPERTY_NAME_LANGUAGE = "LANGUAGE";
    private static final String PROPERTY_NAME_FILEDIR = "FILEDIR";
    private static final String PROPERTY_NAME_FIXATIONLENGTH = "FIXATIONLENGTH";
    private static final String PROPERTY_NAME_CSSFILE = "CSSFILE";
    private static final String PROPERTY_NAME_WHEREISIT_DIR = "WHEREISITDIR";
    private static final String PROPERTY_NAME_QUESTION_LENGTH = "QUESTIONLENGTH";
    private static final String PROPERTY_NAME_ENABLE_REWARD_SOUND = "ENABLE_REWARD_SOUND";
    private static final String PROPERTY_NAME_MENU_BUTTONS_ORIENTATION = "MENU_BUTTONS_ORIENTATION";
    private static final String PROPERTY_NAME_HEATMAP_DISABLED = "HEATMAP_DISABLED";
    private static final String PROPERTY_NAME_MUSIC_VOLUME = "MUSIC_VOLUME";
    private static final String PROPERTY_NAME_MUSIC_FOLDER = "MUSIC_FOLDER";
    private static final String PROPERTY_NAME_EFFECTS_VOLUME = "EFFECTS_VOLUME";

    private static final String CONFIGPATH = Utils.getGazePlayFolder() + "GazePlay.properties";

    private static final boolean DEFAULT_VALUE_GAZEMODE = true;
    private static final String DEFAULT_VALUE_EYETRACKER = EyeTracker.mouse_control.toString();
    private static final String DEFAULT_VALUE_LANGUAGE = "fra";
    private static final int DEFAULT_VALUE_FIXATION_LENGTH = 500;
    private static final String DEFAULT_VALUE_CSS_FILE = DEFAULT_THEME.getPreferredConfigPropertyValue();
    private static final String DEFAULT_VALUE_WHEREISIT_DIR = "";
    private static final int DEFAULT_VALUE_QUESTION_LENGTH = 5000;
    private static final boolean DEFAULT_VALUE_ENABLE_REWARD_SOUND = true;
    private static final String DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION = "HORIZONTAL";
    private static final boolean DEFAULT_VALUE_HEATMAP_DISABLED = false;
    private static final double DEFAULT_VALUE_MUSIC_VOLUME = 0.25;
    private static final String DEFAULT_VALUE_MUSIC_FOLDER = "data" + File.separator + "home" + File.separator
            + "sounds";
    private static final Double DEFAULT_VALUE_EFFECTS_VOLUME = DEFAULT_VALUE_MUSIC_VOLUME;

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

    private static Configuration createFromPropertiesResource() {
        log.info("DEBUG: CONFIG BEGIN");
        Properties properties;
        try {
            log.info("Loading properties : config path : {}", CONFIGPATH);
            properties = loadProperties(CONFIGPATH);
        } catch (FileNotFoundException e) {
            log.warn("Config file not found : {}", CONFIGPATH);
            properties = null;
        } catch (IOException e) {
            log.error("Failure while loading config file {}", CONFIGPATH, e);
            properties = null;
        }
        final Configuration config = new Configuration();
        if (properties != null) {
            log.info("Properties loaded : {}", properties);
            config.populateFromProperties(properties);
        }
        log.info("DEBUG: CONFIG END", properties);
        return config;
    }

    @Getter
    private static final Configuration instance = Configuration.createFromPropertiesResource();

    @Getter
    protected final BooleanProperty gazeModeProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZEMODE,
            DEFAULT_VALUE_GAZEMODE);

    @Getter
    protected final StringProperty eyetrackerProperty = new SimpleStringProperty(this, PROPERTY_NAME_EYETRACKER,
            DEFAULT_VALUE_EYETRACKER);

    @Getter
    protected final StringProperty languageProperty = new SimpleStringProperty(this, PROPERTY_NAME_LANGUAGE,
            DEFAULT_VALUE_LANGUAGE);

    @Getter
    protected final StringProperty filedirProperty = new SimpleStringProperty(this, PROPERTY_NAME_FILEDIR,
            getFileDirectoryDefaultValue());

    @Getter
    protected final IntegerProperty fixationlengthProperty = new SimpleIntegerProperty(this,
            PROPERTY_NAME_FIXATIONLENGTH, DEFAULT_VALUE_FIXATION_LENGTH);

    @Getter
    protected final StringProperty cssfileProperty = new SimpleStringProperty(this, PROPERTY_NAME_CSSFILE,
            DEFAULT_VALUE_CSS_FILE);

    @Getter
    protected final StringProperty whereIsItDirProperty = new SimpleStringProperty(this, PROPERTY_NAME_WHEREISIT_DIR,
            DEFAULT_VALUE_WHEREISIT_DIR);

    @Getter
    protected final IntegerProperty questionLengthProperty = new SimpleIntegerProperty(this,
            PROPERTY_NAME_QUESTION_LENGTH, DEFAULT_VALUE_QUESTION_LENGTH);

    @Getter
    protected final BooleanProperty enableRewardSoundProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_ENABLE_REWARD_SOUND, DEFAULT_VALUE_ENABLE_REWARD_SOUND);

    @Getter
    protected final StringProperty menuButtonsOrientationProperty = new SimpleStringProperty(this,
            PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION);

    @Getter
    protected final BooleanProperty heatMapDisabledProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_HEATMAP_DISABLED, DEFAULT_VALUE_HEATMAP_DISABLED);

    @Getter
    protected final DoubleProperty musicVolumeProperty = new SimpleDoubleProperty(this, PROPERTY_NAME_MUSIC_VOLUME,
            DEFAULT_VALUE_MUSIC_VOLUME);

    @Getter
    protected final StringProperty musicFolderProperty = new SimpleStringProperty(this, PROPERTY_NAME_MUSIC_FOLDER,
            DEFAULT_VALUE_MUSIC_FOLDER);

    @Getter
    protected final DoubleProperty effectsVolumeProperty = new SimpleDoubleProperty(this, PROPERTY_NAME_EFFECTS_VOLUME,
            DEFAULT_VALUE_EFFECTS_VOLUME);

    private Configuration() {

        // Listeners
        musicVolumeProperty.addListener((observable) -> {
            double musicVolume = getMusicVolume();
            if (musicVolume > 1) {
                log.warn("Invalid msuic volume value set : {}. 1 set instead", musicVolume);
                musicVolumeProperty.setValue(1);
            } else if (musicVolume < 0) {
                log.warn("Invalid msuic volume value set : {}. 0 set instead", musicVolume);
                musicVolumeProperty.setValue(0);
            }
        });

        effectsVolumeProperty.addListener((observable) -> {
            double musicVolume = getMusicVolume();
            if (musicVolume > 1) {
                log.warn("Invalid effects volume value set : {}. 1 set instead", musicVolume);
                effectsVolumeProperty.setValue(1);
            } else if (musicVolume < 0) {
                log.warn("Invalid effects volume value set : {}. 0 set instead", musicVolume);
                effectsVolumeProperty.setValue(0);
            }
        });

    }

    public void populateFromProperties(Properties prop) {
        String buffer;

        buffer = prop.getProperty(PROPERTY_NAME_GAZEMODE);
        if (buffer != null) {
            gazeModeProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_EYETRACKER);
        if (buffer != null) {
            eyetrackerProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_LANGUAGE);
        if (buffer != null) {
            languageProperty.setValue(buffer.toLowerCase());
        }

        buffer = prop.getProperty(PROPERTY_NAME_FILEDIR);
        if (buffer != null) {
            filedirProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_FIXATIONLENGTH);
        if (buffer != null) {
            try {
                fixationlengthProperty.setValue(Integer.parseInt(buffer));
            } catch (NumberFormatException e) {
                log.warn("NumberFormatException while parsing value '{}' for property {}", buffer,
                        PROPERTY_NAME_FIXATIONLENGTH);
            }
        }

        buffer = prop.getProperty(PROPERTY_NAME_CSSFILE);
        if (buffer != null) {
            cssfileProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_WHEREISIT_DIR);
        if (buffer != null) {
            whereIsItDirProperty.setValue(buffer.toLowerCase());
        }

        buffer = prop.getProperty(PROPERTY_NAME_QUESTION_LENGTH);
        if (buffer != null) {
            try {
                questionLengthProperty.setValue(Integer.parseInt(buffer));
            } catch (NumberFormatException e) {
                log.warn("NumberFormatException while parsing value '{}' for property {}", buffer,
                        PROPERTY_NAME_QUESTION_LENGTH);
            }
        }

        buffer = prop.getProperty(PROPERTY_NAME_ENABLE_REWARD_SOUND);
        if (buffer != null) {
            enableRewardSoundProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_MENU_BUTTONS_ORIENTATION);
        if (buffer != null) {
            menuButtonsOrientationProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_HEATMAP_DISABLED);
        if (buffer != null) {
            heatMapDisabledProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_MUSIC_VOLUME);
        if (buffer != null) {
            musicVolumeProperty.setValue(Double.parseDouble(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_MUSIC_FOLDER);
        if (buffer != null) {
            musicFolderProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_EFFECTS_VOLUME);
        if (buffer != null) {
            try {
                effectsVolumeProperty.setValue(Double.parseDouble(buffer));
            } catch (NumberFormatException e) {
                log.warn("Malformed property");
            }
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

        properties.setProperty(PROPERTY_NAME_EYETRACKER, this.eyetrackerProperty.getValue());
        properties.setProperty(PROPERTY_NAME_LANGUAGE, this.languageProperty.getValue());
        properties.setProperty(PROPERTY_NAME_FILEDIR, this.filedirProperty.getValue());
        properties.setProperty(PROPERTY_NAME_FIXATIONLENGTH, Integer.toString(this.fixationlengthProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_CSSFILE, this.cssfileProperty.getValue());
        properties.setProperty(PROPERTY_NAME_WHEREISIT_DIR, this.whereIsItDirProperty.getValue());
        properties.setProperty(PROPERTY_NAME_QUESTION_LENGTH, Integer.toString(this.questionLengthProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_ENABLE_REWARD_SOUND,
                Boolean.toString(this.enableRewardSoundProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, this.menuButtonsOrientationProperty.getValue());
        properties.setProperty(PROPERTY_NAME_HEATMAP_DISABLED,
                Boolean.toString(this.heatMapDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MUSIC_VOLUME, Double.toString(this.musicVolumeProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MUSIC_FOLDER, this.musicFolderProperty.getValue());
        properties.setProperty(PROPERTY_NAME_EFFECTS_VOLUME, Double.toString(effectsVolumeProperty.getValue()));

        return properties;
    }

    public void saveConfig() throws IOException {
        Properties properties = toProperties();
        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(CONFIGPATH))) {
            String fileComment = "Automatically generated by GazePlay";
            properties.store(fileOutputStream, fileComment);
        }
        // log.info("Properties saved : {}", properties);
    }

    public void saveConfigIgnoringExceptions() {
        try {
            saveConfig();
        } catch (IOException e) {
            log.error("Exception while writing configuration to file {}", CONFIGPATH, e);
        }
    }

    // Simpler getter

    public Boolean getGazeMode() {
        return gazeModeProperty.getValue();
    }

    public String getEyeTracker() {
        return eyetrackerProperty.getValue();
    }

    public String getLanguage() {
        return languageProperty.getValue();
    }

    public String getFileDir() {
        return filedirProperty.getValue();
    }

    public Integer getFixationLength() {
        return fixationlengthProperty.getValue();
    }

    public String getCssFile() {
        return cssfileProperty.getValue();
    }

    public String getWhereIsItDir() {
        return whereIsItDirProperty.getValue();
    }

    public Integer getQuestionLength() {
        return questionLengthProperty.getValue();
    }

    public Boolean isEnableRewardSound() {
        return enableRewardSoundProperty.getValue();
    }

    public String getMenuButtonsOrientation() {
        return menuButtonsOrientationProperty.getValue();
    }

    public Boolean isHeatMapDisabled() {
        return heatMapDisabledProperty.getValue();
    }

    public Double getMusicVolume() {
        return musicVolumeProperty.getValue();
    }

    public String getMusicFolder() {
        return musicFolderProperty.getValue();
    }

    public Double getEffectsVolume() {
        return effectsVolumeProperty.getValue();
    }
}
