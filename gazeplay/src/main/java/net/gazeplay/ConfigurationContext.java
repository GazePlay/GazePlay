package net.gazeplay;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.themes.BuiltInUiTheme;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Languages;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.ScrollPane;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;

@Slf4j
public class ConfigurationContext extends GraphicalContext<BorderPane> {

    private static final String COLON = "Colon";

    private static final double PREF_WIDTH = 200;

    private static final double PREF_HEIGHT = 25;

    private static Translator translator;

    public static ConfigurationContext newInstance(GazePlay gazePlay) {
        BorderPane root = new BorderPane();

        return new ConfigurationContext(gazePlay, root);
    }

    private ConfigurationContext(GazePlay gazePlay, BorderPane root) {
        super(gazePlay, root);

        translator = gazePlay.getTranslator();

        HomeButton homeButton = createHomeButtonInConfigurationManagementScreen(gazePlay);

        HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER_RIGHT);
        rightControlPane.getChildren().add(homeButton);

        HBox leftControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(leftControlPane);
        leftControlPane.setAlignment(Pos.CENTER_LEFT);

        BorderPane bottomControlPane = new BorderPane();
        bottomControlPane.setLeft(leftControlPane);
        bottomControlPane.setRight(rightControlPane);

        root.setBottom(bottomControlPane);

        I18NText configTitleText = new I18NText(translator, "ConfigTitle");
        // configTitleText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20)); should be managed with css
        configTitleText.setId("title");
        configTitleText.setTextAlignment(TextAlignment.CENTER);

        root.setTop(configTitleText);

        GridPane gridPane = buildConfigGridPane(this, gazePlay);

        ScrollPane settingsPanelScroller = new ScrollPane(gridPane);

        root.setCenter(settingsPanelScroller);

        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
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

    private static GridPane buildConfigGridPane(ConfigurationContext configurationContext, GazePlay gazePlay) {

        final Configuration config = Configuration.getInstance();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(50);
        grid.setVgap(50);
        // grid.setPadding(new Insets(50, 50, 50, 50));

        AtomicInteger currentFormRow = new AtomicInteger(1);

        {
            I18NText label = new I18NText(translator, "Lang", COLON);

            ChoiceBox<Languages> input = buildLanguageChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            I18NText label = new I18NText(translator, "EyeTracker", COLON);

            ChoiceBox<EyeTracker> input = buildEyeTrackerConfigChooser(config, configurationContext);

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
            I18NText label = new I18NText(translator, "MusicFolder", COLON);
            final Node input = buildMusicInput(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        return grid;
    }

    private static void addToGrid(GridPane grid, AtomicInteger currentFormRow, I18NText label, final Node input) {

        final int COLUMN_INDEX_LABEL = 0;
        final int COLUMN_INDEX_INPUT = 1;

        final int currentRowIndex = currentFormRow.incrementAndGet();

        label.setId("item");
        // label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14)); //should be managed with css

        grid.add(label, COLUMN_INDEX_LABEL, currentRowIndex);
        grid.add(input, COLUMN_INDEX_INPUT, currentRowIndex);

        GridPane.setHalignment(label, HPos.LEFT);
        GridPane.setHalignment(input, HPos.LEFT);
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
     * Fonction to use to permit to user to select between several theme
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
                final Scene scene = gazePlay.getPrimaryScene();

                scene.getStylesheets().removeAll(scene.getStylesheets());
                String styleSheetPath = newValue.getStyleSheetPath();
                if (styleSheetPath != null) {
                    scene.getStylesheets().add(styleSheetPath);
                }
            }
        });

        return themesBox;
    }

    /**
     * Fonction to use to permit to user to choose his/her own css file
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

        final Button resetButton = new Button(translator.translate("reset"));
        resetButton.setOnAction((event) -> {
            configuration.getFiledirProperty().setValue(Configuration.DEFAULT_VALUE_FILE_DIR);
        });

        pane.getChildren().add(resetButton);

        return pane;
    }

    private static Node buildWhereIsItDirectoryChooser(Configuration configuration,
            ConfigurationContext configurationContext) {

        final HBox pane = new HBox(5);

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

        final Button resetButton = new Button(translator.translate("reset"));
        resetButton.setOnAction((event) -> {
            configuration.getWhereIsItDirProperty().setValue(Configuration.DEFAULT_VALUE_WHEREISIT_DIR);
        });

        pane.getChildren().add(resetButton);

        return pane;
    }

    private static ChoiceBox<Languages> buildLanguageChooser(Configuration configuration,
            ConfigurationContext configurationContext) {
        Languages currentLanguage = null;
        if (configuration.getLanguage() != null) {
            currentLanguage = Languages.valueOf(configuration.getLanguage());
        }

        ChoiceBox<Languages> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(Languages.values());
        choiceBox.getSelectionModel().select(currentLanguage);

        choiceBox.setPrefWidth(PREF_WIDTH);
        choiceBox.setPrefHeight(PREF_HEIGHT);

        choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Languages>() {
            @Override
            public void changed(ObservableValue<? extends Languages> observable, Languages oldValue,
                    Languages newValue) {

                configuration.getLanguageProperty().setValue(newValue.name());
                configuration.saveConfigIgnoringExceptions();

                configurationContext.getGazePlay().getTranslator().notifyLanguageChanged();
            }
        });

        return choiceBox;
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

        final HBox pane = new HBox(5);
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

            changeMusicFolder(newPropertyValue);
        });

        pane.getChildren().add(buttonLoad);

        final Button resetButton = new Button(translator.translate("reset"));
        resetButton.setOnAction((event) -> {
            changeMusicFolder(Configuration.DEFAULT_VALUE_MUSIC_FOLDER);
        });

        pane.getChildren().add(resetButton);

        return pane;
    }

    private static void changeMusicFolder(final String newMusicFolder) {

        final Configuration configuration = Configuration.getInstance();

        configuration.getMusicFolderProperty().setValue(newMusicFolder);
        configuration.saveConfigIgnoringExceptions();

        BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();

        musicManager.emptyPlaylist();
        musicManager.getAudioFromFolder(newMusicFolder);
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
}
