package net.gazeplay.commons.configuration;

import com.google.common.collect.Sets;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.observableproperties.*;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.gazeplay.commons.themes.BuiltInUiTheme.DEFAULT_THEME;

@Slf4j
public class Configuration {

    private static final String PROPERTY_NAME_EYETRACKER = "EYETRACKER";
    private static final String PROPERTY_NAME_LANGUAGE = "LANGUAGE";
    private static final String PROPERTY_NAME_COUNTRY = "COUNTRY";
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
    private static final String PROPERTY_NAME_VIDEO_RECORDING_ENABLED = "VIDEO_RECORDING_ENABLED";
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
    private static final String PROPERTY_NAME_COLORS_DEFAULT_IMAGE = "COLORS_DEFAULT_IMAGE";
    private static final String PROPERTY_NAME_FORCE_DISPLAY_NEWS = "FORCE_DISPLAY_NEWS";

    private static final String PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME = "LATEST_NEWS_POPUP_LAST_SHOWN_TIME";

    private static final String PROPERTY_NAME_FAVORITE_GAMES = "FAVORITE_GAMES";
    private static final String PROPERTY_NAME_HIDDEN_CATEGORIES = "HIDDEN_CATEGORIES";


    private static final KeyCode DEFAULT_VALUE_QUIT_KEY = KeyCode.Q;
    private static final String DEFAULT_VALUE_EYETRACKER = EyeTracker.mouse_control.toString();
    private static final String DEFAULT_VALUE_LANGUAGE = "fra";
    private static final String DEFAULT_VALUE_COUNTRY = "FR";
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
    private static final boolean DEFAULT_VALUE_VIDEO_RECORDING_ENABLED = false;
    private static final boolean DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED = false;
    public static final double DEFAULT_VALUE_MUSIC_VOLUME = 0.25d;
    public static final String DEFAULT_VALUE_MUSIC_FOLDER = "";
    public static final String DEFAULT_VALUE_BACKGROUND_MUSIC = "songidea(copycat)_0.mp3";
    private static final Double DEFAULT_VALUE_EFFECTS_VOLUME = DEFAULT_VALUE_MUSIC_VOLUME;
    private static final boolean DEFAULT_VALUE_FORCE_DISPLAY_NEWS = false;

    private static final boolean DEFAULT_VALUE_GAZE_MENU = false;
    private static final boolean DEFAULT_VALUE_GAZE_MOUSE = false;
    private static final boolean DEFAULT_VALUE_WHITE_BCKGRD = false;
    private static final double DEFAULT_VALUE_ANIMATION_SPEED_RATIO = 1;
    private static final String DEFAULT_VALUE_USER_NAME = "";
    private static final String DEFAULT_VALUE_USER_PICTURE = "";


    /*
    source : "http://pre07.deviantart.net/c66f/th/pre/i/2016/195/f/8/hatsune_miku_v4x_render_by_katrinasantiago0627-da9y7yr.png";
    * */
    public static final String DEFAULT_VALUE_COLORS_DEFAULT_IMAGE = "data/colors/images/coloriage-dauphins-2.gif";


    @Getter
    @Setter
    private boolean mouseFree = false;

    @Getter
    private final SetProperty<String> favoriteGamesProperty;

    @Getter
    private final SetProperty<String> hiddenCategoriesProperty;

    @Getter
    private final LongProperty latestNewsPopupShownTime;

    @Getter
    private final StringProperty quitKeyProperty;

    @Getter
    private final BooleanProperty gazeMenuEnabledProperty;

    @Getter
    private final StringProperty eyetrackerProperty;

    @Getter
    private final StringProperty languageProperty;

    @Getter
    private final StringProperty countryProperty;

    @Getter
    private final StringProperty filedirProperty;

    @Getter
    private final IntegerProperty fixationlengthProperty;

    @Getter
    private final StringProperty cssfileProperty;

    @Getter
    private final StringProperty whereIsItDirProperty;

    @Getter
    private final LongProperty questionLengthProperty;

    @Getter
    private final BooleanProperty enableRewardSoundProperty;

