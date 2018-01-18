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
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
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

    private GameLifeCycle currentGame;

    public HomeMenuScreen(GazePlay gazePlay, List<GameSpec> games, Scene scene, BorderPane root, Configuration config) {
        super(gazePlay, root, scene);
        this.games = games;

        Rectangle exitButton = createExitButton();

        ConfigurationContext configurationContext = ConfigurationContext.newInstance(gazePlay);
        ConfigurationButton configurationButton = ConfigurationButton.createConfigurationButton(configurationContext);

        HBox leftControlPane = new HBox();
        leftControlPane.setAlignment(Pos.CENTER);
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(leftControlPane);
        leftControlPane.getChildren().add(configurationButton);

        HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER);
        rightControlPane.getChildren().add(exitButton);

        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(leftControlPane);
        bottomPane.setRight(rightControlPane);

        MenuBar menuBar = Utils.buildLicence();

        Node logo = createLogo();
        StackPane topLogoPane = new StackPane();
        topLogoPane.getChildren().add(logo);

        cbxGames = createChoiceBox(games, config);
        cbxGames.getSelectionModel().clearSelection();

        StackPane centerCenterPane = new StackPane();
        centerCenterPane.getChildren().add(cbxGames);

        VBox leftPanel = new VBox();
        leftPanel.getChildren().add(menuBar);

        BorderPane centerPanel = new BorderPane();
        centerPanel.setCenter(centerCenterPane);
        centerPanel.setLeft(leftPanel);

        BorderPane topPane = new BorderPane();
        topPane.setTop(menuBar);
        topPane.setCenter(topLogoPane);

        root.setTop(topPane);
        root.setBottom(bottomPane);
        root.setCenter(centerPanel);

        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 1); -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 5px; -fx-border-color: rgba(60, 63, 65, 0.7); -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }

    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    @Override
    public void setUpOnStage(Stage stage) {
        cbxGames.getSelectionModel().clearSelection();

        super.setUpOnStage(stage);
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
    private ChoiceBox<String> createChoiceBox(List<GameSpec> games, Configuration config) {
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

        GameSpec.GameLauncher gameLauncher = selectedGameSpec.getGameLauncher();

        final Stats stats = gameLauncher.createNewStats(gameContext.getScene());

        gameContext.createToggleFullScreenButtonInGameScreen(gazePlay);
        gameContext.createHomeButtonInGameScreen(gazePlay, stats);

        GameLifeCycle currentGame = gameLauncher.createNewGame(gameContext, stats);
        currentGame.launch();
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
