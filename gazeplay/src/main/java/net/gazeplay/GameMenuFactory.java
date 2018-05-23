package net.gazeplay;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.ProgressButton;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.ProgressPane;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Collection;

@Slf4j
public class GameMenuFactory {

    private boolean useDebuggingBackgrounds = false;

    public Pane createGameButton(GazePlay gazePlay, Scene scene, Configuration config, Multilinguism multilinguism,
            Translator translator, GameSpec gameSpec, GameButtonOrientation orientation) {

        final GameSummary gameSummary = gameSpec.getGameSummary();
        final String gameName = multilinguism.getTrad(gameSummary.getNameCode(), config.getLanguage());

        final I18NText gameTitleText = new I18NText(translator, gameSummary.getNameCode());
        gameTitleText.getStyleClass().add("gameChooserButtonTitle");

        BorderPane thumbnailImageViewContainer = new BorderPane();
        thumbnailImageViewContainer.setPadding(new Insets(1, 1, 1, 1));
        thumbnailImageViewContainer.setOpaqueInsets(new Insets(1, 1, 1, 1));
        if (useDebuggingBackgrounds) {
            thumbnailImageViewContainer
                    .setBackground(new Background(new BackgroundFill(Color.DARKGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        }

        ProgressPane gameCard = new ProgressPane();
        switch (orientation) {
        case HORIZONTAL:
            gameCard.getStyleClass().add("gameChooserButton");
            gameCard.getStyleClass().add("gameChooserButtonHorizontal");
            break;
        case VERTICAL:
            gameCard.getStyleClass().add("gameChooserButton");
            gameCard.getStyleClass().add("gameChooserButtonVertical");
            break;
        }

        if (useDebuggingBackgrounds) {
            gameCard.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        }

        gameCard.getStyleClass().add("button");

        double thumbnailBorderSize = 28d;

        BorderPane gameDescriptionPane = new BorderPane();
        if (useDebuggingBackgrounds) {
            gameDescriptionPane
                    .setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        }

        if (gameSummary.getThumbnailLocation() != null) {
            Image buttonGraphics = new Image(gameSummary.getThumbnailLocation());
            ImageView imageView = new ImageView(buttonGraphics);
            imageView.getStyleClass().add("gameChooserButtonThumbnail");
            imageView.setPreserveRatio(true);
            StackPane.setAlignment(imageView, Pos.CENTER);
            thumbnailImageViewContainer.setCenter(imageView);

            double imageSizeRatio = buttonGraphics.getWidth() / buttonGraphics.getHeight();

            switch (orientation) {
            case HORIZONTAL:
                gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                    double preferedHeight = newValue.doubleValue() - thumbnailBorderSize;
                    imageView.setFitHeight(preferedHeight);
                    imageView.setFitWidth(preferedHeight * imageSizeRatio);
                });
                // gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                // imageView.setFitWidth(newValue.doubleValue() / 2);
                // });
                break;
            case VERTICAL:
                gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                    double preferedWidth = newValue.doubleValue() - thumbnailBorderSize;
                    imageView.setFitWidth(preferedWidth);
                    imageView.setFitHeight(preferedWidth / buttonGraphics.getWidth() * buttonGraphics.getHeight());
                });
                gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                    imageView.setFitHeight(newValue.doubleValue() / 2);
                });
                break;
            }

        }

        if (gameSummary.getGameTypeIndicatorImageLocation() != null) {
            Image buttonGraphics = new Image(gameSummary.getGameTypeIndicatorImageLocation());
            ImageView imageView = new ImageView(buttonGraphics);
            imageView.getStyleClass().add("gameChooserButtonGameTypeIndicator");
            imageView.setPreserveRatio(true);

            switch (orientation) {
            case HORIZONTAL:
                gameCard.heightProperty().addListener(
                        (observableValue, oldValue, newValue) -> imageView.setFitHeight(newValue.doubleValue() / 10));
                StackPane.setAlignment(imageView, Pos.BOTTOM_RIGHT);
                break;
            case VERTICAL:
                gameCard.widthProperty().addListener(
                        (observableValue, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue() / 10));
                StackPane.setAlignment(imageView, Pos.BOTTOM_RIGHT);
                break;
            }

            final VBox gameTypeIndicatorImageViewContainer = new VBox();
            gameTypeIndicatorImageViewContainer.setAlignment(Pos.BOTTOM_RIGHT);
            gameTypeIndicatorImageViewContainer.getChildren().add(imageView);

            gameDescriptionPane.setBottom(gameTypeIndicatorImageViewContainer);
        }

        final VBox gameTitleContainer = new VBox();
        gameTitleContainer.getChildren().add(gameTitleText);
        gameDescriptionPane.setTop(gameTitleContainer);

        switch (orientation) {
        case HORIZONTAL:
            gameDescriptionPane.setPadding(new Insets(0, 10, 0, 10));

            gameCard.button.setRight(gameDescriptionPane);
            gameCard.button.setLeft(thumbnailImageViewContainer);

            // thumbnailImageViewContainer.setAlignment(Pos.CENTER);
            StackPane.setAlignment(gameTitleText, Pos.TOP_RIGHT);
            gameTitleContainer.setAlignment(Pos.TOP_RIGHT);
            gameTitleText.setTextAlignment(TextAlignment.RIGHT);

            gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                // thumbnailImageViewContainer.setPrefWidth(newValue.doubleValue() / 2);
                thumbnailImageViewContainer.setPrefHeight(newValue.doubleValue() / 2 / 16 * 9);
                gameDescriptionPane.setPrefHeight(newValue.doubleValue() - thumbnailBorderSize);
            });
            gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                thumbnailImageViewContainer.setPrefWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                thumbnailImageViewContainer.setMaxWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                gameDescriptionPane.setPrefWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                gameDescriptionPane.setMaxWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                gameTitleText.setWrappingWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);

            });

            break;
        case VERTICAL:
            gameDescriptionPane.setPadding(new Insets(10, 0, 10, 0));

            gameCard.button.setBottom(gameDescriptionPane);
            gameCard.button.setTop(thumbnailImageViewContainer);

            // thumbnailImageViewContainer.setAlignment(Pos.CENTER);
            StackPane.setAlignment(gameTitleText, Pos.TOP_CENTER);
            gameTitleContainer.setAlignment(Pos.TOP_CENTER);
            gameTitleText.setTextAlignment(TextAlignment.CENTER);

            gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                thumbnailImageViewContainer.setPrefWidth(newValue.doubleValue() - thumbnailBorderSize);
                gameDescriptionPane.setPrefWidth(newValue.doubleValue() - thumbnailBorderSize);
                gameTitleText.setWrappingWidth(newValue.doubleValue() - thumbnailBorderSize);
            });
            gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                thumbnailImageViewContainer.setPrefHeight(newValue.doubleValue() / 2);
                gameDescriptionPane.setPrefHeight(newValue.doubleValue() / 2);
            });

            break;
        }

        EventHandler<Event> eventhandler = new EventHandler<Event>() {
            @Override
            public void handle(Event mouseEvent) {
                Collection<GameSpec.GameVariant> variants = gameSpec.getGameVariantGenerator().getVariants();

                if (variants.size() > 1) {
                    log.info("variants = {}", variants);
                    scene.getRoot().setEffect(new BoxBlur());
                    Stage dialog = createDialog(gazePlay, gazePlay.getPrimaryStage(), gameSpec);

                    String dialogTitle = gameName + " : "
                            + multilinguism.getTrad("Choose Game Variante", config.getLanguage());
                    dialog.setTitle(dialogTitle);
                    dialog.show();

                    dialog.toFront();
                    dialog.setAlwaysOnTop(true);

                } else {
                    if (variants.size() == 1) {
                        GameSpec.GameVariant onlyGameVariant = variants.iterator().next();
                        chooseGame(gazePlay, gameSpec, onlyGameVariant, config);
                    } else {
                        chooseGame(gazePlay, gameSpec, null, config);
                    }
                }
            }
        };
        gameCard.addEventHandler(MouseEvent.MOUSE_CLICKED, eventhandler);
        gameCard.assignIndicator(eventhandler);

        return gameCard;
    }

    private Stage createDialog(GazePlay gazePlay, Stage primaryStage, GameSpec gameSpec) {
        // initialize the confirmation dialog
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> primaryStage.getScene().getRoot().setEffect(null));

        FlowPane choicePane = new FlowPane();
        choicePane.setAlignment(Pos.CENTER);

        ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setMinHeight(primaryStage.getHeight() / 3);
        choicePanelScroller.setMinWidth(primaryStage.getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        final Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        for (GameSpec.GameVariant variant : gameSpec.getGameVariantGenerator().getVariants()) {
            ProgressButton button = new ProgressButton(variant.getLabel());
            button.button.getStyleClass().add("gameChooserButton");
            button.button.getStyleClass().add("gameVariation");
            button.button.getStyleClass().add("button");
            button.button.setMinHeight(primaryStage.getHeight() / 10);
            button.button.setMinWidth(primaryStage.getWidth() / 10);
            choicePane.getChildren().add(button);

            EventHandler<Event> event = new EventHandler<Event>() {
                @Override
                public void handle(Event mouseEvent) {
                    dialog.close();
                    chooseGame(gazePlay, gameSpec, variant, config);
                }
            };

            button.button.addEventHandler(MouseEvent.MOUSE_CLICKED, event);

            button.assignIndicator(event);
        }

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene);

        dialog.setScene(scene);
        // scene.getStylesheets().add(getClass().getResource("modal-dialog.css").toExternalForm());

        return dialog;
    }

    private void chooseGame(GazePlay gazePlay, GameSpec selectedGameSpec, GameSpec.GameVariant gameVariant,
            Configuration config) {
        GameContext gameContext = GameContext.newInstance(gazePlay);

        // SecondScreen secondScreen = SecondScreen.launch();

        gazePlay.onGameLaunch(gameContext);

        GameSpec.GameLauncher gameLauncher = selectedGameSpec.getGameLauncher();

        final Stats stats = gameLauncher.createNewStats(gameContext.getScene());

        if (config.isHeatMapDisabled()) {
            log.info("HeatMap is disabled, skipping instanciation of the HeatMap Data model");
        } else {
            gameContext.getGazeDeviceManager().addGazeMotionListener(stats);
        }

        // gameContext.getGazeDeviceManager().addGazeMotionListener(secondScreen);

        GameLifeCycle currentGame = gameLauncher.createNewGame(gameContext, gameVariant, stats);

        gameContext.createControlPanel(gazePlay, stats, currentGame);

        if (selectedGameSpec.getGameSummary().getBackgroundMusicUrl() != null) {

            final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
            if (musicManager.getPlaylist().isEmpty()) {
                musicManager.playRemoteSound(selectedGameSpec.getGameSummary().getBackgroundMusicUrl());
            }
        }

        stats.start();
        currentGame.launch();
    }

}
