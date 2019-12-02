package net.gazeplay.commons.configuration;

import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.observableproperties.ApplicationConfigBackedBooleanProperty;
import net.gazeplay.commons.configuration.observableproperties.ApplicationConfigBackedDoubleProperty;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static net.gazeplay.commons.themes.BuiltInUiTheme.DEFAULT_THEME;

@Slf4j
public class Configuration {

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
    private static final String PROPERTY_NAME_ANIMATION_SPEED_RATIO = "ANIMATION_SPEED_RATIO";
    private static final String PROPERTY_NAME_USER_NAME = "USER_NAME";
    private static final String PROPERTY_NAME_USER_PICTURE = "USER_PICTURE";
    private static final String PROPERTY_NAME_QUIT_KEY = "QUIT_KEY";
    private static final String PROPERTY_NAME_VIDEO_FOLDER = "VIDEO_FOLDER";

    private static final String PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME = "LATEST_NEWS_POPUP_LAST_SHOWN_TIME";

    private static final String PROPERTY_NAME_FAVORITE_GAMES = "FAVORITE_GAMES";
    private static final String PROPERTY_NAME_HIDDEN_CATEGORIES = "HIDDEN_CATEGORIES";


    private static final KeyCode DEFAULT_VALUE_QUIT_KEY = KeyCode.Q;
    private static final boolean DEFAULT_VALUE_GAZEMODE = true;
    private static final String DEFAULT_VALUE_EYETRACKER = EyeTracker.mouse_control.toString();
    private static final String DEFAULT_VALUE_LANGUAGE = "fra";
    private static final int DEFAULT_VALUE_FIXATION_LENGTH = 500;
    private static final String DEFAULT_VALUE_CSS_FILE = DEFAULT_THEME.getPreferredConfigPropertyValue();
    public static final String DEFAULT_VALUE_WHEREISIT_DIR = "";
    private static final long DEFAULT_VALUE_QUESTION_LENGTH = 5000;
    private static final boolean DEFAULT_VALUE_ENABLE_REWARD_SOUND = true;
    private static final String DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION = "HORIZONTAL";
    private static final boolean DEFAULT_VALUE_HEATMAP_DISABLED = false;
    private static final double DEFAULT_VALUE_HEATMAP_OPACITY = 0.7;
    public static final String DEFAULT_VALUE_HEATMAP_COLORS = "0000FF,00FF00,FFFF00,FF0000";
    private static final boolean DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED = false;
    private static final boolean DEFAULT_VALUE_CONVEX_HULL_DISABLED = false;
    private static final boolean DEFAULT_VALUE_VIDEO_RECORDING = false;
    private static final boolean DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED = false;
    public static final double DEFAULT_VALUE_MUSIC_VOLUME = 0.25d;
    public static final String DEFAULT_VALUE_MUSIC_FOLDER = "";
    private static final Double DEFAULT_VALUE_EFFECTS_VOLUME = DEFAULT_VALUE_MUSIC_VOLUME;

    private static final boolean DEFAULT_VALUE_GAZE_MENU = false;
    private static final boolean DEFAULT_VALUE_GAZE_MOUSE = false;
    private static final boolean DEFAULT_VALUE_WHITE_BCKGRD = false;
    private static final double DEFAULT_VALUE_ANIMATION_SPEED_RATIO = 1;
    private static final String DEFAULT_VALUE_USER_NAME = "";
    private static final String DEFAULT_VALUE_USER_PICTURE = "";


    @Getter
    @Setter
    private boolean mouseFree = false;

    @Getter
    private final SimpleSetProperty<String> favoriteGamesProperty = new SimpleSetProperty<>(this, PROPERTY_NAME_FAVORITE_GAMES, new ObservableSetWrapper<>(new LinkedHashSet<>()));

    @Getter
    private final SimpleSetProperty<String> hiddenCategoriesProperty = new SimpleSetProperty<>(this, PROPERTY_NAME_HIDDEN_CATEGORIES, new ObservableSetWrapper<>(new LinkedHashSet<>()));

    @Getter
    private final LongProperty latestNewsPopupShownTime = new SimpleLongProperty(this, PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME, 0);

    @Getter
    private final StringProperty quitKeyProperty = new SimpleStringProperty(this, PROPERTY_NAME_QUIT_KEY, DEFAULT_VALUE_QUIT_KEY.toString());

