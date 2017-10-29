package net.gazeplay.utils;

import gaze.EyeTrackers;
import gaze.configuration.Configuration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.utils.multilinguism.Languages;


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

        log.info("ConfigurationDisplay");
        HomeUtils.clear(scene, root, cbxGames);
        HomeUtils.home(scene, root, cbxGames, null);

        Configuration C = new Configuration();
        log.info(C.toString());

        Text Configuration = new Text("Configuration");
        Configuration.setX(scene.getWidth()*0.4);
        Configuration.setY(60);
        Configuration.setId("title");

        Text language = new Text("Language");
        language.setX(100);
        language.setY(110);
        language.setId("item");
        buildLanguageMenu(C,root,250,100);

        Text eyeTracker = new Text("Eye-Tracker");
        eyeTracker.setX(100);
        eyeTracker.setY(210);
        eyeTracker.setId("item");
        buildEyeTrackerMenu(C,root,250,200);

        root.getChildren().addAll(Configuration,language, eyeTracker);
    }

    private static void buildLanguageMenu(Configuration C, Group root, double posX, double posY){
        ChoiceBox languageBox = new ChoiceBox();
        Languages[] TLanguages = Languages.values();

        for (int i = 0; i < TLanguages.length; i++) {
            languageBox.getItems().add(TLanguages[i]);
        }
        languageBox.setValue(C.language);
        languageBox.setTranslateX(posX);
        languageBox.setTranslateY(posY);
        languageBox.setPrefWidth(prefWidth);
        languageBox.setPrefHeight(prefHeight);

        root.getChildren().add(languageBox);

        languageBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                C.language=TLanguages[newValue.intValue()].toString();
                log.info(C.toString());
                C.saveConfig();
            }
        });
    }

    private static void buildEyeTrackerMenu(Configuration C, Group root, double posX, double posY){
        ChoiceBox EyeTrackersBox = new ChoiceBox();
        EyeTrackers[] TEyeTrackers = EyeTrackers.values();

        for (int i = 0; i < TEyeTrackers.length; i++) {
            EyeTrackersBox.getItems().add(TEyeTrackers[i]);
        }
        EyeTrackersBox.setValue(C.eyetracker);
        EyeTrackersBox.setTranslateX(posX);
        EyeTrackersBox.setTranslateY(posY);
        EyeTrackersBox.setPrefWidth(prefWidth);
        EyeTrackersBox.setPrefHeight(prefHeight);
        root.getChildren().add(EyeTrackersBox);

        EyeTrackersBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                C.eyetracker=TEyeTrackers[newValue.intValue()].toString();
                log.info(C.toString());
                C.saveConfig();
            }
        });
    }
}