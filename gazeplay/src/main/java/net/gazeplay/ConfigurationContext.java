package net.gazeplay;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.ui.I18NButton;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Languages;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConfigurationContext extends GraphicalContext<BorderPane> {

    private static final String COLON = "Colon";

    private static final double PREF_WIDTH = 200;

    private static final double PREF_HEIGHT = 25;

    private static Translator translator;

    private static Boolean ALIGN_LEFT = true;

    private static String currentLanguage;

    public static ConfigurationContext newInstance(GazePlay gazePlay) {
        BorderPane root = new BorderPane();

        return new ConfigurationContext(gazePlay, root);
    }

    private ConfigurationContext(GazePlay gazePlay, BorderPane root) {
        super(gazePlay, root);

        translator = gazePlay.getTranslator();

        currentLanguage = translator.currentLanguage();

        // Align right for Arabic Language
        if (currentLanguage.equals("ara")) {
            ALIGN_LEFT = false;
        }

        // Bottom Pane
        HomeButton homeButton = createHomeButtonInConfigurationManagementScreen(gazePlay);

        HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER_RIGHT);
        if (ALIGN_LEFT) {
            rightControlPane.getChildren().add(homeButton);
        }

        HBox leftControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(leftControlPane);
        leftControlPane.setAlignment(Pos.CENTER_LEFT);
        // HomeButton on the Left for Arabic Language
        if (!ALIGN_LEFT) {
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
        if (!ALIGN_LEFT) {
            root.setAlignment(configTitleText, Pos.BOTTOM_RIGHT);
        }

        root.setTop(configTitleText);

        // Center Pane

        GridPane gridPane = buildConfigGridPane(this, gazePlay);

        ScrollPane settingsPanelScroller = new ScrollPane(gridPane);

        settingsPanelScroller.setFitToWidth(true);
        settingsPanelScroller.setFitToHeight(true);

        gridPane.setAlignment(Pos.CENTER);
        VBox centerCenterPane = new VBox();
        centerCenterPane.setSpacing(40);
        centerCenterPane.setAlignment(Pos.TOP_CENTER);
        // Arabic title alignment
        if (!ALIGN_LEFT) {
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

        HomeButton homeButton = new HomeButton();

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    root.setCursor(Cursor.WAIT); // Change cursor to wait style

                    gazePlay.onReturnToMenu();

                    root.setCursor(Cursor.DEFAULT); // Change cursor to default style
                }
            }
        };

        homeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        return homeButton;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    private GridPane buildConfigGridPane(ConfigurationContext configurationContext, GazePlay gazePlay) {

        final Configuration config = Configuration.getInstance();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(50);
        grid.setVgap(50);
        // grid.setPadding(new Insets(50, 50, 50, 50));

        grid.getStyleClass().add("item");

        AtomicInteger currentFormRow = new AtomicInteger(1);

        {
            I18NText label = new I18NText(translator, "Lang", COLON);

            MenuButton input = buildLanguageChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "EyeTracker", COLON);

            ChoiceBox<EyeTracker> input = buildEyeTrackerConfigChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "QuitKey", COLON);

            ChoiceBox<String> input = buildQuitKeyChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "FileDir", COLON);

            Node input = buildDirectoryChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "LayoutFile", COLON);

            ChoiceBox<BuiltInUiTheme> input = buildStyleThemeChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "FixationLength", COLON);

            ChoiceBox<Double> input = buildFixLengthChooserMenu(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "WhereIsItDirectory", COLON);

            Node input = buildWhereIsItDirectoryChooser(config, configurationContext);

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

        {
            I18NText label = new I18NText(translator, "MenuOrientation", COLON);
            ChoiceBox<GameButtonOrientation> input = buildGameButtonOrientationChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "DisableHeatMap", COLON);
            CheckBox input = buildDisableHeatMapSoundBox(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "DisableSequence", COLON);
            CheckBox input = buildDisableFixationSequenceCheckBox(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }
        {
            I18NText label = new I18NText(translator, "WhiteBackground", COLON);
            CheckBox input = buildEnabledWhiteBackground(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "MusicFolder", COLON);
            final Node input = buildMusicInput(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "EnableGazeMenu", COLON);
            CheckBox input = buildGazeMenu(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "EnableGazeMouse", COLON);
            CheckBox input = buildGazeMouse(config, configurationContext);
            String[] labelParts = label.getText().split(";");
            String concatenateLabel = "";
            for (String labels : labelParts) {
                concatenateLabel = concatenateLabel + labels + "\n\t";
            }
            label.setText(concatenateLabel);
            addToGrid(grid, currentFormRow, label, input);
        }

        return grid;
    }

    private static void addToGrid(GridPane grid, AtomicInteger currentFormRow, I18NText label, final Node input) {

        final int COLUMN_INDEX_LABEL_LEFT = 0;
        final int COLUMN_INDEX_INPUT_LEFT = 1;
        final int COLUMN_INDEX_LABEL_RIGHT = 1;
        final int COLUMN_INDEX_INPUT_RIGHT = 0;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");
        // label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); //should be managed with css

        if (ALIGN_LEFT) {
            grid.add(label, COLUMN_INDEX_LABEL_LEFT, currentRowIndex);
            grid.add(input, COLUMN_INDEX_INPUT_LEFT, currentRowIndex);

            GridPane.setHalignment(label, HPos.LEFT);
            GridPane.setHalignment(input, HPos.LEFT);
        } else {

            grid.add(input, COLUMN_INDEX_INPUT_RIGHT, currentRowIndex);
            grid.add(label, COLUMN_INDEX_LABEL_RIGHT, currentRowIndex);

            GridPane.setHalignment(label, HPos.RIGHT);
            GridPane.setHalignment(input, HPos.RIGHT);
        }
    }

    private static ChoiceBox<Double> buildFixLengthChooserMenu(Configuration configuration,
            ConfigurationContext configurationContext) {

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

        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                final int newPropertyValue = (int) (1000
                        * (double) choiceBox.getItems().get(Integer.parseInt(newValue.intValue() + "")));

                configuration.getFixationlengthProperty().setValue(newPropertyValue);
                configuration.saveConfigIgnoringExceptions();

            }
        });

        return choiceBox;
    }

    private static ChoiceBox<Double> buildQuestionLengthChooserMenu(Configuration configuration,
            ConfigurationContext configurationContext) {

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

        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                final int newPropertyValue = (int) (1000
                        * (double) choiceBox.getItems().get(Integer.parseInt(newValue.intValue() + "")));

                configuration.getQuestionLengthProperty().setValue(newPropertyValue);
                configuration.saveConfigIgnoringExceptions();

            }
        });

        return choiceBox;
    }

    /**
     * Function to use to permit to user to select between several theme
     */
    private static ChoiceBox<BuiltInUiTheme> buildStyleThemeChooser(Configuration configuration,
            ConfigurationContext configurationContext) {
        ChoiceBox<BuiltInUiTheme> themesBox = new ChoiceBox<>();

        final String cssfile = configuration.getCssFile();

        themesBox.getItems().addAll(BuiltInUiTheme.values());

        Optional<BuiltInUiTheme> configuredTheme = BuiltInUiTheme.findFromConfigPropertyValue(cssfile);

        BuiltInUiTheme selected = configuredTheme.orElse(BuiltInUiTheme.DEFAULT_THEME);

        themesBox.setConverter(new StringConverter<BuiltInUiTheme>() {
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

        themesBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BuiltInUiTheme>() {
            @Override
            public void changed(ObservableValue<? extends BuiltInUiTheme> observable, BuiltInUiTheme oldValue,
                    BuiltInUiTheme newValue) {
                String newPropertyValue = newValue.getPreferredConfigPropertyValue();

                configuration.getCssfileProperty().setValue(newPropertyValue);
                configuration.saveConfigIgnoringExceptions();

                final GazePlay gazePlay = GazePlay.getInstance();
                // final Scene scene = gazePlay.getPrimaryScene();

                CssUtil.setPreferredStylesheets(configuration, gazePlay.getPrimaryScene());

                /*
                 * scene.getStylesheets().removeAll(scene.getStylesheets()); String styleSheetPath =
                 * newValue.getStyleSheetPath(); if (styleSheetPath != null) {
                 * scene.getStylesheets().add(styleSheetPath); }
                 */
            }
        });

        return themesBox;
    }

    /**
     * Function to use to permit to user to choose his/her own css file
     */
    private static Button buildStyleFileChooser(Configuration configuration,
            ConfigurationContext configurationContext) {

        Button buttonLoad = new Button(configuration.getCssFile());

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                FileChooser fileChooser = new FileChooser();
                final GazePlay gazePlay = GazePlay.getInstance();
                final Scene scene = gazePlay.getPrimaryScene();
                File file = fileChooser.showOpenDialog(scene.getWindow());
                buttonLoad.setText(file.toString());

                String newPropertyValue = file.toString();
                if (Utils.isWindows()) {
                    newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
                }

                configuration.getCssfileProperty().setValue(newPropertyValue);
                configuration.saveConfigIgnoringExceptions();

                scene.getStylesheets().remove(0);
                scene.getStylesheets().add("file://" + newPropertyValue);

                log.info(scene.getStylesheets().toString());
            }
        });

        return buttonLoad;
    }

    private static Node buildDirectoryChooser(Configuration configuration, ConfigurationContext configurationContext) {

        final HBox pane = new HBox(5);

        final String filedir = configuration.getFileDir();

        Button buttonLoad = new Button(filedir);
        buttonLoad.textProperty().bind(configuration.getFiledirProperty());

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                final File currentFolder = new File(configuration.getFileDir());
                if (currentFolder.isDirectory()) {
                    directoryChooser.setInitialDirectory(currentFolder);
                }
                final GazePlay gazePlay = GazePlay.getInstance();
                final Scene scene = gazePlay.getPrimaryScene();
                File file = directoryChooser.showDialog(scene.getWindow());
                if (file == null) {
                    return;
                }

                String newPropertyValue = file.toString() + Utils.FILESEPARATOR;

                if (Utils.isWindows()) {
                    newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
                }

                configuration.getFiledirProperty().setValue(newPropertyValue);
                configuration.saveConfigIgnoringExceptions();
            }
        });

        pane.getChildren().add(buttonLoad);

        final I18NButton resetButton = new I18NButton(translator, "reset");
        resetButton.setOnAction((event) -> {
            configuration.getFiledirProperty().setValue(Configuration.DEFAULT_VALUE_FILE_DIR);
        });

        pane.getChildren().add(resetButton);

        return pane;
    }

    private static Node buildWhereIsItDirectoryChooser(Configuration configuration,
            ConfigurationContext configurationContext) {

        final HBox pane = new HBox(5);

        // Arabic Alignment
        if (!ALIGN_LEFT) {
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
            final GazePlay gazePlay = GazePlay.getInstance();
            final Scene scene = gazePlay.getPrimaryScene();
            File file = directoryChooser.showDialog(scene.getWindow());
            if (file == null) {
                return;
            }

            String newPropertyValue = file.toString() + Utils.FILESEPARATOR;

            if (Utils.isWindows()) {
                newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
            }

            configuration.getWhereIsItDirProperty().setValue(newPropertyValue);
            configuration.saveConfigIgnoringExceptions();
        });

        pane.getChildren().add(buttonLoad);

        final I18NButton resetButton = new I18NButton(translator, "reset");
        resetButton.setOnAction((event) -> {
            configuration.getWhereIsItDirProperty().setValue(Configuration.DEFAULT_VALUE_WHEREISIT_DIR);
        });

        pane.getChildren().add(resetButton);

        return pane;
    }

    private static MenuButton buildLanguageChooser(Configuration configuration,
            ConfigurationContext configurationContext) {

        String currentCodeLanguage = configuration.getLanguage();

        String currentLanguage = Languages.getLanguage(currentCodeLanguage);

        Image currentFlag = new Image(Languages.getFlags(currentCodeLanguage).get(0));
        ImageView currentFlagImageView = new ImageView(currentFlag);
        currentFlagImageView.setPreserveRatio(true);
        currentFlagImageView.setFitHeight(25);

        MenuButton LanguageBox = new MenuButton(currentLanguage, currentFlagImageView);

        ArrayList<String> CodeLanguages = Languages.getCodes();

        CodeLanguages.sort(Comparator.comparing(String::toString));

        for (String codeLanguage : CodeLanguages) {

            ArrayList<String> flags = Languages.getFlags(codeLanguage);

            for (String flag : flags) {

                Image image = new Image(flag);
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(25);

                String language = Languages.getLanguage(codeLanguage);

                MenuItem LanguagesItem = new MenuItem(language, imageView);

                LanguagesItem.setOnAction(eventMenuLanguages -> {

                    configuration.getLanguageProperty().setValue(codeLanguage);

                    configuration.saveConfigIgnoringExceptions();

                    configurationContext.getGazePlay().getTranslator().notifyLanguageChanged();

                    LanguageBox.setText(Languages.getLanguage(codeLanguage));

                    ImageView newImage = new ImageView(image);
                    newImage.setPreserveRatio(true);
                    newImage.setFitHeight(25);
                    LanguageBox.setGraphic(newImage);

                    if (codeLanguage.equals("ell")) {

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Language information");
                        alert.setHeaderText("Translations have been provided by MK Prossopsis Ltd.");
                        alert.show();
                    } else if (!ALIGN_LEFT || (codeLanguage.equals("ara") && !currentLanguage.equals("ara"))) {

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Language information");
                        alert.setHeaderText(
                                "Alignment settings have just changed for your language, please restart the game for the new changes to take effect. \n\n If you believe there are problems with the translations in your language, please contact us to propose better ones (gazeplay.net) and they will be in the next version.");
                        alert.show();
                    } else if (!codeLanguage.equals("fra") && !codeLanguage.equals("eng")
                            && !codeLanguage.equals("deu")) {

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Language information");
                        alert.setHeaderText(
                                "Translation has just been performed for your language. If you think that some words sound odd in the games, it is maybe a problem of translation. \nPlease contact us to propose better ones (gazeplay.net) and they will be in the next version.");
                        alert.show();
                    }

                });

                LanguageBox.getItems().add(LanguagesItem);
            }
        }

        LanguageBox.setPrefWidth(PREF_WIDTH);
        LanguageBox.setPrefHeight(PREF_HEIGHT);

        return LanguageBox;
    }

    private static ChoiceBox<EyeTracker> buildEyeTrackerConfigChooser(Configuration configuration,
            ConfigurationContext configurationContext) {
        ChoiceBox<EyeTracker> choiceBox = new ChoiceBox<>();

        choiceBox.getItems().addAll(EyeTracker.values());

        EyeTracker selectedEyeTracker = findSelectedEyeTracker(configuration);
        choiceBox.getSelectionModel().select(selectedEyeTracker);

        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EyeTracker>() {
            @Override
            public void changed(ObservableValue<? extends EyeTracker> observable, EyeTracker oldValue,
                    EyeTracker newValue) {
                final String newPropertyValue = newValue.name();
                configuration.getEyetrackerProperty().setValue(newPropertyValue);
                configuration.saveConfigIgnoringExceptions();
            }
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

    private static CheckBox buildEnableRewardSoundBox(Configuration configuration,
            ConfigurationContext configurationContext) {
        CheckBox checkBox = new CheckBox();

        checkBox.setSelected(configuration.isEnableRewardSound());

        checkBox.selectedProperty().addListener((o) -> {

            configuration.getEnableRewardSoundProperty().setValue(checkBox.isSelected());
            configuration.saveConfigIgnoringExceptions();
        });

        return checkBox;
    }

    private static CheckBox buildDisableHeatMapSoundBox(Configuration configuration,
            ConfigurationContext configurationContext) {
        CheckBox checkBox = new CheckBox();

        checkBox.setSelected(configuration.isHeatMapDisabled());

        checkBox.selectedProperty().addListener((o) -> {

            configuration.getHeatMapDisabledProperty().setValue(checkBox.isSelected());
            configuration.saveConfigIgnoringExceptions();
        });

        return checkBox;
    }

    private static CheckBox buildDisableFixationSequenceCheckBox(Configuration configuration,
            ConfigurationContext configurationContext) {
        CheckBox checkBox = new CheckBox();

        checkBox.setSelected(configuration.isFixationSequenceDisabled());

        checkBox.selectedProperty().addListener((o) -> {

            configuration.getFixationSequenceDisabledProperty().setValue(checkBox.isSelected());
            configuration.saveConfigIgnoringExceptions();
        });

        return checkBox;
    }

    private CheckBox buildEnabledWhiteBackground(Configuration configuration,
            ConfigurationContext configurationContext) {
        CheckBox checkBox = new CheckBox();

        checkBox.setSelected(configuration.isBackgroundWhite());

        checkBox.selectedProperty().addListener((o) -> {
            configuration.getWhiteBackgroundProperty().setValue(checkBox.isSelected());
            configuration.saveConfigIgnoringExceptions();
        });
        return checkBox;
    }

    private static CheckBox buildGazeMenu(Configuration configuration, ConfigurationContext configurationContext) {
        CheckBox checkBox = new CheckBox();

        checkBox.setSelected(configuration.isGazeMenuEnable());

        checkBox.selectedProperty().addListener((o) -> {

            configuration.getGazeMenuProperty().setValue(checkBox.isSelected());
            configuration.saveConfigIgnoringExceptions();
        });

        // TODO
        // ****** REMOVE FROM HERE
        checkBox.setDisable(true);
        // TO HERE TO ENABLE******

        return checkBox;
    }

    private static CheckBox buildGazeMouse(Configuration configuration, ConfigurationContext configurationContext) {
        CheckBox checkBox = new CheckBox();

        checkBox.setSelected(configuration.isGazeMouseEnable());

        checkBox.selectedProperty().addListener((o) -> {

            configuration.getGazeMouseProperty().setValue(checkBox.isSelected());
            configuration.saveConfigIgnoringExceptions();
        });

        return checkBox;
    }

    private static ChoiceBox<GameButtonOrientation> buildGameButtonOrientationChooser(Configuration configuration,
            ConfigurationContext configurationContext) {
        ChoiceBox<GameButtonOrientation> choiceBox = new ChoiceBox<>();

        choiceBox.getItems().addAll(GameButtonOrientation.values());

        GameButtonOrientation selectedValue = findSelectedGameButtonOrientation(configuration);
        choiceBox.getSelectionModel().select(selectedValue);

        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<GameButtonOrientation>() {
            @Override
            public void changed(ObservableValue<? extends GameButtonOrientation> observable,
                    GameButtonOrientation oldValue, GameButtonOrientation newValue) {
                final String newPropertyValue = newValue.name();
                configuration.getMenuButtonsOrientationProperty().setValue(newPropertyValue);
                configuration.saveConfigIgnoringExceptions();
            }
        });

        return choiceBox;
    }

    private static Node buildMusicInput(Configuration config, ConfigurationContext configurationContext) {

        changeMusicFolder(config.getMusicFolder(), config);

        final HBox pane = new HBox(5);

        // Arabic Alignment
        if (!ALIGN_LEFT) {
            pane.setAlignment(Pos.BASELINE_RIGHT);
        }

        final String musicFolder = config.getMusicFolder();
        Button buttonLoad = new Button(musicFolder);

        buttonLoad.textProperty().bind(config.getMusicFolderProperty());

        buttonLoad.setOnAction((ActionEvent arg0) -> {
            final Configuration configuration = Configuration.getInstance();

            DirectoryChooser directoryChooser = new DirectoryChooser();

            final File currentMusicFolder = new File(configuration.getMusicFolder());
            if (currentMusicFolder.isDirectory()) {
                directoryChooser.setInitialDirectory(currentMusicFolder);
            }
            final GazePlay gazePlay = GazePlay.getInstance();
            final Scene scene = gazePlay.getPrimaryScene();
            File file = directoryChooser.showDialog(scene.getWindow());
            if (file == null) {
                return;
            }
            // buttonLoad.setText(file.toString() + Utils.FILESEPARATOR);

            String newPropertyValue = file.toString() + Utils.FILESEPARATOR;

            if (Utils.isWindows()) {
                newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
            }

            changeMusicFolder(newPropertyValue, config);
        });

        pane.getChildren().add(buttonLoad);

        final I18NButton resetButton = new I18NButton(translator, "reset");
        resetButton.setOnAction((event) -> {
            changeMusicFolder(Configuration.DEFAULT_VALUE_MUSIC_FOLDER, config);
        });

        pane.getChildren().add(resetButton);

        return pane;
    }

    private static void changeMusicFolder(final String newMusicFolder, Configuration config) {

        String musicFolder = newMusicFolder;

        if (newMusicFolder == "") {
            // TODO find a way to access to this files in a "cleaner" way
            musicFolder = (new File(".")).getAbsolutePath() + "/gazeplay-data/src/main/resources/data" + File.separator
                    + "home" + File.separator + "sounds";
            config.getMusicFolderProperty().setValue(Configuration.DEFAULT_VALUE_MUSIC_FOLDER);
        } else {
            config.getMusicFolderProperty().setValue(musicFolder);
        }

        config.saveConfigIgnoringExceptions();

        BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();

        musicManager.emptyPlaylist();
        musicManager.getAudioFromFolder(musicFolder);
        musicManager.play();
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

    private ChoiceBox<String> buildQuitKeyChooser(Configuration configuration,
            ConfigurationContext configurationContext) {

        ChoiceBox<String> KeyBox = new ChoiceBox<>();
        KeyBox.getItems().addAll("Q", "W", "E", "R", "T", "Y");

        // GameButtonOrientation selectedValue = findSelectedGameButtonOrientation(configuration);
        KeyBox.getSelectionModel().select("Q");

        KeyBox.setPrefWidth(PREF_WIDTH);
        KeyBox.setPrefHeight(PREF_HEIGHT);

        KeyBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                final String newPropertyValue = newValue;
                configuration.getQuitKeyProperty().setValue(newPropertyValue);
                configuration.saveConfigIgnoringExceptions();
            }
        });

        return KeyBox;

    }
}
