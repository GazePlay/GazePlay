package gazeplay;

import blocs.Blocs;
import bubbles.Bubble;
import creampie.CreamPie;
import utils.games.stats.BubblesGamesStats;
import utils.games.stats.ShootGamesStats;
import utils.games.stats.Stats;
import gaze.SecondScreen;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import magiccards.Card;
import ninja.Ninja;
import utils.games.Utils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * Created by schwab on 17/12/2016.
 */
public class GazePlay extends Application {

    private Scene scene;
    private Group root;
    private static ChoiceBox<String> cbxGames;

    public static void main(String[] args) {
        Application.launch(GazePlay.class, args);
    }

    @Override
    public void start(Stage primaryStage) {

        /*String current = null;
        try {
            current = new java.io.File( "." ).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Current dir:"+current);

        System.exit(0);*/

        try {
            System.setProperty("file.encoding","UTF-8");
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null,null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        primaryStage.setTitle("GazePlay");

        primaryStage.setFullScreen(true);

        root = new Group();

        scene = new Scene(root, com.sun.glass.ui.Screen.getScreens().get(0).getWidth(), com.sun.glass.ui.Screen.getScreens().get(0).getHeight(), Color.BLACK);

        cbxGames = new ChoiceBox<>();

       cbxGames.getItems().addAll("\tCreampie", "\tNinja Portraits", "Magic Cards\t\t(2x2)", "Magic Cards\t\t(2x3)", "Magic Cards\t\t(3x2)", "Magic Cards\t\t(3x3)", "blocks\t\t\t(2x2)", "blocks\t\t\t(2x3)", "blocks\t\t\t(3x3)", "\tCarte à gratter", "\tColored Bubbles", "\tPortrait Bubbles");

        //cbxGames.getSelectionModel().selectFirst();

        cbxGames.setScaleX(2);
        cbxGames.setScaleY(2);

        cbxGames.setTranslateX(scene.getWidth()*0.9/2);
        cbxGames.setTranslateY(scene.getHeight()*0.9/2);


        cbxGames.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                chooseGame(newValue);
            }
        });

        root.getChildren().add(cbxGames);

        //root.getChildren().add(Utils.exit(scene));
        Utils.addButtons(scene, root, cbxGames);

        primaryStage.setOnCloseRequest((WindowEvent we)-> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        SecondScreen secondScreen = SecondScreen.launch();
    }

    private void chooseGame(Number value) {

        System.out.println(value);

        Utils.clear(scene, root, cbxGames);

        Stats stats = null;

        switch (value.intValue()){

            case 0 : {

                System.out.println("Creampie");

                stats = new ShootGamesStats(scene);

                CreamPie.launch(root, scene, (ShootGamesStats)stats);

                break;
            }

            case 1 : {

                System.out.println("Ninja Portraits");

                stats = new ShootGamesStats(scene);

                Ninja.launch(root, scene, (ShootGamesStats)stats);

                break;
            }

            case 2 :{

                System.out.println("Magic Cards (2x2)");

                Card.addCards(root, scene, cbxGames,2, 2);

                break;
            }

            case 3 : {

                System.out.println("Magic Cards (2x3)");

                Card.addCards(root, scene, cbxGames,2, 3);

                break;
            }

            case 4 : {

                System.out.println("Magic Cards (3x2)");

                Card.addCards(root, scene, cbxGames,3, 2);

                break;
            }

            case 5 : {

                System.out.println("Magic Cards (3x3)");

                Card.addCards(root, scene, cbxGames,3, 3);

                break;
            }

            case 6 :{

                System.out.println("blocks (2x2)");

                Blocs.makeBlocks(scene, root, cbxGames, 2, 2, true, 1);

                break;
            }

            case 7 : {

                System.out.println("blocks (2x3)");

                Blocs.makeBlocks(scene, root, cbxGames, 2, 3, true, 1);

                break;
            }

            case 8 : {

                System.out.println("blocks (3x3)");

                Blocs.makeBlocks(scene, root, cbxGames, 3, 3, true, 1);

                break;
            }

            case 9 : {

                System.out.println("Carte à gratter");

                Blocs.makeBlocks(scene, root, cbxGames, 10, 10, false, 0.6f);

                break;
            }

            case 10 : {

                System.out.println("Colored Bubbles");

                stats = new BubblesGamesStats(scene);

                Bubble bubble = new Bubble(scene, root, Bubble.COLOR, (BubblesGamesStats) stats);

                break;
            }

            case 11 : {

                System.out.println("Portrait Bubbles");

                stats = new BubblesGamesStats(scene);

                Bubble bubble = new Bubble(scene, root, Bubble.PORTRAIT, (BubblesGamesStats)stats);

                break;
            }

            default : {

                System.out.println("No selection");

                break;
            }
        }

        Utils.home(scene, root, cbxGames, stats);

     //   root.getChildren().add(Utils.home(scene, root, cbxGames));


        //System.exit(0);
    }


}