    @Getter
    private final StringProperty menuButtonsOrientationProperty;

    @Getter
    private final BooleanProperty heatMapDisabledProperty;

    @Getter
    private final DoubleProperty heatMapOpacityProperty;

    @Getter
    private final StringProperty heatMapColorsProperty;

    @Getter
    private final BooleanProperty areaOfInterestDisabledProperty;

    @Getter
    private final BooleanProperty convexHullDisabledProperty;

    @Getter
    private final BooleanProperty videoRecordingEnabledProperty;

    @Getter
    private final BooleanProperty fixationSequenceDisabledProperty;

    @Getter
    private final BooleanProperty gazeMouseEnabledProperty;

    @Getter
    private final BooleanProperty whiteBackgroundProperty;

    @Getter
    private final DoubleProperty musicVolumeProperty;

    @Getter
    private final StringProperty musicFolderProperty;

    @Getter
    private final DoubleProperty effectsVolumeProperty;

    @Getter
    private final DoubleProperty animationSpeedRatioProperty;

    @Getter
    private final StringProperty videoFolderProperty;

    @Getter
    private final StringProperty userNameProperty;

    @Getter
    private final StringProperty userPictureProperty;

    @Getter
    private final StringProperty colorsDefaultImageProperty;

    @Getter
    private final BooleanProperty latestNewsDisplayForced;

    private final File configFile;

    private final ApplicationConfig applicationConfig;

