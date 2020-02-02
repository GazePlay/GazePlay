package net.gazeplay.ui.scenes.configuration;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import lombok.extern.slf4j.Slf4j;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import net.gazeplay.GazePlay;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.multilinguism.I18N;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class ConfigurationContextTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Translator mockTranslator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
    }

    @Test
    void shouldReturnToMenuOnHomeButtonPress() {
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        HomeButton button = context.createHomeButtonInConfigurationManagementScreen(mockGazePlay);
        button.fireEvent(TestingUtils.clickOnTarget(button));

        verify(mockGazePlay).onReturnToMenu();
    }

    @Test
    void shouldBuildConfigGridPane() {
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        GridPane pane = context.buildConfigGridPane(context, mockTranslator);

        ObservableList<Node> children = pane.getChildren();

        assertEquals(64, children.size());
        assertTrue(children.get(3) instanceof MenuButton);
        assertTrue(children.get(7) instanceof ChoiceBox);
        assertTrue(children.get(9) instanceof ChoiceBox);
        assertTrue(children.get(11) instanceof CheckBox);
        assertTrue(children.get(15) instanceof ChoiceBox);
        assertTrue(children.get(17) instanceof ChoiceBox);
        assertTrue(children.get(21) instanceof ChoiceBox);
        assertTrue(children.get(23) instanceof CheckBox);
        assertTrue(children.get(25) instanceof ChoiceBox);
        assertTrue(children.get(41) instanceof CheckBox);
        assertTrue(children.get(43) instanceof ChoiceBox);
        assertTrue(children.get(49) instanceof CheckBox);
        assertTrue(children.get(51) instanceof CheckBox);
        assertTrue(children.get(55) instanceof CheckBox);
        assertTrue(children.get(57) instanceof CheckBox);
        assertTrue(children.get(61) instanceof CheckBox);
        assertTrue(children.get(63) instanceof CheckBox);
    }

    @Test
    void shouldAddCategoryTitleLeftAligned() {
        when(mockTranslator.currentLocale()).thenReturn(Locale.FRANCE);
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        when(mockTranslator.translate(anyString())).thenReturn("category");
        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        context.addCategoryTitle(grid, currentFormRow, label);

        assertTrue(grid.getChildren().get(0) instanceof Separator);
        assertEquals(grid.getChildren().get(0).getProperties().get("gridpane-halignment"), HPos.CENTER);
        assertTrue(grid.getChildren().contains(label));
        assertEquals(grid.getChildren().get(1).getProperties().get("gridpane-halignment"), HPos.LEFT);
    }

    @Test
    void shouldAddCategoryTitleRightAligned() {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));
        ConfigurationContext context = new ConfigurationContext(mockGazePlay);

        when(mockTranslator.translate(anyString())).thenReturn("category");
        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        context.addCategoryTitle(grid, currentFormRow, label);

        assertTrue(grid.getChildren().get(0) instanceof Separator);
        assertEquals(grid.getChildren().get(0).getProperties().get("gridpane-halignment"), HPos.CENTER);
        assertTrue(grid.getChildren().contains(label));
        assertEquals(grid.getChildren().get(1).getProperties().get("gridpane-halignment"), HPos.RIGHT);
    }

    @Test
    void shouldSetupANewMusicFolder() {
        String songName = "songidea(copycat)_0.mp3";
        File testFolder = new File("music_test");
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assertTrue(testFolder.isDirectory());
        assertTrue(expectedFile.exists());

        assertTrue(expectedFile.delete());
        assertTrue(testFolder.delete());
    }

    @Test
    void shouldSetupANewMusicFolderIfTheFolderExists() {
        String songName = "songidea(copycat)_0.mp3";
        File testFolder = new File("music_test");
        assertTrue(testFolder.mkdir());
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assertTrue(testFolder.isDirectory());
        assertTrue(expectedFile.exists());

        assertTrue(expectedFile.delete());
        assertTrue(testFolder.delete());
    }

    @Test
    void shouldSetupANewMusicFolderIfTheSongDoesntExist() {
        String songName = "fakesong.mp3";
        File testFolder = new File("music_test");
        assertTrue(testFolder.mkdir());
        File expectedFile = new File(testFolder, songName);

        ConfigurationContext.setupNewMusicFolder(testFolder, songName);

        assertTrue(testFolder.isDirectory());
        assertFalse(expectedFile.exists());

        assertTrue(testFolder.delete());
    }

    @Test
    void shouldChangeTheMusicFolderAndPlayIfWasPlaying(@Mocked BackgroundMusicManager mockMusicManager,
                                                    @Mocked Configuration mockConfiguration) {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();

        new Expectations() {{
            mockMusicManager.isPlaying();
            result = true;

            BackgroundMusicManager.getInstance();
            result = mockMusicManager;

            mockConfiguration.getMusicFolderProperty();
            result = mockMusicFolderProperty;
        }};

        ConfigurationContext.changeMusicFolder("mockFolder", mockConfiguration);

        new Verifications() {{
            mockMusicManager.play();
            times = 1;
        }};
    }

    @Test
    void shouldChangeTheMusicFolderAndNotPlayIfWasNotPlaying(@Mocked BackgroundMusicManager mockMusicManager,
                                                         @Mocked Configuration mockConfiguration) {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();

        new Expectations() {{
            mockMusicManager.isPlaying();
            result = false;

            BackgroundMusicManager.getInstance();
            result = mockMusicManager;

            mockConfiguration.getMusicFolderProperty();
            result = mockMusicFolderProperty;
        }};

        ConfigurationContext.changeMusicFolder("mockFolder", mockConfiguration);

        new Verifications() {{
            mockMusicManager.play();
            times = 0;
        }};
    }

    @Test
    void shouldChangeTheMusicFolderWithBlankFolder(@Mocked BackgroundMusicManager mockMusicManager,
                                                @Mocked Configuration mockConfiguration) {
        StringProperty mockMusicFolderProperty = new SimpleStringProperty();
        String expectedFolder = System.getProperty("user.home") + File.separator + "GazePlay" + File.separator + "music";

        new Expectations() {{
            mockMusicManager.isPlaying();
            result = false;

            BackgroundMusicManager.getInstance();
            result = mockMusicManager;

            mockConfiguration.getMusicFolderProperty();
            result = mockMusicFolderProperty;
        }};

        ConfigurationContext.changeMusicFolder("", mockConfiguration);

        new Verifications() {{
            mockMusicManager.getAudioFromFolder(expectedFolder);
        }};
    }
}
