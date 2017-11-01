package net.gazeplay.utils;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by schwab on 22/12/2016.
 */
@Slf4j
public class License extends Rectangle {

    public License(double X, double Y, double width, double height, Scene scene, Group root, ChoiceBox<String> cbxGames,
            String language) {

        super(X, Y, width, height);

        this.setFill(new ImagePattern(new Image("data/common/images/license.png"), 0, 0, 1, 1, true));

        EventHandler<Event> homeEvent = new EventHandler<javafx.event.Event>() {
            @Override
            public void handle(javafx.event.Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    HomeUtils.clear(scene, root, cbxGames);

                    root.getChildren().add(licence(width, height));

                    HomeUtils.home(scene, root, cbxGames, null);
                }
            }
        };

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);

    }

    private Text licence(double width, double height) {

        Text text = new Text();

        text.setX(width * 0.1);
        text.setY(height * 0.1);
        text.setFont(new Font(20));

        text.setFill(new Color(1, 1, 1, 1));

        StringBuilder licence = new StringBuilder(10000);
        String line;

        try {
            BufferedReader br = new BufferedReader(new FileReader("data/common/licence.txt"));

            while ((line = br.readLine()) != null) {
                licence.append('\n');
                licence.append(line);
            }
            br.close();
        } catch (IOException e) {
            log.error("Exception", e);
        }

        text.setText(licence.toString());

        return text;

    }

    private ScrollPane licencesp(double width, double height) {

        ScrollPane sp = new ScrollPane();

        Text text = new Text();

        text.setX(width * 0.1);
        text.setY(height * 0.1);
        text.setFont(new Font(20));

        text.setFill(new Color(1, 1, 1, 1));

        StringBuilder licence = new StringBuilder(10000);
        String line;

        try {
            BufferedReader br = new BufferedReader(new FileReader("data/common/licence.txt"));

            while ((line = br.readLine()) != null) {
                licence.append('\n');
                licence.append(line);
            }
            br.close();
        } catch (IOException e) {
            log.error("Exception", e);
        }

        text.setText(licence.toString());

        sp.setContent(text);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setVmax(height);
        // sp.setPrefSize(width*0.8, height*0.8);

        return sp;

    }

}
