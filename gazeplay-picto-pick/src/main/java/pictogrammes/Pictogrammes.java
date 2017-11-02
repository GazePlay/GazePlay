package pictogrammes;

import gaze.SecondScreen;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by schwab on 23/08/2016.
 */
public class Pictogrammes extends Application {

    public static void main(String[] args) {
        Application.launch(pictogrammes.Pictogrammes.class, args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Pictogrammes");

        primaryStage.setFullScreen(true);

        Group root = new Group();
        Scene scene = new Scene(root, 1200, 700, Color.BLACK);

        Pictos pictos = new Pictos(scene);

        root.getChildren().add(pictos);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }
}
