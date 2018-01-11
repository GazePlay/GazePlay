package net.gazeplay;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
public class HomeMenuScreen extends GraphicalContext<BorderPane> {

    public static HomeMenuScreen newInstance(final GazePlay gazePlay, final Configuration config) {

        GamesLocator gamesLocator = new DefaultGamesLocator();
        List<GameSpec> games = gamesLocator.listGames();

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, gazePlay.getPrimaryStage().getWidth(), gazePlay.getPrimaryStage().getHeight(),
                Color.BLACK);

        return new HomeMenuScreen(gazePlay, games, scene, root, config);
    }

    @Getter
    private final ChoiceBox<String> cbxGames;

    private final List<GameSpec> games;

    public HomeMenuScreen(GazePlay gazePlay, List<GameSpec> games, Scene scene, BorderPane root, Configuration config) {
        super(gazePlay, root, scene);
        this.games = games;

        addButtons();

        cbxGames = createChoiceBox(games, config);

        cbxGames.getSelectionModel().clearSelection();

        StackPane centerCenterPane = new StackPane();
        centerCenterPane.getChildren().add(cbxGames);

        VBox verticalMenuBox = new VBox();
        verticalMenuBox.getChildren().add(Utils.buildLicence());

        BorderPane centerBorderPane = new BorderPane();
        centerBorderPane.setCenter(centerCenterPane);
        centerBorderPane.setLeft(verticalMenuBox);

        root.setCenter(centerBorderPane);

        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
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

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        CssUtil.setPreferredStylesheets(config, scene);

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

        GazePlay gazePlay = getGazePlay();

        GameContext gameContext = GameContext.newInstance(gazePlay);

        gazePlay.onGameLaunch(gameContext);
        final Stats stats = selectedGameSpec.launch(gameContext);

        gameContext.createToggleFullScreenButtonInGameScreen(gazePlay);
        gameContext.createHomeButtonInGameScreen(gazePlay, stats);
    }

    private void addButtons() {
        GazePlay gazePlay = getGazePlay();

        Rectangle exitButton = createExitButton();

        ConfigurationContext configurationContext = ConfigurationContext.newInstance(gazePlay);
        ConfigurationButton configurationButton = ConfigurationButton.createConfigurationButton(configurationContext);

        FlowPane leftControlPane = new FlowPane();
        leftControlPane.setAlignment(Pos.TOP_LEFT);
        leftControlPane.getChildren().add(configurationButton);

        FlowPane rightControlPane = new FlowPane();
        rightControlPane.setAlignment(Pos.TOP_RIGHT);
        rightControlPane.getChildren().add(exitButton);

        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(leftControlPane);
        bottomPane.setRight(rightControlPane);

        StackPane topPane = new StackPane();
        topPane.getChildren().add(createLogo());

        root.setTop(topPane);
        root.setBottom(bottomPane);
    }

    private Rectangle createExitButton() {
        CustomButton exitButton = new CustomButton("data/common/images/power-off.png");
        exitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<Event>) e -> System.exit(0));
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
