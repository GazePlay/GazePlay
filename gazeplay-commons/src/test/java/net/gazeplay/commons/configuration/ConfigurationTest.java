package net.gazeplay.commons.configuration;

import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class ConfigurationTest {

    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep + "src" + sep + "test" + sep + "resources" + sep;

    private ApplicationConfig applicationConfig;
    private Configuration configuration;
    private Properties properties;
    private File testProperties;

    private Locale original;

    @BeforeEach
    void setup() throws IOException {
        testProperties = new File(localDataFolder, "GazePlay-test.properties");
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
        applicationConfig = ConfigFactory.create(ApplicationConfig.class, properties);
        configuration = new Configuration(testProperties, applicationConfig);
    }

    @AfterEach
    void reset() {
        testProperties.delete();
        Locale.setDefault(original);
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

        assertEquals(newPath, properties.getProperty("WHEREISIT_DIR"));

        configuration.getWhereIsItDirProperty().set(oldPath);
    }

    @Test
    void givenLocaleIsSetShouldSetDefaultLanguage() {
        assertEquals(Locale.CHINA.getISO3Language(), configuration.getLanguage());
    }

    @Test
    void givenLocaleIsSetShouldSetDefaultCountry() {
        assertEquals(Locale.CHINA.getCountry(), configuration.getCountry());
    }

    @Test
    void shouldGetEyeTracker() {
        assertEquals(properties.get("EYETRACKER"), configuration.getEyeTracker());
    }

    @Test
    void shouldGetQuitKey() {
        assertEquals(properties.get("QUIT_KEY"), configuration.getQuitKey());
    }

    @Test
    void shouldGetFileDir() {
        assertEquals(properties.get("FILEDIR"), configuration.getFileDir());
    }

    @Test
    void shouldSetFileDir() {
        String oldDir = configuration.getFileDir();

        String fileDir = "/some/path";
        configuration.setFileDir(fileDir);
        assertEquals(fileDir, configuration.getFileDir());

        configuration.setFileDir(oldDir);
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
        assertEquals(properties.get("WHEREISIT_DIR"), configuration.getWhereIsItDir());
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
        assertEquals(properties.get("VIDEO_RECORDING_DISABLED"), configuration.isVideoRecordingEnabled().toString());
    }

    @Test
    void shouldGetIsFixationSequenceDisabled() {
        assertEquals(properties.get("FIXATION_SEQUENCE_DISABLED"), configuration.isFixationSequenceDisabled().toString());
    }

    @Test
    void shouldGetMusicFolder() {
        assertEquals(properties.get("MUSIC_FOLDER"), configuration.getMusicFolder());
    }

    @Test
    void shouldGetVideoFolder() {
        assertEquals(properties.get("VIDEO_FOLDER"), configuration.getVideoFolder());
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
