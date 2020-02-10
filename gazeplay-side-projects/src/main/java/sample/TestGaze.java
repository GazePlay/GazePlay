package sample;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

/**
 * Created by schwab on 15/08/2016.
 */
@Slf4j
public class TestGaze extends Application {

    public static void main(final String[] args) {
        Application.launch(sample.TestGaze.class, args);
    }

    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle("TestGaze");
        final Group root = new Group();
        final Scene scene = new Scene(root, 1200, 700, Color.BLACK);

        final Circle c = new Circle(300, 300, 100, Color.WHITESMOKE);

        root.getChildren().add(c);

        c.addEventFilter(GazeEvent.GAZE_ENTERED, event -> log.info("Field filtered strike: " + event.getEventType()));

        c.addEventHandler(GazeEvent.GAZE_ENTERED, event -> log.info("Field handled strike: " + event.getEventType()));

        primaryStage.setScene(scene);
        primaryStage.show();

        c.fireEvent(new GazeEvent());

    }
}
