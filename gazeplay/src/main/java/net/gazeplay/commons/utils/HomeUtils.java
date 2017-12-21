package net.gazeplay.commons.utils;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.StatsDisplay;
import net.gazeplay.commons.utils.games.Utils;

@Slf4j
public class HomeUtils {

    public static Home home;

    public static void home(Scene scene, Group root, ChoiceBox<String> cbxGames, Stats stats) {

        double width = scene.getWidth() / 10;
        double height = width;
        double X = scene.getWidth() * 0.9;
        double Y = scene.getHeight() - height * 1.1;

        home = new Home(X, Y, width, height);

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    scene.setCursor(Cursor.WAIT); // Change cursor to wait style

                    log.info("stats = " + stats);

                    if (stats == null) {

                        goHome(scene, root, cbxGames);
                    } else {

                        StatsDisplay.displayStats(stats, scene, root, cbxGames);
                    }

                    scene.setCursor(Cursor.DEFAULT); // Change cursor to default style
                }
            }
        };

        home.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        root.getChildren().add(home);
    }

    public static void goHome(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        clear(scene, root, cbxGames);

        if (cbxGames != null) {
            cbxGames.getSelectionModel().clearSelection();
            root.getChildren().add(cbxGames);

            cbxGames.setTranslateX(scene.getWidth() * 0.9 / 2);
            cbxGames.setTranslateY(scene.getHeight() * 0.9 / 2);

            addButtons(scene, root, cbxGames);
        }
    }

    public static void clear(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        scene.setFill(Color.BLACK);

        for (Node N : root.getChildren()) {

            N.setTranslateX(-10000);
        }

        root.getChildren().remove(0, root.getChildren().size());

        root.getChildren().add(Bravo.getBravo());

    }

    public static void addButtons(Scene scene, Group root, ChoiceBox<String> cbxGames) {

        double width = scene.getWidth() / 10;
        double heigth = width;
        double XExit = scene.getWidth() * 0.9;
        double Y = scene.getHeight() - heigth * 1.1;

        // License license = new License(XLicence, Y, width, heigth, scene, root, cbxGames);

        // root.getChildren().add(license);

        Rectangle exit = new Rectangle(XExit, Y, width, heigth);

        exit.setFill(new ImagePattern(new Image("data/common/images/power-off.png"), 0, 0, 1, 1, true));

        EventHandler<javafx.event.Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    System.exit(0);

                }
            }
        };

        exit.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

        root.getChildren().add(ConfigurationDisplay.addConfig(scene, root, cbxGames));

        root.getChildren().add(exit);

        root.getChildren().add(logo(scene));

        root.getChildren().add(Utils.buildLicence());
    }

    public static Node logo(Scene scene) {

        double width = scene.getWidth() * 0.5;
        double height = scene.getHeight() * 0.2;

        double posY = scene.getHeight() * 0.1;
        double posX = (scene.getWidth() - width) / 2;

        Rectangle logo = new Rectangle(posX, posY, width, height);
        logo.setFill(new ImagePattern(new Image("data/common/images/gazeplay.jpg"), 0, 0, 1, 1, true));

        return logo;
    }

}
