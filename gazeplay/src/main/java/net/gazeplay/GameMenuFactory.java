package net.gazeplay;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.input.MouseEvent.*;

@Slf4j
@Data
public class GameMenuFactory {

    private final boolean useDebuggingBackgrounds = false;

    private final static double THUMBNAIL_WIDTH_RATIO = 1;
    private final static double THUMBNAIL_HEIGHT_RATIO = 0.4;

    private final static long FAVORITE_SWITCH_FIXATION_DURATION_IN_MILLISECONDS = 1000;

    public GameButtonPane createGameButton(
        @NonNull final GazePlay gazePlay,
        @NonNull final Region root,
        @NonNull final Configuration config,
        @NonNull final Multilinguism multilinguism,
        @NonNull final Translator translator,
        @NonNull final GameSpec gameSpec,
        @NonNull final GameButtonOrientation orientation,
        @NonNull final GazeDeviceManager gazeDeviceManager,
        final boolean isFavorite
    ) {

        final GameSummary gameSummary = gameSpec.getGameSummary();
        final String gameName = multilinguism.getTrad(gameSummary.getNameCode(), config.getLanguage());

        final Image heartIcon;
        if (isFavorite) {
            heartIcon = new Image("data/common/images/heart_filled.png");
        } else {
            heartIcon = new Image("data/common/images/heart_empty.png");
        }

        ImageView favGamesImageView = new ImageView(heartIcon);
        //ImagePattern favGamesImagePattern = new ImagePattern(heartIcon);

        // can't understand the goal of the following 3 lines
        // favGamesImageView.imageProperty().addListener((listener) -> {
        // isFavourite.setValue(favGamesImageView.getImage().equals(new Image("data/common/images/heart_filled.png")));
        // config.saveConfigIgnoringExceptions();
        // });

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

        GameButtonPane gameCard = new GameButtonPane(gameSpec);
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

                    break;
                case VERTICAL:
                    gameCard.widthProperty().addListener((observableValue, oldValue, newValue) -> {
                        double preferredWidth = newValue.doubleValue() * THUMBNAIL_WIDTH_RATIO;
                        imageView.setFitWidth(preferredWidth);
                    });
                    gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> imageView.setFitHeight(newValue.doubleValue() * THUMBNAIL_HEIGHT_RATIO));
                    break;
            }
        }

        final HBox gameCategoryContainer = new HBox();
        final VBox favIconContainer = new VBox(favGamesImageView);
        switch (orientation) {
            case HORIZONTAL:
                gameCategoryContainer.setAlignment(Pos.BOTTOM_RIGHT);
                gameCard.setBottom(gameCategoryContainer);
                gameCard.setTop(favIconContainer);
                break;
            case VERTICAL:
                gameCategoryContainer.setAlignment(Pos.TOP_RIGHT);
                gameCard.setTop(gameCategoryContainer);
                gameCard.setLeft(favIconContainer);
                break;
        }
        for (GameCategories.Category gameCategory : gameSummary.getCategories()) {
            if (gameCategory.getThumbnail() != null) {
                Image buttonGraphics = new Image(gameCategory.getThumbnail());
                ImageView imageView = new ImageView(buttonGraphics);
                imageView.getStyleClass().add("gameChooserButtonGameTypeIndicator");
                imageView.setPreserveRatio(true);
                switch (orientation) {
                    case HORIZONTAL:
                        gameCard.heightProperty().addListener(
                            (observableValue, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue() / 10));
                        gameCategoryContainer.getChildren().add(imageView);
                        break;
                    case VERTICAL:
                        gameCard.widthProperty().addListener(
                            (observableValue, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue() / 10));
                        gameCategoryContainer.getChildren().add(imageView);
                        break;
                }
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

        EventHandler event = (EventHandler<Event>) e -> {

            Collection<GameSpec.GameVariant> variants = gameSpec.getGameVariantGenerator().getVariants();

            if (variants.size() > 1) {
                root.setEffect(new BoxBlur());
                root.setDisable(true);
                GameVariantDialog dialog = new GameVariantDialog(gazePlay, this, gazePlay.getPrimaryStage(), gameSpec, root, gameSpec.getGameVariantGenerator().getVariantChooseText());
                dialog.setTitle(gameName);
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
        };
        gameCard.addEventHandler(MOUSE_PRESSED, event);

        @Data
        class EventState {
            private final long time;
            private final boolean wasFavorite;
        }

        EventHandler favoriteGameSwitchEventHandler = new EventHandler<MouseEvent>() {

            private final AtomicReference<EventState> enteredState = new AtomicReference<>();
            private final AtomicReference<EventState> exitedState = new AtomicReference<>();

            @Override
            public void handle(MouseEvent event) {
                if (event.getEventType() == MOUSE_ENTERED) {
                    boolean wasFavorite = config.getFavoriteGamesProperty().contains(gameSummary.getNameCode());
                    enteredState.set(new EventState(System.currentTimeMillis(), wasFavorite));
                    log.info("enteredState = {}", enteredState);
                    return;
                }
                if (event.getEventType() == MOUSE_EXITED) {
                    boolean wasFavorite = config.getFavoriteGamesProperty().contains(gameSummary.getNameCode());
                    exitedState.set(new EventState(System.currentTimeMillis(), wasFavorite));
                    log.info("exitedState = {}", exitedState);
                    //return;
                }
                //if (event.getEventType() != MOUSE_MOVED) {
                //    return;
                //}
                long fixationDuration = System.currentTimeMillis() - enteredState.get().time;
                if (fixationDuration < FAVORITE_SWITCH_FIXATION_DURATION_IN_MILLISECONDS) {
                    // too early
                    return;
                }
                boolean isFavorite = !enteredState.get().wasFavorite;
                if (isFavorite) {
                    config.getFavoriteGamesProperty().add(gameSummary.getNameCode());
                    favGamesImageView.setImage(new Image("data/common/images/heart_filled.png"));
                } else {
                    config.getFavoriteGamesProperty().remove(gameSummary.getNameCode());
                    favGamesImageView.setImage(new Image("data/common/images/heart_empty.png"));
                }
                config.saveConfigIgnoringExceptions();
            }
        };
        favIconContainer.addEventFilter(MOUSE_ENTERED, favoriteGameSwitchEventHandler);
        favIconContainer.addEventFilter(MOUSE_MOVED, favoriteGameSwitchEventHandler);
        favIconContainer.addEventFilter(MOUSE_EXITED, favoriteGameSwitchEventHandler);

        // pausedEvents.add(gameCard);
        return gameCard;
    }

    public void chooseGame(GazePlay gazePlay, GameSpec selectedGameSpec, GameSpec.GameVariant gameVariant,
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
