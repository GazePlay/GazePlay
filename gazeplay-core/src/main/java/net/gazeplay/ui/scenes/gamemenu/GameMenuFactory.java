package net.gazeplay.ui.scenes.gamemenu;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.*;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.gaze.devicemanager.TobiiGazeDeviceManager;
import net.gazeplay.commons.ui.I18NText;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.commons.utils.stats.Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

@Slf4j
@Data
@Component
public class GameMenuFactory {

    private final static double THUMBNAIL_WIDTH_RATIO = 1;
    private final static double THUMBNAIL_HEIGHT_RATIO = 0.4;

    private final static long FAVORITE_SWITCH_FIXATION_DURATION_IN_MILLISECONDS = 1000;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GameMenuController gameMenuController;

    private ProgressIndicator progressIndicator;
    private Timeline timelineProgressBar;
    private Stats stats;

    public GameButtonPane createGameButton(
        @NonNull final GazePlay gazePlay,
        @NonNull final Region root,
        @NonNull final Configuration config,
        @NonNull final Translator translator,
        @NonNull final GameSpec gameSpec,
        @NonNull final GameButtonOrientation orientation,
        TobiiGazeDeviceManager gazeDeviceManager
    ) {
        Pane thumbpane = new Pane();


        final GameSummary gameSummary = gameSpec.getGameSummary();
        final String gameName = translator.translate(gameSummary.getNameCode());

        final Image heartIcon;
        heartIcon = new Image("data/common/images/heart_filled.png");
        ImageView favGamesImageView = new ImageView(heartIcon);

        if (config.getFavoriteGamesProperty().contains(gameSummary.getNameCode())) {
            favGamesImageView.setEffect(null);
        } else {
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setSaturation(-1);
            favGamesImageView.setEffect(colorAdjust);
        }


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

        gameCard.getStyleClass().add("button");

        double thumbnailBorderSize = 28d;

        BorderPane gameDescriptionPane = new BorderPane();


        if (gameSummary.getGameThumbnail() != null) {

            Image buttonGraphics = new Image(gameSummary.getGameThumbnail(), 200, 200, true, false);
            ImageView imageView = new ImageView(buttonGraphics);
            imageView.getStyleClass().add("gameChooserButtonThumbnail");
            imageView.setPreserveRatio(true);
            thumbpane.getChildren().add(imageView);
            thumbnailContainer.setCenter(thumbpane);

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
                gameCard.setTop(favIconContainer);
                break;
            case VERTICAL:
                gameCard.setLeft(favIconContainer);
                break;
        }
        gameCategoryContainer.setAlignment(Pos.BOTTOM_RIGHT);
        gameCard.setBottom(gameCategoryContainer);
        for (GameCategories.Category gameCategory : gameSummary.getCategories()) {
            if (gameCategory.getThumbnail() != null) {
                Image buttonGraphics = new Image(gameCategory.getThumbnail(), 100, 100, true, false);
                ImageView imageView = new ImageView(buttonGraphics);
                imageView.getStyleClass().add("gameChooserButtonGameTypeIndicator");
                imageView.setPreserveRatio(true);
                switch (orientation) {
                    case HORIZONTAL:
                        gameCard.heightProperty().addListener(
                            (observableValue, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue() / 4));
                        gameCategoryContainer.getChildren().add(imageView);
                        break;
                    case VERTICAL:
                        gameCard.widthProperty().addListener(
                            (observableValue, oldValue, newValue) -> imageView.setFitWidth(newValue.doubleValue() / 4));
                        gameCategoryContainer.getChildren().add(imageView);
                        break;
                }
            }
        }

        Circle recordAllowedLabelCircle = new Circle(12.5);
        if (ReplayingGameFromJson.replayIsAllowed(gameSummary.getNameCode())) {
            recordAllowedLabelCircle.setFill(Color.FORESTGREEN);
        } else {
            recordAllowedLabelCircle.setFill(Color.INDIANRED);
        }
        switch (orientation) {
            case HORIZONTAL:
                gameCard.heightProperty().addListener(
                    (observableValue, oldValue, newValue) -> recordAllowedLabelCircle.setRadius(newValue.doubleValue() / 15));
                gameCategoryContainer.getChildren().add(recordAllowedLabelCircle);
                break;
            case VERTICAL:
                gameCard.widthProperty().addListener(
                    (observableValue, oldValue, newValue) -> recordAllowedLabelCircle.setRadius(newValue.doubleValue() / 15));
                gameCategoryContainer.getChildren().add(recordAllowedLabelCircle);
                break;
        }


        final VBox gameTitleContainer = new VBox();
        gameTitleContainer.getChildren().add(gameTitleText);
        gameDescriptionPane.setTop(gameTitleContainer);

        if (gameDesc != null) {
            gameDesc.setFont(Font.font("Arial", 10));
            gameDesc.setTextAlignment(TextAlignment.JUSTIFY);
            gameDescriptionPane.setCenter(gameDesc);
        }

        switch (orientation) {
            case HORIZONTAL:
                if (gameDesc != null) {
                    gameDesc.wrappingWidthProperty().bind(gameDescriptionPane.prefWidthProperty());
                }
                gameDescriptionPane.setPadding(new Insets(0, 10, 0, 10));

                gameCard.setRight(gameDescriptionPane);
                gameCard.setLeft(thumbnailContainer);

                gameTitleContainer.setAlignment(Pos.TOP_RIGHT);
                gameTitleText.setTextAlignment(TextAlignment.RIGHT);

                gameCard.heightProperty().addListener((observableValue, oldValue, newValue) -> {
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
                if (gameDesc != null) {
                    gameDesc.wrappingWidthProperty().bind(gameDescriptionPane.prefWidthProperty().divide(4. / 3.));
                }
                gameDescriptionPane.setPadding(new Insets(10, 0, 10, 0));

                gameCard.setCenter(thumbnailContainer);
                gameCard.setLeft(favIconContainer);

                BorderPane bottomPane = new BorderPane();
                bottomPane.setRight(gameCategoryContainer);
                bottomPane.setCenter(gameDesc);

                gameDescriptionPane.setCenter(bottomPane);

                gameCard.setBottom(gameDescriptionPane);

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

        gazeDeviceManager.addEventFilter(gameCard);
        gameCard.addEventFilter(MOUSE_PRESSED, (MouseEvent e) -> {
            if (!favGamesImageView.isHover()) {
                gameMenuController.onGameSelection(gazePlay, root, gameSpec, gameName);
            }
            final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
            backgroundMusicManager.pause();
        });

        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setOpacity(0);
        gameCard.addEventFilter(GazeEvent.GAZE_ENTERED, (GazeEvent e) ->{
            progressIndicator.toFront();
            if (!thumbpane.getChildren().contains(progressIndicator)){
                thumbpane.getChildren().add(progressIndicator);
            }
            progressIndicator.setMinWidth(90);
            progressIndicator.setMinHeight(90);

            progressIndicator.setStyle(" -fx-progress-color: " + config.getProgressBarColor());
            progressIndicator.setOpacity(1);
            progressIndicator.setProgress(0);

            if (timelineProgressBar != null){
                timelineProgressBar.stop();
            }
            timelineProgressBar = new Timeline();
            timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(config.getFixationLength()),
                new KeyValue(progressIndicator.progressProperty(), 1)));

            timelineProgressBar.setOnFinished(actionEvent -> {
                if (!favGamesImageView.isHover()){
                    gameMenuController.onGameSelection(gazePlay, root, gameSpec, gameName);
                }
                final BackgroundMusicManager backgroundMusicManager = BackgroundMusicManager.getInstance();
                backgroundMusicManager.pause();
                stopTimerPI();
            });
            timelineProgressBar.play();
        });

        gameCard.addEventFilter(GazeEvent.GAZE_EXITED, (GazeEvent y) ->{
            stopTimerPI();
        });






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
                if (config.getFavoriteGamesProperty().contains(gameSummary.getNameCode())) {
                    config.getFavoriteGamesProperty().remove(gameSummary.getNameCode());
                    ColorAdjust colorAdjust = new ColorAdjust();
                    colorAdjust.setSaturation(-1);
                    favGamesImageView.setEffect(colorAdjust);
                } else {
                    config.getFavoriteGamesProperty().add(gameSummary.getNameCode());
                    favGamesImageView.setEffect(null);
                }
            }
        };
        favIconContainer.addEventFilter(MOUSE_CLICKED, favoriteGameSwitchEventHandler);

        return gameCard;
    }


    private void stopTimerPI(){
        if (timelineProgressBar != null){
            timelineProgressBar.stop();
            progressIndicator.setOpacity(0);
            progressIndicator.setProgress(0);

        }
    }

}
