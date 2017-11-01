package net.gazeplay.utils;

import gaze.EyeTrackers;
import gaze.configuration.Configuration;
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
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.utils.multilinguism.Languages;
import net.gazeplay.utils.multilinguism.Multilinguism;
import utils.games.Utils;

import java.io.File;
import java.io.IOException;

/**
 * Created by schwab on 28/10/2017.
 */

@Slf4j
public class ConfigurationDisplay extends Rectangle {

    private static ConfigurationDisplay config;

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

        config = new ConfigurationDisplay(X, Y, width, height);

        config.setVisible(true);

        EventHandler<Event> configEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    buildConfig(scene, root, cbxGames);
                }
            }
        };

        config.addEventHandler(MouseEvent.MOUSE_CLICKED, configEvent);

        return config;
    }

    private static void buildConfig(Scene scene, Group root, ChoiceBox cbxGames) {

        Multilinguism multilinguism = Multilinguism.getMultilinguism();

        // to add or not a space before colon (:) according to the language
        String colon = multilinguism.getTrad("Colon", Multilinguism.getLanguage());
        if (colon.equals("_noSpace"))
            colon = ": ";
        else
            colon = " : ";

        log.info("ConfigurationDisplay");
        HomeUtils.clear(scene, root, cbxGames);
        HomeUtils.home(scene, root, cbxGames, null);

        Configuration C = new Configuration();
        log.info(C.toString());

        Text Configuration = new Text(multilinguism.getTrad("ConfigTitle", Multilinguism.getLanguage()));
        Configuration.setX(scene.getWidth() * 0.4);
        Configuration.setY(60);
        Configuration.setId("title");

        Text language = new Text(multilinguism.getTrad("Lang", Multilinguism.getLanguage()) + colon);
        language.setX(100);
        language.setY(100);
        language.setId("item");
        buildLanguageMenu(C, root, 250, 105);

        Text eyeTracker = new Text(multilinguism.getTrad("EyeTracker", Multilinguism.getLanguage()) + colon);
        eyeTracker.setX(100);
        eyeTracker.setY(200);
        eyeTracker.setId("item");
        buildEyeTrackerMenu(C, root, 250, 205);

        Text fileDir = new Text(multilinguism.getTrad("FileDir", Multilinguism.getLanguage()) + colon);
        fileDir.setX(100);
        fileDir.setY(300);
        fileDir.setId("item");
        buildDirectoryChooserMenu(scene, C, root, 250, 305);

        root.getChildren().addAll(Configuration, language, eyeTracker, fileDir);
    }

    private static void buildDirectoryChooserMenu(Scene scene, Configuration C, Group root, int posX, int posY) {

        Button buttonLoad = new Button(C.filedir);

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File file = directoryChooser.showDialog(scene.getWindow());
                buttonLoad.setText(file.toString() + Utils.FILESEPARATOR);
                File F = new File(file.toString());
                C.filedir = file.toString() + Utils.FILESEPARATOR;

                if (Utils.isWindows()) {

                    C.filedir = Utils.convertWindowsPath(C.filedir);
                }

                log.info(C.toString());
                C.saveConfig();
            }
        });

        buttonLoad.setTranslateX(posX);
        buttonLoad.setTranslateY(posY);

        root.getChildren().add(buttonLoad);
    }

    private static void buildLanguageMenu(Configuration C, Group root, double posX, double posY) {
        ChoiceBox languageBox = new ChoiceBox();
        Languages[] TLanguages = Languages.values();

        int firstPos = 1;

        for (int i = 0; i < TLanguages.length; i++) {
            languageBox.getItems().add(TLanguages[i]);
            if (TLanguages[i].toString().equals(C.language)) {
                firstPos = i;

            }
        }

        languageBox.getSelectionModel().select(firstPos);
        languageBox.setTranslateX(posX);
        languageBox.setTranslateY(posY);
        languageBox.setPrefWidth(prefWidth);
        languageBox.setPrefHeight(prefHeight);

        root.getChildren().add(languageBox);

        languageBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                C.language = TLanguages[newValue.intValue()].toString();
                log.info(C.toString());
                C.saveConfig();
            }
        });
    }

    private static void buildEyeTrackerMenu(Configuration C, Group root, double posX, double posY) {
        ChoiceBox EyeTrackersBox = new ChoiceBox();
        EyeTrackers[] TEyeTrackers = EyeTrackers.values();

        int firstPos = 1;

        for (int i = 0; i < TEyeTrackers.length; i++) {
            EyeTrackersBox.getItems().add(TEyeTrackers[i]);
            if (TEyeTrackers[i].toString().equals(C.eyetracker)) {
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

                C.eyetracker = TEyeTrackers[newValue.intValue()].toString();
                log.info(C.toString());
                C.saveConfig();
            }
        });
    }
}