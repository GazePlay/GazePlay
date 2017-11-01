package net.gazeplay;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.utils.HomeUtils;
import net.gazeplay.utils.stats.Stats;
import utils.games.Utils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by schwab on 17/12/2016.
 */
@Slf4j
public class GazePlay extends Application {

    private Scene scene;
    private Group root;

    private ChoiceBox<String> cbxGames;

    private List<GameSpec> games;

    private final GamesLocator gamesLocator = new DefaultGamesLocator();

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("GazePlay");

        primaryStage.setFullScreen(true);

        games = gamesLocator.listGames();

        root = new Group();

        scene = new Scene(root, com.sun.glass.ui.Screen.getScreens().get(0).getWidth(),
                com.sun.glass.ui.Screen.getScreens().get(0).getHeight(), Color.BLACK);

        Utils.addStylesheets(scene.getStylesheets());

        log.info(scene.getStylesheets().toString());

        if (scene.getStylesheets().isEmpty())
            scene.getStylesheets().add("data/stylesheets/main-orange.css");

        cbxGames = new ChoiceBox<>();

        List<String> gamesLabels = games.stream().map(GameSpec::getLabel).collect(Collectors.toList());

        cbxGames.getItems().addAll(gamesLabels);

        cbxGames.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                chooseGame(newValue.intValue());
            }
        });

        // root.getChildren().add(cbxGames);

        HomeUtils.goHome(scene, root, cbxGames);

        /*
         * cbxGames.setScaleX(2); cbxGames.setScaleY(2);
         * 
         * cbxGames.setTranslateX(scene.getWidth() * 0.9 / 2); cbxGames.setTranslateY(scene.getHeight() * 0.9 / 2);
         * 
         * cbxGames.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
         * 
         * @Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
         * {
         * 
         * chooseGame(newValue.intValue()); } });
         * 
         * root.getChildren().add(cbxGames);
         * 
         * HomeUtils.addButtons(scene, root, cbxGames);
         */

        primaryStage.setOnCloseRequest((WindowEvent we) -> System.exit(0));

        primaryStage.setScene(scene);

        primaryStage.show();

        // SecondScreen secondScreen = SecondScreen.launch();
    }

    private void chooseGame(int gameIndex) {
        HomeUtils.clear(scene, root, cbxGames);

        log.info("Game number: " + gameIndex);

        if (gameIndex == -1) {
            return;
        }

        GameSpec selectedGameSpec = games.get(gameIndex);

        log.info(selectedGameSpec.getLabel());

        Stats stats = selectedGameSpec.launch(scene, root, cbxGames);

        HomeUtils.home(scene, root, cbxGames, stats);
    }
}
