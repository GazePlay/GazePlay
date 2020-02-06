package net.gazeplay.ui.scenes.configuration;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.util.StringConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.LanguageDetails;
import net.gazeplay.commons.utils.multilinguism.Languages;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.GraphicalContext;
import net.gazeplay.ui.scenes.gamemenu.GameButtonOrientation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConfigurationContext extends GraphicalContext<BorderPane> {

    private static final String COLON = "Colon";

    private static final double PREF_WIDTH = 200;

    private static final double PREF_HEIGHT = 25;

    private final boolean currentLanguageAlignementIsLeftAligned;

    ConfigurationContext(GazePlay gazePlay) {
        super(gazePlay, new BorderPane());

        Translator translator = gazePlay.getTranslator();

        Locale currentLocale = translator.currentLocale();
        LanguageDetails languageDetails = Languages.getLocale(currentLocale);
        currentLanguageAlignementIsLeftAligned = languageDetails.isLeftAligned();

        // Bottom Pane
        HomeButton homeButton = createHomeButtonInConfigurationManagementScreen(gazePlay);

        HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER_RIGHT);
        if (currentLanguageAlignementIsLeftAligned) {
            rightControlPane.getChildren().add(homeButton);
        }

        HBox leftControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(leftControlPane);
        leftControlPane.setAlignment(Pos.CENTER_LEFT);
        // HomeButton on the Left for Arabic Language
        if (!currentLanguageAlignementIsLeftAligned) {
            leftControlPane.getChildren().add(homeButton);
        }

        BorderPane bottomControlPane = new BorderPane();
        bottomControlPane.setLeft(leftControlPane);
        bottomControlPane.setRight(rightControlPane);

        root.setBottom(bottomControlPane);

        // Top Pane
        I18NText configTitleText = new I18NText(translator, "ConfigTitle");
        // configTitleText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20)); should be managed with css
        configTitleText.setId("title");
        configTitleText.setTextAlignment(TextAlignment.CENTER);

        // Arabic title alignment
        if (!currentLanguageAlignementIsLeftAligned) {
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
        if (!currentLanguageAlignementIsLeftAligned) {
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

    private HomeButton createHomeButtonInConfigurationManagementScreen(@NonNull GazePlay gazePlay) {

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

    private GridPane buildConfigGridPane(ConfigurationContext configurationContext, Translator translator) {

        final Configuration config = ActiveConfigurationContext.getInstance();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(50);
        grid.setVgap(50);
        // grid.setPadding(new Insets(50, 50, 50, 50));

        grid.getStyleClass().add("item");

        AtomicInteger currentFormRow = new AtomicInteger(1);

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "LanguageSettings", COLON));
        // Language settings
        {
            I18NText label = new I18NText(translator, "Lang", COLON);

            MenuButton input = buildLanguageChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }


        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "GamesSettings", COLON));
        // Games settings
        {
            I18NText label = new I18NText(translator, "QuitKey", COLON);

            ChoiceBox<String> input = buildQuitKeyChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "QuestionLength", COLON);

            ChoiceBox<Double> input = buildQuestionLengthChooserMenu(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "EnableRewardSound", COLON);
            CheckBox input = buildEnableRewardSoundBox(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }


        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "EyeTrackerSettings", COLON));
        // Eye Tracking settings
        {
            I18NText label = new I18NText(translator, "EyeTracker", COLON);

            ChoiceBox<EyeTracker> input = buildEyeTrackerConfigChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "FixationLength", COLON);

            ChoiceBox<Double> input = buildFixLengthChooserMenu(config, configurationContext);

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
            I18NText label = new I18NText(translator, "WhiteBackground", COLON);
            CheckBox input = buildEnabledWhiteBackground(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "MenuOrientation", COLON);
            ChoiceBox<GameButtonOrientation> input = buildGameButtonOrientationChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "FoldersSettings", COLON));
        // Folders settings
        {
            I18NText label = new I18NText(translator, "FileDir", COLON);

            Node input = buildDirectoryChooser(config, configurationContext, translator);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "MusicFolder", COLON);
            final Node input = buildMusicInput(config, configurationContext, translator);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "VideoFolder", COLON);
            final Node input = buildVideoFolderChooser(config, configurationContext, translator);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "WhereIsItDirectory", COLON);

            Node input = buildWhereIsItDirectoryChooser(config, configurationContext, translator);

            addToGrid(grid, currentFormRow, label, input);
        }

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "StatsSettings", COLON));
        // Stats settings
        addSubCategoryTitle(grid, currentFormRow, new I18NText(translator, "HeatMapSettings", COLON));
        // HeatMap settings
        {
            I18NText label = new I18NText(translator, "DisableHeatMap", COLON);
            CheckBox input = buildDisableHeatMapSoundBox(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "HeatMapOpacity", COLON);
            ChoiceBox input = buildHeatMapOpacityChoiceBox(config);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "HeatMapColors", COLON);
            HBox input = buildHeatMapColorHBox(config, translator);

            addToGrid(grid, currentFormRow, label, input);
        }

        addSubCategoryTitle(grid, currentFormRow, new I18NText(translator, "AOISettings", COLON));
        // AOI settings
        {
            I18NText label = new I18NText(translator, "EnableAreaOfInterest", COLON);
            CheckBox input = buildDisableAreaOfInterest(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "EnableConvexHull", COLON);
            CheckBox input = buildDisableConvexHull(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        addSubCategoryTitle(grid, currentFormRow, new I18NText(translator, "MoreStatsSettings", COLON));
        // More Stats settings
        {
            I18NText label = new I18NText(translator, "DisableSequence", COLON);
            CheckBox input = buildDisableFixationSequenceCheckBox(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "EnableVideoRecording", COLON);
            CheckBox input = buildEnableVideoRecordingCheckbox(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        addCategoryTitle(grid, currentFormRow, new I18NText(translator, "BetaSettings", COLON));
        // Beta settings
        {
            I18NText label = new I18NText(translator, "EnableGazeMenu", COLON);
            CheckBox input = buildGazeMenu(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "EnableGazeMouse", COLON);
            CheckBox input = buildGazeMouseEnabledCheckBox(config, configurationContext);
            String[] labelParts = label.getText().split(";");
            StringBuilder concatenateLabel = new StringBuilder();
            for (String labels : labelParts) {
                concatenateLabel.append(labels).append("\n\t");
            }
            label.setText(concatenateLabel.toString());
            addToGrid(grid, currentFormRow, label, input);
        }

        return grid;
    }

    private void addCategoryTitle(GridPane grid, AtomicInteger currentFormRow, I18NText label) {
        int COLUMN_INDEX_LABEL_LEFT = 0;
        int COLUMN_INDEX_LABEL_RIGHT = 2;


        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");
        // label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); //should be managed with css

        Separator s = new Separator();
        grid.add(s, 0, currentRowIndex, 3, 1);
        GridPane.setHalignment(s, HPos.CENTER);

        if (currentLanguageAlignementIsLeftAligned) {
            int newcurrentRowIndex = currentFormRow.incrementAndGet();
            grid.add(label, COLUMN_INDEX_LABEL_LEFT, newcurrentRowIndex);
            GridPane.setHalignment(label, HPos.LEFT);
        } else {
            int newcurrentRowIndex = currentFormRow.incrementAndGet();
            grid.add(label, COLUMN_INDEX_LABEL_RIGHT, newcurrentRowIndex);
            GridPane.setHalignment(label, HPos.RIGHT);
        }
    }

    private void addSubCategoryTitle(GridPane grid, AtomicInteger currentFormRow, I18NText label) {
        int COLUMN_INDEX_LABEL_LEFT = 0;
        int COLUMN_INDEX_LABEL_RIGHT = 2;
        int COLUMN_INDEX_INPUT_LEFT = 1;
        int COLUMN_INDEX_INPUT_RIGHT = 0;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");
        // label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); //should be managed with css

        if (currentLanguageAlignementIsLeftAligned) {
            grid.add(label, COLUMN_INDEX_LABEL_LEFT, currentRowIndex);
            GridPane.setHalignment(label, HPos.LEFT);
            Separator s = new Separator();
            grid.add(s, COLUMN_INDEX_INPUT_LEFT, currentRowIndex, 2, 1);
            GridPane.setHalignment(s, HPos.LEFT);
        } else {
            grid.add(label, COLUMN_INDEX_LABEL_RIGHT, currentRowIndex);
            GridPane.setHalignment(label, HPos.RIGHT);
            Separator s = new Separator();
            grid.add(s, COLUMN_INDEX_INPUT_RIGHT, currentRowIndex, 2, 1);
            GridPane.setHalignment(s, HPos.RIGHT);
        }
    }


    private void addToGrid(GridPane grid, AtomicInteger currentFormRow, I18NText label, final Node input) {

        int COLUMN_INDEX_LABEL_LEFT = 1;
        int COLUMN_INDEX_INPUT_LEFT = 2;

        int COLUMN_INDEX_LABEL_RIGHT = 1;
        int COLUMN_INDEX_INPUT_RIGHT = 0;


        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");
        // label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); //should be managed with css

        if (currentLanguageAlignementIsLeftAligned) {

            grid.add(label, COLUMN_INDEX_LABEL_LEFT, currentRowIndex);
            GridPane.setHalignment(label, HPos.LEFT);
            grid.add(input, COLUMN_INDEX_INPUT_LEFT, currentRowIndex);
            GridPane.setHalignment(input, HPos.LEFT);

        } else {

            grid.add(label, COLUMN_INDEX_LABEL_RIGHT, currentRowIndex);
            GridPane.setHalignment(label, HPos.RIGHT);
            grid.add(input, COLUMN_INDEX_INPUT_RIGHT, currentRowIndex);
            GridPane.setHalignment(input, HPos.RIGHT);

        }
    }

    private static ChoiceBox<Double> buildFixLengthChooserMenu(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        ChoiceBox<Double> choiceBox = new ChoiceBox<>();

        int i = 300;

        choiceBox.getItems().add((double) configuration.getFixationLength() / 1000);
        while (i <= 30000) {

            choiceBox.getItems().add(((double) i) / 1000);
            i = i + 100;
        }

        choiceBox.getSelectionModel().select(0);
        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            final int newPropertyValue = (int) (1000
                * choiceBox.getItems().get(Integer.parseInt(newValue.intValue() + "")));
            configuration.getFixationlengthProperty().setValue(newPropertyValue);
        });

        return choiceBox;
    }

    private static ChoiceBox<Double> buildQuestionLengthChooserMenu(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {

        ChoiceBox<Double> choiceBox = new ChoiceBox<>();

        int i = 500;

        choiceBox.getItems().add((double) configuration.getQuestionLength() / 1000);
        while (i <= 20000) {

            choiceBox.getItems().add(((double) i) / 1000);
            i = i + 500;
        }

        choiceBox.getSelectionModel().select(0);
        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {

            final int newPropertyValue = (int) (1000
                * choiceBox.getItems().get(Integer.parseInt(newValue.intValue() + "")));

            configuration.getQuestionLengthProperty().setValue(newPropertyValue);
        });

        return choiceBox;
    }

    /**
     * Function to use to permit to user to select between several theme
     */
    private static ChoiceBox<BuiltInUiTheme> buildStyleThemeChooser(Configuration configuration, ConfigurationContext configurationContext) {
        ChoiceBox<BuiltInUiTheme> themesBox = new ChoiceBox<>();

        final String cssfile = configuration.getCssFile();

        themesBox.getItems().addAll(BuiltInUiTheme.values());

        Optional<BuiltInUiTheme> configuredTheme = BuiltInUiTheme.findFromConfigPropertyValue(cssfile);

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
            // final Scene scene = gazePlay.getPrimaryScene();

            CssUtil.setPreferredStylesheets(configuration, gazePlay.getPrimaryScene(), gazePlay.getCurrentScreenDimensionSupplier());

            /*
             * scene.getStylesheets().removeAll(scene.getStylesheets()); String styleSheetPath =
             * newValue.getStyleSheetPath(); if (styleSheetPath != null) {
             * scene.getStylesheets().add(styleSheetPath); }
             */
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

    private static Node buildDirectoryChooser(Configuration configuration, ConfigurationContext configurationContext, Translator translator) {

        final HBox pane = new HBox(5);

        final String filedir = configuration.getFileDir();

        Button buttonLoad = new Button(filedir);
        buttonLoad.textProperty().bind(configuration.getFiledirProperty());

        buttonLoad.setOnAction(arg0 -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            final File currentFolder = new File(configuration.getFileDir());
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

            configuration.getFiledirProperty().setValue(newPropertyValue);
        });

        pane.getChildren().add(buttonLoad);

        final I18NButton resetButton = new I18NButton(translator, "reset");
        resetButton.setOnAction((event) -> configuration.getFiledirProperty().setValue(GazePlayDirectories.getDefaultFileDirectoryDefaultValue().getAbsolutePath()));

        pane.getChildren().add(resetButton);

        return pane;
    }

    private Node buildWhereIsItDirectoryChooser(Configuration configuration,
                                                ConfigurationContext configurationContext,
                                                Translator translator) {

        final HBox pane = new HBox(5);

        // Arabic Alignment
        if (!currentLanguageAlignementIsLeftAligned) {
            pane.setAlignment(Pos.BASELINE_RIGHT);
        }

        final String whereIsItDir = configuration.getWhereIsItDir();
        Button buttonLoad = new Button(whereIsItDir);
        buttonLoad.textProperty().bind(configuration.getWhereIsItDirProperty());

        buttonLoad.setOnAction((ActionEvent arg0) -> {

            DirectoryChooser directoryChooser = new DirectoryChooser();
            final File currentFolder = new File(configuration.getWhereIsItDir());
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

            configuration.getWhereIsItDirProperty().setValue(newPropertyValue);
        });

        pane.getChildren().add(buttonLoad);

        final I18NButton resetButton = new I18NButton(translator, "reset");
        resetButton.setOnAction((event) -> configuration.getWhereIsItDirProperty().setValue(Configuration.DEFAULT_VALUE_WHEREISIT_DIR));

        pane.getChildren().add(resetButton);

        return pane;
    }

    private MenuButton buildLanguageChooser(Configuration configuration,
                                            ConfigurationContext configurationContext) {

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


                MenuItem LanguagesItem = new MenuItem(language.getLabel(), imageView);

                LanguagesItem.setOnAction(eventMenuLanguages -> {

                    configuration.getLanguageProperty().setValue(language.getLocale().getLanguage());
                    configuration.getCountryProperty().setValue(language.getLocale().getCountry());

                    configurationContext.getGazePlay().getTranslator().notifyLanguageChanged();

                    languageBox.setText(language.getLabel());

                    ImageView newImage = new ImageView(image);
                    newImage.setPreserveRatio(true);
                    newImage.setFitHeight(25);
                    languageBox.setGraphic(newImage);

                    if (language.isLeftAligned() != currentLanguageAlignementIsLeftAligned) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Language information");
                        alert.setHeaderText(
                            "Alignment settings have just changed for your language, please restart the game for the new changes to take effect.");
                        alert.show();
                    }
                    if (!language.isStableTranslationAvailable()) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Language information");
                        alert.setHeaderText(
                            "Translation has just been performed for your language. If you think that some words sound odd in the games, it is maybe a problem of translation. \nPlease contact us to propose better ones (gazeplay.net) and they will be in the next version.");
                        alert.show();
                    }
                });

                languageBox.getItems().add(LanguagesItem);
            }
        }

        languageBox.setPrefWidth(PREF_WIDTH);
        languageBox.setPrefHeight(PREF_HEIGHT);

        return languageBox;
    }

    private static ChoiceBox<EyeTracker> buildEyeTrackerConfigChooser(Configuration configuration,
                                                                      ConfigurationContext configurationContext) {
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

    private static CheckBox buildEnableRewardSoundBox(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(configuration.isEnableRewardSound());
        checkBox.selectedProperty().addListener((o) -> {
            configuration.getEnableRewardSoundProperty().setValue(checkBox.isSelected());
        });
        return checkBox;
    }

    private static CheckBox buildDisableHeatMapSoundBox(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(configuration.isHeatMapDisabled());
        checkBox.selectedProperty().addListener((o) -> {
            configuration.getHeatMapDisabledProperty().setValue(checkBox.isSelected());
        });
        return checkBox;
    }

    private static CheckBox buildDisableAreaOfInterest(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(configuration.getAreaOfInterestDisabledProperty().getValue());
        checkBox.selectedProperty().bindBidirectional(configuration.getAreaOfInterestDisabledProperty());
        return checkBox;
    }

    private static CheckBox buildDisableConvexHull(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(configuration.getConvexHullDisabledProperty().getValue());
        checkBox.selectedProperty().bindBidirectional(configuration.getConvexHullDisabledProperty());
        return checkBox;
    }

    private static CheckBox buildEnableVideoRecordingCheckbox(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(configuration.getVideoRecordingEnabledProperty().getValue());
        checkBox.selectedProperty().bindBidirectional(configuration.getVideoRecordingEnabledProperty());
        return checkBox;
    }

    private static CheckBox buildDisableFixationSequenceCheckBox(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(configuration.getFixationSequenceDisabledProperty().getValue());
        checkBox.selectedProperty().bindBidirectional(configuration.getFixationSequenceDisabledProperty());
        return checkBox;
    }

    private CheckBox buildEnabledWhiteBackground(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(configuration.getWhiteBackgroundProperty().getValue());
        checkBox.selectedProperty().bindBidirectional(configuration.getWhiteBackgroundProperty());
        return checkBox;
    }

    private static CheckBox buildGazeMenu(Configuration configuration, ConfigurationContext configurationContext) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(configuration.getGazeMenuEnabledProperty().getValue());
        checkBox.selectedProperty().bindBidirectional(configuration.getGazeMenuEnabledProperty());

        // TODO
        // ****** REMOVE FROM HERE
        checkBox.setDisable(true);
        // TO HERE TO ENABLE******

        return checkBox;
    }

    private static CheckBox buildGazeMouseEnabledCheckBox(Configuration configuration, ConfigurationContext configurationContext) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(configuration.getGazeMouseEnabledProperty().getValue());
        checkBox.selectedProperty().bindBidirectional(configuration.getGazeMouseEnabledProperty());
        return checkBox;
    }

    private static ChoiceBox<GameButtonOrientation> buildGameButtonOrientationChooser(
        Configuration configuration,
        ConfigurationContext configurationContext
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

    private Node buildMusicInput(Configuration config, ConfigurationContext configurationContext, Translator translator) {

        changeMusicFolder(config.getMusicFolder(), config);

        final HBox pane = new HBox(5);

        // Arabic Alignment
        if (!currentLanguageAlignementIsLeftAligned) {
            pane.setAlignment(Pos.BASELINE_RIGHT);
        }

        final String musicFolder = config.getMusicFolder();
        Button buttonLoad = new Button(musicFolder);

        buttonLoad.textProperty().bind(config.getMusicFolderProperty());

        buttonLoad.setOnAction((ActionEvent arg0) -> {
            final Configuration configuration = ActiveConfigurationContext.getInstance();

            DirectoryChooser directoryChooser = new DirectoryChooser();

            final File currentMusicFolder = new File(configuration.getMusicFolder());
            if (currentMusicFolder.isDirectory()) {
                directoryChooser.setInitialDirectory(currentMusicFolder);
            }
            final GazePlay gazePlay = configurationContext.getGazePlay();
            final Scene scene = gazePlay.getPrimaryScene();
            File file = directoryChooser.showDialog(scene.getWindow());
            if (file == null) {
                return;
            }
            // buttonLoad.setText(file.toString() + Utils.FILESEPARATOR);

            String newPropertyValue = file.getAbsolutePath();

            if (Utils.isWindows()) {
                newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
            }

            changeMusicFolder(newPropertyValue, config);
        });

        pane.getChildren().add(buttonLoad);

        final I18NButton resetButton = new I18NButton(translator, "reset");
        resetButton.setOnAction((event) -> changeMusicFolder(Configuration.DEFAULT_VALUE_MUSIC_FOLDER, config));

        pane.getChildren().add(resetButton);

        return pane;
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

    private static HBox buildVideoFolderChooser(Configuration config, ConfigurationContext configurationContext, Translator translator) {
        HBox hbox = new HBox(5);

        Button buttonFolder = new Button(config.getVideoFolder());
        buttonFolder.textProperty().bind(config.getVideoFolderProperty());
        I18NButton buttonReset = new I18NButton(translator, "reset");
        hbox.getChildren().addAll(buttonFolder, buttonReset);

        buttonFolder.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            final File currentVideoFolder = new File(config.getVideoFolder());
            if (currentVideoFolder.isDirectory()) {
                directoryChooser.setInitialDirectory(currentVideoFolder);
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
            config.getVideoFolderProperty().setValue(newPropertyValue);
        });

        buttonReset.setOnAction(e -> {
            config.getVideoFolderProperty().setValue(GazePlayDirectories.getVideosFilesDirectory().getAbsolutePath());
        });

        return hbox;
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

    private ChoiceBox<String> buildQuitKeyChooser(
        Configuration configuration,
        ConfigurationContext configurationContext
    ) {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);
        choiceBox.getItems().addAll("Q", "W", "E", "R", "T", "Y");
        choiceBox.getSelectionModel().select(configuration.getQuitKeyProperty().getValue());
        choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            configuration.getQuitKeyProperty().setValue(newValue);
        });
        return choiceBox;
    }

    private ChoiceBox<Double> buildHeatMapOpacityChoiceBox(Configuration config) {
        ChoiceBox<Double> choiceBox = new ChoiceBox();
        for (double i = 0; i <= 10; i++) {
            choiceBox.getItems().add(i / 10);
        }
        choiceBox.getSelectionModel().select(config.getHeatMapOpacity());
        choiceBox.setPrefSize(PREF_WIDTH, PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            config.getHeatMapOpacityProperty().setValue(newValue);
        });
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

    private HBox buildHeatMapColorHBox(Configuration config, Translator translator) {
        HBox hbox = new HBox();
        hbox.setSpacing(5);

        final I18NButton resetButton = new I18NButton(translator, "reset");

        Button plusButton = new Button("+");

        Button minusButton = new Button("-");

        hbox.getChildren().addAll(resetButton, plusButton, minusButton);

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
