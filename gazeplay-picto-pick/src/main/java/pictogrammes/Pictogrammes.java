package pictogrammes;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.gazeplay.commons.gaze.SecondScreen;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManagerFactory;

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

        GazeDeviceManager gazeDeviceManager = GazeDeviceManagerFactory.getInstance().createNewGazeListener();

        Pictos pictos = new Pictos(scene, gazeDeviceManager);

        root.getChildren().add(pictos);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen.launch();
    }
}
