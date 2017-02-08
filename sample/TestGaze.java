package sample;

import gaze.GazeEvent;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * Created by schwab on 15/08/2016.
 */
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

        c.addEventFilter(
                GazeEvent.GAZE_ENTERED,
                event -> System.out.println(
                        "Field filtered strike: " + event.getEventType()
                )
        );

        c.addEventHandler(
                GazeEvent.GAZE_ENTERED,
                event -> System.out.println(
                        "Field handled strike: " + event.getEventType()
                )
        );

        primaryStage.setScene(scene);
        primaryStage.show();



        c.fireEvent(new GazeEvent());

    }
}