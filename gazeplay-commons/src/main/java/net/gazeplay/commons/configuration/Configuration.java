package net.gazeplay.commons.configuration;

import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.utils.games.Utils;

import java.io.*;
import java.util.Properties;

import static net.gazeplay.commons.themes.BuiltInUiTheme.DEFAULT_THEME;

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
    private static final String PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED = "FIXATIONSEQUENCE_DISABLED";
    private static final String PROPERTY_NAME_MUSIC_VOLUME = "MUSIC_VOLUME";
    private static final String PROPERTY_NAME_MUSIC_FOLDER = "MUSIC_FOLDER";
    private static final String PROPERTY_NAME_EFFECTS_VOLUME = "EFFECTS_VOLUME";
    private static final String PROPERTY_NAME_GAZE_MENU = "GAZE_MENU";
    private static final String PROPERTY_NAME_GAZE_MOUSE = "GAZE_MOUSE";
    private static final String PROPERTY_NAME_WHITE_BCKGRD = "WHITE_BACKGROUND";
    private static final String PROPERTY_NAME_SPEED_EFFECTS = "SPEED_EFFECTS";
    private static final String PROPERTY_NAME_USER_NAME = "USER_NAME";
    private static final String PROPERTY_NAME_USER_PICTURE = "USER_PICTURE";
    private static final String PROPERTY_NAME_QUIT_KEY = "QUIT_KEY";
    /*
     * Game Categories Properties
     */
    private static final String PROPERTY_NAME_TARGET_GAMES = "Target games";
    private static final String PROPERTY_NAME_SEARCHING_GAMES = "Searching games";
    private static final String PROPERTY_NAME_MEMORIZATION_GAMES = "Memorization games";
    private static final String PROPERTY_NAME_NO_CATEGORY_GAMES = "No category games";

    @Getter
    @Setter
    private static String CONFIGPATH = Utils.getGazePlayFolder() + "GazePlay.properties";
    private static final KeyCode DEFAULT_VALUE_QUIT_KEY = KeyCode.Q;
    private static final boolean DEFAULT_VALUE_GAZEMODE = true;
    private static final String DEFAULT_VALUE_EYETRACKER = EyeTracker.mouse_control.toString();
    private static final String DEFAULT_VALUE_LANGUAGE = "fra";
    private static final int DEFAULT_VALUE_FIXATION_LENGTH = 500;
    private static final String DEFAULT_VALUE_CSS_FILE = DEFAULT_THEME.getPreferredConfigPropertyValue();
    public static final String DEFAULT_VALUE_WHEREISIT_DIR = "";
    private static final int DEFAULT_VALUE_QUESTION_LENGTH = 5000;
    private static final boolean DEFAULT_VALUE_ENABLE_REWARD_SOUND = true;
    private static final String DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION = "HORIZONTAL";
    private static final boolean DEFAULT_VALUE_HEATMAP_DISABLED = false;
    private static final boolean DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED = false;
    public static final double DEFAULT_VALUE_MUSIC_VOLUME = 0.25;
    public static final String DEFAULT_VALUE_MUSIC_FOLDER = "";
    private static final Double DEFAULT_VALUE_EFFECTS_VOLUME = DEFAULT_VALUE_MUSIC_VOLUME;
    private static final boolean DEFAULT_VALUE_TARGET_GAMES = true;
    private static final boolean DEFAULT_VALUE_MEMORIZATION_GAMES = true;
    private static final boolean DEFAULT_VALUE_SEARCHING_GAMES = true;
    private static final boolean DEFAULT_VALUE_NO_CATEGORY_GAMES = true;
    // next thing to do
    // private static final String DEFAULT_EXIT_SHORTCUT_KEY = "SPACE";
    @Setter
    @Getter
    public static String DEFAULT_VALUE_FILE_DIR = getFileDirectoryDefaultValue();
    public static final boolean DEFAULT_VALUE_GAZE_MENU = false;
    public static final boolean DEFAULT_VALUE_GAZE_MOUSE = false;
    public static final boolean DEFAULT_VALUE_WHITE_BCKGRD = false;
    public static final double DEFAULT_VALUE_SPEED_EFFECTS = 4;
    private static final String DEFAULT_VALUE_USER_NAME = "";
    public static final String DEFAULT_VALUE_USER_PICTURE = "";

    public static boolean isMouseFree = false;

    private static String getFileDirectoryDefaultValue() {
        return Utils.getGazePlayFolder() + "files" /* + Utils.FILESEPARATOR */;
    }

    public static String getFileDirectoryUserValue(String user) {
        return Utils.getGazePlayFolder() + "profiles/" + user + Utils.FILESEPARATOR
                + "files" /* + Utils.FILESEPARATOR */;
    }

    private static Properties loadProperties(String propertiesFilePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(propertiesFilePath)) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }

    public static Configuration createFromPropertiesResource() {
        Properties properties;
        try {
            log.info("loading new properties from ={}", CONFIGPATH);
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
        return config;
    }

    public Configuration reset() {
        return Configuration.createFromPropertiesResource();
    }

    /*
     * public static final Configuration getInstance() { return Configuration.createFromPropertiesResource(); }
     */

    @Getter
    @Setter
    private static Configuration instance = Configuration.createFromPropertiesResource();

    @Getter
    protected final StringProperty QuitKeyProperty = new SimpleStringProperty(this, PROPERTY_NAME_QUIT_KEY,
            DEFAULT_VALUE_QUIT_KEY.toString());
    @Getter
    protected final BooleanProperty gazeModeProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZEMODE,
            DEFAULT_VALUE_GAZEMODE);

    @Getter
    protected final BooleanProperty gazeMenuProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZE_MENU,
            DEFAULT_VALUE_GAZE_MENU);

    @Getter
    protected final StringProperty eyetrackerProperty = new SimpleStringProperty(this, PROPERTY_NAME_EYETRACKER,
            DEFAULT_VALUE_EYETRACKER);

    @Getter
    protected final StringProperty languageProperty = new SimpleStringProperty(this, PROPERTY_NAME_LANGUAGE,
            DEFAULT_VALUE_LANGUAGE);

    @Getter
    protected final StringProperty filedirProperty = new SimpleStringProperty(this, PROPERTY_NAME_FILEDIR,
            DEFAULT_VALUE_FILE_DIR);

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
    protected final BooleanProperty fixationSequenceDisabledProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED, DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED);
    @Getter
    protected final BooleanProperty gazeMouseProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZE_MOUSE,
            DEFAULT_VALUE_GAZE_MOUSE);

    @Getter
    protected final BooleanProperty whiteBackgroundProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_WHITE_BCKGRD, DEFAULT_VALUE_WHITE_BCKGRD);

    @Getter
    protected final DoubleProperty musicVolumeProperty = new SimpleDoubleProperty(this, PROPERTY_NAME_MUSIC_VOLUME,
            DEFAULT_VALUE_MUSIC_VOLUME);

    @Getter
    protected final StringProperty musicFolderProperty = new SimpleStringProperty(this, PROPERTY_NAME_MUSIC_FOLDER,
            DEFAULT_VALUE_MUSIC_FOLDER);

    @Getter
    protected final DoubleProperty effectsVolumeProperty = new SimpleDoubleProperty(this, PROPERTY_NAME_EFFECTS_VOLUME,
            DEFAULT_VALUE_EFFECTS_VOLUME);

    @Getter
    protected final DoubleProperty speedEffectsProperty = new SimpleDoubleProperty(this, PROPERTY_NAME_SPEED_EFFECTS,
            DEFAULT_VALUE_SPEED_EFFECTS);

    @Getter
    protected final StringProperty userNameProperty = new SimpleStringProperty(this, PROPERTY_NAME_USER_NAME,
            DEFAULT_VALUE_USER_NAME);
    @Getter
    protected final StringProperty userPictureProperty = new SimpleStringProperty(this, PROPERTY_NAME_USER_PICTURE,
            DEFAULT_VALUE_USER_PICTURE);

    @Getter
    protected final BooleanProperty targetCategoryProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_TARGET_GAMES,
            DEFAULT_VALUE_TARGET_GAMES);

    @Getter
    protected final BooleanProperty memorizationCategoryProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_MEMORIZATION_GAMES, DEFAULT_VALUE_MEMORIZATION_GAMES);

    @Getter
    protected final BooleanProperty searchingCategoryProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_SEARCHING_GAMES, DEFAULT_VALUE_SEARCHING_GAMES);

    @Getter
    protected final BooleanProperty noCategoryProperty = new SimpleBooleanProperty(this,
            PROPERTY_NAME_NO_CATEGORY_GAMES, DEFAULT_VALUE_NO_CATEGORY_GAMES);

    protected Configuration() {

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

        buffer = prop.getProperty(PROPERTY_NAME_QUIT_KEY);
        if (buffer != null) {
            QuitKeyProperty.setValue(buffer);
        }

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

        buffer = prop.getProperty(PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED);
        if (buffer != null) {
            fixationSequenceDisabledProperty.setValue(Boolean.parseBoolean(buffer));
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

        buffer = prop.getProperty(PROPERTY_NAME_SPEED_EFFECTS);
        if (buffer != null) {
            try {
                speedEffectsProperty.setValue(Double.parseDouble(buffer));
            } catch (NumberFormatException e) {
                log.warn("Malformed property");
            }
        }

        buffer = prop.getProperty(PROPERTY_NAME_GAZE_MENU);
        if (buffer != null) {
            gazeMenuProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_GAZE_MOUSE);
        if (buffer != null) {
            gazeMouseProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_WHITE_BCKGRD);
        if (buffer != null) {
            whiteBackgroundProperty.setValue(Boolean.parseBoolean(buffer));
        }

        buffer = prop.getProperty(PROPERTY_NAME_USER_NAME);
        if (buffer != null) {
            userNameProperty.setValue(buffer);
        }

        buffer = prop.getProperty(PROPERTY_NAME_USER_PICTURE);
        if (buffer != null) {
            userPictureProperty.setValue(buffer);
        }
        buffer = prop.getProperty(PROPERTY_NAME_TARGET_GAMES);
        if (buffer != null) {
            targetCategoryProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_SEARCHING_GAMES);
        if (buffer != null) {
            searchingCategoryProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_MEMORIZATION_GAMES);
        if (buffer != null) {
            memorizationCategoryProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_NO_CATEGORY_GAMES);
        if (buffer != null) {
            noCategoryProperty.setValue(Boolean.parseBoolean(buffer));
        }

    }

    public Properties toProperties() {
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
        properties.setProperty(PROPERTY_NAME_QUIT_KEY, this.QuitKeyProperty.getValue());
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
        properties.setProperty(PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED,
                Boolean.toString(this.fixationSequenceDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MUSIC_VOLUME, Double.toString(this.musicVolumeProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MUSIC_FOLDER, this.musicFolderProperty.getValue());
        properties.setProperty(PROPERTY_NAME_EFFECTS_VOLUME, Double.toString(effectsVolumeProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_SPEED_EFFECTS, Double.toString(speedEffectsProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_WHITE_BCKGRD, Boolean.toString(whiteBackgroundProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_USER_NAME, this.userNameProperty.getValue());
        properties.setProperty(PROPERTY_NAME_USER_PICTURE, this.userPictureProperty.getValue());
        /*
         * properties.setProperty(PROPERTY_NAME_GAZE_MENU, Boolean.toString(this.gazeMenuProperty.getValue()));
         */
        properties.setProperty(PROPERTY_NAME_TARGET_GAMES, Boolean.toString(targetCategoryProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MEMORIZATION_GAMES,
                Boolean.toString(memorizationCategoryProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_SEARCHING_GAMES, Boolean.toString(searchingCategoryProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_NO_CATEGORY_GAMES, Boolean.toString(noCategoryProperty.getValue()));
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

    public String getQuitKey() {
        System.out.println(QuitKeyProperty.getValue());
        return QuitKeyProperty.getValue();
    }

    public String getLanguage() {
        return languageProperty.getValue();
    }

    public String getFileDir() {
        return filedirProperty.getValue();
    }

    public void setFileDir(String s) {
        filedirProperty.setValue(s);
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

    public Boolean isFixationSequenceDisabled() {
        return fixationSequenceDisabledProperty.getValue();
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

    public Double getSpeedEffects() {
        double modifVal = speedEffectsProperty.getValue();
        if (modifVal < 4) {
            modifVal = 1 / (5 - modifVal);
        } else {
            modifVal = modifVal - 3;
        }
        return 1 / modifVal;
    }

    public Boolean isGazeMenuEnable() {
        return gazeMenuProperty.getValue();
    }

    public Boolean isGazeMouseEnable() {
        return gazeMouseProperty.getValue();
    }

    public Boolean isBackgroundWhite() {
        return whiteBackgroundProperty.getValue();
    }

    public String getUserName() {
        return userNameProperty.getValue();
    }

    public String getUserPicture() {
        return userPictureProperty.getValue();
    }

    public void setUserName(String newName) {
        userNameProperty.setValue(newName);
    }

    public void setUserPicture(String newPicture) {
        userPictureProperty.setValue(newPicture);
    }

    public Boolean targetCategory() {
        return targetCategoryProperty.getValue();
    }

    public Boolean memorizationCategory() {
        return memorizationCategoryProperty.getValue();
    }

    public Boolean searchingCategory() {
        return searchingCategoryProperty.getValue();
    }

    public Boolean noCategory() {
        return noCategoryProperty.getValue();
    }

}
