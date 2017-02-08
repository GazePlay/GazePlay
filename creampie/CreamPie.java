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

        launch(root, scene);

        primaryStage.setOnCloseRequest((WindowEvent we)-> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }

    public static void launch(Group root, Scene scene){

        Hand hand = new Hand(scene);

        Target portrait = new Target(hand);

        root.getChildren().add(portrait);

        root.getChildren().add(hand);
    }
}
