package net.gazeplay.commons.configuration;

import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.utils.games.Utils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final String PROPERTY_NAME_HEATMAP_OPACITY = "HEATMAP_OPACITY";
    private static final String PROPERTY_NAME_HEATMAP_COLORS = "HEATMAP_COLORS";
    private static final String PROPERTY_NAME_AREA_OF_INTEREST_DISABLED = "AREA_OF_INTEREST_DISABLED";
    private static final String PROPERTY_NAME_CONVEX_HULL_DISABLED = "CONVEX_HULL_DISABLED";
    private static final String PROPERTY_NAME_VIDEO_RECORDING_DISABLED = "VIDEO_RECORDING_DISABLED";
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
    private static final String PROPERTY_NAME_VIDEO_FOLDER = "VIDEO_FOLDER";

    private static final String PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME = "LATEST_NEWS_POPUP_LAST_SHOWN_TIME";

    private static final String PROPERTY_NAME_FAVORITE_GAMES = "FAVORITE_GAMES";
    private static final String PROPERTY_NAME_HIDDEN_CATEGORIES = "HIDDEN_CATEGORIES";

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
    private static final double DEFAULT_VALUE_HEATMAP_OPACITY = 0.7;
    public static final String DEFAULT_VALUE_HEATMAP_COLORS = "0000FF,00FF00,FFFF00,FF0000";
    private static final boolean DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED = false;
    private static final boolean DEFAULT_VALUE_CONVEX_HULL_DISABLED = false;
    private static final boolean DEFAULT_VALUE_VIDEO_RECORDING = false;
    private static final boolean DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED = false;
    public static final double DEFAULT_VALUE_MUSIC_VOLUME = 0.25;
    public static final String DEFAULT_VALUE_MUSIC_FOLDER = "";
    private static final Double DEFAULT_VALUE_EFFECTS_VOLUME = DEFAULT_VALUE_MUSIC_VOLUME;

    @Setter
    @Getter
    public static String DEFAULT_VALUE_FILE_DIR = getFileDirectoryDefaultValue();
    private static final boolean DEFAULT_VALUE_GAZE_MENU = false;
    private static final boolean DEFAULT_VALUE_GAZE_MOUSE = false;
    private static final boolean DEFAULT_VALUE_WHITE_BCKGRD = false;
    private static final double DEFAULT_VALUE_SPEED_EFFECTS = 4;
    private static final String DEFAULT_VALUE_USER_NAME = "";
    private static final String DEFAULT_VALUE_USER_PICTURE = "";
    public static final String DEFAULT_VALUE_VIDEO_FOLDER = getFileDirectoryDefaultValue() + "/videos";

    @Getter
    @Setter
    private static boolean mouseFree = false;

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
        Properties properties = null;
        try {
            log.info("loading new properties from ={}", CONFIGPATH);
            properties = loadProperties(CONFIGPATH);
            log.info("Properties loaded : {}", properties);
        } catch (FileNotFoundException e) {
            log.warn("Config file not found : {}", CONFIGPATH);
        } catch (IOException e) {
            log.error("Failure while loading config file {}", CONFIGPATH, e);
        }
        final Configuration config = new Configuration();
        if (properties != null) {
            config.populateFromProperties(properties);
        }
        return config;
    }

    /*
     * public static final Configuration getInstance() { return Configuration.createFromPropertiesResource(); }
     */

    @Getter
    @Setter
    private static Configuration instance = Configuration.createFromPropertiesResource();

    @Getter
    private final SimpleSetProperty<String> favoriteGamesProperty = new SimpleSetProperty<>(this, PROPERTY_NAME_FAVORITE_GAMES, new ObservableSetWrapper<>(new LinkedHashSet<>()));

    @Getter
    private final SimpleSetProperty<String> hiddenCategoriesProperty = new SimpleSetProperty<>(this, PROPERTY_NAME_HIDDEN_CATEGORIES, new ObservableSetWrapper<>(new LinkedHashSet<>()));

    @Getter
    private final LongProperty latestNewsPopupShownTime = new SimpleLongProperty(this, PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME, 0);

    @Getter
    protected final StringProperty quitKeyProperty = new SimpleStringProperty(this, PROPERTY_NAME_QUIT_KEY,
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
    protected final DoubleProperty heatMapOpacityProperty = new SimpleDoubleProperty(this,
        PROPERTY_NAME_HEATMAP_OPACITY, DEFAULT_VALUE_HEATMAP_OPACITY);
    @Getter
    protected final StringProperty heatMapColorsProperty = new SimpleStringProperty(this, PROPERTY_NAME_HEATMAP_COLORS,
        DEFAULT_VALUE_HEATMAP_COLORS);
    @Getter
    protected final BooleanProperty areaOfInterestDisabledProperty = new SimpleBooleanProperty(this,
        PROPERTY_NAME_AREA_OF_INTEREST_DISABLED, DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED);
    @Getter
    protected final BooleanProperty convexHullDisabledProperty = new SimpleBooleanProperty(this,
        PROPERTY_NAME_CONVEX_HULL_DISABLED, DEFAULT_VALUE_CONVEX_HULL_DISABLED);
    @Getter
    protected final BooleanProperty videoRecordingDisabledProperty = new SimpleBooleanProperty(this,
        PROPERTY_NAME_VIDEO_RECORDING_DISABLED, DEFAULT_VALUE_VIDEO_RECORDING);
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
    protected final StringProperty videoFolderProperty = new SimpleStringProperty(this, PROPERTY_NAME_VIDEO_FOLDER,
        DEFAULT_VALUE_VIDEO_FOLDER);

    @Getter
    protected final StringProperty userNameProperty = new SimpleStringProperty(this, PROPERTY_NAME_USER_NAME,
        DEFAULT_VALUE_USER_NAME);
    @Getter
    protected final StringProperty userPictureProperty = new SimpleStringProperty(this, PROPERTY_NAME_USER_PICTURE,
        DEFAULT_VALUE_USER_PICTURE);

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

    private void populateFromProperties(Properties prop) {
        String buffer;

        buffer = prop.getProperty(PROPERTY_NAME_QUIT_KEY);
        if (buffer != null) {
            quitKeyProperty.setValue(buffer);
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
        buffer = prop.getProperty(PROPERTY_NAME_HEATMAP_OPACITY);
        if (buffer != null) {
            heatMapOpacityProperty.setValue(Double.parseDouble(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_HEATMAP_COLORS);
        if (buffer != null) {
            heatMapColorsProperty.setValue(buffer);
        }
        buffer = prop.getProperty(PROPERTY_NAME_AREA_OF_INTEREST_DISABLED);
        if (buffer != null) {
            areaOfInterestDisabledProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_CONVEX_HULL_DISABLED);
        if (buffer != null) {
            convexHullDisabledProperty.setValue(Boolean.parseBoolean(buffer));
        }
        buffer = prop.getProperty(PROPERTY_NAME_VIDEO_RECORDING_DISABLED);
        if (buffer != null) {
            videoRecordingDisabledProperty.setValue(Boolean.parseBoolean(buffer));
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

        buffer = prop.getProperty(PROPERTY_NAME_VIDEO_FOLDER);
        if (buffer != null) {
            videoFolderProperty.setValue(buffer);
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

        buffer = prop.getProperty(PROPERTY_NAME_FAVORITE_GAMES);
        if (buffer != null) {
            Set<String> values = new HashSet<>(Arrays.asList(buffer.split(",")));
            favoriteGamesProperty.get().addAll(values);
        }

        buffer = prop.getProperty(PROPERTY_NAME_HIDDEN_CATEGORIES);
        if (buffer != null) {
            Set<String> values = new HashSet<>(Arrays.asList(buffer.split(",")));
            hiddenCategoriesProperty.get().addAll(values);
        }

        buffer = prop.getProperty(PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME);
        if (buffer != null) {
            try {
                latestNewsPopupShownTime.setValue(Long.parseLong(buffer));
            } catch (NumberFormatException e) {
                log.warn("Malformed property");
            }
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
        properties.setProperty(PROPERTY_NAME_QUIT_KEY, this.quitKeyProperty.getValue());
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
        properties.setProperty(PROPERTY_NAME_HEATMAP_OPACITY, Double.toString(this.heatMapOpacityProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_HEATMAP_COLORS, this.heatMapColorsProperty.getValue());
        properties.setProperty(PROPERTY_NAME_AREA_OF_INTEREST_DISABLED,
            Boolean.toString(this.areaOfInterestDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_CONVEX_HULL_DISABLED,
            Boolean.toString(this.convexHullDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_VIDEO_RECORDING_DISABLED,
            Boolean.toString(this.videoRecordingDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED,
            Boolean.toString(this.fixationSequenceDisabledProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MUSIC_VOLUME, Double.toString(this.musicVolumeProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_MUSIC_FOLDER, this.musicFolderProperty.getValue());
        properties.setProperty(PROPERTY_NAME_EFFECTS_VOLUME, Double.toString(effectsVolumeProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_SPEED_EFFECTS, Double.toString(speedEffectsProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_VIDEO_FOLDER, this.videoFolderProperty.getValue());
        properties.setProperty(PROPERTY_NAME_WHITE_BCKGRD, Boolean.toString(whiteBackgroundProperty.getValue()));
        properties.setProperty(PROPERTY_NAME_USER_NAME, this.userNameProperty.getValue());
        properties.setProperty(PROPERTY_NAME_USER_PICTURE, this.userPictureProperty.getValue());
        /*
         * properties.setProperty(PROPERTY_NAME_GAZE_MENU, Boolean.toString(this.gazeMenuProperty.getValue()));
         */

        properties.setProperty(PROPERTY_NAME_FAVORITE_GAMES, favoriteGamesProperty.getValue().parallelStream().collect(Collectors.joining(",")));
        properties.setProperty(PROPERTY_NAME_HIDDEN_CATEGORIES, hiddenCategoriesProperty.getValue().parallelStream().collect(Collectors.joining(",")));

        properties.setProperty(PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME, Long.toString(latestNewsPopupShownTime.getValue()));

        return properties;
    }

    private void saveConfig() throws IOException {
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
        // System.out.println(QuitKeyProperty.getValue());
        return quitKeyProperty.getValue();
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

    public Double getHeatMapOpacity() {
        return heatMapOpacityProperty.getValue();
    }

    public ArrayList<Color> getHeatMapColors() {
        String colorsString = heatMapColorsProperty.getValue();
        ArrayList<Color> colors = new ArrayList<>();
        for (String colorString : colorsString.split(",")) {
            colors.add(Color.web(colorString));
        }
        return colors;
    }

    public Boolean isAreaOfInterestEnabled() {
        return areaOfInterestDisabledProperty.getValue();
    }

    public Boolean isConvexHullEnabled() {
        return convexHullDisabledProperty.getValue();
    }

    public Boolean isVideoRecordingEnabled() {
        return videoRecordingDisabledProperty.getValue();
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

    public String getVideoFolder() {
        return videoFolderProperty.getValue();
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

}