    protected Configuration(final File configFile, final ApplicationConfig applicationConfig) {
        this.configFile = configFile;
        this.applicationConfig = applicationConfig;

        final PropertyChangeListener propertyChangeListener = evt -> saveConfigIgnoringExceptions();

        languageProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_LANGUAGE, DEFAULT_VALUE_LANGUAGE, propertyChangeListener);
        countryProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_COUNTRY, DEFAULT_VALUE_COUNTRY, propertyChangeListener);

        eyetrackerProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_EYETRACKER, DEFAULT_VALUE_EYETRACKER, propertyChangeListener);

        musicVolumeProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_MUSIC_VOLUME, DEFAULT_VALUE_MUSIC_VOLUME, propertyChangeListener);
        effectsVolumeProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_EFFECTS_VOLUME, DEFAULT_VALUE_EFFECTS_VOLUME, propertyChangeListener);

        animationSpeedRatioProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_ANIMATION_SPEED_RATIO, DEFAULT_VALUE_ANIMATION_SPEED_RATIO, propertyChangeListener);

        heatMapOpacityProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_HEATMAP_OPACITY, DEFAULT_VALUE_HEATMAP_OPACITY, propertyChangeListener);
        heatMapColorsProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_HEATMAP_COLORS, DEFAULT_VALUE_HEATMAP_COLORS, propertyChangeListener);
        heatMapDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_HEATMAP_DISABLED, DEFAULT_VALUE_HEATMAP_DISABLED, propertyChangeListener);

        musicVolumeProperty.addListener(new RatioChangeListener(musicVolumeProperty));
        effectsVolumeProperty.addListener(new RatioChangeListener(effectsVolumeProperty));

        enableRewardSoundProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_ENABLE_REWARD_SOUND, DEFAULT_VALUE_ENABLE_REWARD_SOUND, propertyChangeListener);

        areaOfInterestDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_AREA_OF_INTEREST_DISABLED, DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED, propertyChangeListener);
        convexHullDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_CONVEX_HULL_DISABLED, DEFAULT_VALUE_CONVEX_HULL_DISABLED, propertyChangeListener);
        videoRecordingEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_VIDEO_RECORDING_ENABLED, DEFAULT_VALUE_VIDEO_RECORDING_ENABLED, propertyChangeListener);
        fixationSequenceDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED, DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED, propertyChangeListener);
        gazeMenuEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_GAZE_MENU, DEFAULT_VALUE_GAZE_MENU, propertyChangeListener);
        gazeMouseEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_GAZE_MOUSE, DEFAULT_VALUE_GAZE_MOUSE, propertyChangeListener);
        whiteBackgroundProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_WHITE_BCKGRD, DEFAULT_VALUE_WHITE_BCKGRD, propertyChangeListener);

        menuButtonsOrientationProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION, propertyChangeListener);
        cssfileProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_CSSFILE, DEFAULT_VALUE_CSS_FILE, propertyChangeListener);
        quitKeyProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_QUIT_KEY, DEFAULT_VALUE_QUIT_KEY.toString(), propertyChangeListener);

        questionLengthProperty = new ApplicationConfigBackedLongProperty(applicationConfig, PROPERTY_NAME_QUESTION_LENGTH, DEFAULT_VALUE_QUESTION_LENGTH, propertyChangeListener);
        fixationlengthProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_FIXATIONLENGTH, DEFAULT_VALUE_FIXATION_LENGTH, propertyChangeListener);

        filedirProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_FILEDIR, GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath(), propertyChangeListener);
        musicFolderProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_MUSIC_FOLDER, DEFAULT_VALUE_MUSIC_FOLDER, propertyChangeListener);
        videoFolderProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_VIDEO_FOLDER, GazePlayDirectories.getVideosFilesDirectory().getAbsolutePath(), propertyChangeListener);
        userNameProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_USER_NAME, DEFAULT_VALUE_USER_NAME, propertyChangeListener);
        userPictureProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_USER_PICTURE, DEFAULT_VALUE_USER_PICTURE, propertyChangeListener);
        colorsDefaultImageProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_COLORS_DEFAULT_IMAGE, DEFAULT_VALUE_COLORS_DEFAULT_IMAGE, propertyChangeListener);

        whereIsItDirProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_WHEREISIT_DIR, DEFAULT_VALUE_WHEREISIT_DIR, propertyChangeListener);

        latestNewsPopupShownTime = new ApplicationConfigBackedLongProperty(applicationConfig, PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME, 0, propertyChangeListener);

        favoriteGamesProperty = new ApplicationConfigBackedStringSetProperty(applicationConfig, PROPERTY_NAME_FAVORITE_GAMES, Sets.newLinkedHashSet(), propertyChangeListener);
        hiddenCategoriesProperty = new ApplicationConfigBackedStringSetProperty(applicationConfig, PROPERTY_NAME_HIDDEN_CATEGORIES, Sets.newLinkedHashSet(), propertyChangeListener);

        latestNewsDisplayForced = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_FORCE_DISPLAY_NEWS, DEFAULT_VALUE_FORCE_DISPLAY_NEWS, propertyChangeListener);
        populateFromApplicationConfig(applicationConfig);

    }

    private void saveConfig() throws IOException {
        log.info("Saving Config {} ...", configFile);
        try (final FileOutputStream fileOutputStream = new FileOutputStream(configFile)) {
            final String fileComment = "Automatically generated by GazePlay";
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
        } catch (final IOException e) {
            log.error("Exception while writing configuration to file {}", configFile, e);
        }
    }

    private void populateFromApplicationConfig(final ApplicationConfig prop) {
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

    public String getCountry() {
        return countryProperty.getValue();
    }

    public String getFileDir() {
        return filedirProperty.getValue();
    }

    public void setFileDir(final String s) {
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
        final String colorsString = heatMapColorsProperty.getValue();
        final List<Color> colors = new ArrayList<>();
        for (final String colorString : colorsString.split(",")) {
            colors.add(Color.web(colorString));
        }
        return colors;
    }

    public Boolean isVideoRecordingEnabled() {
        return getVideoRecordingEnabledProperty().getValue();
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
        return gazeMenuEnabledProperty.getValue();
    }

    public Boolean isGazeMouseEnable() {
        return gazeMouseEnabledProperty.getValue();
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

    public void setUserName(final String newName) {
        userNameProperty.setValue(newName);
    }

    public void setUserPicture(final String newPicture) {
        userPictureProperty.setValue(newPicture);
    }

    public Boolean isLatestNewsDisplayForced() {
        return latestNewsDisplayForced.getValue();
    }

}
