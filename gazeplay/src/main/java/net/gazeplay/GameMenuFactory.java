package net.gazeplay;

import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CssUtil;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.labyrinth.Mouse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Data
public class GameMenuFactory {

    private final boolean useDebuggingBackgrounds = false;

    public GazeDeviceManager gazeDeviceManager;

    private final static double THUMBNAIL_WIDTH_RATIO = 1;
    private final static double THUMBNAIL_HEIGHT_RATIO = 0.4;

    @Getter
    private List<GameButtonPane> pausedEvents = new LinkedList<GameButtonPane>();

    public GameButtonPane createGameButton(GazePlay gazePlay, final Region root, Configuration config,
            Multilinguism multilinguism, Translator translator, GameSpec gameSpec, GameButtonOrientation orientation,
            GazeDeviceManager gazeDeviceManager, BooleanProperty isFavourite) {
        this.gazeDeviceManager = gazeDeviceManager;

        final GameSummary gameSummary = gameSpec.getGameSummary();
        final String gameName = multilinguism.getTrad(gameSummary.getNameCode(), config.getLanguage());

        Image heartIcon;
        if (isFavourite.getValue())
            heartIcon = new Image("data/common/images/heart_filled.png");
        else
            heartIcon = new Image("data/common/images/heart_empty.png");

        ImageView favGamesIcon = new ImageView(heartIcon);
        favGamesIcon.imageProperty().addListener((l) -> {
            isFavourite.setValue(favGamesIcon.getImage().equals(new Image("data/common/images/heart_filled.png")));
            config.saveConfigIgnoringExceptions();
        });

        final I18NText gameTitleText = new I18NText(translator, gameSummary.getNameCode());
        gameTitleText.getStyleClass().add("gameChooserButtonTitle");

        I18NText gameDesc = null;
        if (gameSummary.getDescription() != null) {
            gameDesc = new I18NText(translator, gameSummary.getDescription());
            gameDesc.getStyleClass().add("gameChooserButtonDesc");
        }

        BorderPane thumbnailContainer = new BorderPane();
        thumbnailContainer.setPadding(new Insets(1, 1, 1, 1));
        thumbnailContainer.setOpaqueInsets(new Insets(1, 1, 1, 1));
        if (useDebuggingBackgrounds) {
            thumbnailContainer
                    .setBackground(new Background(new BackgroundFill(Color.DARKGREY, CornerRadii.EMPTY, Insets.EMPTY)));
        }

        GameButtonPane gameCard = new GameButtonPane();
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

        if (gameSummary.getGameThumbnail() != null) {
            Image buttonGraphics = new Image(gameSummary.getGameThumbnail());
            ImageView imageView = new ImageView(buttonGraphics);
            imageView.getStyleClass().add("gameChooserButtonThumbnail");
            imageView.setPreserveRatio(true);
            thumbnailContainer.setCenter(imageView);

            double imageSizeRatio = buttonGraphics.getWidth() / buttonGraphics.getHeight();

            switch (orientation) {
            case HORIZONTAL:
                gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                    double preferredHeight = newValue.doubleValue() - thumbnailBorderSize;
                    imageView.setFitHeight(preferredHeight - 10);
                    imageView.setFitWidth(preferredHeight * imageSizeRatio);
                });
                // gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                // imageView.setFitWidth(newValue.doubleValue() / 2);
                // });
                break;
            case VERTICAL:
                gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                    double preferredWidth = newValue.doubleValue() * THUMBNAIL_WIDTH_RATIO;
                    imageView.setFitWidth(preferredWidth);
                });
                gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                    imageView.setFitHeight(newValue.doubleValue() * THUMBNAIL_HEIGHT_RATIO);
                });
                break;
            }
        }

        if (gameSummary.getCategory().getThumbnail() != null) {
            Image buttonGraphics = new Image(gameSummary.getCategory().getThumbnail());
            ImageView imageView = new ImageView(buttonGraphics);
            imageView.getStyleClass().add("gameChooserButtonGameTypeIndicator");
            imageView.setPreserveRatio(true);

            final VBox gameCategoryContainer = new VBox();
            switch (orientation) {
            case HORIZONTAL:
                gameCard.heightProperty().addListener(
                        (observableValue, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue() / 10));

                gameCategoryContainer.setAlignment(Pos.BOTTOM_RIGHT);
                gameCategoryContainer.getChildren().add(imageView);
                gameCard.setBottom(gameCategoryContainer);
                VBox favIconContainer = new VBox(favGamesIcon);
                gameCard.setTop(favIconContainer);

                break;
            case VERTICAL:
                gameCard.widthProperty().addListener(
                        (observableValue, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue() / 10));

                gameCategoryContainer.setAlignment(Pos.TOP_RIGHT);
                gameCategoryContainer.getChildren().add(imageView);
                gameCard.setTop(gameCategoryContainer);
                break;
            }
        }

        final VBox gameTitleContainer = new VBox();
        gameTitleContainer.getChildren().add(gameTitleText);
        gameDescriptionPane.setTop(gameTitleContainer);

        if (gameDesc != null) {
            gameDesc.wrappingWidthProperty().bind(gameDescriptionPane.prefWidthProperty());
            gameDesc.setFont(Font.font("Arial", 10));
            gameDesc.setTextAlignment(TextAlignment.JUSTIFY);
            gameDescriptionPane.setCenter(gameDesc);
        }

        switch (orientation) {
        case HORIZONTAL:
            gameDescriptionPane.setPadding(new Insets(0, 10, 0, 10));

            gameCard.setRight(gameDescriptionPane);
            gameCard.setLeft(thumbnailContainer);

            gameTitleContainer.setAlignment(Pos.TOP_RIGHT);
            gameTitleText.setTextAlignment(TextAlignment.RIGHT);

            gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                // thumbnailContainer.setPrefWidth(newValue.doubleValue() / 2);
                thumbnailContainer.setPrefHeight(newValue.doubleValue() / 2 / 16 * 9);
                gameDescriptionPane.setPrefHeight(newValue.doubleValue() - thumbnailBorderSize);
            });
            gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                thumbnailContainer.setPrefWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                thumbnailContainer.setMaxWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                gameDescriptionPane.setPrefWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                gameDescriptionPane.setMaxWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);
                gameTitleText.setWrappingWidth(newValue.doubleValue() / 2 - thumbnailBorderSize);

            });

            break;
        case VERTICAL:
            gameDescriptionPane.setPadding(new Insets(10, 0, 10, 0));

            gameCard.setBottom(gameDescriptionPane);
            gameCard.setCenter(thumbnailContainer);

            VBox favIconContainer = new VBox(favGamesIcon);
            gameCard.setLeft(favIconContainer);

            gameTitleContainer.setAlignment(Pos.TOP_CENTER);
            gameTitleText.setTextAlignment(TextAlignment.CENTER);

            gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                thumbnailContainer.setPrefWidth(newValue.doubleValue() - thumbnailBorderSize);
                gameDescriptionPane.setPrefWidth(newValue.doubleValue() - thumbnailBorderSize);
                gameTitleText.setWrappingWidth(newValue.doubleValue() - thumbnailBorderSize);
            });
            gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
                thumbnailContainer.setPrefHeight(newValue.doubleValue() / 2);
                gameDescriptionPane.setPrefHeight(newValue.doubleValue() / 2);
            });

            break;
        }

        EventHandler event = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                Collection<GameSpec.GameVariant> variants = gameSpec.getGameVariantGenerator().getVariants();

                if (variants.size() > 1) {
                    log.debug("variants = {}", variants);
                    root.setEffect(new BoxBlur());
                    root.setDisable(true);
                    Stage dialog = createDialog(gazePlay, gazePlay.getPrimaryStage(), gameSpec, root);

                    String dialogTitle = gameName + " : "
                            + multilinguism.getTrad("Choose Game Variant", config.getLanguage());
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
        EventHandler favGameHandler_enter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                switch (isFavourite.getValue().toString()){
                    case "true":
                        favGamesIcon.setImage(new Image("data/common/images/heart_empty.png"));
                        isFavourite.setValue(false);
                        config.saveConfigIgnoringExceptions();

                        break;
                    case "false":
                        favGamesIcon.setImage(new Image("data/common/images/heart_filled.png"));
                        isFavourite.setValue(true);
                        config.saveConfigIgnoringExceptions();

                        break;
                }
            }
        };

        gameCard.addEventHandler(MouseEvent.MOUSE_CLICKED, event);
        favGamesIcon.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, favGameHandler_enter);
        pausedEvents.add(gameCard);
        return gameCard;
    }

    private Stage createDialog(GazePlay gazePlay, Stage primaryStage, GameSpec gameSpec, Region root) {
        // initialize the confirmation dialog
        final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setOnCloseRequest(windowEvent -> {
            primaryStage.getScene().getRoot().setEffect(null);
            root.setDisable(false);
        });

        FlowPane choicePane = new FlowPane();
        choicePane.setAlignment(Pos.CENTER);

        ScrollPane choicePanelScroller = new ScrollPane(choicePane);
        choicePanelScroller.setMinHeight(primaryStage.getHeight() / 3);
        choicePanelScroller.setMinWidth(primaryStage.getWidth() / 3);
        choicePanelScroller.setFitToWidth(true);
        choicePanelScroller.setFitToHeight(true);

        final Configuration config = Configuration.getInstance();

        for (GameSpec.GameVariant variant : gameSpec.getGameVariantGenerator().getVariants()) {
            Button button;

            if (variant instanceof GameSpec.DimensionGameVariant)
                button = new Button(variant.getLabel());

            else if (variant instanceof GameSpec.CupsGameVariant)
                button = new Button(((GameSpec.CupsGameVariant) variant).getNoCups()
                        + new I18NText(gazePlay.getTranslator(), variant.getLabel()).getText());
            else if (variant instanceof GameSpec.TargetsGameVariant)
                button = new Button(((GameSpec.TargetsGameVariant) variant).getNoTargets()
                        + new I18NText(gazePlay.getTranslator(), variant.getLabel()).getText());
            else
                button = new Button(new I18NText(gazePlay.getTranslator(), variant.getLabel()).getText());

            button.getStyleClass().add("gameChooserButton");
            button.getStyleClass().add("gameVariation");
            button.getStyleClass().add("button");
            button.setMinHeight(primaryStage.getHeight() / 10);
            button.setMinWidth(primaryStage.getWidth() / 10);
            choicePane.getChildren().add(button);

            EventHandler<Event> event = new EventHandler<Event>() {
                @Override
                public void handle(Event mouseEvent) {
                    dialog.close();
                    root.setDisable(false);
                    chooseGame(gazePlay, gameSpec, variant, config);
                }
            };
            button.addEventHandler(MouseEvent.MOUSE_CLICKED, event);

        } // end for

        Scene scene = new Scene(choicePanelScroller, Color.TRANSPARENT);

        CssUtil.setPreferredStylesheets(config, scene);

        dialog.setScene(scene);
        // scene.getStylesheets().add(getClass().getResource("modal-dialog.css").toExternalForm());

        return dialog;
    }

    private void chooseGame(GazePlay gazePlay, GameSpec selectedGameSpec, GameSpec.GameVariant gameVariant,
            Configuration config) {
        GameContext gameContext = GameContext.newInstance(gazePlay);

        gazePlay.onGameLaunch(gameContext);

        GameSpec.GameLauncher gameLauncher = selectedGameSpec.getGameLauncher();

        final Scene scene = gazePlay.getPrimaryScene();
        final Stats stats = gameLauncher.createNewStats(scene);

        // if (config.isHeatMapDisabled()) {
        // log.info("HeatMap is disabled, skipping instantiation of the HeatMap Data model");
        // } else {
        // // gameContext.getGazeDeviceManager().addGazeMotionListener(stats);
        // }

        // gameContext.getGazeDeviceManager().addGazeMotionListener(secondScreen);

        GameLifeCycle currentGame = gameLauncher.createNewGame(gameContext, gameVariant, stats);

        gameContext.createControlPanel(gazePlay, stats, currentGame);

        gameContext.createQuitShortcut(gazePlay, stats, currentGame);

        gameContext.speedAdjust(gazePlay);

        if (selectedGameSpec.getGameSummary().getBackgroundMusicUrl() != null) {

            final BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
            log.info("is default music set : {}", musicManager.getIsCustomMusicSet().getValue());
            if (!musicManager.getIsCustomMusicSet().getValue() || musicManager.getPlaylist().isEmpty()) {
                musicManager.emptyPlaylist();
                musicManager.playMusicAlone(selectedGameSpec.getGameSummary().getBackgroundMusicUrl());
                gameContext.updateMusicControler();
            }
        }

        stats.start();
        currentGame.launch();
    }

}
