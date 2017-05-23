package samplesAmela;

import gaze.SecondScreen;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by schwab on 28/08/2016.
 */
public class Bubbles extends Application {


    public static void main(String[] args) {
        Application.launch(Bubbles.class, args);
    }


    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Bubbles");
        primaryStage.setFullScreen(true);

        Group root = new Group();
        Scene scene = new Scene(root, 1200, 700, Color.BLACK);
        primaryStage.setOnCloseRequest((WindowEvent we)-> System.exit(0));

        primaryStage.setScene(scene);

        Bubble bubble = new Bubble(scene);

        root.getChildren().add(bubble);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();


    }


}