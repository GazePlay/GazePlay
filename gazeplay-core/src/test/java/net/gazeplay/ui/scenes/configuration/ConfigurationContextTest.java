package net.gazeplay.ui.scenes.configuration;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.AppMode;
import net.gazeplay.GazePlay;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.ui.scenes.gamemenu.GameButtonOrientation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(ApplicationExtension.class)
class ConfigurationContextTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Translator mockTranslator;

    @Mock
    private Configuration mockConfig;

    @Mock
    private ConfigurationContext mockContext;

    /**
     * The real Configuration setters write into the JavaFX property returned by the matching getter. Reproduces that
     * on the mock, so that a test can observe the value written by the UI component.
     */
    private static <T> org.mockito.stubbing.Stubber writeInto(Property<T> property) {
        return doAnswer(invocation -> {
            property.setValue(invocation.getArgument(0));
            return null;
        });
    }

    private final BackgroundMusicManager originalMusicManager = BackgroundMusicManager.getInstance();

    @AfterEach
    void restoreGlobalState() {
        BackgroundMusicManager.setInstance(originalMusicManager);
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(20d, 20d));
    }

    @Test
    void shouldReturnToMenuWithTheBackButton() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);

            context.createBackButton(mockGazePlay).fire();

            verify(mockGazePlay).onReturnToMenu();
        }).get();
    }

    /**
     * @return the control of a setting, found by the translation key of its name in the sections
     */
    private static Node control(List<SettingsPage.Section> sections, String key) {
        return sections.stream().map(section -> section.find(key)).flatMap(java.util.Optional::stream)
            .findFirst().orElseThrow(() -> new AssertionError("No setting " + key));
    }

    private static boolean has(List<SettingsPage.Section> sections, String key) {
        return sections.stream().anyMatch(section -> section.find(key).isPresent());
    }

    @Test
    void shouldBuildTheSectionsOfTheEvaluations() throws Exception {
        when(mockGazePlay.getMode()).thenReturn(AppMode.EVALUATIONS);
        // asyncFx(...).get(): a failed assertion fails the test (not in Platform.runLater)
        WaitForAsyncUtils.asyncFx(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);

            List<SettingsPage.Section> sections = context.buildSections(mockTranslator);

            // Only the settings used by the evaluations: no shortcut, no games, folders, AOI nor more statistics
            assertEquals(List.of("SettingsLanguage", "SettingsEyeTracker", "SettingsAppearance", "SettingsEvaluations",
                "SettingsStatistics"), sections.stream().map(SettingsPage.Section::titleKey).toList());
            assertInstanceOf(MenuButton.class, control(sections, "Lang"));
            // Eye tracker, fixation length
            assertInstanceOf(ChoiceBox.class, control(sections, "EyeTracker"));
            assertInstanceOf(Spinner.class, control(sections, "FixationLength"));
            // Appearance: theme, background color (the dark theme has its button on every page)
            assertInstanceOf(ComboBox.class, control(sections, "AccentColor"));
            assertInstanceOf(HBox.class, control(sections, "BackgroundColor"));
            assertFalse(has(sections, "DarkTh"));
            // Evaluations: imported evaluations, way of taking them, delay before selection, sound blocking the
            // selection. No quit key (the gear button leaves an evaluation); the import and the results folder are on
            // the menu of the evaluations only (maintainer's request of 29/10/2026)
            assertFalse(has(sections, "QuitKey"));
            VBox imported = assertInstanceOf(VBox.class, control(sections, "EvalImported"));
            assertTrue(imported.getChildren().stream().noneMatch(Button.class::isInstance));
            assertInstanceOf(ChoiceBox.class, control(sections, "EvalOptions"));
            assertInstanceOf(Spinner.class, control(sections, "EvalDelayBeforeSelection"));
            assertInstanceOf(CheckBox.class, control(sections, "EvalSoundBlocksSelection"));
            assertFalse(has(sections, "EvalResults"));
            // Heatmaps of the screens: disabled, opacity, colors, in their own card
            assertInstanceOf(CheckBox.class, control(sections, "DisableHeatMap"));
            assertInstanceOf(ChoiceBox.class, control(sections, "HeatMapOpacity"));
            assertInstanceOf(HBox.class, control(sections, "HeatMapColors"));
            assertEquals(List.of("HeatMapSettings"),
                sections.getLast().groups().stream().map(SettingsPage.Group::titleKey).toList());
            for (String gamesOnly : List.of("CreateShortcut", "QuestionLength", "MenuOrientation", "FileDir",
                "ResultDir", "EnableAreaOfInterest", "EnableVideoRecording")) {
                assertFalse(has(sections, gamesOnly), gamesOnly);
            }
        }).get();
    }

    @Test
    void shouldBuildTheSectionsOfTheGames() throws Exception {
        // asyncFx(...).get(): a failed assertion fails the test (not in Platform.runLater)
        WaitForAsyncUtils.asyncFx(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);

            // Layout of the "Games" mode (the mode of a mocked GazePlay is null: the games), without the evaluations
            List<SettingsPage.Section> sections = context.buildSections(mockTranslator);

            assertEquals(List.of("SettingsLanguage", "SettingsGames", "SettingsEyeTracker", "SettingsAppearance",
                "SettingsFolders", "SettingsStatistics"), sections.stream().map(SettingsPage.Section::titleKey).toList());
            assertInstanceOf(MenuButton.class, control(sections, "Lang"));
            assertEquals(Utils.isWindows(), has(sections, "CreateShortcut"));
            // Games: question length, reask on fail, reward sound, limiters, piano tile colors (no quit key: the gear
            // button leaves a game)
            assertFalse(has(sections, "QuitKey"));
            assertInstanceOf(Spinner.class, control(sections, "QuestionLength"));
            assertInstanceOf(CheckBox.class, control(sections, "ReaskQuestionOnFail"));
            assertInstanceOf(CheckBox.class, control(sections, "EnableRewardSound"));
            assertInstanceOf(HBox.class, control(sections, "Limiter Time"));
            assertInstanceOf(HBox.class, control(sections, "Limiter Score"));
            assertInstanceOf(HBox.class, control(sections, "PianoTileColors"));
            // Eye tracker, fixation length
            assertInstanceOf(ChoiceBox.class, control(sections, "EyeTracker"));
            assertInstanceOf(Spinner.class, control(sections, "FixationLength"));
            // Appearance: theme, background color, orientation of the cards, background enabled (the dark theme has its
            // button on every page)
            assertInstanceOf(ComboBox.class, control(sections, "AccentColor"));
            assertInstanceOf(HBox.class, control(sections, "BackgroundColor"));
            assertInstanceOf(ChoiceBox.class, control(sections, "MenuOrientation"));
            assertInstanceOf(CheckBox.class, control(sections, "BackgroundEnabled"));
            assertFalse(has(sections, "DarkTh"));
            // Folders: files, music, where is it
            assertInstanceOf(Button.class, control(sections, "FileDir"));
            assertInstanceOf(HBox.class, control(sections, "MusicFolder"));
            assertInstanceOf(HBox.class, control(sections, "WhereIsItDirectory"));
            // Statistics: results folder, then heatmap (disabled, opacity, colors), AOI (enabled), sequence and
            // video, each group in its own card
            assertInstanceOf(Button.class, control(sections, "ResultDir"));
            assertInstanceOf(CheckBox.class, control(sections, "DisableHeatMap"));
            assertInstanceOf(ChoiceBox.class, control(sections, "HeatMapOpacity"));
            assertInstanceOf(HBox.class, control(sections, "HeatMapColors"));
            assertInstanceOf(CheckBox.class, control(sections, "EnableAreaOfInterest"));
            assertInstanceOf(CheckBox.class, control(sections, "DisableSequence"));
            assertInstanceOf(CheckBox.class, control(sections, "EnableVideoRecording"));
            assertEquals(java.util.Arrays.asList(null, "HeatMapSettings", "AOISettings", "MoreStatsSettings"),
                sections.getLast().groups().stream().map(SettingsPage.Group::titleKey).toList());
            assertFalse(has(sections, "EvalImported"));
        }).get();
    }

    /**
     * A left-to-right language: the page is not mirrored (sections on the left, names of the settings on the left).
     */
    @Test
    void shouldLayOutTheSettingsFromLeftToRight() throws Exception {
        when(mockTranslator.currentLocale()).thenReturn(Locale.FRANCE);

        ConfigurationContext context = WaitForAsyncUtils.asyncFx(() -> new ConfigurationContext(mockGazePlay)).get();

        assertNotEquals(NodeOrientation.RIGHT_TO_LEFT, context.getRoot().getNodeOrientation());
    }

    /**
     * Arabic: the whole page is mirrored (it used to align each row of the grid to the right).
     */
    @Test
    void shouldMirrorTheSettingsForArabic() throws Exception {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));

        ConfigurationContext context = WaitForAsyncUtils.asyncFx(() -> new ConfigurationContext(mockGazePlay)).get();

        assertEquals(NodeOrientation.RIGHT_TO_LEFT, context.getRoot().getNodeOrientation());
    }

    /**
     * The sections on the left, the first one shown; choosing another one shows its settings, one row per setting.
     */
    @Test
    void shouldShowTheChosenSection() throws Exception {
        when(mockTranslator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        CheckBox first = new CheckBox();
        CheckBox second = new CheckBox();
        List<SettingsPage.Section> sections = List.of(
            new SettingsPage.SectionBuilder("One", net.gazeplay.ui.menus.MenuIcon.GAMES).add("A", first).build(),
            new SettingsPage.SectionBuilder("Two", net.gazeplay.ui.menus.MenuIcon.FOLDER).group("Group")
                .add("B", second).build());

        WaitForAsyncUtils.asyncFx(() -> {
            HBox page = SettingsPage.create(mockTranslator, sections);
            VBox navigation = (VBox) page.getChildren().get(0);
            ScrollPane content = (ScrollPane) page.getChildren().get(1);

            assertEquals(2, navigation.getChildren().size());
            assertTrue(((ToggleButton) navigation.getChildren().get(0)).isSelected());
            assertEquals(List.of("A"), settingKeys(content.getContent()));

            ((ToggleButton) navigation.getChildren().get(1)).fire();

            assertTrue(((ToggleButton) navigation.getChildren().get(1)).isSelected());
            assertEquals(List.of("B"), settingKeys(content.getContent()));
            assertTrue(content.getContent().lookupAll(".gp-setting-row").iterator().next()
                .lookupAll(".check-box").contains(second));
        }).get();
    }

    /**
     * @return the keys of the settings shown in a section, in their order
     */
    private static List<String> settingKeys(Node section) {
        return section.lookupAll(".gp-setting-row").stream()
            .map(row -> (String) row.getProperties().get(SettingsPage.SETTING_KEY)).toList();
    }

    @Test
    void shouldBuildSpinner() {
        SimpleIntegerProperty length = new SimpleIntegerProperty();

        Spinner<Double> result = ConfigurationContext.buildSpinner(0.3, 10, 0.5, 0.1, length);
        assertEquals(0.5, result.getValue());

        result.increment(5);
        assertEquals(1, result.getValue());
        assertEquals(1000, length.get());
    }

    @Test
    void shouldSetSpinnerValueToMaxIfHigher() {
        SimpleIntegerProperty length = new SimpleIntegerProperty();

        Spinner<Double> result = ConfigurationContext.buildSpinner(0.3, 10, 0.5, 0.1, length);
        assertEquals(0.5, result.getValue());

        result.getEditor().setText("11");
        result.commitValue();

        assertEquals(10, result.getValue());
        assertEquals(10000, length.get());
    }

    @Test
    void shouldSetSpinnerValueToMinIfLower() {
        SimpleIntegerProperty length = new SimpleIntegerProperty();

        Spinner<Double> result = ConfigurationContext.buildSpinner(0.3, 10, 0.5, 0.1, length);
        assertEquals(0.5, result.getValue());

        result.getEditor().setText("0.2");
        result.commitValue();

        assertEquals(0.3, result.getValue());
        assertEquals(300, length.get());
    }

    @Test
    void shouldCreateMainColorChooserNonDefault() {
        StringProperty cssFileProperty = new SimpleStringProperty("builtin:BLUE");
        ObservableList<String> stylesheets = FXCollections.observableArrayList();
        Scene mockScene = mock(Scene.class);

        when(mockConfig.getCssFile()).thenReturn("builtin:BLUE");
        when(mockConfig.getCssFileProperty()).thenReturn(cssFileProperty);
        writeInto(cssFileProperty).when(mockConfig).setCssFile(anyString());
        when(mockContext.getGazePlay()).thenReturn(mockGazePlay);
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockScene.getStylesheets()).thenReturn(stylesheets);

        ComboBox<BuiltInUiTheme> result = ConfigurationContext.buildMainColorChooser(mockConfig, mockContext, mockTranslator);

        assertEquals(BuiltInUiTheme.offeredColors(), result.getItems());
        assertEquals(BuiltInUiTheme.BLUE, result.getValue());
        assertEquals(cssFileProperty.getValue(), "builtin:BLUE");

        result.setValue(BuiltInUiTheme.GREEN);

        assertEquals(BuiltInUiTheme.GREEN, result.getValue());
        assertEquals(cssFileProperty.getValue(), "builtin:GREEN");
    }

    @Test
    void shouldCreateMainColorChooserDefault() {
        StringProperty cssFileProperty = new SimpleStringProperty("builtin:WRONG");
        ObservableList<String> stylesheets = FXCollections.observableArrayList();
        Scene mockScene = mock(Scene.class);

        when(mockConfig.getCssFile()).thenReturn("builtin:WRONG");
        when(mockConfig.getCssFileProperty()).thenReturn(cssFileProperty);
        writeInto(cssFileProperty).when(mockConfig).setCssFile(anyString());
        when(mockContext.getGazePlay()).thenReturn(mockGazePlay);
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockScene.getStylesheets()).thenReturn(stylesheets);

        ComboBox<BuiltInUiTheme> result = ConfigurationContext.buildMainColorChooser(mockConfig, mockContext, mockTranslator);

        assertEquals(BuiltInUiTheme.offeredColors(), result.getItems());
        assertEquals(BuiltInUiTheme.SILVER_AND_GOLD, result.getValue());

        result.setValue(BuiltInUiTheme.GREEN);

        assertEquals(BuiltInUiTheme.GREEN, result.getValue());
        assertEquals(cssFileProperty.getValue(), "builtin:GREEN");
    }

    @ParameterizedTest
    @EnumSource(ConfigurationContext.DirectoryType.class)
    void shouldCreateDirectoryChooser(ConfigurationContext.DirectoryType type) throws InterruptedException {
        BackgroundMusicManager.setInstance(mock(BackgroundMusicManager.class));

        StringProperty fileDirProperty = new SimpleStringProperty(System.getProperty("user.home") + "/GazePlay/");
        Scene mockScene = mock(Scene.class);
        Window mockWindow = mock(Window.class);

        Map<ConfigurationContext.DirectoryType, String> answers = Map.of(
            ConfigurationContext.DirectoryType.FILE, GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath(),
            ConfigurationContext.DirectoryType.WHERE_IS_IT, Configuration.DEFAULT_VALUE_WHERE_IS_IT_DIR,
            ConfigurationContext.DirectoryType.MUSIC, new File(System.getProperty("user.home") + "/GazePlay/", "music").getAbsolutePath()
        );

        when(mockConfig.getWhereIsItDir()).thenReturn(fileDirProperty.getValue());
        when(mockConfig.getWhereIsItDirProperty()).thenReturn(fileDirProperty);
        when(mockConfig.getFileDir()).thenReturn(fileDirProperty.getValue());
        when(mockConfig.getFileDirProperty()).thenReturn(fileDirProperty);
        when(mockConfig.getMusicDir()).thenAnswer(invocation -> fileDirProperty.getValue());
        when(mockConfig.getMusicDirProperty()).thenReturn(fileDirProperty);
        writeInto(fileDirProperty).when(mockConfig).setWhereIsItDir(anyString());
        writeInto(fileDirProperty).when(mockConfig).setFileDir(anyString());
        writeInto(fileDirProperty).when(mockConfig).setMusicDir(anyString());

        when(mockContext.getGazePlay()).thenReturn(mockGazePlay);
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockScene.getWindow()).thenReturn(mockWindow);
        when(mockTranslator.translate("Select")).thenReturn("Select");

        if (type != ConfigurationContext.DirectoryType.SHORTCUT) {
            Platform.runLater(() -> {
                ConfigurationContext context = new ConfigurationContext(mockGazePlay);
                HBox result = (HBox) context.buildDirectoryChooser(mockConfig, mockContext, mockTranslator, type);
                Button loadButton = (Button) result.getChildren().get(0);
                Button resetButton = (Button) result.getChildren().get(1);

                assertEquals(fileDirProperty.getValue(), loadButton.textProperty().getValue());

                resetButton.fire();
                assertEquals(answers.get(type), fileDirProperty.getValue());
                // No folder (the default of "Where is it?"): the button says "Select" instead of being empty
                String shown = answers.get(type).isBlank() ? "Select" : answers.get(type);
                assertEquals(shown, loadButton.textProperty().getValue());
            });
            TestingUtils.waitForRunLater();
        }
    }

    @Test
    void shouldBuildLanguageChooser() throws InterruptedException {
        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            StringProperty languageProperty = new SimpleStringProperty("eng");
            StringProperty countryProperty = new SimpleStringProperty("GB");
            try (MockedStatic<ActiveConfigurationContext> utilities = Mockito.mockStatic(ActiveConfigurationContext.class)) {
                when(mockContext.getGazePlay()).thenReturn(mockGazePlay);
                when(mockConfig.getLanguage()).thenReturn(languageProperty.getValue());
                when(mockConfig.getCountry()).thenReturn(countryProperty.getValue());
                when(mockConfig.getLanguageProperty()).thenReturn(languageProperty);
                when(mockConfig.getCountryProperty()).thenReturn(countryProperty);
                writeInto(languageProperty).when(mockConfig).setLanguage(anyString());
                writeInto(countryProperty).when(mockConfig).setCountry(anyString());

                utilities.when(ActiveConfigurationContext::getInstance).thenReturn(mockConfig);
                MenuButton result = context.buildLanguageChooser(mockConfig, context);

                assertEquals(25, result.getItems().size());

                result.getItems().get(1).fire();

                ImageView image = (ImageView) result.getGraphic();
                assertTrue(image.getImage().getUrl().contains("Arab"));
                assertEquals("ara", languageProperty.getValue());
            }
        });
        TestingUtils.waitForRunLater();
    }

    @Test
    void shouldBuildEyeTrackerChooser() {
        StringProperty eyeTrackerProperty = new SimpleStringProperty("mouse_control");

        when(mockConfig.getEyeTracker()).thenReturn(eyeTrackerProperty.getValue());
        when(mockConfig.getEyeTrackerProperty()).thenReturn(eyeTrackerProperty);
        writeInto(eyeTrackerProperty).when(mockConfig).setEyeTracker(anyString());

        when(mockTranslator.translate("InputTobiiAndMouse")).thenReturn("Tobii and mouse");

        ChoiceBox<EyeTracker> result = ConfigurationContext.buildEyeTrackerConfigChooser(mockConfig, mockTranslator);

        // Mouse only, Tobii only, both (EyeTribe was removed)
        assertEquals(3, result.getItems().size());
        assertEquals(EyeTracker.mouse_control, result.getValue());

        result.setValue(EyeTracker.tobii_and_mouse);

        assertEquals("tobii_and_mouse", eyeTrackerProperty.getValue());
        assertEquals("Tobii and mouse", result.getConverter().toString(EyeTracker.tobii_and_mouse));
    }

    @Test
    void shouldBuildCheckBox() throws InterruptedException {
        BooleanProperty testProperty = new SimpleBooleanProperty(true);

        CheckBox result = ConfigurationContext.buildCheckBox(testProperty);
        assertTrue(result.isSelected());

        Platform.runLater(result::fire);
        TestingUtils.waitForRunLater();

        assertFalse(testProperty.getValue());
    }

    /**
     * The orientation chosen in the settings is saved and read again at the next start (a new configuration read
     * from the file, as after a restart).
     */
    @Test
    void shouldSaveTheOrientationOfTheMenu() {
        ActiveConfigurationContext.switchToDefaultUser();
        Configuration configuration = ActiveConfigurationContext.getInstance();
        ChoiceBox<GameButtonOrientation> chooser = ConfigurationContext.buildGameButtonOrientationChooser(configuration);

        chooser.setValue(GameButtonOrientation.VERTICAL);
        ActiveConfigurationContext.switchToDefaultUser();

        assertEquals(GameButtonOrientation.VERTICAL,
            GameButtonOrientation.fromConfig(ActiveConfigurationContext.getInstance()));
        chooser.setValue(GameButtonOrientation.HORIZONTAL);
    }

    @Test
    void shouldBuildGameButtonOrientationChooser() {
        StringProperty buttonOrientationProperty = new SimpleStringProperty("HORIZONTAL");
        when(mockConfig.getMenuButtonsOrientationProperty()).thenReturn(buttonOrientationProperty);
        when(mockConfig.getMenuButtonsOrientation()).thenReturn(buttonOrientationProperty.getValue());
        writeInto(buttonOrientationProperty).when(mockConfig).setMenuButtonsOrientation(anyString());

        ChoiceBox<GameButtonOrientation> result = ConfigurationContext.buildGameButtonOrientationChooser(mockConfig);

        assertEquals(2, result.getItems().size());
        result.setValue(GameButtonOrientation.VERTICAL);

        assertEquals("VERTICAL", buttonOrientationProperty.getValue());
    }

    /**
     * Since commit 850e26f9a ("Now music don't start by default"), changing the music folder never resumes playback,
     * even if music was playing before.
     */
    @Test
    void shouldChangeTheMusicFolderWithoutResumingPlayback() {
        BackgroundMusicManager mockMusicManager = mock(BackgroundMusicManager.class);
        BackgroundMusicManager.setInstance(mockMusicManager);
        Configuration mockConfiguration = mock(Configuration.class);

        ConfigurationContext.changeMusicFolder("mockFolder", mockConfiguration);

        verify(mockConfiguration).setMusicDir("mockFolder");
        verify(mockMusicManager).emptyPlaylist();
        verify(mockMusicManager).getAudioFromFolder("mockFolder");
        verify(mockMusicManager, never()).play();
    }

    @Test
    void shouldChangeTheMusicFolderWithBlankFolder() {
        BackgroundMusicManager mockMusicManager = mock(BackgroundMusicManager.class);
        BackgroundMusicManager.setInstance(mockMusicManager);
        Configuration mockConfiguration = mock(Configuration.class);
        String expectedFolder = System.getProperty("user.home") + File.separator + "GazePlay" + File.separator + "music";

        ConfigurationContext.changeMusicFolder("", mockConfiguration);

        verify(mockConfiguration).setMusicDir(expectedFolder);
        verify(mockMusicManager).getAudioFromFolder(expectedFolder);
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
    void shouldBuildHeatMapOpacityChoiceBox() throws InterruptedException {
        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            DoubleProperty heatMapProperty = new SimpleDoubleProperty(1);

            when(mockConfig.getHeatMapOpacityProperty()).thenReturn(heatMapProperty);
            when(mockConfig.getHeatMapOpacity()).thenReturn(heatMapProperty.getValue());
            doAnswer(invocation -> {
                heatMapProperty.set(invocation.getArgument(0));
                return null;
            }).when(mockConfig).setHeatMapOpacity(anyDouble());

            ChoiceBox<Double> result = context.buildHeatMapOpacityChoiceBox(mockConfig);

            assertEquals(11, result.getItems().size());

            result.setValue(0.2);
            assertEquals(0.2, heatMapProperty.getValue());
        });
        TestingUtils.waitForRunLater();
    }

    @Test
    void shouldBuildHeatMapColorHBox() throws InterruptedException {
        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            StringProperty heatMapProperty = new SimpleStringProperty("001122, 110022, 002211, 112200");
            List<Color> colors = List.of(Color.web("001122"), Color.web("110022"), Color.web("002211"), Color.web("112200"));

            when(mockConfig.getHeatMapColorsProperty()).thenReturn(heatMapProperty);
            when(mockConfig.getHeatMapColors()).thenReturn(colors);

            HBox result = context.buildHeatMapColorHBox(mockConfig, mockTranslator);
            assertEquals(7, result.getChildren().size());

            Button reset = (Button) result.getChildren().get(0);
            Button plus = (Button) result.getChildren().get(1);
            Button minus = (Button) result.getChildren().get(2);

            plus.fire();

            assertEquals(8, result.getChildren().size());

            reset.fire();

            assertEquals(7, result.getChildren().size());

            minus.fire();

            assertEquals(6, result.getChildren().size());
        });
        TestingUtils.waitForRunLater();
    }
}
