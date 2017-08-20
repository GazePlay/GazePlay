package ninja;

import gaze.SecondScreen;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import utils.games.Stats;

/**
 * Created by schwab on 26/12/2016.
 */
public class Ninja extends Application {

        public static void main(String[] args) {
            Application.launch(ninja.Ninja.class, args);
        }

        @Override
        public void start(Stage primaryStage) {

            primaryStage.setTitle("Ninja Portraits");

            primaryStage.setFullScreen(true);

            Group root = new Group();

            Scene scene = new Scene(root, com.sun.glass.ui.Screen.getScreens().get(0).getWidth(), com.sun.glass.ui.Screen.getScreens().get(0).getHeight(), Color.BLACK);

            Stats stats = new Stats();

            launch(root, scene, stats);

            primaryStage.setOnCloseRequest((WindowEvent we)-> System.exit(0));

            primaryStage.setScene(scene);

            primaryStage.show();

            SecondScreen secondScreen = SecondScreen.launch();
        }

    public static void launch(Group root, Scene scene, Stats stats) {

        Target portrait = new Target(root, stats);

        root.getChildren().add(portrait);
    }
}
