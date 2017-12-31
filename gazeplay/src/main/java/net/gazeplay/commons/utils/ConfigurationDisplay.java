package net.gazeplay.commons.utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.gaze.EyeTrackers;
import net.gazeplay.commons.gaze.GazeUtils;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.layout.Themes;
import net.gazeplay.commons.utils.multilinguism.Languages;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;

import java.io.File;

/**
 * Created by schwab on 28/10/2017.
 */

@Slf4j
public class ConfigurationDisplay extends Rectangle {

    private static ConfigurationDisplay configurationDisplay;

    private static double prefWidth = 200;
    private static double prefHeight = 25;

    private ConfigurationDisplay(double X, double Y, double width, double heigth) {

        super(X, Y, width, heigth);

        this.setFill(new ImagePattern(new Image("data/common/images/configuration-button-alt4.png"), 0, 0, 1, 1, true));
    }

    public static ConfigurationDisplay addConfig(Scene scene, Group root, ChoiceBox cbxGames) {

        double width = scene.getWidth() / 10;
        double height = width;
        double X = 0;
        double Y = scene.getHeight() - height * 1.1;

        configurationDisplay = new ConfigurationDisplay(X, Y, width, height);

        configurationDisplay.setVisible(true);

        EventHandler<Event> configEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    buildConfig(scene, root, cbxGames);
                }
            }
        };

        configurationDisplay.addEventHandler(MouseEvent.MOUSE_CLICKED, configEvent);

        return configurationDisplay;
    }

    private static void buildConfig(Scene scene, Group root, ChoiceBox cbxGames) {

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        Multilinguism multilinguism = Multilinguism.getSingleton();

        // to add or not a space before colon (:) according to the language
        String colon = multilinguism.getTrad("Colon", config.getLanguage());
        if (colon.equals("_noSpace"))
            colon = ": ";
        else
            colon = " : ";

        log.info("ConfigurationDisplay");
        HomeUtils.clear();
        HomeUtils.home(scene, root, cbxGames, null);

        log.info(config.toString());

        Text Configuration = new Text(multilinguism.getTrad("ConfigTitle", config.getLanguage()));
        Configuration.setX(scene.getWidth() * 0.4);
        Configuration.setY(60);
        Configuration.setId("title");

        Text language = new Text(multilinguism.getTrad("Lang", config.getLanguage()) + colon);
        language.setX(100);
        language.setY(100);
        language.setId("item");
        buildLanguageMenu(config, scene, root, cbxGames, 250, 105);

        Text eyeTracker = new Text(multilinguism.getTrad("EyeTracker", config.getLanguage()) + colon);
        eyeTracker.setX(100);
        eyeTracker.setY(200);
        eyeTracker.setId("item");
        buildEyeTrackerMenu(config, root, 250, 205);

        Text fileDir = new Text(multilinguism.getTrad("FileDir", config.getLanguage()) + colon);
        fileDir.setX(100);
        fileDir.setY(300);
        fileDir.setId("item");
        buildDirectoryChooserMenu(scene, config, root, 250, 305);

        Text styleFile = new Text(multilinguism.getTrad("LayoutFile", config.getLanguage()) + colon);
        styleFile.setX(100);
        styleFile.setY(400);
        styleFile.setId("item");
        buildStyleChooser(scene, config, root, 250, 405);

        Text fixLength = new Text(multilinguism.getTrad("FixationLength", config.getLanguage()) + colon);
        fixLength.setX(100);
        fixLength.setY(500);
        fixLength.setId("item");
        buildFixLengthChooserMenu(scene, config, root, 250, 505);

        Text wisGameDir = new Text(multilinguism.getTrad("WhereIsItDirectory", config.getLanguage()) + colon);
        wisGameDir.setX(100);
        wisGameDir.setY(600);
        wisGameDir.setId("item");
        buildWITDirectoryChooserMenu(scene, config, root, 250, 605);

        root.getChildren().addAll(Configuration, language, eyeTracker, fileDir, styleFile, fixLength, wisGameDir);
    }

    private static void buildFixLengthChooserMenu(Scene scene, Configuration configuration, Group root, int posX,
            int posY) {

        ChoiceBox FixLengthBox = new ChoiceBox();

        int i = 300;

        FixLengthBox.getItems().add(new Double((double) configuration.getFixationlength()) / 1000);
        while (i <= 30000) {

            FixLengthBox.getItems().add(new Double(((double) i) / 1000));
            i = i + 100;
        }

        FixLengthBox.getSelectionModel().select(0);
        FixLengthBox.setTranslateX(posX);
        FixLengthBox.setTranslateY(posY);
        FixLengthBox.setPrefWidth(prefWidth);
        FixLengthBox.setPrefHeight(prefHeight);

        root.getChildren().add(FixLengthBox);

        FixLengthBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                final int newPropertyValue = (int) (1000
                        * (double) FixLengthBox.getItems().get(Integer.parseInt(newValue.intValue() + "")));

                ConfigurationBuilder.createFromPropertiesResource().withFixationLength(newPropertyValue)
                        .saveConfigIgnoringExceptions();

            }
        });
    }

    /**
     * Fonction to use to permit to user to select between several theme
     *
     * @param scene
     * @param configuration
     * @param root
     * @param posX
     * @param posY
     */

    private static void buildStyleChooser(Scene scene, Configuration configuration, Group root, int posX, int posY) {

        ChoiceBox themesBox = new ChoiceBox();
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
        themesBox.setTranslateX(posX);
        themesBox.setTranslateY(posY);
        themesBox.setPrefWidth(prefWidth);
        themesBox.setPrefHeight(prefHeight);
        root.getChildren().add(themesBox);

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

                scene.getStylesheets().remove(0);

                scene.getStylesheets().add(newPropertyValue);

                log.info(scene.getStylesheets().toString());
            }
        });
    }

    /**
     * Fonction to use to permit to user to choose his/her own css file
     *
     * @param scene
     * @param configuration
     * @param root
     * @param posX
     * @param posY
     */

    private static void buildStyleFileChooser(Scene scene, Configuration configuration, Group root, int posX,
            int posY) {

        Button buttonLoad = new Button(configuration.getCssfile());

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(scene.getWindow());
                buttonLoad.setText(file.toString());

                String newPropertyValue = file.toString();
                if (Utils.isWindows()) {
                    newPropertyValue = Utils.convertWindowsPath(newPropertyValue);
                }

                ConfigurationBuilder.createFromPropertiesResource().withCssFile(newPropertyValue)
                        .saveConfigIgnoringExceptions();

                log.info(scene.getStylesheets().toString());

                scene.getStylesheets().remove(0);

                scene.getStylesheets().add("file://" + newPropertyValue);

                log.info(scene.getStylesheets().toString());
            }
        });

        buttonLoad.setTranslateX(posX);
        buttonLoad.setTranslateY(posY);

        root.getChildren().add(buttonLoad);
    }

    private static void buildDirectoryChooserMenu(Scene scene, Configuration configuration, Group root, int posX,
            int posY) {

        final String filedir = configuration.getFiledir();

        Button buttonLoad = new Button(filedir);

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File file = directoryChooser.showDialog(scene.getWindow());
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

        buttonLoad.setTranslateX(posX);
        buttonLoad.setTranslateY(posY);

        root.getChildren().add(buttonLoad);
    }

    private static void buildWITDirectoryChooserMenu(Scene scene, Configuration configuration, Group root, int posX,
            int posY) {

        final String whereIsItDir = configuration.getWhereIsItDir();
        Button buttonLoad = new Button(whereIsItDir);

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File file = directoryChooser.showDialog(scene.getWindow());
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

        buttonLoad.setTranslateX(posX);
        buttonLoad.setTranslateY(posY);

        root.getChildren().add(buttonLoad);
    }

    private static void buildLanguageMenu(Configuration configuration, Scene scene, Group root, ChoiceBox cbxGames,
            double posX, double posY) {

        Languages currentLanguage = null;
        if (configuration.getLanguage() != null) {
            currentLanguage = Languages.valueOf(configuration.getLanguage());
        }

        ChoiceBox<Languages> languageBox = new ChoiceBox<>();
        languageBox.getItems().addAll(Languages.values());
        languageBox.getSelectionModel().select(currentLanguage);

        languageBox.setTranslateX(posX);
        languageBox.setTranslateY(posY);
        languageBox.setPrefWidth(prefWidth);
        languageBox.setPrefHeight(prefHeight);

        root.getChildren().add(languageBox);

        languageBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Languages>() {
            @Override
            public void changed(ObservableValue<? extends Languages> observable, Languages oldValue,
                    Languages newValue) {

                ConfigurationBuilder.createFromPropertiesResource().withLanguage(newValue.name())
                        .saveConfigIgnoringExceptions();

                GazePlay.getInstance().onLanguageChanged();

                buildConfig(scene, root, cbxGames);// game names change following the language
            }
        });
    }

    private static void buildEyeTrackerMenu(Configuration configuration, Group root, double posX, double posY) {
        ChoiceBox EyeTrackersBox = new ChoiceBox();
        EyeTrackers[] TEyeTrackers = EyeTrackers.values();

        int firstPos = 1;

        for (int i = 0; i < TEyeTrackers.length; i++) {
            EyeTrackersBox.getItems().add(TEyeTrackers[i]);
            if (TEyeTrackers[i].toString().equals(configuration.getEyetracker())) {
                firstPos = i;

            }
        }

        EyeTrackersBox.getSelectionModel().select(firstPos);
        EyeTrackersBox.setTranslateX(posX);
        EyeTrackersBox.setTranslateY(posY);
        EyeTrackersBox.setPrefWidth(prefWidth);
        EyeTrackersBox.setPrefHeight(prefHeight);
        root.getChildren().add(EyeTrackersBox);

        EyeTrackersBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                final String newPropertyValue = TEyeTrackers[newValue.intValue()].toString();
                ConfigurationBuilder.createFromPropertiesResource().withEyeTracker(newPropertyValue)
                        .saveConfigIgnoringExceptions();
            }
        });
    }
}
