package net.gazeplay.games.animals;

//It is repeated always, it works like a charm :)

import com.sun.glass.ui.Screen;
import gaze.SecondScreen;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


/**
 * Created by Didier Schwab on the 18/11/2017
 */

public class whereIsTheAnimal extends Application {

    private static Group root;
    private static Scene scene;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Where is the animal ?");

        primaryStage.setFullScreen(true);

        root = new Group();

        scene = new Scene(root, Screen.getScreens().get(0).getWidth(), Screen.getScreens().get(0).getHeight(), Color.BLACK);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        AnimalStats stats = new AnimalStats(scene);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }



}
