package net.gazeplay;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.ConfigurationButton;
import net.gazeplay.commons.utils.ControlPanelConfigurator;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Collection;
import java.util.List;

@Data
@Slf4j
public class HomeMenuScreen extends GraphicalContext<BorderPane> {

    /**
     * The horizontal growing factor for buttons to adapt screen size.
     */
    private final static double GAME_CHOOSER_BUTTON_HGROW_FACTOR = 3.6;

    /**
     * The vertical growing factor for buttons to adapt screen size.
     */
    private final static double GAME_CHOOSER_BUTTON_VGROW_FACTOR = 8.5;

    public static HomeMenuScreen newInstance(final GazePlay gazePlay, final Configuration config) {

        GamesLocator gamesLocator = new DefaultGamesLocator();
        List<GameSpec> games = gamesLocator.listGames();

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, gazePlay.getPrimaryStage().getWidth(), gazePlay.getPrimaryStage().getHeight(),
                Color.BLACK);

        return new HomeMenuScreen(gazePlay, games, scene, root, config);
    }

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

        Button toggleFullScreenButton = createToggleFullScreenButtonInGameScreen(gazePlay);

        HBox rightControlPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(rightControlPane);
        rightControlPane.setAlignment(Pos.CENTER);
        rightControlPane.getChildren().add(toggleFullScreenButton);

        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(leftControlPane);
        bottomPane.setRight(rightControlPane);

        MenuBar menuBar = Utils.buildLicence();

        Node logo = createLogo();
        StackPane topLogoPane = new StackPane();
        topLogoPane.getChildren().add(logo);

        HBox topRightPane = new HBox();
        ControlPanelConfigurator.getSingleton().customizeControlePaneLayout(topRightPane);
        topRightPane.setAlignment(Pos.TOP_CENTER);
        topRightPane.getChildren().add(exitButton);

        Pane gamePickerChoicePane = createGamePickerChoicePane(games, config);

        VBox centerCenterPane = new VBox();
        centerCenterPane.setSpacing(40);
        centerCenterPane.setAlignment(Pos.TOP_CENTER);
        centerCenterPane.getChildren().add(gamePickerChoicePane);

        VBox leftPanel = new VBox();
        leftPanel.getChildren().add(menuBar);

        BorderPane centerPanel = new BorderPane();
        centerPanel.setCenter(centerCenterPane);
        centerPanel.setLeft(leftPanel);

        BorderPane topPane = new BorderPane();
        topPane.setTop(menuBar);
        topPane.setCenter(topLogoPane);
        topPane.setRight(topRightPane);

        root.setTop(topPane);
        root.setBottom(bottomPane);
        root.setCenter(centerPanel);

        root.setStyle("-fx-background-color: rgba(0, 0, 0, 1); " + "-fx-background-radius: 8px; "
                + "-fx-border-radius: 8px; " + "-fx-border-width: 5px; " + "-fx-border-color: rgba(60, 63, 65, 0.7); "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.8), 10, 0, 0, 0);");
    }

    @Override
    public ObservableList<Node> getChildren() {
        return root.getChildren();
    }

    @Override
    public void setUpOnStage(Stage stage) {
        super.setUpOnStage(stage);
    }

    public void onLanguageChanged() {

    }

    private Stage createDialog(Stage primaryStage, GameSpec gameSpec) {
        // initialize the confirmation dialog
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> primaryStage.getScene().getRoot().setEffect(null));

        FlowPane choicePane = new FlowPane();
        choicePane.setAlignment(Pos.CENTER);

        GazePlay gazePlay = GazePlay.getInstance();

        for (GameSpec.GameVariant variant : gameSpec.getGameVariantGenerator().getVariants()) {
            Button button = new Button(variant.getLabel());
            button.getStyleClass().add("gameChooserButton");
            button.getStyleClass().add("variant");
            button.getStyleClass().add("button");
            choicePane.getChildren().add(button);

            button.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    dialog.close();
                    chooseGame(gameSpec, variant);
                }
            });
        }

        Scene scene = new Scene(choicePane, Color.TRANSPARENT);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();
        CssUtil.setPreferredStylesheets(config, scene);

        dialog.setScene(scene);
        // scene.getStylesheets().add(getClass().getResource("modal-dialog.css").toExternalForm());

        return dialog;
    }

    private Pane createGamePickerChoicePane(List<GameSpec> games, Configuration config) {

        FlowPane choicePanel = new FlowPane();
        choicePanel.setAlignment(Pos.CENTER);
        choicePanel.setHgap(10);
        choicePanel.setVgap(10);

        Multilinguism multilinguism = Multilinguism.getSingleton();

        for (GameSpec gameSpec : games) {
            final GameSummary gameSummary = gameSpec.getGameSummary();

            String gameName = multilinguism.getTrad(gameSummary.getNameCode(), config.getLanguage());

            StackPane gameCard = new StackPane();
            gameCard.getStyleClass().add("gameChooserButton");
            gameCard.getStyleClass().add("button");

            Label text = new Label(gameName);

            text.getStyleClass().add("gameChooserButtonTitle");

            if (gameSummary.getGameTypeIndicatorImageLocation() != null) {
                Image buttonGraphics = new Image(gameSummary.getGameTypeIndicatorImageLocation());
                ImageView imageView = new ImageView(buttonGraphics);
                imageView.getStyleClass().add("gameChooserButtonGameTypeIndicator");
                imageView.setFitWidth(32);
                imageView.setFitHeight(32);
                StackPane.setAlignment(imageView, Pos.TOP_RIGHT);
                gameCard.getChildren().add(imageView);
            }

            if (gameSummary.getThumbnailLocation() != null) {
                Image buttonGraphics = new Image(gameSummary.getThumbnailLocation());
                ImageView imageView = new ImageView(buttonGraphics);
                imageView.getStyleClass().add("gameChooserButtonThumbnail");
                imageView.setPreserveRatio(true);

                int thumbnailBorderSize = 28;
                gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> imageView
                        .setFitWidth(newValue.doubleValue() - thumbnailBorderSize));
                gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> imageView
                        .setFitHeight(newValue.doubleValue() - thumbnailBorderSize));
                StackPane.setAlignment(imageView, Pos.CENTER_LEFT);
                gameCard.getChildren().add(imageView);
            }

            StackPane.setAlignment(text, Pos.BOTTOM_RIGHT);
            text.setPadding(new Insets(20, 20, 20, 20));
            gameCard.getChildren().add(text);

            Stage primaryStage = GazePlay.getInstance().getPrimaryStage();

            // Adapt buttons size to screen size
            primaryStage.heightProperty().addListener((o) -> {
                if (choicePanel.getHeight() > 0) {
                    gameCard.setPrefHeight(primaryStage.getHeight() / GAME_CHOOSER_BUTTON_VGROW_FACTOR);
                }
            });

            primaryStage.widthProperty().addListener((o) -> {
                if (choicePanel.getHeight() > 0) {
                    gameCard.setPrefWidth(primaryStage.getWidth() / GAME_CHOOSER_BUTTON_HGROW_FACTOR);
                }
            });

            gameCard.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    Collection<GameSpec.GameVariant> variants = gameSpec.getGameVariantGenerator().getVariants();

                    if (variants.size() > 1) {
                        log.info("variants = {}", variants);
                        getScene().getRoot().setEffect(new BoxBlur());
                        Stage dialog = createDialog(getGazePlay().getPrimaryStage(), gameSpec);

                        String dialogTitle = gameName + " : "
                                + multilinguism.getTrad("Choose Game Variante", config.getLanguage());
                        dialog.setTitle(dialogTitle);
                        dialog.show();

                        dialog.toFront();
                        dialog.setAlwaysOnTop(true);

                    } else {
                        if (variants.size() == 1) {
                            GameSpec.GameVariant onlyGameVariant = variants.iterator().next();
                            chooseGame(gameSpec, onlyGameVariant);
                        } else {
                            chooseGame(gameSpec, null);
                        }
                    }
                }
            });

            choicePanel.getChildren().add(gameCard);
        }

        return choicePanel;
    }

    private void chooseGame(GameSpec selectedGameSpec, GameSpec.GameVariant gameVariant) {
        GazePlay gazePlay = getGazePlay();

        GameContext gameContext = GameContext.newInstance(gazePlay);

        // SecondScreen secondScreen = SecondScreen.launch();

        gazePlay.onGameLaunch(gameContext);

        GameSpec.GameLauncher gameLauncher = selectedGameSpec.getGameLauncher();

        final Stats stats = gameLauncher.createNewStats(gameContext.getScene());

        gameContext.getGazeDeviceManager().addGazeMotionListener(stats);
        // gameContext.getGazeDeviceManager().addGazeMotionListener(secondScreen);

        gameContext.createControlPanel(gazePlay, stats);

        GameLifeCycle currentGame = gameLauncher.createNewGame(gameContext, gameVariant, stats);
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
