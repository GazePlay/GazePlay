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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.gazeplay.commons.themes.BuiltInUiTheme.DEFAULT_THEME;

@Slf4j
public class Configuration {

    /* PROPERTY NAMES */
    /* Language settings */
    private static final String PROPERTY_NAME_LANGUAGE = "LANGUAGE";
    private static final String PROPERTY_NAME_COUNTRY = "COUNTRY";
    /* Games settings */
    private static final String PROPERTY_NAME_QUIT_KEY = "QUIT_KEY";
    private static final String PROPERTY_NAME_QUESTION_LENGTH = "QUESTION_LENGTH";
    private static final String PROPERTY_NAME_QUESTION_REASKED_ON_FAIL = "QUESTION_REASKED_ON_FAIL";
    private static final String PROPERTY_NAME_REWARD_SOUND_ENABLED = "REWARD_SOUND_ENABLED";
    private static final String PROPERTY_NAME_LIMITER_TIME_ENABLED = "LIMITER_TIME_ENABLED";
    private static final String PROPERTY_NAME_LIMITER_TIME_VALUE = "LIMITER_TIME_VALUE";
    private static final String PROPERTY_NAME_LIMITER_SCORE_ENABLED = "LIMITER_SCORE_ENABLED";
    private static final String PROPERTY_NAME_LIMITER_SCORE_VALUE = "LIMITER_SCORE_VALUE";
    /* Bera settings */
    private static final String PROPERTY_NAME_TRANSITION_TIME = "TRANSITION_TIME";
    private static final String PROPERTY_NAME_DELAY_BEFORE_SELECTION_TIME = "DELAY_BEFORE_SELECTION_TIME";
    private static final String PROPERTY_NAME_QUESTION_TIME_ENABLED = "QUESTION_TIME_ENABLED";
    private static final String PROPERTY_NAME_QUESTION_TIME = "QUESTION_TIME";
    private static final String PROPERTY_NAME_COLUMNAR_IMAGES_ENABLED = "COLUMNAR_IMAGES_ENABLED";
    private static final String PROPERTY_NAME_SOUND_ENABLED = "SOUND_ENABLED";
    private static final String PROPERTY_NAME_FEEDBACK = "FEEDBACK";
    /* Eye-tracking settings */
    private static final String PROPERTY_NAME_EYETRACKER = "EYETRACKER";
    private static final String PROPERTY_NAME_FIXATION_LENGTH = "FIXATION_LENGTH";
    /* Graphics settings */
    private static final String PROPERTY_NAME_CSS_FILE = "CSS_FILE";
    private static final String PROPERTY_NAME_BACKGROUND_STYLE = "BACKGROUND_STYLE";
    private static final String PROPERTY_NAME_BACKGROUND_ENABLED = "BACKGROUND_ENABLED";
    private static final String PROPERTY_NAME_MENU_BUTTONS_ORIENTATION = "MENU_BUTTONS_ORIENTATION";
    /* Folder settings */
    private static final String PROPERTY_NAME_FILEDIR = "FILEDIR";
    private static final String PROPERTY_NAME_MUSIC_FOLDER = "MUSIC_FOLDER";
    private static final String PROPERTY_NAME_VIDEO_FOLDER = "VIDEO_FOLDER";
    private static final String PROPERTY_NAME_WHEREISIT_DIR = "WHEREISIT_DIR";
    /* Stats settings */
    private static final String PROPERTY_NAME_HEATMAP_DISABLED = "HEATMAP_DISABLED";
    private static final String PROPERTY_NAME_HEATMAP_OPACITY = "HEATMAP_OPACITY";
    private static final String PROPERTY_NAME_HEATMAP_COLORS = "HEATMAP_COLORS";
    private static final String PROPERTY_NAME_AREA_OF_INTEREST_DISABLED = "AREA_OF_INTEREST_DISABLED";
    private static final String PROPERTY_NAME_CONVEX_HULL_DISABLED = "CONVEX_HULL_DISABLED";
    private static final String PROPERTY_NAME_VIDEO_RECORDING_ENABLED = "VIDEO_RECORDING_ENABLED";
    private static final String PROPERTY_NAME_FIXATION_SEQUENCE_DISABLED = "FIXATION_SEQUENCE_DISABLED";
    private static final String PROPERTY_NAME_DATA_COLLECT_AUTHORIZED = "DATA_COLLECT_AUTHORIZED";
    /* In game settings */
    private static final String PROPERTY_NAME_MUSIC_VOLUME = "MUSIC_VOLUME";
    private static final String PROPERTY_NAME_EFFECTS_VOLUME = "EFFECTS_VOLUME";
    private static final String PROPERTY_NAME_ANIMATION_SPEED_RATIO = "ANIMATION_SPEED_RATIO";
    private static final String PROPERTY_NAME_ELEMENT_SIZE = "ELEMENT_SIZE";
    private static final String PROPERTY_NAME_PROGRESS_BAR_SIZE = "PROGRESS_BAR_SIZE";
    private static final String PROPERTY_NAME_PROGRESS_BAR_COLOR = "PROGRESS_BAR_COLOR";
    /* Menu settings */
    private static final String PROPERTY_NAME_USER_NAME = "USER_NAME";
    private static final String PROPERTY_NAME_USER_PICTURE = "USER_PICTURE";
    private static final String PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME = "LATEST_NEWS_POPUP_LAST_SHOWN_TIME";
    private static final String PROPERTY_NAME_DISPLAY_NEWS_FORCED = "DISPLAY_NEWS_FORCED";
    private static final String PROPERTY_NAME_FIRST_OPENING = "FIRST_OPENING";
    private static final String PROPERTY_NAME_SHORTCUT_FOLDER = "SHORTCUT_FOLDER";
    private static final String PROPERTY_NAME_COLORS_DEFAULT_IMAGE = "COLORS_DEFAULT_IMAGE";
    private static final String PROPERTY_NAME_FAVORITE_GAMES = "FAVORITE_GAMES";
    private static final String PROPERTY_NAME_HIDDEN_CATEGORIES = "HIDDEN_CATEGORIES";

