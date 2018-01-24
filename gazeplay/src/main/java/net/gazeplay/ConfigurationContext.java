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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.HomeButton;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.layout.Themes;
import net.gazeplay.commons.utils.multilinguism.Languages;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConfigurationContext extends GraphicalContext<BorderPane> {

    private static double prefWidth = 200;

    private static double prefHeight = 25;

    public static ConfigurationContext newInstance(GazePlay gazePlay) {
        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, gazePlay.getPrimaryStage().getWidth(), gazePlay.getPrimaryStage().getHeight(),
                Color.BLACK);

        return new ConfigurationContext(gazePlay, root, scene);
    }

    private ConfigurationContext(GazePlay gazePlay, BorderPane root, Scene scene) {
        super(gazePlay, root, scene);

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

        GridPane gridPane = buildConfigGridPane(this);
        root.setCenter(gridPane);

        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }

    private HomeButton createHomeButtonInConfigurationManagementScreen(@NonNull GazePlay gazePlay) {

        HomeButton homeButton = new HomeButton();

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    scene.setCursor(Cursor.WAIT); // Change cursor to wait style

                    gazePlay.onReturnToMenu();

                    scene.setCursor(Cursor.DEFAULT); // Change cursor to default style
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

    private static GridPane buildConfigGridPane(ConfigurationContext configurationContext) {

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        Multilinguism multilinguism = Multilinguism.getSingleton();

        // to add or not a space before colon (:) according to the language
        String colon = multilinguism.getTrad("Colon", config.getLanguage());
        if (colon.equals("_noSpace"))
            colon = ": ";
        else
            colon = " : ";

        log.info("ConfigurationButton");

        log.info(config.toString());

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(100);
        grid.setVgap(50);
        // grid.setPadding(new Insets(50, 50, 50, 50));

        Text configTitleText = new Text(multilinguism.getTrad("ConfigTitle", config.getLanguage()));
        // configTitleText.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20)); should be managed with css
        configTitleText.setId("title");
        configTitleText.setTextAlignment(TextAlignment.CENTER);
        grid.add(configTitleText, 0, 0, 2, 1);

        AtomicInteger currentFormRow = new AtomicInteger(1);

        {
            Text label = new Text(multilinguism.getTrad("Lang", config.getLanguage()) + colon);

            ChoiceBox<Languages> input = buildLanguageChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            Text label = new Text(multilinguism.getTrad("EyeTracker", config.getLanguage()) + colon);

            ChoiceBox<EyeTracker> input = buildEyeTrackerConfigChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            Text label = new Text(multilinguism.getTrad("FileDir", config.getLanguage()) + colon);

            Button input = buildDirectoryChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            Text label = new Text(multilinguism.getTrad("LayoutFile", config.getLanguage()) + colon);

            ChoiceBox<Themes> input = buildStyleThemeChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            Text label = new Text(multilinguism.getTrad("FixationLength", config.getLanguage()) + colon);

            ChoiceBox<Double> input = buildFixLengthChooserMenu(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            Text label = new Text(multilinguism.getTrad("WhereIsItDirectory", config.getLanguage()) + colon);

            Button input = buildWhereIsItDirectoryChooser(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        {
            Text label = new Text(multilinguism.getTrad("QuestionLength", config.getLanguage()) + colon);

            ChoiceBox<Double> input = buildQuestionLengthChooserMenu(config, configurationContext);

            addToGrid(grid, currentFormRow, label, input);
        }

        return grid;
    }

    private static void addToGrid(GridPane grid, AtomicInteger currentFormRow, Text label, Control input) {

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

        choiceBox.getItems().add((double) configuration.getFixationlength() / 1000);
        while (i <= 30000) {

            choiceBox.getItems().add(((double) i) / 1000);
            i = i + 100;
        }

        choiceBox.getSelectionModel().select(0);
        choiceBox.setPrefWidth(prefWidth);
        choiceBox.setPrefHeight(prefHeight);

        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                final int newPropertyValue = (int) (1000
                        * (double) choiceBox.getItems().get(Integer.parseInt(newValue.intValue() + "")));

                ConfigurationBuilder.createFromPropertiesResource().withFixationLength(newPropertyValue)
                        .saveConfigIgnoringExceptions();

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
        choiceBox.setPrefWidth(prefWidth);
        choiceBox.setPrefHeight(prefHeight);

        choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                final int newPropertyValue = (int) (1000
                        * (double) choiceBox.getItems().get(Integer.parseInt(newValue.intValue() + "")));

                ConfigurationBuilder.createFromPropertiesResource().withQuestionLength(newPropertyValue)
                        .saveConfigIgnoringExceptions();

            }
        });

        return choiceBox;
    }

    /**
     * Fonction to use to permit to user to select between several theme
     */
    private static ChoiceBox<Themes> buildStyleThemeChooser(Configuration configuration,
            ConfigurationContext configurationContext) {
        ChoiceBox<Themes> themesBox = new ChoiceBox<>();
        Themes[] TThemes = Themes.values();

        int firstPos = 1;

        for (int i = 0; i < TThemes.length; i++) {
            themesBox.getItems().add(TThemes[i]);
        }
        final String cssfile = configuration.getCssfile();

        if (cssfile.indexOf("orange") > 0) {
            themesBox.getSelectionModel().select(0);
        } else if (cssfile.indexOf("green") > 0) {
            themesBox.getSelectionModel().select(1);
        } else if (cssfile.indexOf("light-blue") > 0) {
            themesBox.getSelectionModel().select(2);
        } else
            themesBox.getSelectionModel().select(3);
        themesBox.setPrefWidth(prefWidth);
        themesBox.setPrefHeight(prefHeight);

        themesBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                log.info(newValue + "");

                String newPropertyValue;

                if (TThemes[newValue.intValue()].toString().equals("green"))
                    newPropertyValue = "data/stylesheets/main-green.css";
                else if (TThemes[newValue.intValue()].toString().equals("blue"))
                    newPropertyValue = "data/stylesheets/main-blue.css";
                else if (TThemes[newValue.intValue()].toString().equals("light_blue"))
                    newPropertyValue = "data/stylesheets/main-light-blue.css";
                else
                    newPropertyValue = "data/stylesheets/main-orange.css";

                ConfigurationBuilder.createFromPropertiesResource().withCssFile(newPropertyValue)
                        .saveConfigIgnoringExceptions();

                Scene scene = configurationContext.getScene();

                scene.getStylesheets().remove(0);
                scene.getStylesheets().add(newPropertyValue);

                log.info(scene.getStylesheets().toString());
            }
        });

        return themesBox;
    }

    /**
     * Fonction to use to permit to user to choose his/her own css file
     */
    private static Button buildStyleFileChooser(Configuration configuration,
            ConfigurationContext configurationContext) {

        Button buttonLoad = new Button(configuration.getCssfile());

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                FileChooser fileChooser = new FileChooser();
                Scene scene = configurationContext.getScene();
                File file = fileChooser.showOpenDialog(scene.getWindow());
                buttonLoad.setText(file.toString());

                String newPropertyValue = file.toString();
                if (Utils.isWindows()) {
                    newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
                }

                ConfigurationBuilder.createFromPropertiesResource().withCssFile(newPropertyValue)
                        .saveConfigIgnoringExceptions();

                scene.getStylesheets().remove(0);
                scene.getStylesheets().add("file://" + newPropertyValue);

                log.info(scene.getStylesheets().toString());
            }
        });

        return buttonLoad;
    }

    private static Button buildDirectoryChooser(Configuration configuration,
            ConfigurationContext configurationContext) {

        final String filedir = configuration.getFiledir();

        Button buttonLoad = new Button(filedir);

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File file = directoryChooser.showDialog(configurationContext.getScene().getWindow());
                if (file == null) {
                    return;
                }
                buttonLoad.setText(file.toString() + Utils.FILESEPARATOR);

                String newPropertyValue = file.toString() + Utils.FILESEPARATOR;

                if (Utils.isWindows()) {
                    newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
                }

                ConfigurationBuilder.createFromPropertiesResource().withFileDir(newPropertyValue)
                        .saveConfigIgnoringExceptions();
            }
        });

        return buttonLoad;
    }

    private static Button buildWhereIsItDirectoryChooser(Configuration configuration,
            ConfigurationContext configurationContext) {

        final String whereIsItDir = configuration.getWhereIsItDir();
        Button buttonLoad = new Button(whereIsItDir);

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File file = directoryChooser.showDialog(configurationContext.getScene().getWindow());
                if (file == null) {
                    return;
                }
                buttonLoad.setText(file.toString() + Utils.FILESEPARATOR);

                String newPropertyValue = file.toString() + Utils.FILESEPARATOR;

                if (Utils.isWindows()) {
                    newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
                }

                ConfigurationBuilder.createFromPropertiesResource().withWhereIsItDir(newPropertyValue)
                        .saveConfigIgnoringExceptions();
            }
        });

        return buttonLoad;
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

        choiceBox.setPrefWidth(prefWidth);
        choiceBox.setPrefHeight(prefHeight);

        choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Languages>() {
            @Override
            public void changed(ObservableValue<? extends Languages> observable, Languages oldValue,
                    Languages newValue) {

                ConfigurationBuilder.createFromPropertiesResource().withLanguage(newValue.name())
                        .saveConfigIgnoringExceptions();

                configurationContext.getGazePlay().getHomeMenuScreen().onLanguageChanged();

                GridPane gridPane = buildConfigGridPane(configurationContext);
                configurationContext.getRoot().setCenter(null);
                configurationContext.getRoot().setCenter(gridPane);
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

        choiceBox.setPrefWidth(prefWidth);
        choiceBox.setPrefHeight(prefHeight);

        choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EyeTracker>() {
            @Override
            public void changed(ObservableValue<? extends EyeTracker> observable, EyeTracker oldValue,
                    EyeTracker newValue) {
                final String newPropertyValue = newValue.name();
                ConfigurationBuilder.createFromPropertiesResource().withEyeTracker(newPropertyValue)
                        .saveConfigIgnoringExceptions();
            }
        });

        return choiceBox;
    }

    private static EyeTracker findSelectedEyeTracker(Configuration configuration) {
        for (EyeTracker currentEyeTracker : EyeTracker.values()) {
            if (currentEyeTracker.name().equals(configuration.getEyetracker())) {
                return currentEyeTracker;
            }
        }
        return null;
    }

}
