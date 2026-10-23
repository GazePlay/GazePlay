package net.gazeplay.ui.scenes.configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
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

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    void shouldReturnToMenuOnHomeButtonPress() throws InterruptedException {
        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);

            HomeButton button = context.createHomeButtonInConfigurationManagementScreen(mockGazePlay);
            button.fireEvent(TestingUtils.clickOnTarget(button));

            verify(mockGazePlay).onReturnToMenu();
        });
        TestingUtils.waitForRunLater();
    }

    @Test
    void shouldBuildConfigGridPane() throws InterruptedException {
        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);

            GridPane pane = context.buildConfigGridPane(context, mockTranslator);

            ObservableList<Node> children = pane.getChildren();

            int notDisplayedElts = 0;
            if (!Utils.isWindows()) {
                notDisplayedElts = 2;
            }

            // Layout of the "gazeplay" edition (the "bera" edition builds another grid)
            assertEquals(72 - notDisplayedElts, children.size());
            // Language settings
            assertTrue(children.get(3) instanceof MenuButton);
            // Games settings: quit key, question length, reask on fail, reward sound, limiters, piano tile colors
            assertTrue(children.get(9 - notDisplayedElts) instanceof ChoiceBox);
            assertTrue(children.get(11 - notDisplayedElts) instanceof Spinner);
            assertTrue(children.get(13 - notDisplayedElts) instanceof CheckBox);
            assertTrue(children.get(15 - notDisplayedElts) instanceof CheckBox);
            assertTrue(children.get(17 - notDisplayedElts) instanceof HBox);
            assertTrue(children.get(19 - notDisplayedElts) instanceof HBox);
            assertTrue(children.get(21 - notDisplayedElts) instanceof HBox);
            // Eye-tracker settings: eye-tracker, fixation length
            assertTrue(children.get(25 - notDisplayedElts) instanceof ChoiceBox);
            assertTrue(children.get(27 - notDisplayedElts) instanceof Spinner);
            // Graphics settings: theme, background color, menu orientation, background enabled, dark theme
            assertTrue(children.get(31 - notDisplayedElts) instanceof ChoiceBox);
            assertTrue(children.get(33 - notDisplayedElts) instanceof HBox);
            assertTrue(children.get(35 - notDisplayedElts) instanceof ChoiceBox);
            assertTrue(children.get(37 - notDisplayedElts) instanceof CheckBox);
            assertTrue(children.get(39 - notDisplayedElts) instanceof HBox);
            // Folders settings: files, music, where is it
            assertTrue(children.get(43 - notDisplayedElts) instanceof Button);
            assertTrue(children.get(45 - notDisplayedElts) instanceof HBox);
            assertTrue(children.get(47 - notDisplayedElts) instanceof HBox);
            // Stats settings: results folder, heatmap (disabled, opacity, colors), AOI (enabled, convex hull),
            // sequence, video
            assertTrue(children.get(51 - notDisplayedElts) instanceof Button);
            assertTrue(children.get(55 - notDisplayedElts) instanceof CheckBox);
            assertTrue(children.get(57 - notDisplayedElts) instanceof ChoiceBox);
            assertTrue(children.get(59 - notDisplayedElts) instanceof HBox);
            assertTrue(children.get(63 - notDisplayedElts) instanceof CheckBox);
            assertTrue(children.get(65 - notDisplayedElts) instanceof CheckBox);
            assertTrue(children.get(69 - notDisplayedElts) instanceof CheckBox);
            assertTrue(children.get(71 - notDisplayedElts) instanceof CheckBox);
        });
        TestingUtils.waitForRunLater();

    }

    @Test
    void shouldAddCategoryTitleLeftAligned() throws InterruptedException {
        when(mockTranslator.currentLocale()).thenReturn(Locale.FRANCE);
        when(mockTranslator.translate(anyString())).thenReturn("category");

        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            context.addCategoryTitle(grid, currentFormRow, label);
        });
        TestingUtils.waitForRunLater();

        assertTrue(grid.getChildren().get(0) instanceof Separator);
        assertEquals(HPos.CENTER, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.LEFT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddCategoryTitleRightAligned() throws InterruptedException {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));
        when(mockTranslator.translate(anyString())).thenReturn("category");

        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            context.addCategoryTitle(grid, currentFormRow, label);
        });
        TestingUtils.waitForRunLater();

        assertTrue(grid.getChildren().get(0) instanceof Separator);
        assertEquals(HPos.CENTER, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.RIGHT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddSubcategoryTitleLeftAligned() throws InterruptedException {
        when(mockTranslator.currentLocale()).thenReturn(Locale.FRANCE);
        when(mockTranslator.translate(anyString())).thenReturn("category");

        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            context.addSubCategoryTitle(grid, currentFormRow, label);
        });
        TestingUtils.waitForRunLater();

        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.LEFT, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().get(1) instanceof Separator);
        assertEquals(HPos.LEFT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddSubcategoryTitleRightAligned() throws InterruptedException {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));
        when(mockTranslator.translate(anyString())).thenReturn("category");

        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");

        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            context.addSubCategoryTitle(grid, currentFormRow, label);
        });
        TestingUtils.waitForRunLater();

        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.RIGHT, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().get(1) instanceof Separator);
        assertEquals(HPos.RIGHT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddNodeToGridTitleLeftAligned() throws InterruptedException {
        when(mockTranslator.currentLocale()).thenReturn(Locale.FRANCE);
        when(mockTranslator.translate(anyString())).thenReturn("category");

        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");
        CheckBox input = new CheckBox();

        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            context.addToGrid(grid, currentFormRow, label, input);
        });
        TestingUtils.waitForRunLater();

        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.LEFT, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().contains(input));
        assertEquals(HPos.LEFT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
    }

    @Test
    void shouldAddNodeToGridRightAligned() throws InterruptedException {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));
        when(mockTranslator.translate(anyString())).thenReturn("category");

        GridPane grid = new GridPane();
        AtomicInteger currentFormRow = new AtomicInteger(1);
        I18NText label = new I18NText(mockTranslator, "category");
        CheckBox input = new CheckBox();

        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            context.addToGrid(grid, currentFormRow, label, input);
        });
        TestingUtils.waitForRunLater();

        assertTrue(grid.getChildren().contains(label));
        assertEquals(HPos.RIGHT, grid.getChildren().get(0).getProperties().get("gridpane-halignment"));
        assertTrue(grid.getChildren().contains(input));
        assertEquals(HPos.RIGHT, grid.getChildren().get(1).getProperties().get("gridpane-halignment"));
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
    void shouldCreateThemeChooserNonDefault() {
        StringProperty cssFileProperty = new SimpleStringProperty("builtin:BLUE");
        ObservableList<String> stylesheets = FXCollections.observableArrayList();
        Scene mockScene = mock(Scene.class);

        when(mockConfig.getCssFile()).thenReturn("builtin:BLUE");
        when(mockConfig.getCssFileProperty()).thenReturn(cssFileProperty);
        writeInto(cssFileProperty).when(mockConfig).setCssFile(anyString());
        when(mockContext.getGazePlay()).thenReturn(mockGazePlay);
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockScene.getStylesheets()).thenReturn(stylesheets);

        ChoiceBox<BuiltInUiTheme> result = ConfigurationContext.buildStyleThemeChooser(mockConfig, mockContext);

        assertEquals(BuiltInUiTheme.values().length, result.getItems().size());
        assertEquals(BuiltInUiTheme.BLUE, result.getValue());
        assertEquals(cssFileProperty.getValue(), "builtin:BLUE");

        result.setValue(BuiltInUiTheme.GREEN);

        assertEquals(BuiltInUiTheme.GREEN, result.getValue());
        assertEquals(cssFileProperty.getValue(), "builtin:GREEN");
    }

    @Test
    void shouldCreateThemeChooserDefault() {
        StringProperty cssFileProperty = new SimpleStringProperty("builtin:WRONG");
        ObservableList<String> stylesheets = FXCollections.observableArrayList();
        Scene mockScene = mock(Scene.class);

        when(mockConfig.getCssFile()).thenReturn("builtin:WRONG");
        when(mockConfig.getCssFileProperty()).thenReturn(cssFileProperty);
        writeInto(cssFileProperty).when(mockConfig).setCssFile(anyString());
        when(mockContext.getGazePlay()).thenReturn(mockGazePlay);
        when(mockGazePlay.getPrimaryScene()).thenReturn(mockScene);
        when(mockScene.getStylesheets()).thenReturn(stylesheets);

        ChoiceBox<BuiltInUiTheme> result = ConfigurationContext.buildStyleThemeChooser(mockConfig, mockContext);

        assertEquals(BuiltInUiTheme.values().length, result.getItems().size());
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

        if (type != ConfigurationContext.DirectoryType.SHORTCUT) {
            Platform.runLater(() -> {
                ConfigurationContext context = new ConfigurationContext(mockGazePlay);
                HBox result = (HBox) context.buildDirectoryChooser(mockConfig, mockContext, mockTranslator, type);
                Button loadButton = (Button) result.getChildren().get(0);
                Button resetButton = (Button) result.getChildren().get(1);

                assertEquals(fileDirProperty.getValue(), loadButton.textProperty().getValue());

                resetButton.fire();
                assertEquals(answers.get(type), fileDirProperty.getValue());
                assertEquals(answers.get(type), loadButton.textProperty().getValue());
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
    void shouldBuildQuitKeyChooser() throws InterruptedException {
        Platform.runLater(() -> {
            ConfigurationContext context = new ConfigurationContext(mockGazePlay);
            StringProperty quitProperty = new SimpleStringProperty("Y");

            when(mockConfig.getQuitKeyProperty()).thenReturn(quitProperty);
            writeInto(quitProperty).when(mockConfig).setQuitKey(anyString());

            ChoiceBox<String> result = context.buildQuitKeyChooser(mockConfig);
            // Only Q and E are offered since the quit key chooser was restricted
            assertEquals(2, result.getItems().size());

            result.setValue("Q");
            assertEquals("Q", quitProperty.getValue());
        });
        TestingUtils.waitForRunLater();
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
