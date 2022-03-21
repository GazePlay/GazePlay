package net.gazeplay.ui.scenes.configuration;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import mslinks.ShellLink;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.BackgroundStyle;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.configuration.Configuration;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConfigurationContext extends GraphicalContext<BorderPane> {

    private static final String COLON = "Colon";

    private static final double PREF_WIDTH = 200;

    private static final double PREF_HEIGHT = 25;

    private final boolean currentLanguageAlignmentIsLeftAligned;

    private IGameVariant currentSelectedVariant;
    private GameSpec currentSelectedGame;

    private GridPane gridPane;

    ConfigurationContext(GazePlay gazePlay) {
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

        root.setStyle(
            "-fx-background-color: rgba(0,0,0,1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");

    }

    HomeButton createHomeButtonInConfigurationManagementScreen(@NonNull GazePlay gazePlay) {
        Dimension2D screenDimension = gazePlay.getCurrentScreenDimensionSupplier().get();
        HomeButton homeButton = new HomeButton(screenDimension);

        EventHandler<Event> homeEvent = e -> {
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
                I18NText label = new I18NText(translator, "CreateShortCut", COLON);

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

            CheckBox input = buildCheckBox(config.getReaskQuestionOnFail());

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "EnableRewardSound", COLON);

            CheckBox input = buildCheckBox(config.getEnableRewardSoundProperty());

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


        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "EyeTrackerSettings", COLON));
        // Eye Tracking settings
        {
            I18NText label = new I18NText(translator, "EyeTracker", COLON);

            ChoiceBox<EyeTracker> input = buildEyeTrackerConfigChooser(config);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "FixationLength", COLON);

            Spinner<Double> input = buildSpinner(0, 10, (double) config.getFixationLength() / 1000,
                0.1, config.getFixationlengthProperty());

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
            I18NText label = new I18NText(translator, "BackgroundStyle", COLON);
            HBox input = buildBackgroundStyleToggleGroup(config, translator);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "BackgroundEnabled", COLON);
            CheckBox input = buildCheckBox(config.getBackgroundEnabledProperty());

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "MenuOrientation", COLON);
            ChoiceBox<GameButtonOrientation> input = buildGameButtonOrientationChooser(config);

            addToGrid(grid, currentFormRow, label, input);
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
            I18NText label = new I18NText(translator, "VideoFolder", COLON);
            final Node input = buildDirectoryChooser(config, configurationContext, translator, DirectoryType.VIDEO);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "WhereIsItDirectory", COLON);

            Node input = buildDirectoryChooser(config, configurationContext, translator, DirectoryType.WHERE_IS_IT);

            addToGrid(grid, currentFormRow, label, input);
        }

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "StatsSettings", COLON));
        // Stats settings
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
        {
            I18NText label = new I18NText(translator, "EnableConvexHull", COLON);

            CheckBox input = buildCheckBox(config.getConvexHullDisabledProperty());

            /* REMOVE FROM HERE */
            input.setDisable(true);
            label.setOpacity(0.5);
            /* TO HERE TO ENABLE CONVEX HULL FOR AOI */

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
        {
            I18NText label = new I18NText(translator, "EnableMultipleScreenshots", COLON);

            CheckBox input = buildCheckBox(config.getScreenshot());

            addSubToGrid(grid, currentFormRow, label, input);
        }

        return grid;
    }

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

    public void resetPane(GazePlay gazePlay) {
        gridPane = buildConfigGridPane(this, gazePlay.getTranslator());
    }

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
            configuration.getCssfileProperty().setValue(newPropertyValue);
            final GazePlay gazePlay = configurationContext.getGazePlay();

            CssUtil.setPreferredStylesheets(configuration, gazePlay.getPrimaryScene(), gazePlay.getCurrentScreenDimensionSupplier());
        });

        return themesBox;
    }

    /**
     * Function to use to permit to user to choose his/her own css file
     */
    private static Button buildStyleFileChooser(Configuration configuration,
                                                ConfigurationContext configurationContext) {

        Button buttonLoad = new Button(configuration.getCssFile());

        buttonLoad.setOnAction(arg0 -> {
            FileChooser fileChooser = new FileChooser();
            final GazePlay gazePlay = configurationContext.getGazePlay();
            final Scene scene = gazePlay.getPrimaryScene();
            File file = fileChooser.showOpenDialog(scene.getWindow());
            buttonLoad.setText(file.toString());

            String newPropertyValue = file.toString();
            if (Utils.isWindows()) {
                newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
            }

            configuration.getCssfileProperty().setValue(newPropertyValue);

            scene.getStylesheets().remove(0);
            scene.getStylesheets().add("file://" + newPropertyValue);

            log.info(scene.getStylesheets().toString());
        });

        return buttonLoad;
    }

    enum DirectoryType {
        FILE, WHERE_IS_IT, MUSIC, VIDEO, SHORTCUT
    }

    private Node buildImageChooser(Configuration configuration,
                                   ConfigurationContext configurationContext,
                                   Translator translator) {

        final Button selectButton = new Button("select");
        Stage dialog = new CustomFileChooser(configuration, configurationContext, translator, getGazePlay());

        selectButton.setOnAction(e -> {
            dialog.show();
            dialog.sizeToScene();
            getGazePlay().getPrimaryStage().getScene().getRoot().setEffect(new GaussianBlur());
        });

        return selectButton;
    }

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
                changeMusicFolder(configuration.getMusicFolder(), configuration);
                fileDir = configuration.getMusicFolder();
                break;
            case VIDEO:
                fileDir = configuration.getVideoFolder();
                break;
            case SHORTCUT:
                fileDir = configuration.getShortcutFolder();
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
                    currentFolder = new File(configuration.getMusicFolder());
                    break;
                case VIDEO:
                    currentFolder = new File(configuration.getVideoFolder());
                    break;
                case SHORTCUT:
                    currentFolder = new File(configuration.getShortcutFolder());
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
                    configuration.getWhereIsItDirProperty().setValue(newPropertyValue);
                    break;
                case MUSIC:
                    changeMusicFolder(newPropertyValue, configuration);
                    break;
                case VIDEO:
                    configuration.getVideoFolderProperty().setValue(newPropertyValue);
                    break;
                case SHORTCUT:
                    configuration.getShortcutFolderProperty().setValue(newPropertyValue);
                    break;
                default:
                    configuration.getFiledirProperty().setValue(newPropertyValue);
            }

        });

        final I18NButton resetButton = new I18NButton(translator, "reset");

        switch (type) {
            case WHERE_IS_IT:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = Configuration.DEFAULT_VALUE_WHEREISIT_DIR;
                        configuration.getWhereIsItDirProperty()
                            .setValue(defaultValue);
                        buttonLoad.textProperty().setValue(defaultValue);
                    });
                break;
            case MUSIC:
                resetButton.setOnAction(
                    e -> {
                        changeMusicFolder(Configuration.DEFAULT_VALUE_MUSIC_FOLDER, configuration);
                        buttonLoad.textProperty().setValue(configuration.getMusicFolderProperty().getValue());
                    });
                break;
            case VIDEO:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = GazePlayDirectories.getVideosFilesDirectory().getAbsolutePath();
                        configuration.getVideoFolderProperty().setValue(defaultValue);
                        buttonLoad.textProperty().setValue(defaultValue);
                    });
                break;
            case SHORTCUT:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = GazePlayDirectories.getShortcutDirectory().getAbsolutePath();
                        configuration.getShortcutFolderProperty().setValue(defaultValue);
                        buttonLoad.textProperty().setValue(defaultValue);
                    });
                break;
            default:
                resetButton.setOnAction(
                    e -> {
                        String defaultValue = GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath();
                        configuration.getFiledirProperty().setValue(defaultValue);
                        buttonLoad.textProperty().setValue(defaultValue);
                    });
        }

        pane.getChildren().addAll(buttonLoad, resetButton);

        return pane;
    }

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

        // generateButton.disabledProperty().isNotEqualTo(gameBox.textProperty().isEqualTo("Select Game"));
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
                    slwithvariant.saveTo(configuration.getShortcutFolderProperty().getValue() + "\\" + gameBox.getText() + (currentSelectedVariant == null ? "" : (" - " + variantBox.getText())) + ".lnk");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        shortCutBox.getChildren().add(generateButton);

        gameBox.setPrefWidth(PREF_WIDTH);
        gameBox.setPrefHeight(PREF_HEIGHT);

        return shortCutBox;
    }

    MenuButton buildLanguageChooser(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        String currentCodeLanguage = configuration.getLanguage();
        String currentCodeCountry = configuration.getCountry();
        Locale currentLocale = new Locale(currentCodeLanguage, currentCodeCountry);
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

                    configuration.getLanguageProperty().setValue(language.getLocale().getLanguage());
                    configuration.getCountryProperty().setValue(language.getLocale().getCountry());
                    ActiveConfigurationContext.getInstance().getLanguageProperty().setValue(language.getLocale().getLanguage());
                    ActiveConfigurationContext.getInstance().getCountryProperty().setValue(language.getLocale().getCountry());

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

    static ChoiceBox<EyeTracker> buildEyeTrackerConfigChooser(Configuration configuration) {
        ChoiceBox<EyeTracker> choiceBox = new ChoiceBox<>();

        choiceBox.getItems().addAll(EyeTracker.values());

        EyeTracker selectedEyeTracker = findSelectedEyeTracker(configuration);
        choiceBox.getSelectionModel().select(selectedEyeTracker);

        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            final String newPropertyValue = newValue.name();
            configuration.getEyetrackerProperty().setValue(newPropertyValue);
        });

        return choiceBox;
    }

    private static EyeTracker findSelectedEyeTracker(Configuration configuration) {
        for (EyeTracker currentEyeTracker : EyeTracker.values()) {
            if (currentEyeTracker.name().equals(configuration.getEyeTracker())) {
                return currentEyeTracker;
            }
        }
        return null;
    }


    static CheckBox buildCheckBox(BooleanProperty selectionProperty) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selectionProperty.getValue());
        checkBox.selectedProperty().bindBidirectional(selectionProperty);
        return checkBox;
    }

    private HBox buildBackgroundStyleToggleGroup(
        Configuration configuration,
        Translator translator
    ) {
        ToggleGroup group = new ToggleGroup();
        I18NToggleButton darkButton = new I18NToggleButton(translator, "Dark");
        I18NToggleButton lightButton = new I18NToggleButton(translator, "Light");
        darkButton.setToggleGroup(group);
        lightButton.setToggleGroup(group);


        configuration.getBackgroundStyle().accept(new BackgroundStyleVisitor<Void>() {
            @Override
            public Void visitLight() {
                lightButton.setSelected(true);
                return null;
            }

            @Override
            public Void visitDark() {
                darkButton.setSelected(true);
                return null;
            }
        });

        configuration.getBackgroundStyleProperty().addListener((obs, oldVal, newVal) -> {
            newVal.accept(new BackgroundStyleVisitor<Void>() {
                @Override
                public Void visitLight() {
                    lightButton.setSelected(true);
                    return null;
                }

                @Override
                public Void visitDark() {
                    darkButton.setSelected(true);
                    return null;
                }
            });
        });

        lightButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                configuration.setBackgroundStyle(BackgroundStyle.LIGHT);
            }
        });

        darkButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                configuration.setBackgroundStyle(BackgroundStyle.DARK);
            }
        });

        HBox hb = new HBox();
        hb.getChildren().addAll(darkButton, lightButton);

        return hb;
    }

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
            configuration.getMenuButtonsOrientationProperty().setValue(newPropertyValue);
        });
        return choiceBox;
    }

    static void changeMusicFolder(final String newMusicFolder, Configuration config) {

        String musicFolder = newMusicFolder;

        if (musicFolder.isBlank()) {
            File gazePlayFolder = GazePlayDirectories.getGazePlayFolder();
            File gazePlayMusicFolder = new File(gazePlayFolder, "music");

            String songName = Configuration.DEFAULT_VALUE_BACKGROUND_MUSIC;
            setupNewMusicFolder(gazePlayMusicFolder, songName);

            musicFolder = gazePlayMusicFolder.getAbsolutePath();
        }

        config.getMusicFolderProperty().setValue(musicFolder);

        BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();

        boolean wasPlaying = musicManager.isPlaying();

        musicManager.emptyPlaylist();
        musicManager.getAudioFromFolder(musicFolder);

        if (wasPlaying)
            musicManager.play();
    }

    static void setupNewMusicFolder(File gazePlayMusicFolder, String defaultSong) {
        // Copy resource into users root folder, and set that to be the new default music folder. Then the user
        // can easily add their own songs to it without having to change configurations.

        if (!gazePlayMusicFolder.exists()) {
            boolean musicFolderCreated = gazePlayMusicFolder.mkdir();
            log.debug("musicFolderCreated = " + musicFolderCreated);
        }

        String resourcePath = "data/home/sounds/" + defaultSong;

        try {
            InputStream defaultMusicTrack = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            if (gazePlayMusicFolder.exists()) {
                Files.copy(defaultMusicTrack,
                    Paths.get(new File(gazePlayMusicFolder, defaultSong).getAbsolutePath()),
                    StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (NullPointerException ne) {
            log.debug(String.format("Could not find %s: %s", resourcePath, ne.toString()));
        } catch (IOException ie) {
            log.debug(String.format("Could not copy file at %s to %s: %s", resourcePath, gazePlayMusicFolder, ie.toString()));
        }
    }

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

    ChoiceBox<String> buildQuitKeyChooser(
        Configuration configuration
    ) {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);
        choiceBox.getItems().addAll("Q", "W", "E", "R", "T", "Y");
        choiceBox.getSelectionModel().select(configuration.getQuitKeyProperty().getValue());
        choiceBox.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> configuration.getQuitKeyProperty().setValue(newValue));
        return choiceBox;
    }

    ChoiceBox<Double> buildHeatMapOpacityChoiceBox(Configuration config) {
        ChoiceBox<Double> choiceBox = new ChoiceBox<>();
        for (double i = 0; i <= 10; i++) {
            choiceBox.getItems().add(i / 10);
        }
        choiceBox.getSelectionModel().select(config.getHeatMapOpacity());
        choiceBox.setPrefSize(PREF_WIDTH, PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedItemProperty()
            .addListener((observableValue, oldValue, newValue) -> config.getHeatMapOpacityProperty().setValue(newValue));
        return choiceBox;
    }

    private void fillHBoxWithColorPickers(HBox hbox, Configuration config) {
        for (Color color : config.getHeatMapColors()) {
            ColorPicker colorPicker = new ColorPicker(color);
            colorPicker.valueProperty()
                .addListener((observableValue, color1, t1) -> updateHeatMapColorProperty(hbox, config));
            hbox.getChildren().add(colorPicker);
        }
    }

    HBox buildHeatMapColorHBox(Configuration config, Translator translator) {
        HBox hbox = new HBox();
        hbox.setSpacing(5);

        final I18NButton resetButton = new I18NButton(translator, "reset");
        Button plusButton = new Button("+");
        Button minusButton = new Button("-");

        hbox.getChildren().addAll(resetButton, plusButton, minusButton);

        int btnCount = hbox.getChildren().size();
        resetButton.prefWidthProperty().bind(hbox.widthProperty().divide(btnCount));

        resetButton.setOnAction((event) -> {
            config.getHeatMapColorsProperty().setValue(Configuration.DEFAULT_VALUE_HEATMAP_COLORS);
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

    private HBox buildLimiterTime(Configuration config, Translator translator) {

        HBox hbox = new HBox();
        hbox.setSpacing(5);

        CheckBox limitTime = buildCheckBox(config.getLimiterTProperty());

        I18NText time = new I18NText(translator, "Time(seconds)");
        time.setFill(Color.WHITE);

        Spinner<Integer> spinnerT = new Spinner<>(3, 180, config.getLimiterTime(), 1);
        spinnerT.setEditable(true);
        spinnerT.setPrefWidth(PREF_WIDTH);
        spinnerT.setPrefHeight(PREF_HEIGHT);

        spinnerT.valueProperty().addListener((observable, oldValue, newValue) -> {
            final int newPropertyValue = spinnerT.getValue();
            config.getLimiterTimeProperty().setValue(newPropertyValue);
        });

        if (limitTime.isSelected()) {
            time.setVisible(true);
            spinnerT.setVisible(true);
        } else {
            time.setVisible(false);
            spinnerT.setVisible(false);
        }

        limitTime.setOnAction(e -> {
            if (!config.isLimiterT()) {
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

    private HBox buildLimiterScore(Configuration config, Translator translator) {

        HBox hbox = new HBox();
        hbox.setSpacing(5);

        CheckBox limitScore = buildCheckBox(config.getLimiterSProperty());

        I18NText score = new I18NText(translator, "score");
        score.setFill(Color.WHITE);

        Spinner<Integer> spinnerS = new Spinner<>(3, 180, config.getLimiterScore(), 1);
        spinnerS.setEditable(true);
        spinnerS.setPrefWidth(PREF_WIDTH);
        spinnerS.setPrefHeight(PREF_HEIGHT);

        spinnerS.valueProperty().addListener((observable, oldValue, newValue) -> {
            final int newPropertyValue = spinnerS.getValue();
            config.getLimiterScoreProperty().setValue(newPropertyValue);
        });

        if (limitScore.isSelected()) {
            score.setVisible(true);
            spinnerS.setVisible(true);
        } else {
            score.setVisible(false);
            spinnerS.setVisible(false);
        }

        limitScore.setOnAction(e -> {
            if (!config.isLimiterS()) {
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

    private void updateHeatMapColorProperty(HBox hbox, Configuration config) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 3; i < hbox.getChildren().size(); i++) {
            stringBuilder.append(((ColorPicker) (hbox.getChildren().get(i))).getValue().toString());
            if (i != hbox.getChildren().size() - 1)
                stringBuilder.append(",");
        }
        log.info(stringBuilder.toString());
        config.getHeatMapColorsProperty().setValue(stringBuilder.toString());
    }
}
