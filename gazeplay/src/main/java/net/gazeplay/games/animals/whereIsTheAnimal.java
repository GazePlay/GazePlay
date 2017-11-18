package net.gazeplay.games.animals;

//It is repeated always, it works like a charm :)

import com.sun.glass.ui.Screen;
import gaze.SecondScreen;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
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

        buildGame(root, scene);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }

    public void buildGame(Group groupRoot, Scene gameScene) {

        root = groupRoot;
        scene = gameScene;

        Rectangle2D bounds = javafx.stage.Screen.getPrimary().getBounds();

        double width = bounds.getWidth();
        double height = bounds.getHeight();

        Rectangle R1 = new Rectangle(width/2, height/2);
        Rectangle R2 = new Rectangle(width/2, height/2);
        Rectangle R3 = new Rectangle(width/2, height/2);
        Rectangle R4 = new Rectangle(width/2, height/2);

        R1.setX(0);
        R1.setY(0);
        R2.setX(width/2);
        R2.setY(0);
        R3.setX(0);
        R3.setY(height/2);
        R4.setX(width/2);
        R4.setY(height/2);

        R1.setFill(new ImagePattern(new Image("data/animals/images/bees/bee-1040521_1280.jpg"), 0, 0, 1, 1, true));
        R2.setFill(new ImagePattern(new Image("data/animals/images/cats/cat-1337527_1280.jpg"), 0, 0, 1, 1, true));
        R3.setFill(new ImagePattern(new Image("data/animals/images/crocodiles/animal-194914_1280.jpg"), 0, 0, 1, 1, true));
        R4.setFill(new ImagePattern(new Image("data/animals/images/horses/horse-2572051_1280.jpg"), 0, 0, 1, 1, true));

        root.getChildren().addAll(R1,R2,R3,R4);
    }


}