    @Getter
    private final BooleanProperty gazeModeProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZEMODE, DEFAULT_VALUE_GAZEMODE);

    @Getter
    private final BooleanProperty gazeMenuProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZE_MENU, DEFAULT_VALUE_GAZE_MENU);

    @Getter
    private final StringProperty eyetrackerProperty = new SimpleStringProperty(this, PROPERTY_NAME_EYETRACKER, DEFAULT_VALUE_EYETRACKER);

    @Getter
    private final StringProperty languageProperty = new SimpleStringProperty(this, PROPERTY_NAME_LANGUAGE, DEFAULT_VALUE_LANGUAGE);

    @Getter
    private final StringProperty filedirProperty = new SimpleStringProperty(this, PROPERTY_NAME_FILEDIR, GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath());

    @Getter
    private final IntegerProperty fixationlengthProperty = new SimpleIntegerProperty(this, PROPERTY_NAME_FIXATIONLENGTH, DEFAULT_VALUE_FIXATION_LENGTH);

    @Getter
    private final StringProperty cssfileProperty = new SimpleStringProperty(this, PROPERTY_NAME_CSSFILE, DEFAULT_VALUE_CSS_FILE);

    @Getter
    private final StringProperty whereIsItDirProperty = new SimpleStringProperty(this, PROPERTY_NAME_WHEREISIT_DIR, DEFAULT_VALUE_WHEREISIT_DIR);

    @Getter
    private final LongProperty questionLengthProperty = new SimpleLongProperty(this, PROPERTY_NAME_QUESTION_LENGTH, DEFAULT_VALUE_QUESTION_LENGTH);

    @Getter
    private final BooleanProperty enableRewardSoundProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_ENABLE_REWARD_SOUND, DEFAULT_VALUE_ENABLE_REWARD_SOUND);

    @Getter
    private final StringProperty menuButtonsOrientationProperty = new SimpleStringProperty(this, PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION);

    @Getter
    private final BooleanProperty heatMapDisabledProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_HEATMAP_DISABLED, DEFAULT_VALUE_HEATMAP_DISABLED);

    @Getter
    private final DoubleProperty heatMapOpacityProperty;

    @Getter
    private final StringProperty heatMapColorsProperty = new SimpleStringProperty(this, PROPERTY_NAME_HEATMAP_COLORS, DEFAULT_VALUE_HEATMAP_COLORS);

    @Getter
    private final BooleanProperty areaOfInterestDisabledProperty;

    @Getter
    private final BooleanProperty convexHullDisabledProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_CONVEX_HULL_DISABLED, DEFAULT_VALUE_CONVEX_HULL_DISABLED);

    @Getter
    private final BooleanProperty videoRecordingDisabledProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_VIDEO_RECORDING_DISABLED, DEFAULT_VALUE_VIDEO_RECORDING);

    @Getter
    private final BooleanProperty fixationSequenceDisabledProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED, DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED);

    @Getter
    private final BooleanProperty gazeMouseProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_GAZE_MOUSE, DEFAULT_VALUE_GAZE_MOUSE);

    @Getter
    private final BooleanProperty whiteBackgroundProperty = new SimpleBooleanProperty(this, PROPERTY_NAME_WHITE_BCKGRD, DEFAULT_VALUE_WHITE_BCKGRD);

    @Getter
    private final DoubleProperty musicVolumeProperty;

    @Getter
    private final StringProperty musicFolderProperty = new SimpleStringProperty(this, PROPERTY_NAME_MUSIC_FOLDER, DEFAULT_VALUE_MUSIC_FOLDER);

    @Getter
    private final DoubleProperty effectsVolumeProperty;

    @Getter
    private final DoubleProperty animationSpeedRatioProperty;

    @Getter
    private final StringProperty videoFolderProperty = new SimpleStringProperty(this, PROPERTY_NAME_VIDEO_FOLDER, GazePlayDirectories.getVideosFilesDirectory().getAbsolutePath());

    @Getter
    private final StringProperty userNameProperty = new SimpleStringProperty(this, PROPERTY_NAME_USER_NAME, DEFAULT_VALUE_USER_NAME);

    @Getter
    private final StringProperty userPictureProperty = new SimpleStringProperty(this, PROPERTY_NAME_USER_PICTURE, DEFAULT_VALUE_USER_PICTURE);

    private final File configFile;

    private final ApplicationConfig applicationConfig;

    protected Configuration(File configFile, ApplicationConfig applicationConfig) {
        this.configFile = configFile;
        this.applicationConfig = applicationConfig;

        PropertyChangeListener propertyChangeListener = evt -> saveConfigIgnoringExceptions();

        musicVolumeProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_MUSIC_VOLUME, DEFAULT_VALUE_MUSIC_VOLUME, propertyChangeListener);
        effectsVolumeProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_EFFECTS_VOLUME, DEFAULT_VALUE_EFFECTS_VOLUME, propertyChangeListener);

        animationSpeedRatioProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_ANIMATION_SPEED_RATIO, DEFAULT_VALUE_ANIMATION_SPEED_RATIO, propertyChangeListener);

        heatMapOpacityProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_HEATMAP_OPACITY, DEFAULT_VALUE_HEATMAP_OPACITY, propertyChangeListener);

        musicVolumeProperty.addListener(new RatioChangeListener(musicVolumeProperty));
        effectsVolumeProperty.addListener(new RatioChangeListener(effectsVolumeProperty));

        areaOfInterestDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_AREA_OF_INTEREST_DISABLED, DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED, propertyChangeListener);

        populateFromApplicationConfig(applicationConfig);
    }

    private void saveConfig() throws IOException {
        log.info("Saving Config ...");
        persistConfig(applicationConfig);
        try (FileOutputStream fileOutputStream = new FileOutputStream(configFile)) {
            String fileComment = "Automatically generated by GazePlay";
            applicationConfig.store(fileOutputStream, fileComment);
        }
    }

    /**
     * when everything is using an ApplicationConfigBacked...Property, 
     * there is not need to call this method anymore, 
     * it should be called by the ApplicationConfigBacked...Property itself
     */
    @Deprecated
    public void saveConfigIgnoringExceptions() {
        try {
            saveConfig();
        } catch (IOException e) {
            log.error("Exception while writing configuration to file {}", configFile, e);
        }
    }

    private void populateFromApplicationConfig(ApplicationConfig prop) {
        String buffer;

        quitKeyProperty.setValue(prop.getProperty(PROPERTY_NAME_QUIT_KEY, DEFAULT_VALUE_QUIT_KEY.toString()));

        gazeModeProperty.setValue(Boolean.parseBoolean(prop.getProperty(PROPERTY_NAME_GAZEMODE, Boolean.toString(DEFAULT_VALUE_GAZEMODE))));

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
                questionLengthProperty.setValue(Long.parseLong(buffer));
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

        buffer = prop.getProperty(PROPERTY_NAME_ANIMATION_SPEED_RATIO);
        if (buffer != null) {
            try {
                animationSpeedRatioProperty.setValue(Double.parseDouble(buffer));
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

    private void persistConfig(ApplicationConfig applicationConfig) {
        applicationConfig.setProperty(PROPERTY_NAME_EYETRACKER, eyetrackerProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_LANGUAGE, languageProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_QUIT_KEY, quitKeyProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_FILEDIR, filedirProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_FIXATIONLENGTH, Integer.toString(fixationlengthProperty.getValue()));
        applicationConfig.setProperty(PROPERTY_NAME_CSSFILE, cssfileProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_WHEREISIT_DIR, whereIsItDirProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_QUESTION_LENGTH, Long.toString(questionLengthProperty.getValue()));
        applicationConfig.setProperty(PROPERTY_NAME_ENABLE_REWARD_SOUND, Boolean.toString(enableRewardSoundProperty.getValue()));
        applicationConfig.setProperty(PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, menuButtonsOrientationProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_HEATMAP_DISABLED, Boolean.toString(heatMapDisabledProperty.getValue()));
        applicationConfig.setProperty(PROPERTY_NAME_HEATMAP_COLORS, heatMapColorsProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_AREA_OF_INTEREST_DISABLED, Boolean.toString(areaOfInterestDisabledProperty.getValue()));
        applicationConfig.setProperty(PROPERTY_NAME_CONVEX_HULL_DISABLED, Boolean.toString(convexHullDisabledProperty.getValue()));
        applicationConfig.setProperty(PROPERTY_NAME_VIDEO_RECORDING_DISABLED, Boolean.toString(videoRecordingDisabledProperty.getValue()));
        applicationConfig.setProperty(PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED, Boolean.toString(fixationSequenceDisabledProperty.getValue()));
        applicationConfig.setProperty(PROPERTY_NAME_MUSIC_FOLDER, musicFolderProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_VIDEO_FOLDER, videoFolderProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_WHITE_BCKGRD, Boolean.toString(whiteBackgroundProperty.getValue()));
        applicationConfig.setProperty(PROPERTY_NAME_USER_NAME, userNameProperty.getValue());
        applicationConfig.setProperty(PROPERTY_NAME_USER_PICTURE, userPictureProperty.getValue());

        applicationConfig.setProperty(PROPERTY_NAME_FAVORITE_GAMES, favoriteGamesProperty.getValue().parallelStream().collect(Collectors.joining(",")));
        applicationConfig.setProperty(PROPERTY_NAME_HIDDEN_CATEGORIES, hiddenCategoriesProperty.getValue().parallelStream().collect(Collectors.joining(",")));

        applicationConfig.setProperty(PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME, Long.toString(latestNewsPopupShownTime.getValue()));
    }

    public String getEyeTracker() {
        return eyetrackerProperty.getValue();
    }

    public String getQuitKey() {
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

    public long getQuestionLength() {
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

    public List<Color> getHeatMapColors() {
        String colorsString = heatMapColorsProperty.getValue();
        List<Color> colors = new ArrayList<>();
        for (String colorString : colorsString.split(",")) {
            colors.add(Color.web(colorString));
        }
        return colors;
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

    public String getMusicFolder() {
        return musicFolderProperty.getValue();
    }

    public String getVideoFolder() {
        return videoFolderProperty.getValue();
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
