package arduino.windmill;

import com.sun.glass.ui.Screen;
import net.gazeplay.commons.gaze.GazeListener;
import net.gazeplay.commons.gaze.GazeListenerFactory;
import net.gazeplay.commons.gaze.SecondScreen;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by schwab on 23/10/2016.
 */
public class WindMill extends Application {

    public static void main(String[] args) {
        Application.launch(WindMill.class, args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("WindMill");

        primaryStage.setFullScreen(true);

        Group root = new Group();

        Scene scene = new Scene(root, Screen.getScreens().get(0).getWidth(), Screen.getScreens().get(0).getHeight(),
                Color.BLACK);

        GazeListener gazeListener = GazeListenerFactory.getInstance().createNewGazeListener();

        Choices pictos = new Choices(scene, gazeListener);

        root.getChildren().add(pictos);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen.launch();
    }
}
