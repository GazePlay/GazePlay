package magiccards;

import gaze.SecondScreen;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import utils.games.stats.HiddenItemsGames;

/**
 * Created by schwab on 17/09/2016.
 */
public class MagicCards  extends Application {

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Magic Card");

        primaryStage.setFullScreen(true);

        Group root = new Group();

        Scene scene = new Scene(root, com.sun.glass.ui.Screen.getScreens().get(0).getWidth(), com.sun.glass.ui.Screen.getScreens().get(0).getHeight(), Color.BLACK);

        HiddenItemsGames stats = new HiddenItemsGames(scene);

        Card.addCards(root, scene, null,2, 2, stats);

        primaryStage.setOnCloseRequest((WindowEvent we)-> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }
}