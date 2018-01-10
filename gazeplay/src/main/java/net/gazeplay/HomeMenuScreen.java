package net.gazeplay;

import com.sun.glass.ui.Screen;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
public class HomeMenuScreen {

    @Getter
    private final GazePlay gazePlay;

    @Getter
    private final Scene scene;

    private final Group root;

    @Getter
    private final ChoiceBox<String> cbxGames;

    private final List<GameSpec> games;

    private final GamesLocator gamesLocator;

    public HomeMenuScreen(final GazePlay gazePlay, final Configuration config) {
        this.gazePlay = gazePlay;

        gamesLocator = new DefaultGamesLocator();
        games = gamesLocator.listGames();

        root = new Group();

        final Screen screen = Screen.getScreens().get(0);
        log.info("Screen size: {} x {}", screen.getWidth(), screen.getHeight());

        double ratioToScreenSize = 1;
        scene = new Scene(root, screen.getWidth() * ratioToScreenSize, screen.getHeight() * ratioToScreenSize,
                Color.BLACK);

        CssUtil.setPreferredStylesheets(config, scene);

        // end of System information
        for (int i = 0; i < 5; i++) {
            log.info("***********************");
        }

        cbxGames = createChoiceBox(games, config);

        cbxGames.getSelectionModel().clearSelection();
        root.getChildren().add(cbxGames);

        cbxGames.setTranslateX(scene.getWidth() * 0.9 / 2);
        cbxGames.setTranslateY(scene.getHeight() * 0.9 / 2);

        addButtons();
    }

    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    public void setUpOnStage(Stage stage) {
        cbxGames.getSelectionModel().clearSelection();

        stage.setTitle("GazePlay");

        // setting the scene again will exit fullscreen
        // so we need to backup the fullscreen status, and restore it after the scene has been set
        boolean fullscreen = stage.isFullScreen();
        stage.setScene(scene);
        stage.setFullScreen(fullscreen);

        stage.setOnCloseRequest((WindowEvent we) -> stage.close());

        stage.show();
    }

    public void onLanguageChanged() {
        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        final List<String> gamesLabels = generateTranslatedGamesNames(games, config);

        this.cbxGames.getItems().clear();
        this.cbxGames.getItems().addAll(gamesLabels);
    }

    /**
     * This command is called when games have to be updated (example: when language changed)
     */
    private ChoiceBox createChoiceBox(List<GameSpec> games, Configuration config) {
        List<String> gamesLabels = generateTranslatedGamesNames(games, config);

        ChoiceBox<String> cbxGames = new ChoiceBox<>();
        cbxGames.getItems().addAll(gamesLabels);
        cbxGames.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                chooseGame(newValue.intValue());
            }
        });
        return cbxGames;
    }

    private List<String> generateTranslatedGamesNames(List<GameSpec> games, Configuration config) {
        final String language = config.getLanguage();
        final Multilinguism multilinguism = Multilinguism.getSingleton();

        return games.stream()
                .map(gameSpec -> new Pair<>(gameSpec, multilinguism.getTrad(gameSpec.getNameCode(), language)))
                .map(pair -> {
                    String variationHint = pair.getKey().getVariationHint();
                    if (variationHint == null) {
                        return pair.getValue();
                    }
                    return pair.getValue() + " " + variationHint;
                }).collect(Collectors.toList());
    }

    private void chooseGame(int gameIndex) {
        log.info("Game number: " + gameIndex);

        if (gameIndex == -1) {
            return;
        }

        GameSpec selectedGameSpec = games.get(gameIndex);

        chooseGame(selectedGameSpec);
    }

    private void chooseGame(GameSpec selectedGameSpec) {
        log.info(selectedGameSpec.getNameCode() + " " + selectedGameSpec.getVariationHint());

        GameContext gameContext = GameContext.newInstance(gazePlay);

        gazePlay.onGameLaunch(gameContext);
        final Stats stats = selectedGameSpec.launch(gameContext);

        gameContext.createToggleFullScreenButtonInGameScreen(gazePlay);
        gameContext.createHomeButtonInGameScreen(gazePlay, stats);
    }

    private void addButtons() {

        double width = scene.getWidth() / 10;
        double heigth = width;
        double XExit = scene.getWidth() * 0.9;
        double Y = scene.getHeight() - heigth * 1.1;

        // License license = new License(XLicence, Y, width, heigth, scene, root, cbxGames);

        // root.getChildren().add(license);

        Rectangle exitButton = createExitButton(width, heigth, XExit, Y);

        ConfigurationContext configurationContext = ConfigurationContext.newInstance(gazePlay);
        ConfigurationButton configurationButton = ConfigurationButton.createConfigurationDisplay(configurationContext);

        getChildren().add(configurationButton);
        getChildren().add(exitButton);
        getChildren().add(createLogo());
        getChildren().add(Utils.buildLicence());
    }

    private Rectangle createExitButton(double width, double heigth, double XExit, double y) {
        EventHandler<Event> homeEvent = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {

                    System.exit(0);

                }
            }
        };

        Rectangle exitButton = new Rectangle(XExit, y, width, heigth);
        exitButton.setFill(new ImagePattern(new Image("data/common/images/power-off.png"), 0, 0, 1, 1, true));
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, homeEvent);
        return exitButton;
    }

    private Node createLogo() {
        double width = scene.getWidth() * 0.5;
        double height = scene.getHeight() * 0.2;

        double posY = scene.getHeight() * 0.1;
        double posX = (scene.getWidth() - width) / 2;

        Rectangle logo = new Rectangle(posX, posY, width, height);
        logo.setFill(new ImagePattern(new Image("data/common/images/gazeplay.jpg"), 0, 0, 1, 1, true));

        return logo;
    }

}
