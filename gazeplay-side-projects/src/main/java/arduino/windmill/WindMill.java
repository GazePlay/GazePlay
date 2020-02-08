package arduino.windmill;

import com.sun.glass.ui.Screen;
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
 * Created by schwab on 23/10/2016.
 */
public class WindMill extends Application {

    public static void main(final String[] args) {
        Application.launch(WindMill.class, args);
    }

    @Override
    public void start(final Stage primaryStage) {

        primaryStage.setTitle("WindMill");

        primaryStage.setFullScreen(true);

        final Group root = new Group();

        final Scene scene = new Scene(root, Screen.getScreens().get(0).getWidth(), Screen.getScreens().get(0).getHeight(),
            Color.BLACK);

        final GazeDeviceManagerFactory gazeDeviceManagerFactory = new GazeDeviceManagerFactory();
        final GazeDeviceManager gazeDeviceManager = gazeDeviceManagerFactory.get();

        final Choices pictos = new Choices(scene, gazeDeviceManager);

        root.getChildren().add(pictos);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen.launch();
    }
}
