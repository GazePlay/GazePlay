package sample;

import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by schwab on 15/08/2016.
 */
@Slf4j
public class TestGaze extends Application {

    public static void main(String[] args) {
        Application.launch(sample.TestGaze.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TestGaze");
        Group root = new Group();
        Scene scene = new Scene(root, 1200, 700, Color.BLACK);

        Circle c = new Circle(300, 300, 100, Color.WHITESMOKE);

        root.getChildren().add(c);

        c.addEventFilter(GazeEvent.GAZE_ENTERED, event -> log.info("Field filtered strike: " + event.getEventType()));

        c.addEventHandler(GazeEvent.GAZE_ENTERED, event -> log.info("Field handled strike: " + event.getEventType()));

        primaryStage.setScene(scene);
        primaryStage.show();

        c.fireEvent(new GazeEvent());

    }
}
