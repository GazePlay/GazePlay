package creampie;

/**
 * Created by schwab on 12/08/2016.
 */

import gaze.SecondScreen;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import utils.games.stats.ShootGamesStats;
import utils.games.stats.Stats;

public class CreamPie extends Application {

    public static void main(String[] args) {
        Application.launch(creampie.CreamPie.class, args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("CreamPie");

        primaryStage.setFullScreen(true);

        Group root = new Group();

        Scene scene = new Scene(root, com.sun.glass.ui.Screen.getScreens().get(0).getWidth(), com.sun.glass.ui.Screen.getScreens().get(0).getHeight(), Color.BLACK);

        Stats stats = new ShootGamesStats(scene);

        launch(root, scene, (ShootGamesStats)stats);

        primaryStage.setOnCloseRequest((WindowEvent we)-> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }

    public static void launch(Group root, javafx.scene.Scene scene, ShootGamesStats stats){

        Hand hand = new Hand(scene);

        Target portrait = new Target(hand, stats);

        root.getChildren().add(portrait);

        root.getChildren().add(hand);
    }
}
