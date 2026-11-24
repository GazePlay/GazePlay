package net.gazeplay.commons.configuration;

import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class ConfigurationTest {

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep;

    private ApplicationConfig applicationConfig;
    private Configuration configuration;
    private Properties properties;
    private File testProperties;

    private Locale original;

    @TempDir
    File tempDir;

    @BeforeEach
    void setup() throws IOException {
        // Work on a copy outside of the source tree, as the configuration is saved on each change
        testProperties = new File(tempDir, "GazePlay-test.properties");
        File originalFile = new File(localDataFolder, "GazePlay.properties");
        Files.copy(originalFile.toPath(), testProperties.toPath());

        properties = new Properties();

        try (InputStream is = Files.newInputStream(testProperties.toPath())) {
            properties.load(is);
        } catch (IOException ie) {
            log.debug("Error in loading test properties: ", ie);
        }

        original = Locale.getDefault();
        Locale.setDefault(Locale.CHINA);
        applicationConfig = new ApplicationConfig(properties);
        configuration = new Configuration(testProperties, applicationConfig);
    }

    @AfterEach
    void reset() {
        testProperties.delete();
        Locale.setDefault(original);
    }

    /**
     * 24/11/2026: a game, in its own JVM, writes the settings changed in its menu Esc; the menu reads them again when
     * the game ends, and no longer erases them at its next change of a setting.
     */
    @Test
    void shouldKeepTheSettingsChangedByAGame() {
        final Configuration game = ConfigurationSource.createFromPropertiesResource(testProperties);
        game.getGameDarkThemeEnabledProperty().set(true);
        game.getDarkButtonsEnabledProperty().set(true);
        assertFalse(configuration.isGameDarkThemeEnabled(), "the menu still has its former value");

        configuration.reloadFromFile();
        configuration.getWhereIsItDirProperty().set("another/path");

        final Configuration read = ConfigurationSource.createFromPropertiesResource(testProperties);
        assertTrue(read.isGameDarkThemeEnabled());
        assertTrue(read.isDarkButtonsEnabled());
        assertEquals("another/path", read.getWhereIsItDir());
    }

    @Test
    void shouldStoreTheApplicationConfig() {
        String oldPath = configuration.getWhereIsItDir();
        String newPath = "some/path";

        configuration.getWhereIsItDirProperty().set(newPath);
        configuration.saveConfigIgnoringExceptions();

        try (InputStream is = Files.newInputStream(testProperties.toPath())) {
            properties.load(is);
        } catch (IOException ie) {
            log.debug("Error in loading test properties: ", ie);
        }

        assertEquals(newPath, properties.getProperty("WHERE_IS_IT_DIR"));

        configuration.getWhereIsItDirProperty().set(oldPath);
    }

    /**
     * French and English only since 18/11/2026: a profile saved in another language is read in English, a French one
     * (France or the former "Francophonie") stays French; the file is not rewritten.
     */
    @Test
    void shouldReadALanguageNoLongerOfferedInEnglish() {
        final String[][] cases = {
            {"fra", "FR", "fra", "FR"}, {"fra", "", "fra", "FR"}, {"eng", "GB", "eng", "GB"}, {"eng", "US", "eng", "GB"},
            {"deu", "DE", "eng", "GB"}, {"chn", "", "eng", "GB"}, {"ara", "", "eng", "GB"}, {"", "", "eng", "GB"}};
        for (final String[] each : cases) {
            configuration.getLanguageProperty().set(each[0]);
            configuration.getCountryProperty().set(each[1]);
            assertEquals(each[2], configuration.getLanguage(), each[0] + "-" + each[1]);
            assertEquals(each[3], configuration.getCountry(), each[0] + "-" + each[1]);
            assertEquals(each[0], configuration.getLanguageProperty().get(), "kept as saved");
        }
    }

    /**
     * The voice of the games (18/11/2026): a woman by default, a man when chosen, a woman for any other value.
     */
    @Test
    void shouldGiveTheVoiceOfTheGames() {
        assertEquals("w", configuration.getVoice(), "a woman by default");
        configuration.getVoiceProperty().set("m");
        assertEquals("m", configuration.getVoice());
        configuration.getVoiceProperty().set("something else");
        assertEquals("w", configuration.getVoice());
        configuration.getVoiceProperty().set("w");
        assertEquals("w", configuration.getVoice());
    }

    /**
     * The fixation of the moving targets (20/11/2026): starts again from zero by default, keeps its progress when
     * chosen; put back to zero by the default values of the games.
     */
    @Test
    void shouldStartTheFixationOfTheMovingTargetsAgainByDefault() {
        assertFalse(configuration.isMovingFixationKept(), "from zero by default");
        configuration.getMovingFixationKeptProperty().set(true);
        assertTrue(configuration.isMovingFixationKept());
        configuration.resetGameSettings();
        assertFalse(configuration.isMovingFixationKept());
    }

    /**
     * Maintainer's request of 18/11/2026: the button "Default values" of the settings puts back the settings of the
     * games (those of the menu Esc), and keeps the others.
     */
    @Test
    void shouldPutBackTheDefaultValuesOfTheSettingsOfTheGamesOnly() {
        configuration.setUserName("someone");
        configuration.getMusicVolumeProperty().set(0.9);
        configuration.getEffectsVolumeProperty().set(0.8);
        configuration.getFixationLengthProperty().set(700);
        configuration.getAnimationSpeedRatioProperty().set(3);
        configuration.getElementSizeProperty().set(150);
        configuration.getRingThicknessProperty().set(200);
        configuration.getProgressBarColorProperty().set("RED");

        configuration.resetGameSettings();

        assertEquals(Configuration.DEFAULT_VALUE_MUSIC_VOLUME, configuration.getMusicVolume(), 1e-9);
        assertEquals(Configuration.DEFAULT_VALUE_EFFECTS_VOLUME, configuration.getEffectsVolume(), 1e-9);
        assertEquals(Configuration.DEFAULT_VALUE_FIXATION_LENGTH, configuration.getFixationLength());
        assertEquals(Configuration.DEFAULT_VALUE_ANIMATION_SPEED_RATIO, configuration.getAnimationSpeedRatio(), 1e-9);
        assertEquals(Configuration.DEFAULT_VALUE_ELEMENT_SIZE, configuration.getElementSize());
        assertEquals(Configuration.DEFAULT_VALUE_RING_THICKNESS, configuration.getRingThickness());
        assertEquals(Configuration.RING_COLOR_ACCENT, configuration.getProgressBarColor());
        assertEquals("someone", configuration.getUserName());
    }

    @Test
    void shouldSaveWithoutLeavingTemporaryFiles() throws IOException {
        configuration.setUserName("someone");

        try (Stream<Path> files = Files.list(tempDir.toPath())) {
            assertEquals(List.of(testProperties.toPath()), files.collect(Collectors.toList()));
        }
        Properties saved = new Properties();
        try (InputStream is = Files.newInputStream(testProperties.toPath())) {
            saved.load(is);
        }
        assertEquals("someone", saved.getProperty("USER_NAME"));
        assertEquals(properties.getProperty("FILE_DIR"), saved.getProperty("FILE_DIR"));
    }

    /**
     * The configuration file used to be truncated then rewritten in place: a failure during the write (crash, full
     * disk...) left a partial file, and every setting missing from it silently came back to its default value.
     */
    @Test
    void shouldKeepThePreviousFileWhenSavingFails() throws IOException {
        byte[] original = Files.readAllBytes(testProperties.toPath());
        ApplicationConfig failingConfig = new ApplicationConfig(properties) {
            @Override
            public synchronized void store(OutputStream out, String comments) throws IOException {
                out.write("EYE_TRACKER=mou".getBytes(StandardCharsets.ISO_8859_1));
                throw new IOException("Simulated failure during the write");
            }
        };
        Configuration failingConfiguration = new Configuration(testProperties, failingConfig);

        failingConfiguration.setUserName("someone");

        assertArrayEquals(original, Files.readAllBytes(testProperties.toPath()));
        try (Stream<Path> files = Files.list(tempDir.toPath())) {
            assertEquals(List.of(testProperties.toPath()), files.collect(Collectors.toList()));
        }
    }

    /**
     * On Windows, a file opened without delete sharing (as java.io.FileInputStream does, or an antivirus) cannot be
     * replaced: the change must still be saved, by rewriting the file in place as before.
     */
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void shouldStillSaveWhenTheFileCannotBeReplaced() throws IOException {
        try (InputStream lockingReader = new FileInputStream(testProperties)) {
            configuration.setUserName("someone");
        }

        Properties saved = new Properties();
        try (InputStream is = Files.newInputStream(testProperties.toPath())) {
            saved.load(is);
        }
        assertEquals("someone", saved.getProperty("USER_NAME"));
        try (Stream<Path> files = Files.list(tempDir.toPath())) {
            assertEquals(List.of(testProperties.toPath()), files.collect(Collectors.toList()));
        }
    }

    /**
     * The language of a new profile is the one of the system (Chinese here, set before the configuration is made),
     * read in English since 18/11/2026: French and English are the only languages.
     */
    @Test
    void givenLocaleIsSetShouldSetDefaultLanguage() {
        assertEquals(Locale.CHINA.getISO3Language(), configuration.getLanguageProperty().get(), "the system's");
        assertEquals("eng", configuration.getLanguage());
    }

    @Test
    void givenLocaleIsSetShouldSetDefaultCountry() {
        assertEquals(Locale.CHINA.getCountry(), configuration.getCountryProperty().get(), "the system's");
        assertEquals("GB", configuration.getCountry());
    }

    /**
     * EyeTribe was removed (October 2026): a setting still saying "eyetribe" gives the mouse, instead of an error at
     * the creation of the eye tracker.
     */
    @Test
    void shouldReadTheRemovedEyeTribeAsTheMouse() {
        Properties withEyeTribe = new Properties();
        withEyeTribe.setProperty("EYE_TRACKER", "eyetribe");

        Configuration result = new Configuration(testProperties, new ApplicationConfig(withEyeTribe));

        assertEquals("mouse_control", result.getEyeTracker());
    }

    @Test
    void shouldGetEyeTracker() {
        assertEquals(properties.get("EYE_TRACKER"), configuration.getEyeTracker());
    }

    /**
     * The quit key setting was removed (30/10/2026: the menu leaves a game or an evaluation). A file written
     * before still has QUIT_KEY (as the test file does): it is read without error, its other settings are kept, and it
     * is saved again; the unknown key stays in the file, unused.
     */
    @Test
    void shouldReadAndSaveAFileWithTheRemovedQuitKey() throws IOException {
        assertEquals("Q", properties.getProperty("QUIT_KEY"));
        configuration.setUserName("someone");

        Properties saved = new Properties();
        try (InputStream is = Files.newInputStream(testProperties.toPath())) {
            saved.load(is);
        }
        assertEquals("someone", saved.getProperty("USER_NAME"));
        assertEquals(properties.getProperty("EYE_TRACKER"), saved.getProperty("EYE_TRACKER"));
    }

    /**
     * The files folder of a profile was removed (3/11/2026: one folder of images for all the games): a file written
     * before keeps its FILE_DIR, unused.
     */
    @Test
    void shouldKeepTheRemovedFilesFolderInTheFile() throws IOException {
        assertNotNull(properties.getProperty("FILE_DIR"));
        configuration.setUserName("someone");
        Properties saved = new Properties();
        try (InputStream is = Files.newInputStream(testProperties.toPath())) {
            saved.load(is);
        }
        assertEquals(properties.getProperty("FILE_DIR"), saved.getProperty("FILE_DIR"));
    }

    /**
     * The time and score limiters were removed (16/11/2026, maintainer's decision of 4/11/2026): a file written before
     * (as the test file) keeps its LIMITER_* keys, unused, and is read and saved again without error.
     */
    @Test
    void shouldKeepTheRemovedLimitersInTheFile() throws IOException {
        assertEquals("true", properties.getProperty("LIMITER_TIME_ENABLED"));
        configuration.setUserName("someone");
        Properties saved = new Properties();
        try (InputStream is = Files.newInputStream(testProperties.toPath())) {
            saved.load(is);
        }
        assertEquals("someone", saved.getProperty("USER_NAME"));
        assertEquals("true", saved.getProperty("LIMITER_TIME_ENABLED"));
        assertEquals("60", saved.getProperty("LIMITER_TIME"));
    }

    @Test
    void shouldGetFixationLength() {
        assertEquals(properties.get("FIXATION_LENGTH"), configuration.getFixationLength().toString());
    }

    @Test
    void shouldGetCssFile() {
        assertEquals(properties.get("CSS_FILE"), configuration.getCssFile());
    }

    @Test
    void shouldGetWhereIsItDir() {
        assertEquals(properties.get("WHERE_IS_IT_DIR"), configuration.getWhereIsItDir());
    }

    @Test
    void shouldGetQuestionLength() {
        assertEquals(properties.get("QUESTION_LENGTH"), Long.toString(configuration.getQuestionLength()));
    }

    @Test
    void shouldGetIsEnableRewardSound() {
        assertEquals(properties.get("REWARD_SOUND_ENABLED"), configuration.isRewardSoundEnabled().toString());
    }

    @Test
    void shouldGetMenuButtonsOrientation() {
        assertEquals(properties.get("MENU_BUTTONS_ORIENTATION"), configuration.getMenuButtonsOrientation());
    }

    @Test
    void shouldGetIsHeatMapDisabled() {
        assertEquals(properties.get("HEATMAP_DISABLED"), configuration.isHeatMapDisabled().toString());
    }

    @Test
    void shouldGetHeatMapOpacity() {
        assertEquals(properties.get("HEATMAP_OPACITY"), configuration.getHeatMapOpacity().toString());
    }

    @Test
    void shouldGetHeatMapColors() {
        configuration.getHeatMapColorsProperty().set("0000FF,00FF00,FFFF00,FF0000");
        List<Color> expected = List.of(
            Color.web("0000FF"),
            Color.web("00FF00"),
            Color.web("FFFF00"),
            Color.web("FF0000")
        );
        assertEquals(expected, configuration.getHeatMapColors());
    }

    @Test
    void shouldGetIsVideoRecordingEnabled() {
        assertEquals(properties.get("VIDEO_RECORDING_ENABLED"), configuration.isVideoRecordingEnabled().toString());
    }

    @Test
    void shouldGetIsFixationSequenceDisabled() {
        assertEquals(properties.get("FIXATION_SEQUENCE_DISABLED"), configuration.isFixationSequenceDisabled().toString());
    }

    @Test
    void shouldGetMusicFolder() {
        assertEquals(properties.get("MUSIC_DIR"), configuration.getMusicDir());
    }

    @Test
    void shouldGetBackgroundStyle() {
        assertEquals(properties.get("BACKGROUND_STYLE"), configuration.getBackgroundStyle().toString());
    }

    @Test
    void shouldSetBackgroundStyle() {
        BackgroundStyle oldStyle = configuration.getBackgroundStyle();
        BackgroundStyle newStyle = BackgroundStyle.DARK;

        configuration.setBackgroundStyle(newStyle);
        assertEquals(newStyle, configuration.getBackgroundStyle());

        configuration.setBackgroundStyle(oldStyle);
    }

    @Test
    void shouldGetIsBackgroundEnabled() {
        assertEquals(properties.get("BACKGROUND_ENABLED"), configuration.isBackgroundEnabled().toString());
    }

    @Test
    void shouldGetUserName() {
        assertEquals(properties.get("USER_NAME"), configuration.getUserName());
    }

    @Test
    void shouldGetUserPicture() {
        assertEquals(properties.get("USER_PICTURE"), configuration.getUserPicture());
    }

    @Test
    void shouldSetUserName() {
        String oldName = configuration.getUserName();
        String newName = "test user name";

        configuration.setUserName(newName);
        assertEquals(newName, configuration.getUserName());

        configuration.setUserName(oldName);
    }

    @Test
    void shouldSetUserPicture() {
        String oldPicture = configuration.getUserPicture();
        String newPicture = "/test/user/picture.png";

        configuration.setUserPicture(newPicture);
        assertEquals(newPicture, configuration.getUserPicture());

        configuration.setUserPicture(oldPicture);
    }

    @Test
    void shouldGetIsLatestNewsDisplayForced() {
        assertEquals(properties.get("DISPLAY_NEWS_FORCED"), configuration.isLatestNewsDisplayForced().toString());
    }
}
