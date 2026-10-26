package net.gazeplay.ui.scenes.configuration;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import mslinks.ShellLink;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.ui.SystemFileManager;
import net.gazeplay.commons.configuration.*;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.ui.*;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.LanguageDetails;
import net.gazeplay.commons.utils.multilinguism.Languages;
import net.gazeplay.components.CssUtil;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.scenes.gamemenu.GameButtonOrientation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The settings page: one row per setting of {@link Configuration}, grouped by category (language, games, eye tracking,
 * graphics, folders, statistics...), each control bound to its property so that a change is saved at once. Arabic is
 * laid out from right to left.
 */
@Slf4j
public class ConfigurationContext extends GraphicalContext<BorderPane> {

    private static final String COLON = "Colon";

    private static final double PREF_WIDTH = 200;

    private static final double PREF_HEIGHT = 25;

    private final boolean currentLanguageAlignmentIsLeftAligned;

    private IGameVariant currentSelectedVariant;
    private GameSpec currentSelectedGame;

    private GridPane gridPane;

    Configuration config = ActiveConfigurationContext.getInstance();

    /**
     * Builds the page for the active profile, with a home button and the music controls.
     */
    public ConfigurationContext(GazePlay gazePlay) {
        super(gazePlay, new BorderPane());

        Translator translator = gazePlay.getTranslator();

        Locale currentLocale = translator.currentLocale();
        LanguageDetails languageDetails = Languages.getLocale(currentLocale);
        currentLanguageAlignmentIsLeftAligned = languageDetails.isLeftAligned();

        // Bottom Pane
        HomeButton homeButton = createHomeButtonInConfigurationManagementScreen(gazePlay);

        I18NTooltip tooltipBackToMenu = new I18NTooltip(gazePlay.getTranslator(), "BackToMenu");
        I18NTooltip.install(homeButton, tooltipBackToMenu);

        HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER_RIGHT);
        if (currentLanguageAlignmentIsLeftAligned) {
            rightControlPane.getChildren().add(homeButton);
        }

        HBox leftControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlPaneLayout(leftControlPane);
        leftControlPane.setAlignment(Pos.CENTER_LEFT);
        // HomeButton on the Left for Arabic Language
        if (!currentLanguageAlignmentIsLeftAligned) {
            leftControlPane.getChildren().add(homeButton);
        }

        BorderPane bottomControlPane = new BorderPane();
        bottomControlPane.setLeft(leftControlPane);
        bottomControlPane.setRight(rightControlPane);

        root.setBottom(bottomControlPane);

        // Top Pane
        I18NText configTitleText = new I18NText(translator, "ConfigTitle");
        configTitleText.setId("title");
        configTitleText.setTextAlignment(TextAlignment.CENTER);

        // Arabic title alignment
        if (!currentLanguageAlignmentIsLeftAligned) {
            BorderPane.setAlignment(configTitleText, Pos.BOTTOM_RIGHT);
        }

        root.setTop(configTitleText);

        // Center Pane

        GridPane gridPane = buildConfigGridPane(this, translator);

        ScrollPane settingsPanelScroller = new ScrollPane(gridPane);

        settingsPanelScroller.setFitToWidth(true);
        settingsPanelScroller.setFitToHeight(true);

        gridPane.setAlignment(Pos.CENTER);
        VBox centerCenterPane = new VBox();
        centerCenterPane.setSpacing(40);
        centerCenterPane.setAlignment(Pos.TOP_CENTER);
        // Arabic title alignment
        if (!currentLanguageAlignmentIsLeftAligned) {
            gridPane.setAlignment(Pos.TOP_RIGHT);
        } else {
            gridPane.setAlignment(Pos.TOP_LEFT);
        }

        gridPane.setPadding(new Insets(20));
        centerCenterPane.getChildren().add(settingsPanelScroller);

        root.setCenter(centerCenterPane);

