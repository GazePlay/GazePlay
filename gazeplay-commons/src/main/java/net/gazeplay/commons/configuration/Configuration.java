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

    private static final String PROPERTY_NAME_EYETRACKER = "EYETRACKER";
    private static final String PROPERTY_NAME_LANGUAGE = "LANGUAGE";
    private static final String PROPERTY_NAME_COUNTRY = "COUNTRY";
    private static final String PROPERTY_NAME_FILEDIR = "FILEDIR";
    private static final String PROPERTY_NAME_FIXATIONLENGTH = "FIXATIONLENGTH";
    private static final String PROPERTY_NAME_CSSFILE = "CSSFILE";
    private static final String PROPERTY_NAME_WHEREISIT_DIR = "WHEREISITDIR";
    private static final String PROPERTY_NAME_QUESTION_LENGTH = "QUESTIONLENGTH";
    private static final String PROPERTY_NAME_ENABLE_REWARD_SOUND = "ENABLE_REWARD_SOUND";
    private static final String PROPERTY_NAME_REASK_QUESTION_ON_FAIL = "REASK_QUESTION_ON_FAIL";
    private static final String PROPERTY_NAME_LIMITERT = "LIMITERT";
    private static final String PROPERTY_NAME_LIMITERS = "LIMITERS";
    private static final String PROPERTY_NAME_LIMITER_TIME = "LIMITER_TIME";
    private static final String PROPERTY_NAME_LIMITER_SCORE = "LIMITER_SCORE";
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
    private static final String PROPERTY_NAME_BACKGROUND_STYLE = "BACKGROUND_STYLE";
    private static final String PROPERTY_NAME_BACKGROUND_ENABLED = "BACKGROUND_ENABLED";
    private static final String PROPERTY_NAME_ANIMATION_SPEED_RATIO = "ANIMATION_SPEED_RATIO";
    private static final String PROPERTY_NAME_PROGRESS_BAR_SIZE = "PROGRESS_BAR_SIZE";
    private static final String PROPERTY_NAME_PROGRESS_BAR_COLOR = "PROGRESS_BAR_COLOR";
    private static final String PROPERTY_NAME_USER_NAME = "USER_NAME";
    private static final String PROPERTY_NAME_USER_PICTURE = "USER_PICTURE";
    private static final String PROPERTY_NAME_QUIT_KEY = "QUIT_KEY";
    private static final String PROPERTY_NAME_VIDEO_FOLDER = "VIDEO_FOLDER";
    private static final String PROPERTY_NAME_SHORTCUT_FOLDER = "SHORTCUT_FOLDER";
    private static final String PROPERTY_NAME_COLORS_DEFAULT_IMAGE = "COLORS_DEFAULT_IMAGE";
    private static final String PROPERTY_NAME_FORCE_DISPLAY_NEWS = "FORCE_DISPLAY_NEWS";
    private static final String PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME = "LATEST_NEWS_POPUP_LAST_SHOWN_TIME";
    private static final String PROPERTY_NAME_FAVORITE_GAMES = "FAVORITE_GAMES";
    private static final String PROPERTY_NAME_HIDDEN_CATEGORIES = "HIDDEN_CATEGORIES";
    private static final String PROPERTY_NAME_ELEMENTSIZE = "ELEMENT_SIZE";
    private static final String PROPERTY_NAME_QUESTION_TIME = "QUESTION_TIME";
    private static final String PROPERTY_NAME_TRANSITION_TIME = "TRANSITION_TIME";
    private static final String PROPERTY_NAME_DELAY_BEFORE_SELECTION_TIME = "DELAY_BEFORE_SELECTION_TIME";
    private static final String PROPERTY_NAME_QUESTION_TIME_ENABLED = "QUESTION_TIME_ENABLED";
    private static final String PROPERTY_NAME_COLUMNAR_IMAGES_ENABLED = "COLUMNAR_IMAGES_ENABLED";
    private static final String PROPERTY_NAME_SOUND_ENABLED = "SOUND_ENABLED";
    private static final String PROPERTY_NAME_SOUND_VOLUME_ENABLED = "SOUND_VOLUME_ENABLED";
    private static final String PROPERTY_NAME_FEEDBACK = "FEEDBACK";

    private static final KeyCode DEFAULT_VALUE_QUIT_KEY = KeyCode.Q;
    private static final String DEFAULT_VALUE_EYETRACKER = EyeTracker.tobii.toString();
    private static final int DEFAULT_VALUE_FIXATION_LENGTH = 2000;
    private static final String DEFAULT_VALUE_CSS_FILE = DEFAULT_THEME.getPreferredConfigPropertyValue();
    public static final String DEFAULT_VALUE_WHEREISIT_DIR = "";
    private static final long DEFAULT_VALUE_QUESTION_LENGTH = 5000;
    private static final boolean DEFAULT_VALUE_ENABLE_REWARD_SOUND = true;
    private static final boolean DEFAULT_VALUE_REASK_QUESTION_ON_FAIL = true;
    private static final boolean DEFAULT_VALUE_LIMITERTIME = false;
    private static final boolean DEFAULT_VALUE_LIMITERSCORE = false;
    private static final int DEFAULT_VALUE_LIMITER_TIME = 90;
    private static final int DEFAULT_VALUE_LIMITER_SCORE = 90;
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
    private static final BackgroundStyle DEFAULT_VALUE_BACKGROUND_STYLE = BackgroundStyle.DARK;
    private static final boolean DEFAULT_VALUE_BACKGROUND_ENABLED = true;
    private static final double DEFAULT_VALUE_ANIMATION_SPEED_RATIO = 1;
    private static final int DEFAULT_VALUE_PROGRESS_BAR_SIZE = 100;
    private static final String DEFAULT_VALUE_PROGRESS_BAR_COLOR = "YELLOW";
    private static final String DEFAULT_VALUE_USER_NAME = "";
    private static final String DEFAULT_VALUE_USER_PICTURE = "";
    private static final int DEFAULT_VALUE_ELEMENT_SIZE = 100;
    private static final int DEFAULT_VALUE_TRANSITION_TIME = 2000;
    private static final int DEFAULT_VALUE_DELAY_BEFORE_SELECTION_TIME = 1000;
    private static final int DEFAULT_VALUE_QUESTION_TIME = 5000;
    private static final boolean DEFAULT_VALUE_QUESTION_TIME_ENABLED = false;
    private static final boolean DEFAULT_VALUE_COLUMNAR_IMAGES_ENABLED = false;
    private static final boolean DEFAULT_VALUE_SOUND_ENABLED = true;
    private static final int DEFAULT_VALUE_SOUND_VOLUME = 50000;
    private static final String DEFAULT_VALUE_FEEDBACK = Feedback.standard.toString();

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
    private final BooleanProperty reaskQuestionOnFail;

    @Getter
    private final BooleanProperty enableRewardSoundProperty;

    @Getter
    private final BooleanProperty limiterSProperty;

    @Getter
    private final BooleanProperty limiterTProperty;

    @Getter
    private final IntegerProperty limiterTimeProperty;

    @Getter
    private final IntegerProperty limiterScoreProperty;

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
    private final ObjectProperty<BackgroundStyle> backgroundStyleProperty;

    @Getter
    private final BooleanProperty backgroundEnabledProperty;

    @Getter
    private final DoubleProperty musicVolumeProperty;

    @Getter
    private final StringProperty musicFolderProperty;

    @Getter
    private final DoubleProperty effectsVolumeProperty;

    @Getter
    private final DoubleProperty animationSpeedRatioProperty;

    @Getter
    private final IntegerProperty progressBarSizeProperty;

    @Getter
    private final StringProperty progressBarColorProperty;

    @Getter
    private final StringProperty videoFolderProperty;

    @Getter
    private final StringProperty shortcutFolderProperty;

    @Getter
    private final StringProperty userNameProperty;

    @Getter
    private final StringProperty userPictureProperty;

    @Getter
    private final StringProperty colorsDefaultImageProperty;

    @Getter
    private final BooleanProperty latestNewsDisplayForced;

    @Getter
    private final IntegerProperty elementSizeProperty;

    @Getter
    private final IntegerProperty questionTimeProperty;

    @Getter
    private final IntegerProperty transitionTimeProperty;

    @Getter
    private final IntegerProperty delayBeforeSelectionTimeProperty;

    @Getter
    private final BooleanProperty questionTimeEnabledProperty;

    @Getter
    private final BooleanProperty columnarImagesEnabledProperty;

    @Getter
    private final BooleanProperty soundEnabledProperty;

    @Getter
    private final IntegerProperty soundVolumeProperty;

    @Getter
    private final StringProperty feedbackProperty;

    private final File configFile;

    private final ApplicationConfig applicationConfig;

    protected Configuration(final File configFile, final ApplicationConfig applicationConfig) {
        this.configFile = configFile;
        this.applicationConfig = applicationConfig;

        final PropertyChangeListener propertyChangeListener = evt -> saveConfigIgnoringExceptions();

        languageProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_LANGUAGE, Locale.getDefault().getISO3Language(), propertyChangeListener);
        countryProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_COUNTRY, Locale.getDefault().getCountry(), propertyChangeListener);

        eyetrackerProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_EYETRACKER, DEFAULT_VALUE_EYETRACKER, propertyChangeListener);
        if (eyetrackerProperty.getValue().equals("tobii_eyeX_4C")){
            eyetrackerProperty.setValue("tobii");
        }

        musicVolumeProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_MUSIC_VOLUME, DEFAULT_VALUE_MUSIC_VOLUME, propertyChangeListener);
        effectsVolumeProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_EFFECTS_VOLUME, DEFAULT_VALUE_EFFECTS_VOLUME, propertyChangeListener);

        musicVolumeProperty.addListener(new RatioChangeListener(musicVolumeProperty));
        effectsVolumeProperty.addListener(new RatioChangeListener(effectsVolumeProperty));

        animationSpeedRatioProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_ANIMATION_SPEED_RATIO, DEFAULT_VALUE_ANIMATION_SPEED_RATIO, propertyChangeListener);
        progressBarSizeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_PROGRESS_BAR_SIZE, DEFAULT_VALUE_PROGRESS_BAR_SIZE, propertyChangeListener);
        progressBarColorProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_PROGRESS_BAR_COLOR, DEFAULT_VALUE_PROGRESS_BAR_COLOR, propertyChangeListener);

        heatMapOpacityProperty = new ApplicationConfigBackedDoubleProperty(applicationConfig, PROPERTY_NAME_HEATMAP_OPACITY, DEFAULT_VALUE_HEATMAP_OPACITY, propertyChangeListener);
        heatMapColorsProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_HEATMAP_COLORS, DEFAULT_VALUE_HEATMAP_COLORS, propertyChangeListener);
        heatMapDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_HEATMAP_DISABLED, DEFAULT_VALUE_HEATMAP_DISABLED, propertyChangeListener);

        enableRewardSoundProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_ENABLE_REWARD_SOUND, DEFAULT_VALUE_ENABLE_REWARD_SOUND, propertyChangeListener);
        reaskQuestionOnFail = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_REASK_QUESTION_ON_FAIL, DEFAULT_VALUE_REASK_QUESTION_ON_FAIL, propertyChangeListener);

        limiterSProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_LIMITERS, DEFAULT_VALUE_LIMITERSCORE, propertyChangeListener);
        limiterTProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_LIMITERT, DEFAULT_VALUE_LIMITERTIME, propertyChangeListener);
        limiterTimeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_LIMITER_TIME, DEFAULT_VALUE_LIMITER_TIME, propertyChangeListener);
        limiterScoreProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_LIMITER_SCORE, DEFAULT_VALUE_LIMITER_SCORE, propertyChangeListener);

        areaOfInterestDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_AREA_OF_INTEREST_DISABLED, DEFAULT_VALUE_AREA_OF_INTEREST_DISABLED, propertyChangeListener);
        convexHullDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_CONVEX_HULL_DISABLED, DEFAULT_VALUE_CONVEX_HULL_DISABLED, propertyChangeListener);
        videoRecordingEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_VIDEO_RECORDING_ENABLED, DEFAULT_VALUE_VIDEO_RECORDING_ENABLED, propertyChangeListener);
        fixationSequenceDisabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_FIXATIONSEQUENCE_DISABLED, DEFAULT_VALUE_FIXATIONSEQUENCE_DISABLED, propertyChangeListener);
        backgroundStyleProperty = new ApplicationConfigBackedObjectProperty<>(applicationConfig, PROPERTY_NAME_BACKGROUND_STYLE, DEFAULT_VALUE_BACKGROUND_STYLE, propertyChangeListener,
            new EnumMarshaller<>(),
            new EnumUnmarshaller<>(BackgroundStyle.class));
        backgroundEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_BACKGROUND_ENABLED, DEFAULT_VALUE_BACKGROUND_ENABLED, propertyChangeListener);

        menuButtonsOrientationProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_MENU_BUTTONS_ORIENTATION, DEFAULT_VALUE_MENU_BUTTONS_ORIENTATION, propertyChangeListener);
        cssfileProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_CSSFILE, DEFAULT_VALUE_CSS_FILE, propertyChangeListener);
        quitKeyProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_QUIT_KEY, DEFAULT_VALUE_QUIT_KEY.toString(), propertyChangeListener);

        questionLengthProperty = new ApplicationConfigBackedLongProperty(applicationConfig, PROPERTY_NAME_QUESTION_LENGTH, DEFAULT_VALUE_QUESTION_LENGTH, propertyChangeListener);
        fixationlengthProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_FIXATIONLENGTH, DEFAULT_VALUE_FIXATION_LENGTH, propertyChangeListener);

        filedirProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_FILEDIR, GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath(), propertyChangeListener);
        musicFolderProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_MUSIC_FOLDER, DEFAULT_VALUE_MUSIC_FOLDER, propertyChangeListener);
        videoFolderProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_VIDEO_FOLDER, GazePlayDirectories.getVideosFilesDirectory().getAbsolutePath(), propertyChangeListener);
        shortcutFolderProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_SHORTCUT_FOLDER, GazePlayDirectories.getShortcutDirectory().getAbsolutePath(), propertyChangeListener);
        userNameProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_USER_NAME, DEFAULT_VALUE_USER_NAME, propertyChangeListener);
        userPictureProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_USER_PICTURE, DEFAULT_VALUE_USER_PICTURE, propertyChangeListener);
        colorsDefaultImageProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_COLORS_DEFAULT_IMAGE, DEFAULT_VALUE_COLORS_DEFAULT_IMAGE, propertyChangeListener);

        whereIsItDirProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_WHEREISIT_DIR, DEFAULT_VALUE_WHEREISIT_DIR, propertyChangeListener);

        latestNewsPopupShownTime = new ApplicationConfigBackedLongProperty(applicationConfig, PROPERTY_NAME_LATEST_NEWS_POPUP_LAST_SHOWN_TIME, 0, propertyChangeListener);

        favoriteGamesProperty = new ApplicationConfigBackedStringSetProperty(applicationConfig, PROPERTY_NAME_FAVORITE_GAMES, Sets.newLinkedHashSet(), propertyChangeListener);
        hiddenCategoriesProperty = new ApplicationConfigBackedStringSetProperty(applicationConfig, PROPERTY_NAME_HIDDEN_CATEGORIES, Sets.newLinkedHashSet(), propertyChangeListener);

        latestNewsDisplayForced = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_FORCE_DISPLAY_NEWS, DEFAULT_VALUE_FORCE_DISPLAY_NEWS, propertyChangeListener);

        elementSizeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_ELEMENTSIZE, DEFAULT_VALUE_ELEMENT_SIZE, propertyChangeListener);

        questionTimeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_QUESTION_TIME, DEFAULT_VALUE_QUESTION_TIME, propertyChangeListener);
        transitionTimeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_TRANSITION_TIME, DEFAULT_VALUE_TRANSITION_TIME, propertyChangeListener);
        delayBeforeSelectionTimeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_DELAY_BEFORE_SELECTION_TIME, DEFAULT_VALUE_DELAY_BEFORE_SELECTION_TIME, propertyChangeListener);
        questionTimeEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_QUESTION_TIME_ENABLED, DEFAULT_VALUE_QUESTION_TIME_ENABLED, propertyChangeListener);
        columnarImagesEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_COLUMNAR_IMAGES_ENABLED, DEFAULT_VALUE_COLUMNAR_IMAGES_ENABLED, propertyChangeListener);
        soundEnabledProperty = new ApplicationConfigBackedBooleanProperty(applicationConfig, PROPERTY_NAME_SOUND_ENABLED, DEFAULT_VALUE_SOUND_ENABLED, propertyChangeListener);
        soundVolumeProperty = new ApplicationConfigBackedIntegerProperty(applicationConfig, PROPERTY_NAME_SOUND_VOLUME_ENABLED, DEFAULT_VALUE_SOUND_VOLUME, propertyChangeListener);
        feedbackProperty = new ApplicationConfigBackedStringProperty(applicationConfig, PROPERTY_NAME_FEEDBACK, DEFAULT_VALUE_FEEDBACK, propertyChangeListener);

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

    public Integer getProgressBarSize() {
        return progressBarSizeProperty.getValue();
    }

    public String getProgressBarColor() {
        return progressBarColorProperty.getValue();
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

    public Boolean isReaskedQuestionOnFail() {
        return reaskQuestionOnFail.getValue();
    }

    public boolean isLimiterS() {
        return limiterSProperty.getValue();
    }

    public boolean isLimiterT() {
        return limiterTProperty.getValue();
    }

    public int getLimiterTime() {
        return limiterTimeProperty.getValue();
    }

    public int getLimiterScore() {
        return limiterScoreProperty.getValue();
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

    public String getShortcutFolder() {
        return shortcutFolderProperty.getValue();
    }

    public BackgroundStyle getBackgroundStyle() {
        return backgroundStyleProperty.getValue();
    }

    public void setBackgroundStyle(final BackgroundStyle newValue) {
        backgroundStyleProperty.setValue(newValue);
    }

    public Boolean isBackgroundEnabled() {
        return backgroundEnabledProperty.getValue();
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

    public void setFixationLength(final int fixationLength) {
        fixationlengthProperty.setValue(fixationLength);
    }

    public Integer getElementSize() {
        return elementSizeProperty.getValue();
    }

    public Integer getQuestionTime() {
        return questionTimeProperty.getValue();
    }

    public Integer getTransitionTime() {return transitionTimeProperty.getValue(); }

    public Integer getDelayBeforeSelectionTime() {return delayBeforeSelectionTimeProperty.getValue(); }

    public Boolean isQuestionTimeEnabled(){ return questionTimeEnabledProperty.getValue(); }

    public Boolean isColumnarImagesEnabled(){ return columnarImagesEnabledProperty.getValue(); }

    public Boolean isSoundEnabled(){ return soundEnabledProperty.getValue(); }

    public Integer getSoundVolume() {
        return soundVolumeProperty.getValue();
    }

    public String getFeedback() {
        return feedbackProperty.getValue();
    }
}