    /* DEFAULT VALUES */
    /* Games settings */
    private static final KeyCode DEFAULT_VALUE_QUIT_KEY = KeyCode.Q;
    private static final long DEFAULT_VALUE_QUESTION_LENGTH = 5000;
    private static final boolean DEFAULT_VALUE_QUESTION_REASKED_ON_FAIL = true;
    private static final boolean DEFAULT_VALUE_REWARD_SOUND_ENABLED = true;
    private static final boolean DEFAULT_VALUE_LIMITER_TIME_ENABLED = false;
    private static final int DEFAULT_VALUE_LIMITER_TIME_VALUE = 90;
    private static final boolean DEFAULT_VALUE_LIMITER_SCORE_ENABLED = false;
    private static final int DEFAULT_VALUE_LIMITER_SCORE_VALUE = 90;
    /* Bera settings */
    private static final int DEFAULT_VALUE_TRANSITION_TIME = 2000;
    private static final int DEFAULT_VALUE_DELAY_BEFORE_SELECTION_TIME = 1000;
    private static final boolean DEFAULT_VALUE_QUESTION_TIME_ENABLED = false;
    private static final int DEFAULT_VALUE_QUESTION_TIME = 5000;
    private static final boolean DEFAULT_VALUE_COLUMNAR_IMAGES_ENABLED = false;
    private static final boolean DEFAULT_VALUE_SOUND_ENABLED = true;
    private static final String DEFAULT_VALUE_FEEDBACK = Feedback.standard.toString();
    /* Eye-tracking settings */
    private static final String DEFAULT_VALUE_EYETRACKER = EyeTracker.tobii.toString();
    private static final int DEFAULT_VALUE_FIXATION_LENGTH = 2000;
    /* Graphics settings */
    private static final String DEFAULT_VALUE_CSS_FILE = DEFAULT_THEME.getPreferredConfigPropertyValue();
    private static final BackgroundStyle DEFAULT_VALUE_BACKGROUND_STYLE = BackgroundStyle.DARK;
    private static final boolean DEFAULT_VALUE_BACKGROUND_ENABLED = true;
    private static final String DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION = "HORIZONTAL";
    /* Folder settings */
    public static final String DEFAULT_VALUE_MUSIC_FOLDER = "";
    public static final String DEFAULT_VALUE_WHEREISIT_DIR = "";
    /* Stats settings */
    private static final boolean DEFAULT_VALUE_HEATMAP_DISABLED = false;
    private static final double DEFAULT_VALUE_HEATMAP_OPACITY = 0.7;
    public static final String DEFAULT_VALUE_HEATMAP_COLORS = "0000FF,00FF00,FFFF00,FF0000";
    private static final boolean DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED = false;
    private static final boolean DEFAULT_VALUE_CONVEX_HULL_DISABLED = false;
    private static final boolean DEFAULT_VALUE_VIDEO_RECORDING_ENABLED = false;
    private static final boolean DEFAULT_VALUE_FIXATION_SEQUENCE_DISABLED = false;
    private static final boolean DEFAULT_VALUE_DATA_COLLECT_AUTHORIZED = true;
    /* In game settings */
    public static final String DEFAULT_VALUE_BACKGROUND_MUSIC = "songidea(copycat)_0.mp3";
    public static final double DEFAULT_VALUE_MUSIC_VOLUME = 0.25d;
    private static final Double DEFAULT_VALUE_EFFECTS_VOLUME = DEFAULT_VALUE_MUSIC_VOLUME;
    private static final double DEFAULT_VALUE_ANIMATION_SPEED_RATIO = 1;
    private static final int DEFAULT_VALUE_ELEMENT_SIZE = 100;
    private static final int DEFAULT_VALUE_PROGRESS_BAR_SIZE = 100;
    private static final String DEFAULT_VALUE_PROGRESS_BAR_COLOR = "YELLOW";
    /* Menu settings */
    private static final String DEFAULT_VALUE_USER_NAME = "";
    private static final String DEFAULT_VALUE_USER_PICTURE = "";
    private static final boolean DEFAULT_VALUE_DISPLAY_NEWS_FORCED = false;
    private static final boolean DEFAULT_VALUE_FIRST_OPENING = true;
    public static final String DEFAULT_VALUE_COLORS_DEFAULT_IMAGE = "data/colors/images/coloriage-dauphins-2.gif";

