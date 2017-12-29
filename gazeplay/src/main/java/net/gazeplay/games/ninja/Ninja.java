package net.gazeplay.games.ninja;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.gazeplay.commons.gaze.SecondScreen;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.stats.ShootGamesStats;

/**
 * Created by schwab on 26/12/2016.
 */
public class Ninja extends Application {

    public static void main(String[] args) {
        Application.launch(net.gazeplay.games.ninja.Ninja.class, args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Ninja Portraits");

        primaryStage.setFullScreen(true);

        Group root = new Group();

        Scene scene = new Scene(root, com.sun.glass.ui.Screen.getScreens().get(0).getWidth(),
                com.sun.glass.ui.Screen.getScreens().get(0).getHeight(), Color.BLACK);

        ShootGamesStats stats = new ShootGamesStats(scene);

        launch(root, scene, stats);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen.launch();
    }

    public static void launch(Group root, Scene scene, ShootGamesStats stats) {

        Portrait.RandomPositionGenerator randomPositionGenerator = new Portrait.RandomPositionGenerator(scene);

        Target portrait = new Target(root, randomPositionGenerator, stats, Portrait.loadAllImages());

        root.getChildren().add(portrait);
    }
}
