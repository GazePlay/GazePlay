package blocs;

/**
 * Created by schwab on 29/10/2016.
 */

import com.sun.glass.ui.Screen;
import gaze.GazeEvent;
import gaze.GazeUtils;
import gaze.SecondScreen;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.scene.control.ChoiceBox;
import utils.games.Home;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import utils.games.Bravo;
import utils.games.Utils;

public class Blocs extends Application {

    private static EventHandler<Event> enterEvent;
    private static Group blockRoot;
    private static int count;
    private static int initCount;
    private static float p4w;
    private static boolean finished = false;
    private static Scene theScene;
    private static int nColomns;
    private static int nLines;
    private static boolean hasColors;
    private static Bravo bravo = new Bravo();
    private static ChoiceBox<String> choiceBox;



    public static void main(String[] args) {Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Blocs");

        primaryStage.setFullScreen(true);

        blockRoot = new Group();

        theScene = new Scene(blockRoot, Screen.getScreens().get(0).getWidth(), Screen.getScreens().get(0).getHeight(), Color.BLACK);

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(theScene);

        makeBlocks(theScene, blockRoot, null, 2, 2, true, 1);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }

    public static void makeBlocks(Scene scene, Group root, ChoiceBox<String> cbxGames, int nbColomns, int nbLines, boolean colors, float percents4Win){

        finished = false;

        p4w = percents4Win;

        blockRoot=root;

        nColomns = nbColomns;

        nLines = nbLines;

        hasColors = colors;

        theScene = scene;

        choiceBox = cbxGames;

        blockRoot.getChildren().add(bravo);

        //scene.setFill(new ImagePattern(new Image("file:data/blocs/images/déménagement.jpg")));

        Image[] images = Utils.getImages(System.getProperty("user.home") + "/GazePlay/files/images/blocs");

        int value = (int)Math.floor(Math.random()*images.length);

        System.out.println(value);

        scene.setFill(new ImagePattern(images[value]));

        enterEvent = buildEvent();

        int width = (int)(scene.getWidth() / nbColomns);
        int height = (int)(scene.getHeight() / nbLines);

        initCount = nbColomns * nbLines;

        count = initCount;

        for (int i = 0; i < nbColomns; i++)
            for (int j = 0; j < nbLines; j++) {

                Rectangle R = new Rectangle(i * width, j * height, width, height);
                if(colors)
                    R.setFill(new Color(Math.random(), Math.random(), Math.random(), 1));
                else
                    R.setFill(Color.BLACK);
                root.getChildren().add(R);

                GazeUtils.addEventFilter(R);

                R.addEventFilter(MouseEvent.ANY, enterEvent);

                R.addEventFilter(GazeEvent.ANY, enterEvent);
            }
    }

    private static EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if(!finished && e.getEventType().equals(MouseEvent.MOUSE_ENTERED) || e.getEventType().equals(GazeEvent.GAZE_ENTERED)) {

                    Rectangle R = (Rectangle) e.getTarget();

                    R.removeEventFilter(MouseEvent.ANY, enterEvent);
                    R.removeEventFilter(GazeEvent.ANY, enterEvent);
                    GazeUtils.removeEventFilter(R);
                    R.setTranslateX(-10000);
                    R.setOpacity(0);
                    count--;

                    if(((float)initCount-count)/initCount >= p4w && !finished){

                        finished = true;

                        for(Node N : blockRoot.getChildren()){

                            if(! (N instanceof Home) && ! (N instanceof Bravo)) {

                                N.setTranslateX(-10000);
                                N.setOpacity(0);
                                N.removeEventFilter(MouseEvent.ANY, enterEvent);
                                R.removeEventFilter(GazeEvent.ANY, enterEvent);
                                GazeUtils.removeEventFilter(R);
                            }
                        }
                        SequentialTransition sequence = bravo.win();
                        sequence.setOnFinished(new EventHandler<ActionEvent>() {

                            @Override
                            public void handle(ActionEvent actionEvent) {
                                Utils.clear(theScene, blockRoot, choiceBox);
                                makeBlocks(theScene, blockRoot, choiceBox, nColomns, nLines, hasColors, p4w);
                            }
                        });
                    }
                }
            }
        };
    }
}