    /* PROPERTIES */
    /* Language settings */
    @Getter
    private final StringProperty languageProperty;
    @Getter
    private final StringProperty countryProperty;
    /* Games settings */
    @Getter
    private final StringProperty quitKeyProperty;
    @Getter
    private final LongProperty questionLengthProperty;
    @Getter
    private final BooleanProperty questionReaskedOnFailProperty;
    @Getter
    private final BooleanProperty rewardSoundEnabledProperty;
    @Getter
    private final BooleanProperty limiterTimeEnabledProperty;
    @Getter
    private final IntegerProperty limiterTimeValueProperty;
    @Getter
    private final BooleanProperty limiterScoreEnabledProperty;
    @Getter
    private final IntegerProperty limiterScoreValueProperty;
    /* Bera settings */
    @Getter
    private final IntegerProperty transitionTimeProperty;
    @Getter
    private final IntegerProperty delayBeforeSelectionTimeProperty;
    @Getter
    private final BooleanProperty questionTimeEnabledProperty;
    @Getter
    private final IntegerProperty questionTimeProperty;
    @Getter
    private final BooleanProperty columnarImagesEnabledProperty;
    @Getter
    private final BooleanProperty soundEnabledProperty;
    @Getter
    private final StringProperty feedbackProperty;
    /* Eye-tracking settings */
    @Getter
    private final StringProperty eyeTrackerProperty;
    @Getter
    private final IntegerProperty fixationLengthProperty;
    /* Graphics settings */
    @Getter
    private final StringProperty cssFileProperty;
    @Getter
    private final ObjectProperty<BackgroundStyle> backgroundStyleProperty;
    @Getter
    private final BooleanProperty backgroundEnabledProperty;
    @Getter
    private final StringProperty menuButtonsOrientationProperty;
    /* Folder settings */
    @Getter
    private final StringProperty fileDirProperty;
    @Getter
    private final StringProperty musicFolderProperty;
    @Getter
    private final StringProperty videoFolderProperty;
    @Getter
    private final StringProperty whereIsItDirProperty;
    /* Stats settings */
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
    private final BooleanProperty fixationSequenceDisabledProperty;
    @Getter
    private final BooleanProperty videoRecordingEnabledProperty;
    @Getter
    private final BooleanProperty dataCollectAuthorizedProperty;
    /* In game settings */
    @Getter
    private final DoubleProperty musicVolumeProperty;
    @Getter
    private final DoubleProperty effectsVolumeProperty;
    @Getter
    private final DoubleProperty animationSpeedRatioProperty;
    @Getter
    private final IntegerProperty elementSizeProperty;
    @Getter
    private final IntegerProperty progressBarSizeProperty;
    @Getter
    private final StringProperty progressBarColorProperty;
    /* Menu settings */
    @Getter
    private final StringProperty userNameProperty;
    @Getter
    private final StringProperty userPictureProperty;
    @Getter
    private final LongProperty latestNewsPopupShownTime;
    @Getter
    private final BooleanProperty latestNewsDisplayForcedProperty;
    @Getter
    private final BooleanProperty firstOpeningProperty;
    @Getter
    private final StringProperty colorsDefaultImageProperty;
    @Getter
    private final StringProperty shortcutFolderProperty;
    @Getter
    private final SetProperty<String> favoriteGamesProperty;
    @Getter
    private final SetProperty<String> hiddenCategoriesProperty;