        this.changeConstrutorCss(config);
    }

    /**
     * @return the home button, which applies the theme and goes back to the menu
     */
    HomeButton createHomeButtonInConfigurationManagementScreen(@NonNull GazePlay gazePlay) {
        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();
        HomeButton homeButton = new HomeButton(screenDimension);

        EventHandler<Event> homeEvent = e -> {
            gazePlay.onDisplayConfigurationManagement();
            if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
                root.setCursor(Cursor.WAIT); // Change cursor to wait style
                gazePlay.onReturnToMenu();
                root.setCursor(Cursor.DEFAULT); // Change cursor to default style
            }
        };

        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);
        return homeButton;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    /**
     * @return the grid of all the settings
     */
    GridPane buildConfigGridPane(ConfigurationContext configurationContext, Translator translator) {
        Configuration config = ActiveConfigurationContext.getInstance();

        if ((config.getUserName()) != null && !config.getUserName().equals("")) {
            ActiveConfigurationContext.switchToUser(config.getUserName());
        } else {
            ActiveConfigurationContext.switchToDefaultUser();
        }

        config = ActiveConfigurationContext.getInstance();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(50);
        grid.setVgap(50);

        grid.getStyleClass().add("item");

        AtomicInteger currentFormRow = new AtomicInteger(1);

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "LanguageSettings", COLON));
        // Language settings
        {
            I18NText label = new I18NText(translator, "Lang", COLON);

            MenuButton input = buildLanguageChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        if (Utils.isWindows()) {
            {
                I18NText label = new I18NText(translator, "CreateShortcut", COLON);

                VBox input = buildVariantShortcutMaker(config, configurationContext);

                addToGrid(grid, currentFormRow, label, input);
            }
        }

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "GamesSettings", COLON));
        // Games settings
        {
            I18NText label = new I18NText(translator, "QuitKey", COLON);

            ChoiceBox<String> input = buildQuitKeyChooser(config);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "QuestionLength", COLON);

            Spinner<Double> input = buildSpinner(0.5, 20, (double) config.getQuestionLength() / 1000,
                0.5, config.getQuestionLengthProperty());

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "ReaskQuestionOnFail", COLON);

            CheckBox input = buildCheckBox(config.getQuestionReaskedOnFailProperty());

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "EnableRewardSound", COLON);

            CheckBox input = buildCheckBox(config.getRewardSoundEnabledProperty());

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "Limiter Time", COLON);

            HBox input = buildLimiterTime(config, translator);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "Limiter Score", COLON);

            HBox input = buildLimiterScore(config, translator);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "PianoTileColors", COLON);

            HBox input = chooseColorHBox(config.getTileColorsProperty());

            addToGrid(grid, currentFormRow, label, input);
        }


        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "EyeTrackerSettings", COLON));
        // Eye Tracking settings
        {
            I18NText label = new I18NText(translator, "EyeTracker", COLON);

            ChoiceBox<EyeTracker> input = buildEyeTrackerConfigChooser(config, translator);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "FixationLength", COLON);

            Spinner<Double> input = buildSpinner(0, 10, (double) config.getFixationLength() / 1000,
                0.1, config.getFixationLengthProperty());

            addToGrid(grid, currentFormRow, label, input);
        }

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "GraphicsSettings", COLON));
        // Graphics settings
        {
            I18NText label = new I18NText(translator, "LayoutFile", COLON);

            ChoiceBox<BuiltInUiTheme> input = buildStyleThemeChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "BackgroundColor", COLON);

            HBox input = chooseColorHBox(config.getBackgroundColorProperty());

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "MenuOrientation", COLON);
            ChoiceBox<GameButtonOrientation> input = buildGameButtonOrientationChooser(config);
            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "BackgroundEnabled", COLON);
            CheckBox input = buildCheckBox(config.getBackgroundEnabledProperty());

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "DarkTh", COLON);
            HBox darkTheme = buildDarktheme(config, configurationContext);
            addToGrid(grid, currentFormRow, label, darkTheme);
        }

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "FoldersSettings", COLON));
        // Folders settings
        {
            I18NText label = new I18NText(translator, "FileDir", COLON);

            Node input = buildImageChooser(config, configurationContext, translator);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "MusicFolder", COLON);
            final Node input = buildDirectoryChooser(config, configurationContext, translator, DirectoryType.MUSIC);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "WhereIsItDirectory", COLON);

            Node input = buildDirectoryChooser(config, configurationContext, translator, DirectoryType.WHERE_IS_IT);

            addToGrid(grid, currentFormRow, label, input);
        }

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "StatsSettings", COLON));
        // Stats settings
        {
            // Opens the folder of the results
            I18NText label = new I18NText(translator, "ResultDir", COLON);

            Node input = buildResultFolder(config, configurationContext, translator);

            addToGrid(grid, currentFormRow, label, input);
        }
        addSubCategoryTitle(grid, currentFormRow, new I18NText(translator, "HeatMapSettings", COLON));
        // HeatMap settings
        {
            I18NText label = new I18NText(translator, "DisableHeatMap", COLON);

            CheckBox input = buildCheckBox(config.getHeatMapDisabledProperty());

            addSubToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "HeatMapOpacity", COLON);
            ChoiceBox<Double> input = buildHeatMapOpacityChoiceBox(config);

            addSubToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "HeatMapColors", COLON);
            HBox input = buildHeatMapColorHBox(config, translator);

            addSubToGrid(grid, currentFormRow, label, input);
        }

        addSubCategoryTitle(grid, currentFormRow, new I18NText(translator, "AOISettings", COLON));
        // AOI settings
        {
            I18NText label = new I18NText(translator, "EnableAreaOfInterest", COLON);

            CheckBox input = buildCheckBox(config.getAreaOfInterestDisabledProperty());

            addSubToGrid(grid, currentFormRow, label, input);
        }
        addSubCategoryTitle(grid, currentFormRow, new I18NText(translator, "MoreStatsSettings", COLON));
        // More Stats settings
        {
            I18NText label = new I18NText(translator, "DisableSequence", COLON);

            CheckBox input = buildCheckBox(config.getFixationSequenceDisabledProperty());

            addSubToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "EnableVideoRecording", COLON);

            CheckBox input = buildCheckBox(config.getVideoRecordingEnabledProperty());

            addSubToGrid(grid, currentFormRow, label, input);
        }

        this.changeGridCss(config);
        return grid;
    }

    /**
     * Adds the title of a category on a new row of the grid (on the right side for a right-to-left language).
     */
    void addCategoryTitle(GridPane grid, AtomicInteger currentFormRow, I18NText label) {
        int columnIndexLabelLeft = 0;
        int columnIndexLabelRight = 2;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");

        Separator s = new Separator();
        grid.add(s, 0, currentRowIndex, 4, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        int newCurrentRowIndex = currentFormRow.incrementAndGet();
        if (currentLanguageAlignmentIsLeftAligned) {
            grid.add(label, columnIndexLabelLeft, newCurrentRowIndex);
            GridPane.setHalignment(label, HPos.LEFT);
        } else {
            grid.add(label, columnIndexLabelRight, newCurrentRowIndex);
            GridPane.setHalignment(label, HPos.RIGHT);
        }
    }

    /**
     * Adds the title of a subcategory on a new row of the grid.
     */
    void addSubCategoryTitle(GridPane grid, AtomicInteger currentFormRow, I18NText label) {
        int columnIndexLabelLeft = 1;
        int columnIndexLabelRight = 2;
        int columnIndexInputLeft = 1;
        int columnIndexInputRight = 0;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");

        Separator s = new Separator();

        if (currentLanguageAlignmentIsLeftAligned) {
            grid.add(label, columnIndexLabelLeft, currentRowIndex);
            GridPane.setHalignment(label, HPos.LEFT);
            grid.add(s, columnIndexInputLeft + 1, currentRowIndex, 2, 1);
            GridPane.setHalignment(s, HPos.LEFT);
        } else {
            grid.add(label, columnIndexLabelRight, currentRowIndex);
            GridPane.setHalignment(label, HPos.RIGHT);
            grid.add(s, columnIndexInputRight + 1, currentRowIndex, 2, 1);
            GridPane.setHalignment(s, HPos.RIGHT);
        }
    }

    /**
     * Adds a setting (its label and its control) on a new row of the grid.
     */
    void addToGrid(GridPane grid, AtomicInteger currentFormRow, I18NText label, final Node input) {
        int columnIndexLabelLeft = 1;
        int columnIndexInputLeft = 2;
        int columnIndexLabelRight = 1;
        int columnIndexInputRight = 0;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");

        if (currentLanguageAlignmentIsLeftAligned) {
            grid.add(label, columnIndexLabelLeft, currentRowIndex);
            GridPane.setHalignment(label, HPos.LEFT);
            grid.add(input, columnIndexInputLeft, currentRowIndex);
            GridPane.setHalignment(input, HPos.LEFT);
        } else {
            grid.add(label, columnIndexLabelRight, currentRowIndex);
            GridPane.setHalignment(label, HPos.RIGHT);
            grid.add(input, columnIndexInputRight, currentRowIndex);
            GridPane.setHalignment(input, HPos.RIGHT);
        }
    }

    /**
     * Adds a setting of a subcategory, indented, on a new row of the grid.
     */
    void addSubToGrid(GridPane grid, AtomicInteger currentFormRow, I18NText label, final Node input) {
        int columnIndexLabelLeft = 2;
        int columnIndexInputLeft = 3;
        int columnIndexLabelRight = 2;
        int columnIndexInputRight = 1;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");

        if (currentLanguageAlignmentIsLeftAligned) {
            grid.add(label, columnIndexLabelLeft, currentRowIndex);
            GridPane.setHalignment(label, HPos.LEFT);
            grid.add(input, columnIndexInputLeft, currentRowIndex);
            GridPane.setHalignment(input, HPos.LEFT);
        } else {
            grid.add(label, columnIndexLabelRight, currentRowIndex);
            GridPane.setHalignment(label, HPos.RIGHT);
            grid.add(input, columnIndexInputRight, currentRowIndex);
            GridPane.setHalignment(input, HPos.RIGHT);
        }
    }

    /**
     * @return a spinner of a decimal setting, bound to its property, between {@code min} and {@code max}
     */
    static Spinner<Double> buildSpinner(
        double min,
        double max,
        double initialValue,
        double step,
        Property<Number> configProperty
    ) {
        Spinner<Double> spinner = new Spinner<>(min, max, initialValue, step);
        spinner.setEditable(true);
        spinner.setPrefWidth(PREF_WIDTH);
        spinner.setPrefHeight(PREF_HEIGHT);

        spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue >= min || newValue <= max) {
                final int newPropertyValue = (int) (1000 * spinner.getValue());
                configProperty.setValue(newPropertyValue);
            } else if (newValue > max) {
                final int newPropertyValue = (int) (1000 * max);
                configProperty.setValue(newPropertyValue);
            } else if (newValue < min) {
                final int newPropertyValue = (int) (1000 * min);
                configProperty.setValue(newPropertyValue);
            }
        });

        return spinner;
    }

    /**
     * Function to use to permit to user to select between several theme
     */
    static ChoiceBox<BuiltInUiTheme> buildStyleThemeChooser(Configuration configuration, ConfigurationContext configurationContext) {
        ChoiceBox<BuiltInUiTheme> themesBox = new ChoiceBox<>();

        final String cssFile = configuration.getCssFile();
        themesBox.getItems().addAll(BuiltInUiTheme.values());
        Optional<BuiltInUiTheme> configuredTheme = BuiltInUiTheme.findFromConfigPropertyValue(cssFile);
        BuiltInUiTheme selected = configuredTheme.orElse(BuiltInUiTheme.DEFAULT_THEME);

        themesBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(BuiltInUiTheme object) {
                return object.getLabel();
            }

            @Override
            public BuiltInUiTheme fromString(String string) {
                return null;
            }
        });

        themesBox.getSelectionModel().select(selected);
        themesBox.setPrefWidth(PREF_WIDTH);
        themesBox.setPrefHeight(PREF_HEIGHT);

        themesBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String newPropertyValue = newValue.getPreferredConfigPropertyValue();
            configuration.setCssFile(newPropertyValue);
            final GazePlay gazePlay = configurationContext.getGazePlay();

            CssUtil.setPreferredStylesheets(configuration, gazePlay.getPrimaryScene(), gazePlay.getCurrentScreenDimensionSupplier());
        });

        return themesBox;
    }

    /**
     * Folders that can be chosen in the settings.
     */
    enum DirectoryType {
        FILE, WHERE_IS_IT, MUSIC, SHORTCUT
    }

    /**
     * @return the button opening the chooser of the images of the games (see {@link CustomFileChooser})
     */
    private Node buildImageChooser(Configuration configuration,
                                   ConfigurationContext configurationContext,
                                   Translator translator) {

        final I18NButton selectButton = new I18NButton(translator, "Select");
        Stage dialog = new CustomFileChooser(configuration, configurationContext, translator, getGazePlay());

        selectButton.setOnAction(e -> {
            dialog.show();
            dialog.sizeToScene();
            getGazePlay().getPrimaryStage().getScene().getRoot().setEffect(new GaussianBlur());
        });

        return selectButton;
    }

    /**
     * @return the button opening the folder of the results in the file manager of the system
     */
    private Node buildResultFolder(Configuration configuration,
                                   ConfigurationContext configurationContext,
                                   Translator translator) {


        final I18NButton selectButton = new I18NButton(translator, "SeeFolder");

        selectButton.setOnAction(e -> {
            // The folder where Stats writes the results of the current profile (it used to be a hard-coded
            // C:\Users\<user.name> path, wrong when user.home is elsewhere)
            final File folder = GazePlayDirectories.getUserStatsFolder(configuration.getUserName());
            if (!folder.isDirectory() && !folder.mkdirs()) {
                log.warn("Cannot create the folder {}", folder);
            }
            SystemFileManager.openFolder(folder);
        });

        return selectButton;
    }

    /**
     * @return the button choosing a folder of a setting (files, "Where is it?" images, music or shortcuts), showing the
     *   current folder, and a reset button going back to the default folder
     */
    Node buildDirectoryChooser(
        Configuration configuration,
        ConfigurationContext configurationContext,
        Translator translator,
        DirectoryType type
    ) {
        final HBox pane = new HBox(5);
        final String fileDir;
        Button buttonLoad;

        // Arabic Alignment
        if (!currentLanguageAlignmentIsLeftAligned) {
            pane.setAlignment(Pos.BASELINE_RIGHT);
        }

        switch (type) {
            case WHERE_IS_IT:
                fileDir = configuration.getWhereIsItDir();
                break;
            case MUSIC:
                changeMusicFolder(configuration.getMusicDir(), configuration);
                fileDir = configuration.getMusicDir();
                break;
            case SHORTCUT:
                fileDir = configuration.getShortcutDir();
                break;
            default:
                fileDir = configuration.getFileDir();
        }

        buttonLoad = new Button(fileDir);

        buttonLoad.setOnAction(arg0 -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            final File currentFolder;

            switch (type) {
                case WHERE_IS_IT:
                    currentFolder = new File(configuration.getWhereIsItDir());
                    break;
                case MUSIC:
                    currentFolder = new File(configuration.getMusicDir());
                    break;
                case SHORTCUT:
                    currentFolder = new File(configuration.getShortcutDir());
                    break;
                default:
                    currentFolder = new File(configuration.getFileDir());
            }

            if (currentFolder.isDirectory()) {
                directoryChooser.setInitialDirectory(currentFolder);
            }
            final GazePlay gazePlay = configurationContext.getGazePlay();
            final Scene scene = gazePlay.getPrimaryScene();
            File file = directoryChooser.showDialog(scene.getWindow());
            if (file == null) {
                return;
            }

            String newPropertyValue = file.getAbsolutePath();

            if (Utils.isWindows()) {
                newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
            }

            buttonLoad.textProperty().setValue(newPropertyValue);

            switch (type) {
                case WHERE_IS_IT:
                    configuration.setWhereIsItDir(newPropertyValue);
                    break;
                case MUSIC:
                    changeMusicFolder(newPropertyValue, configuration);
                    break;
                case SHORTCUT:
                    configuration.setShortcutDir(newPropertyValue);
                    break;
                default:
                    configuration.setFileDir(newPropertyValue);
            }

        });

        final I18NButton resetButton = new I18NButton(translator, "Reset");

        switch (type) {
            case WHERE_IS_IT:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = Configuration.DEFAULT_VALUE_WHERE_IS_IT_DIR;
                        configuration.setWhereIsItDir(defaultValue);
                        buttonLoad.textProperty().setValue(defaultValue);
                    });
                break;
            case MUSIC:
                resetButton.setOnAction(
                    e -> {
                        changeMusicFolder(Configuration.DEFAULT_VALUE_MUSIC_DIR, configuration);
                        buttonLoad.textProperty().setValue(configuration.getMusicDir());
                    });
                break;
            case SHORTCUT:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = GazePlayDirectories.getShortcutDirectory().getAbsolutePath();
                        configuration.setShortcutDir(defaultValue);
                        buttonLoad.textProperty().setValue(defaultValue);
                    });
                break;
            default:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath();
                        configuration.setFileDir(defaultValue);
                        buttonLoad.textProperty().setValue(defaultValue);
                    });
        }

        pane.getChildren().addAll(buttonLoad, resetButton);

        return pane;
    }

    /**
     * @return the controls creating a shortcut that starts a game (and a variant) directly: the folder of the
     *   shortcuts, the game, and the "Generate" button
     */
    VBox buildVariantShortcutMaker(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {

        VBox shortCutBox = new VBox(buildDirectoryChooser(configuration, configurationContext, getGazePlay().getTranslator(), DirectoryType.SHORTCUT));
        MenuButton gameBox = new MenuButton();
        I18NButton generateButton = new I18NButton(getGazePlay().getTranslator(), "Generate");
        gameBox.setText(getGazePlay().getTranslator().translate("SelectGame"));
        gameBox.textProperty().addListener((arg, oldVal, newVal) -> {
            if (newVal.equals(getGazePlay().getTranslator().translate("SelectGame"))) {
                generateButton.setDisable(true);
                generateButton.setOpacity(0.5);
            } else {
                generateButton.setDisable(false);
                generateButton.setOpacity(1);
            }
        });
        generateButton.setDisable(true);
        generateButton.setOpacity(0.5);
        MenuButton variantBox = new MenuButton(getGazePlay().getTranslator().translate("SelectVariant"));

        String userOption = configuration.getUserName().length() == 0 ? "--default-user" : "--user " + configuration.getUserName();

        GamesLocator gamesLocator = getGazePlay().getGamesLocator();
        if (gamesLocator != null) {
            List<GameSpec> games = gamesLocator.listGames(getGazePlay().getTranslator());
            for (GameSpec game : games) {

                Set<IGameVariant> variants = game.getGameVariantGenerator().getVariants();
                MenuItem gameShortcutItem = new MenuItem(getGazePlay().getTranslator().translate(game.getGameSummary().getNameCode()));
                gameShortcutItem.setOnAction(event -> {
                    currentSelectedGame = game;
                    currentSelectedVariant = null;
                    variantBox.getItems().clear();
                    variantBox.setText(getGazePlay().getTranslator().translate("SelectVariant"));
                    shortCutBox.getChildren().remove(variantBox);
                    gameBox.setText(gameShortcutItem.getText());
                    if (variants.size() > 0) {
                        shortCutBox.getChildren().remove(generateButton);

                        for (IGameVariant variant : variants) {
                            MenuItem gameAndVariantShortcut = new MenuItem(variant.getLabel(getGazePlay().getTranslator()));

                            gameAndVariantShortcut.setOnAction(event2 -> {

                                currentSelectedVariant = variant;
                                variantBox.setText(gameAndVariantShortcut.getText());
                            });

                            variantBox.getItems().add(gameAndVariantShortcut);
                        }
                        shortCutBox.getChildren().add(variantBox);
                        shortCutBox.getChildren().add(generateButton);
                    }

                });

                gameBox.getItems().add(gameShortcutItem);
            }
        }
        shortCutBox.getChildren().add(gameBox);

        generateButton.setOnAction(e3 -> {
            if (currentSelectedGame != null) {
                String gameOption = "--game \"" + currentSelectedGame.getGameSummary().getNameCode() + "\"";
                String variantOption = currentSelectedVariant == null ? "" : "--variant \"" + currentSelectedVariant.toString() + "\"";
                Path currentRelativePath = Paths.get("");
                String currentBinPath = currentRelativePath.toAbsolutePath().toString();
                ShellLink slwithvariant = ShellLink.createLink(currentBinPath + "\\gazeplay-windows.bat")
                    .setWorkingDir(currentBinPath)
                    .setIconLocation(currentBinPath + "\\gazeplayicon.ico")
                    .setCMDArgs(userOption + " " + gameOption + " " + variantOption);
                slwithvariant.getHeader().setIconIndex(0);

                try {
                    slwithvariant.saveTo(configuration.getShortcutDir() + "\\" + gameBox.getText() + (currentSelectedVariant == null ? "" : (" - " + variantBox.getText())) + ".lnk");
                } catch (IOException e) {
                    log.warn("Cannot create the shortcut of the game", e);
                }
            }
        });

        shortCutBox.getChildren().add(generateButton);

        gameBox.setPrefWidth(PREF_WIDTH);
        gameBox.setPrefHeight(PREF_HEIGHT);

        return shortCutBox;
    }

    /**
     * @return the menu of the languages, with their flags; choosing one translates the interface at once
     */
    MenuButton buildLanguageChooser(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        String currentCodeLanguage = configuration.getLanguage();
        String currentCodeCountry = configuration.getCountry();
        Locale currentLocale = Locale.of(currentCodeLanguage, currentCodeCountry);
        LanguageDetails currentLanguageDetails = Languages.getLocale(currentLocale);

        Image currentFlag = new Image(currentLanguageDetails.getFlags().get(0));
        ImageView currentFlagImageView = new ImageView(currentFlag);
        currentFlagImageView.setPreserveRatio(true);
        currentFlagImageView.setFitHeight(25);

        MenuButton languageBox = new MenuButton(currentLanguageDetails.getLabel(), currentFlagImageView);

        for (LanguageDetails language : Languages.getAllLanguageDetails()) {

            List<String> flags = language.getFlags();

            for (String flag : flags) {

                Image image = new Image(flag);
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(25);

                MenuItem languagesItem = new MenuItem(language.getLabel(), imageView);

                languagesItem.setOnAction(eventMenuLanguages -> {

                    configuration.setLanguage(language.getLocale().getLanguage());
                    configuration.setCountry(language.getLocale().getCountry());
                    ActiveConfigurationContext.getInstance().setLanguage(language.getLocale().getLanguage());
                    ActiveConfigurationContext.getInstance().setCountry(language.getLocale().getCountry());

                    configurationContext.getGazePlay().getTranslator().notifyLanguageChanged();

                    languageBox.setText(language.getLabel());

                    ImageView newImage = new ImageView(image);
                    newImage.setPreserveRatio(true);
                    newImage.setFitHeight(25);
                    languageBox.setGraphic(newImage);

                    if (language.isLeftAligned() != currentLanguageAlignmentIsLeftAligned) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Language information");
                        alert.setHeaderText(
                            "Alignment settings have just changed for your language, please restart GazePlay for the new changes to take effect.");
                        alert.show();
                    }
                    if (!language.isStableTranslationAvailable()) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Language information");
                        alert.setHeaderText(
                            "Translation has just been performed for your language. If you think that some words sound odd in the games, it is maybe a problem of translation. \nPlease contact us to propose better ones at www.gazeplay.net and they will be in the next version.");
                        alert.show();
                    }
                });

                languageBox.getItems().add(languagesItem);
            }
        }

        languageBox.setPrefWidth(PREF_WIDTH);
        languageBox.setPrefHeight(PREF_HEIGHT);

        return languageBox;
    }

    /**
     * @return the choice of the input of the games (mouse only, Tobii only, both), bound to the EYE_TRACKER setting
     */
    static ChoiceBox<EyeTracker> buildEyeTrackerConfigChooser(Configuration configuration, Translator translator) {
        ChoiceBox<EyeTracker> choiceBox = new ChoiceBox<>();

        choiceBox.getItems().addAll(EyeTracker.values());
        // Readable names instead of the values of the setting: mouse only, gaze only, or both
        choiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(EyeTracker eyeTracker) {
                if (eyeTracker == null) {
                    return "";
                }
                return switch (eyeTracker) {
                    case mouse_control -> translator.translate("InputMouseOnly");
                    case tobii -> translator.translate("InputTobiiOnly");
                    case tobii_and_mouse -> translator.translate("InputTobiiAndMouse");
                };
            }

            @Override
            public EyeTracker fromString(String string) {
                return null;
            }
        });

        EyeTracker selectedEyeTracker = findSelectedEyeTracker(configuration);
        choiceBox.getSelectionModel().select(selectedEyeTracker);

        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final String newPropertyValue = newValue.name();
            configuration.setEyeTracker(newPropertyValue);
        });

        return choiceBox;
    }

    /**
     * @return the input of the settings, null for an unknown value
     */
    private static EyeTracker findSelectedEyeTracker(Configuration configuration) {
        for (EyeTracker currentEyeTracker : EyeTracker.values()) {
            if (currentEyeTracker.name().equals(configuration.getEyeTracker())) {
                return currentEyeTracker;
            }
        }
        return null;
    }

    /**
     * @return a check box bound to a boolean setting
     */
    static CheckBox buildCheckBox(BooleanProperty selectionProperty) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selectionProperty.getValue());
        checkBox.selectedProperty().bindBidirectional(selectionProperty);
        return checkBox;
    }

    /**
     * @return the choice of the orientation of the buttons of the menu, bound to the MENU_BUTTONS_ORIENTATION setting
     */
    static ChoiceBox<GameButtonOrientation> buildGameButtonOrientationChooser(
        Configuration configuration
    ) {
        ChoiceBox<GameButtonOrientation> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(GameButtonOrientation.values());
        GameButtonOrientation selectedValue = findSelectedGameButtonOrientation(configuration);
        choiceBox.getSelectionModel().select(selectedValue);
        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final String newPropertyValue = newValue.name();
            configuration.setMenuButtonsOrientation(newPropertyValue);
        });
        return choiceBox;
    }

    /**
     * Sets the music folder and loads its music into the playlist; a blank folder gives the "music" folder of the
     * GazePlay folder, created with the default music.
     */
    static void changeMusicFolder(final String newMusicFolder, Configuration config) {

        String musicFolder = newMusicFolder;

        if (musicFolder.isBlank()) {
            File gazePlayFolder = GazePlayDirectories.getGazePlayFolder();
            File gazePlayMusicFolder = new File(gazePlayFolder, "music");

            String songName = Configuration.DEFAULT_VALUE_BACKGROUND_MUSIC;
            setupNewMusicFolder(gazePlayMusicFolder, songName);

            musicFolder = gazePlayMusicFolder.getAbsolutePath();
        }

        config.setMusicDir(musicFolder);

        BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();

        musicManager.emptyPlaylist();
        musicManager.getAudioFromFolder(musicFolder);

    }

    /**
     * Creates a music folder with the default music in it, if it does not exist.
     */
    static void setupNewMusicFolder(File gazePlayMusicFolder, String defaultSong) {
        // Copy resource into users root folder, and set that to be the new default music folder. Then the user
        // can easily add their own songs to it without having to change configurations.

        if (!gazePlayMusicFolder.exists()) {
            boolean musicFolderCreated = gazePlayMusicFolder.mkdir();
            log.debug("musicFolderCreated = " + musicFolderCreated);
        }

        String resourcePath = "data/home/sounds/" + defaultSong;

        // The stream is closed (it used to leak) and a missing resource is checked instead of a NullPointerException
        try (InputStream defaultMusicTrack = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (defaultMusicTrack == null) {
                log.debug("Could not find {}", resourcePath);
            } else if (gazePlayMusicFolder.exists()) {
                Files.copy(defaultMusicTrack,
                    Paths.get(new File(gazePlayMusicFolder, defaultSong).getAbsolutePath()),
                    StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ie) {
            log.debug(String.format("Could not copy file at %s to %s: %s", resourcePath, gazePlayMusicFolder, ie.toString()));
        }
    }

    /**
     * @return the orientation of the settings, null when unknown
     */
    private static GameButtonOrientation findSelectedGameButtonOrientation(Configuration configuration) {
        final String configValue = configuration.getMenuButtonsOrientation();
        if (configValue == null) {
            return null;
        }
        try {
            return GameButtonOrientation.valueOf(configValue);
        } catch (IllegalArgumentException e) {
            log.warn("IllegalArgumentException : unsupported GameButtonOrientation value : {}", configValue, e);
            return null;
        }
    }

    /**
     * @return the choice of the key leaving a game, bound to the QUIT_KEY setting
     */
    ChoiceBox<String> buildQuitKeyChooser(
        Configuration configuration
    ) {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);
        choiceBox.getItems().addAll("Q", "E");
        choiceBox.getSelectionModel().select(configuration.getQuitKey());
        choiceBox.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> configuration.setQuitKey(newValue));
        return choiceBox;
    }

    /**
     * @return the choice of the opacity of the heatmaps, from 0 to 1, bound to the HEATMAP_OPACITY setting
     */
    ChoiceBox<Double> buildHeatMapOpacityChoiceBox(Configuration config) {
        ChoiceBox<Double> choiceBox = new ChoiceBox<>();
        for (double i = 0; i <= 10; i++) {
            choiceBox.getItems().add(i / 10);
        }
        choiceBox.getSelectionModel().select(config.getHeatMapOpacity());
        choiceBox.setPrefSize(PREF_WIDTH, PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedItemProperty()
            .addListener((observableValue, oldValue, newValue) -> config.setHeatMapOpacity(newValue));
        return choiceBox;
    }

    /**
     * Adds one color picker per color of the heatmaps; a change saves the colors.
     */
    private void fillHBoxWithColorPickers(HBox hbox, Configuration config) {
        for (Color color : config.getHeatMapColors()) {
            ColorPicker colorPicker = new ColorPicker(color);
            colorPicker.valueProperty()
                .addListener((observableValue, color1, t1) -> updateHeatMapColorProperty(hbox, config));
            hbox.getChildren().add(colorPicker);
        }
    }

    /**
     * @return a color picker bound to a color setting
     */
    private HBox chooseColorHBox(StringProperty property){
        HBox hBox = new HBox();
        hBox.setSpacing(5);
        ColorPicker colorPicker = new ColorPicker(Color.web(property.getValue()));
        colorPicker.valueProperty().addListener((observableValue, color, t1) -> property.setValue(colorPicker.getValue().toString()));
        hBox.getChildren().add(colorPicker);
        return hBox;
    }

    /**
     * @return the colors of the heatmaps, with buttons resetting them, adding a color and removing the last one
     */
    HBox buildHeatMapColorHBox(Configuration config, Translator translator) {
        HBox hbox = new HBox();
        hbox.setSpacing(5);

        final I18NButton resetButton = new I18NButton(translator, "Reset");
        Button plusButton = new Button("+");
        Button minusButton = new Button("-");

        hbox.getChildren().addAll(resetButton, plusButton, minusButton);

        int btnCount = hbox.getChildren().size();
        resetButton.prefWidthProperty().bind(hbox.widthProperty().divide(btnCount));

        resetButton.setOnAction((event) -> {
            config.setHeatMapColors(Configuration.DEFAULT_VALUE_HEATMAP_COLORS);
            hbox.getChildren().remove(3, hbox.getChildren().size());
            fillHBoxWithColorPickers(hbox, config);
            minusButton.setDisable(false);
        });

        minusButton.setOnAction(e -> {
            if (hbox.getChildren().size() > 5) {
                hbox.getChildren().remove(hbox.getChildren().size() - 1);
                plusButton.setDisable(false);
            }

            if (hbox.getChildren().size() == 5) {
                minusButton.setDisable(true);
            }
            updateHeatMapColorProperty(hbox, config);
        });

        plusButton.setOnAction(e -> {
            if (hbox.getChildren().size() < 10) {
                ColorPicker colorPicker = new ColorPicker(Color.RED);
                colorPicker.valueProperty()
                    .addListener((observableValue, color1, t1) -> updateHeatMapColorProperty(hbox, config));
                hbox.getChildren().add(colorPicker);
                updateHeatMapColorProperty(hbox, config);
                minusButton.setDisable(false);
            }

            if (hbox.getChildren().size() >= 10) {
                plusButton.setDisable(true);
            }
        });

        fillHBoxWithColorPickers(hbox, config);

        if (hbox.getChildren().size() >= 10) {
            plusButton.setDisable(true);
        } else if (hbox.getChildren().size() <= 5) {
            minusButton.setDisable(true);
        }

        return hbox;
    }

    /**
     * @return the time limit of the games: enabled or not, and its duration in seconds
     */
    private HBox buildLimiterTime(Configuration config, Translator translator) {

        HBox hbox = new HBox();
        hbox.setSpacing(5);

        CheckBox limitTime = buildCheckBox(config.getLimiterTimeEnabledProperty());

        I18NText time = new I18NText(translator, "Time", "Colon");
        time.setFill(Color.WHITE);

        Spinner<Integer> spinnerT = new Spinner<>(3, 180, config.getLimiterTime(), 1);
        spinnerT.setEditable(true);
        spinnerT.setPrefWidth(PREF_WIDTH);
        spinnerT.setPrefHeight(PREF_HEIGHT);

        spinnerT.valueProperty().addListener((observable, oldValue, newValue) -> {
            final int newPropertyValue = spinnerT.getValue();
            config.setLimiterTime(newPropertyValue);
        });

        if (limitTime.isSelected()) {
            time.setVisible(true);
            spinnerT.setVisible(true);
        } else {
            time.setVisible(false);
            spinnerT.setVisible(false);
        }

        limitTime.setOnAction(e -> {
            if (!config.isLimiterTimeEnabled()) {
                time.setVisible(false);
                spinnerT.setVisible(false);
            } else {
                time.setVisible(true);
                spinnerT.setVisible(true);
            }
        });

        hbox.getChildren().addAll(limitTime, time, spinnerT);

        return hbox;
    }

    /**
     * @return the score limit of the games: enabled or not, and the score
     */
    private HBox buildLimiterScore(Configuration config, Translator translator) {

        HBox hbox = new HBox();
        hbox.setSpacing(5);

        CheckBox limitScore = buildCheckBox(config.getLimiterScoreEnabledProperty());

        I18NText score = new I18NText(translator, "Score", "Colon");
        score.setFill(Color.WHITE);

        Spinner<Integer> spinnerS = new Spinner<>(3, 180, config.getLimiterScore(), 1);
        spinnerS.setEditable(true);
        spinnerS.setPrefWidth(PREF_WIDTH);
        spinnerS.setPrefHeight(PREF_HEIGHT);

        spinnerS.valueProperty().addListener((observable, oldValue, newValue) -> {
            final int newPropertyValue = spinnerS.getValue();
            config.setLimiterScore(newPropertyValue);
        });

        if (limitScore.isSelected(
        )) {
            score.setVisible(true);
            spinnerS.setVisible(true);
        } else {
            score.setVisible(false);
            spinnerS.setVisible(false);
        }

        limitScore.setOnAction(e -> {
            if (!config.isLimiterScoreEnabled()) {
                score.setVisible(false);
                spinnerS.setVisible(false);
            } else {
                score.setVisible(true);
                spinnerS.setVisible(true);
            }
        });

        hbox.getChildren().addAll(limitScore, score, spinnerS);

        return hbox;
    }

    /**
     * @return the check box of the dark theme, which restyles the page at once
     */
    private HBox buildDarktheme(Configuration config, ConfigurationContext configurationContext) {

        HBox hbox = new HBox();
        hbox.setSpacing(5);
        CheckBox darkTheme = buildCheckBox(config.getDarkThemeEnabledProperty());
        final GazePlay gazePlay = configurationContext.getGazePlay();
        darkTheme.setOnAction(e -> {
            config.setDarkThemeEnabled(config.isDarkThemeEnabled());
            CssUtil.setPreferredStylesheets(config, gazePlay.getPrimaryScene(), gazePlay.getCurrentScreenDimensionSupplier());
            this.changeConstrutorCss(config);
            this.changeGridCss(config);
        });
        hbox.getChildren().addAll(darkTheme);
        return hbox;
    }

    /**
     * Saves the colors of the color pickers as the colors of the heatmaps.
     */
    private void updateHeatMapColorProperty(HBox hbox, Configuration config) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 3; i < hbox.getChildren().size(); i++) {
            stringBuilder.append(((ColorPicker) (hbox.getChildren().get(i))).getValue().toString());
            if (i != hbox.getChildren().size() - 1)
                stringBuilder.append(",");
        }
        log.info(stringBuilder.toString());
        config.setHeatMapColors(stringBuilder.toString());
    }

    /**
     * Styles the page for the light or dark theme.
     */
    public void changeConstrutorCss(Configuration config){
        if (!config.isDarkThemeEnabled()) {
            root.setStyle(
                "-fx-background-color: #fffaf0; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
        } else {
            root.setStyle(
                "-fx-background-color: rgba(0,0,0,1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
        }
    }

    /**
     * Styles the grid of the settings for the light or dark theme.
     */
    public void changeGridCss(Configuration config){
        if (config.isDarkThemeEnabled()) {
            root.setStyle("-fx-background-color: rgba(0,0,0,1); " + "-fx-background-radius: 8px; "
                + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
        } else {
            root.setStyle("-fx-background-color: #fffaf0; " + "-fx-background-radius: 8px; "
                + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: white; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
        }
    }
}
