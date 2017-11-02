package gaze;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.Point2D;
import gaze.configuration.Configuration;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by schwab on 10/09/2016.
 */
@Slf4j
public class SecondScreen {

    private static final int pixelWidth = 20;
    private static final int lightingLength = 20;
    private static final Color lightingColor = Color.BLUE;
    private static Lighting[][] T;

    static final GazeManager gm = GazeManager.getInstance();
    static boolean success = gm.activate();
    static IGazeListener gazeListener;

    private SecondScreen() {

    }

    public static SecondScreen launch() {

        ObservableList<Screen> screens = Screen.getScreens();

        if (screens.size() < 2)
            return null;

        Screen screen1 = screens.get(0);
        Screen screen2 = screens.get(1);
        log.info("screen1.getBounds() = " + screen1.getBounds());
        log.info("screen2.getBounds() = " + screen2.getBounds());
        Stage stage2 = new Stage();
        stage2.setScene(new Scene(new Label("primary")));
        stage2.setX(screen2.getVisualBounds().getMinX());
        stage2.setY(screen2.getVisualBounds().getMinY());
        stage2.setWidth(screen1.getBounds().getWidth());
        stage2.setHeight(screen1.getBounds().getHeight());
        Group root = new Group();
        Scene scene = new Scene(root, screen1.getBounds().getWidth(), screen1.getBounds().getHeight(), Color.BLACK);

        makeLighting(root, screen2.getBounds());

        stage2.setScene(scene);

        stage2.show();

        SecondScreen sc = new SecondScreen();

        Configuration config = new Configuration();

        /*
         * if (config.eyetracker.equals("tobii")) Tobii.execProg(sc); else
         */
        if (config.gazeMode.equals("true"))
            gazeListener = new EyeTribeGazeListener(sc);
        else
            gazeListener = new FuzzyGazeListener(sc);

        return sc;
    }

    private static void makeLighting(Group root, Rectangle2D RScreen) {

        int width = (int) RScreen.getWidth();
        int height = (int) RScreen.getHeight();

        T = new Lighting[width / pixelWidth][height / pixelWidth];
        for (int i = 0; i < T.length; i++)
            for (int j = 0; j < T[i].length; j++) {

                T[i][j] = new Lighting(i * pixelWidth, j * pixelWidth, pixelWidth, lightingLength, lightingColor);
                root.getChildren().add(T[i][j]);
            }
    }

    public static void light(Point2D rawCoordinates) {

        T[(int) (rawCoordinates.x / pixelWidth)][(int) (rawCoordinates.y / pixelWidth)].enter();
    }

    public static void light(javafx.geometry.Point2D rawCoordinates) {

        T[(int) (rawCoordinates.getX() / pixelWidth)][(int) (rawCoordinates.getY() / pixelWidth)].enter();
    }
}