    @Getter
    @Setter
    private boolean mouseFree = false;
    private final File configFile;
    private final ApplicationConfig applicationConfig;

    protected Configuration(final File configFile, final ApplicationConfig applicationConfig) {
        this.configFile = configFile;
        this.applicationConfig = applicationConfig;
        final PropertyChangeListener propertyChangeListener = evt -> saveConfigIgnoringExceptions();

        /* Language settings */
        languageProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_LANGUAGE, Locale.getDefault().getISO3Language(), propertyChangeListener);
        countryProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_COUNTRY, Locale.getDefault().getCountry(), propertyChangeListener);

        /* Games settings */
        quitKeyProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_QUIT_KEY, DEFAULT_VALUE_QUIT_KEY.toString(), propertyChangeListener);
        questionLengthProperty = new ApplicationConfigBackedLongProperty(applicationConfig, PROPERTY_NAME_QUESTION_LENGTH, DEFAULT_VALUE_QUESTION_LENGTH, propertyChangeListener);
        questionReaskedOnFailProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_QUESTION_REASKED_ON_FAIL, DEFAULT_VALUE_QUESTION_REASKED_ON_FAIL, propertyChangeListener);
        rewardSoundEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_REWARD_SOUND_ENABLED, DEFAULT_VALUE_REWARD_SOUND_ENABLED, propertyChangeListener);
        limiterTimeEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_LIMITER_TIME_ENABLED, DEFAULT_VALUE_LIMITER_TIME_ENABLED, propertyChangeListener);
        limiterTimeValueProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_LIMITER_TIME_VALUE, DEFAULT_VALUE_LIMITER_TIME_VALUE, propertyChangeListener);
        limiterScoreEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_LIMITER_SCORE_ENABLED, DEFAULT_VALUE_LIMITER_SCORE_ENABLED, propertyChangeListener);
        limiterScoreValueProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_LIMITER_SCORE_VALUE, DEFAULT_VALUE_LIMITER_SCORE_VALUE, propertyChangeListener);

        /* Bera settings */
        transitionTimeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_TRANSITION_TIME, DEFAULT_VALUE_TRANSITION_TIME, propertyChangeListener);
        delayBeforeSelectionTimeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_DELAY_BEFORE_SELECTION_TIME, DEFAULT_VALUE_DELAY_BEFORE_SELECTION_TIME, propertyChangeListener);
        questionTimeEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_QUESTION_TIME_ENABLED, DEFAULT_VALUE_QUESTION_TIME_ENABLED, propertyChangeListener);
        questionTimeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_QUESTION_TIME, DEFAULT_VALUE_QUESTION_TIME, propertyChangeListener);
        columnarImagesEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_COLUMNAR_IMAGES_ENABLED, DEFAULT_VALUE_COLUMNAR_IMAGES_ENABLED, propertyChangeListener);
        soundEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_SOUND_ENABLED, DEFAULT_VALUE_SOUND_ENABLED, propertyChangeListener);
        feedbackProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_FEEDBACK, DEFAULT_VALUE_FEEDBACK, propertyChangeListener);

        /* Eye-tracking settings */
        eyeTrackerProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_EYETRACKER, DEFAULT_VALUE_EYETRACKER, propertyChangeListener);
        fixationLengthProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_FIXATION_LENGTH, DEFAULT_VALUE_FIXATION_LENGTH, propertyChangeListener);

        /* Graphics settings */
        cssFileProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_CSS_FILE, DEFAULT_VALUE_CSS_FILE, propertyChangeListener);
        backgroundStyleProperty = new ApplicationConfigBackedObjectProperty<>(applicationConfig, PROPERTY_NAME_BACKGROUND_STYLE, DEFAULT_VALUE_BACKGROUND_STYLE, propertyChangeListener,
            new EnumMarshaller<>(), new EnumUnmarshaller<>(BackgroundStyle.class));
        backgroundEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_BACKGROUND_ENABLED, DEFAULT_VALUE_BACKGROUND_ENABLED, propertyChangeListener);
        menuButtonsOrientationProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION, propertyChangeListener);

        /* Folder settings */
        fileDirProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_FILEDIR, GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath(), propertyChangeListener);
        musicFolderProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_MUSIC_FOLDER, DEFAULT_VALUE_MUSIC_FOLDER, propertyChangeListener);
        videoFolderProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_VIDEO_FOLDER, GazePlayDirectories.getVideosFilesDirectory().getAbsolutePath(), propertyChangeListener);
        whereIsItDirProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_WHEREISIT_DIR, DEFAULT_VALUE_WHEREISIT_DIR, propertyChangeListener);

        /* Stats settings */
        heatMapOpacityProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_HEATMAP_OPACITY, DEFAULT_VALUE_HEATMAP_OPACITY, propertyChangeListener);
        heatMapColorsProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_HEATMAP_COLORS, DEFAULT_VALUE_HEATMAP_COLORS, propertyChangeListener);
        heatMapDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_HEATMAP_DISABLED, DEFAULT_VALUE_HEATMAP_DISABLED, propertyChangeListener);
        areaOfInterestDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_AREA_OF_INTEREST_DISABLED, DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED, propertyChangeListener);
        convexHullDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_CONVEX_HULL_DISABLED, DEFAULT_VALUE_CONVEX_HULL_DISABLED, propertyChangeListener);
        fixationSequenceDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_FIXATION_SEQUENCE_DISABLED, DEFAULT_VALUE_FIXATION_SEQUENCE_DISABLED, propertyChangeListener);
        videoRecordingEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_VIDEO_RECORDING_ENABLED, DEFAULT_VALUE_VIDEO_RECORDING_ENABLED, propertyChangeListener);
        dataCollectAuthorizedProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_DATA_COLLECT_AUTHORIZED, DEFAULT_VALUE_DATA_COLLECT_AUTHORIZED, propertyChangeListener);

        /* In game settings */
        musicVolumeProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_MUSIC_VOLUME, DEFAULT_VALUE_MUSIC_VOLUME, propertyChangeListener);
        effectsVolumeProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_EFFECTS_VOLUME, DEFAULT_VALUE_EFFECTS_VOLUME, propertyChangeListener);
        animationSpeedRatioProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_ANIMATION_SPEED_RATIO, DEFAULT_VALUE_ANIMATION_SPEED_RATIO, propertyChangeListener);
        elementSizeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_ELEMENT_SIZE, DEFAULT_VALUE_ELEMENT_SIZE, propertyChangeListener);
        progressBarSizeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_PROGRESS_BAR_SIZE, DEFAULT_VALUE_PROGRESS_BAR_SIZE, propertyChangeListener);
        progressBarColorProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_PROGRESS_BAR_COLOR, DEFAULT_VALUE_PROGRESS_BAR_COLOR, propertyChangeListener);

        /* Menu settings */
        userNameProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_USER_NAME, DEFAULT_VALUE_USER_NAME, propertyChangeListener);
        userPictureProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_USER_PICTURE, DEFAULT_VALUE_USER_PICTURE, propertyChangeListener);
        latestNewsPopupShownTime = new ApplicationConfigBackedLongProperty(applicationConfig, PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME, 0, propertyChangeListener);
        latestNewsDisplayForcedProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_DISPLAY_NEWS_FORCED, DEFAULT_VALUE_DISPLAY_NEWS_FORCED, propertyChangeListener);
        firstOpeningProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_FIRST_OPENING, DEFAULT_VALUE_FIRST_OPENING, propertyChangeListener);
        shortcutFolderProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_SHORTCUT_FOLDER, GazePlayDirectories.getShortcutDirectory().getAbsolutePath(), propertyChangeListener);
        colorsDefaultImageProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_COLORS_DEFAULT_IMAGE, DEFAULT_VALUE_COLORS_DEFAULT_IMAGE, propertyChangeListener);
        favoriteGamesProperty = new ApplicationConfigBackedStringSetProperty(applicationConfig, PROPERTY_NAME_FAVORITE_GAMES, Sets.newLinkedHashSet(), propertyChangeListener);
        hiddenCategoriesProperty = new ApplicationConfigBackedStringSetProperty(applicationConfig, PROPERTY_NAME_HIDDEN_CATEGORIES, Sets.newLinkedHashSet(), propertyChangeListener);

        if (eyeTrackerProperty.getValue().equals("tobii_eyeX_4C")) {
            eyeTrackerProperty.setValue("tobii");
        }
        musicVolumeProperty.addListener(new RatioChangeListener(musicVolumeProperty));
        effectsVolumeProperty.addListener(new RatioChangeListener(effectsVolumeProperty));
    }

    private void saveConfig() throws IOException {
        log.info("Saving Config {} ...", configFile);
        try (final OutputStream fileOutputStream = Files.newOutputStream(configFile.toPath())) {
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

    /* GETTERS */

    /* Language settings */

    public String getLanguage() {
        return languageProperty.getValue();
    }

    public String getCountry() {
        return countryProperty.getValue();
    }

    /* Games settings */

    public String getQuitKey() {
        return quitKeyProperty.getValue();
    }

    public long getQuestionLength() {
        return questionLengthProperty.getValue();
    }

    public Boolean isQuestionReaskedOnFail() {
        return questionReaskedOnFailProperty.getValue();
    }

    public Boolean isRewardSoundEnabled() {
        return rewardSoundEnabledProperty.getValue();
    }

    public boolean isLimiterTimeEnabled() {
        return limiterTimeEnabledProperty.getValue();
    }

    public int getLimiterTimeValue() {
        return limiterTimeValueProperty.getValue();
    }

    public boolean isLimiterScoreEnabled() {
        return limiterScoreEnabledProperty.getValue();
    }

    public int getLimiterScoreValue() {
        return limiterScoreValueProperty.getValue();
    }

    /* Bera settings */

    public Integer getTransitionTime() {
        return transitionTimeProperty.getValue();
    }

    public Integer getDelayBeforeSelectionTime() {
        return delayBeforeSelectionTimeProperty.getValue();
    }

    public Boolean isQuestionTimeEnabled() {
        return questionTimeEnabledProperty.getValue();
    }

    public Integer getQuestionTime() {
        return questionTimeProperty.getValue();
    }

    public Boolean isColumnarImagesEnabled() {
        return columnarImagesEnabledProperty.getValue();
    }

    public Boolean isSoundEnabled() {
        return soundEnabledProperty.getValue();
    }

    public String getFeedback() {
        return feedbackProperty.getValue();
    }

    /* Eye-tracking settings */

    public String getEyeTracker() {
        return eyeTrackerProperty.getValue();
    }

    public Integer getFixationLength() {
        return fixationLengthProperty.getValue();
    }

    /* Graphics settings */

    public String getCssFile() {
        return cssFileProperty.getValue();
    }

    public BackgroundStyle getBackgroundStyle() {
        return backgroundStyleProperty.getValue();
    }

    public Boolean isBackgroundEnabled() {
        return backgroundEnabledProperty.getValue();
    }

    public String getMenuButtonsOrientation() {
        return menuButtonsOrientationProperty.getValue();
    }

    /* Folder settings */

    public String getFileDir() {
        return fileDirProperty.getValue();
    }

    public String getMusicFolder() {
        return musicFolderProperty.getValue();
    }

    public String getVideoFolder() {
        return videoFolderProperty.getValue();
    }

    public String getWhereIsItDir() {
        return whereIsItDirProperty.getValue();
    }

    /* Stats settings */

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

    public Boolean isAreaOfInterestDisabled() {
        return areaOfInterestDisabledProperty.getValue();
    }

    public Boolean isConvexHullDisabled() {
        return convexHullDisabledProperty.getValue();
    }

    public Boolean isFixationSequenceDisabled() {
        return fixationSequenceDisabledProperty.getValue();
    }

    public Boolean isVideoRecordingEnabled() {
        return getVideoRecordingEnabledProperty().getValue();
    }

    public Boolean isDataCollectAuthorized() {
        return dataCollectAuthorizedProperty.getValue();
    }

    /* In game settings */

    public Integer getElementSize() {
        return elementSizeProperty.getValue();
    }

    public Integer getProgressBarSize() {
        return progressBarSizeProperty.getValue();
    }

    public String getProgressBarColor() {
        return progressBarColorProperty.getValue();
    }

    /* Menu settings */

    public String getUserName() {
        return userNameProperty.getValue();
    }

    public String getUserPicture() {
        return userPictureProperty.getValue();
    }

    public Boolean isLatestNewsDisplayForced() {
        return latestNewsDisplayForcedProperty.getValue();
    }

    public Boolean isFirstOpening() {
        return firstOpeningProperty.getValue();
    }

    public String getShortcutFolder() {
        return shortcutFolderProperty.getValue();
    }

    /* SETTERS */

    public void setFixationLength(final int fixationLength) {
        fixationLengthProperty.setValue(fixationLength);
    }

    public void setBackgroundStyle(final BackgroundStyle newValue) {
        backgroundStyleProperty.setValue(newValue);
    }

    public void setFileDir(final String s) {
        fileDirProperty.setValue(s);
    }

    public void setUserName(final String newName) {
        userNameProperty.setValue(newName);
    }

    public void setUserPicture(final String newPicture) {
        userPictureProperty.setValue(newPicture);
    }
}
