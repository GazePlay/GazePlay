package net.gazeplay.games.magiccards;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Created by schwab on 17/09/2016.
 */
@Slf4j
public class Card extends Parent {

    private static final float zoom_factor = 1.1f;

    private final double fixationlength;

    private final Rectangle card;

    @Getter
    private final boolean winner;

    private final Image image;

    private final IGameContext gameContext;

    private final double initWidth;
    private final double initHeight;

    private final double initX;
    private final double initY;

    private final MagicCards gameInstance;

    /**
     * true if the card has been turned
     */
    private boolean turned = false;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;
    private final Stats stats;

    private final EventHandler<Event> enterEvent;

    /**
     * Use a comme Timeline object so we can stop the current animation to prevent overlapses.
     */
    private Timeline currentTimeline;

    public Card(final double positionX, final double positionY, final double width, final double height, final Image image, final boolean winner,
                final IGameContext gameContext, final Stats stats, final MagicCards gameInstance, final int fixationlength) {

        this.card = new Rectangle(positionX, positionY, width, height);
        this.card.setFill(new ImagePattern(new Image("data/magiccards/images/red-card-game.png"), 0, 0, 1, 1, true));

        this.image = image;
        this.winner = winner; // true if it is the good card

        this.gameContext = gameContext;

        this.stats = stats;

        this.fixationlength = fixationlength;

        this.gameInstance = gameInstance;

        this.initWidth = width;
        this.initHeight = height;

        this.initX = positionX;
        this.initY = positionY;

        this.getChildren().add(card);

        this.progressIndicator = createProgressIndicator(width, height);
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(card);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        // Prevent null pointer exception
        currentTimeline = new Timeline();
    }

    private ProgressIndicator createProgressIndicator(final double width, final double height) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX() + width * 0.05);
        indicator.setTranslateY(card.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onCorrectCardSelected() {

        final javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        stats.incNbGoals();

        final int final_zoom = 2;

        progressIndicator.setOpacity(0);

        gameInstance.removeAllIncorrectCards();

        currentTimeline.stop();
        currentTimeline = new Timeline();

        currentTimeline.getKeyFrames().add(
            new KeyFrame(new Duration(1000), new KeyValue(card.widthProperty(), card.getWidth() * final_zoom)));
        currentTimeline.getKeyFrames().add(
            new KeyFrame(new Duration(1000), new KeyValue(card.heightProperty(), card.getHeight() * final_zoom)));
        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
            new KeyValue(card.xProperty(), (dimension2D.getWidth() - card.getWidth() * final_zoom) / 2)));
        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
            new KeyValue(card.yProperty(), (dimension2D.getHeight() - card.getHeight() * final_zoom) / 2)));

        currentTimeline.onFinishedProperty().set(actionEvent -> gameContext.playWinTransition(500, actionEvent1 -> {
            gameInstance.dispose();

            gameContext.clear();

            gameInstance.launch();

            stats.notifyNewRoundReady();

            gameContext.onGameStarted();
        }));

        currentTimeline.play();
    }

    private void onWrongCardSelected() {

        currentTimeline.stop();
        currentTimeline = new Timeline();

        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(card.opacityProperty(), 0)));

        currentTimeline.play();

        progressIndicator.setOpacity(0);
    }

    private EventHandler<Event> buildEvent() {

        return e -> {

            if (turned) {
                return;
            }

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                progressIndicator.setOpacity(1);
                progressIndicator.setProgress(0);

                currentTimeline.stop();
                currentTimeline = new Timeline();

                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.xProperty(), card.getX() - (initWidth * zoom_factor - initWidth) / 2)));
                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.yProperty(), card.getY() - (initHeight * zoom_factor - initHeight) / 2)));
                currentTimeline.getKeyFrames().add(
                    new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth * zoom_factor)));
                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.heightProperty(), initHeight * zoom_factor)));

                timelineProgressBar = new Timeline();

                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                    new KeyValue(progressIndicator.progressProperty(), 1)));

                currentTimeline.play();

                timelineProgressBar.setOnFinished(actionEvent -> {

                    turned = true;

                    card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));

                    card.removeEventFilter(MouseEvent.ANY, enterEvent);
                    card.removeEventFilter(GazeEvent.ANY, enterEvent);

                    if (winner) {
                        onCorrectCardSelected();
                    } else {// bad card
                        onWrongCardSelected();
                    }
                });
                timelineProgressBar.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                currentTimeline.stop();
                currentTimeline = new Timeline();

                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.xProperty(), card.getX() + (initWidth * zoom_factor - initWidth) / 2)));
                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.yProperty(), card.getY() + (initHeight * zoom_factor - initHeight) / 2)));
                currentTimeline.getKeyFrames()
                    .add(new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth)));
                currentTimeline.getKeyFrames()
                    .add(new KeyFrame(new Duration(1), new KeyValue(card.heightProperty(), initHeight)));

                // Be sure that the card is properly positionned at the end
                currentTimeline.setOnFinished((event) -> {
                    card.setX(initX);
                    card.setY(initY);
                });

                currentTimeline.play();

                timelineProgressBar.stop();

                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }

}
