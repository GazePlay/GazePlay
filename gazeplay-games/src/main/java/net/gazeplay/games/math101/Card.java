package net.gazeplay.games.math101;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Card extends Parent {

    private static final float zoomFactor = 1.05f;

    private final double fixationlength;

    private final Rectangle card;

    @Getter
    private final boolean winner;

    private final Image image; // REPLACE IMAGE WITH TEXT
    private final Text text;

    final StackPane stack;

    private final IGameContext gameContext;

    private final double initWidth;
    private final double initHeight;

    private final double initX;
    private final double initY;

    private final Math101 gameInstance;

    /**
     * true if the card has been turned
     */
    private boolean turned = false;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;
    final Stats stats;
    private final boolean inReplayMode;

    final EventHandler<Event> enterEvent;

    /**
     * Use a comme Timeline object so we can stop the current animation to prevent overlaps.
     */

    private Timeline currentTimeline;

    public Card(final double positionX, final double positionY, final double width, final double height, final Image image,
                final boolean winner, final int value, final IGameContext gameContext, final Stats stats,
                final Math101 gameInstance, final int fixationlength, final boolean inReplayMode) {
        this.card = new Rectangle(positionX, positionY, width, height);
        this.card.setFill(new ImagePattern(new Image("data/magiccards/images/red-card-game.png"), 0, 0, 1, 1, true));

        this.card.setFill(Color.BEIGE);
        this.card.setFill(new ImagePattern(new Image("data/math101/images/note2.png"), 0, 0, 1, 1, true));

        // Setting the height and width of the arc
        // this.card.setArcWidth(30.0);
        // this.card.setArcHeight(20.0);

        final DropShadow e = new DropShadow();
        e.setColor(Color.GRAY);
        e.setWidth(10);
        e.setHeight(10);
        e.setOffsetX(5);
        e.setOffsetY(5);
        e.setRadius(3);
        this.card.setEffect(e);

        this.image = image;
        this.winner = winner; // true if it is the good card

        this.text = new Text("" + value);
        this.text.setFont(new Font("Tsukushi A Round Gothic Bold", 200));

        this.stack = new StackPane();
        this.stack.getChildren().addAll(card, text);

        this.gameContext = gameContext;
        this.stats = stats;
        this.fixationlength = fixationlength;
        this.gameInstance = gameInstance;
        this.inReplayMode = inReplayMode;

        this.initWidth = width;
        this.initHeight = height;

        this.initX = positionX;
        this.initY = positionY;

        stack.setLayoutX(positionX);
        stack.setLayoutY(positionY);

        this.getChildren().add(stack);

        this.progressIndicator = createProgressIndicator(width, height);
        verifyProgressBarValue();
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        gameContext.getGazeDeviceManager().addEventFilter(card);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);

        // Prevent null pointer exception
        currentTimeline = new Timeline();
    }

    private ProgressIndicator createProgressIndicator(final double width, final double height) {
        double finalWidth = width * 0.9 * gameContext.getConfiguration().getProgressBarSize() / 100;
        double finalHeight = height * 0.9 * gameContext.getConfiguration().getProgressBarSize() / 100;
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX() + 0.05 * width * gameContext.getConfiguration().getProgressBarSize() / 100);
        indicator.setTranslateY(card.getY() + 0.2 * height * gameContext.getConfiguration().getProgressBarSize() / 100);
        indicator.setMinWidth(finalWidth);
        indicator.setMinHeight(finalHeight);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onCorrectCardSelected() {
        if (!inReplayMode) {
            stats.incrementNumberOfGoalsReached();
        }

        final double finalZoom = 1.15;

        progressIndicator.setOpacity(0);
        text.setOpacity(0);

        gameInstance.removeAllIncorrectCards();

        currentTimeline.stop();
        currentTimeline = new Timeline();

        currentTimeline.getKeyFrames().add(
            new KeyFrame(new Duration(1000), new KeyValue(card.widthProperty(), card.getWidth() * finalZoom)));
        currentTimeline.getKeyFrames().add(
            new KeyFrame(new Duration(1000), new KeyValue(card.heightProperty(), card.getHeight() * finalZoom)));
        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(card.xProperty(), 0)));

        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(card.yProperty(), 0)));

        currentTimeline.onFinishedProperty().set(actionEvent ->
        {
            gameContext.updateScore(stats, gameInstance);
            gameContext.playWinTransition(500, actionEvent1 -> {
                gameInstance.dispose();

                gameContext.clear();

                gameInstance.launch();
            });
        });

        currentTimeline.play();
    }

    private void onWrongCardSelected() {

        currentTimeline.stop();
        currentTimeline = new Timeline();

        currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(card.opacityProperty(), 0)));

        currentTimeline.play();

        progressIndicator.setOpacity(0);
        text.setOpacity(0);
    }

    private EventHandler<Event> buildEvent() {

        return e -> {

            if (turned) {
                return;
            }

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                verifyProgressBarValue();
                progressIndicator.setOpacity(0.4);
                progressIndicator.setProgress(0);

                currentTimeline.stop();
                currentTimeline = new Timeline();

                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.xProperty(), card.getX() - (initWidth * zoomFactor - initWidth) / 2)));
                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.yProperty(), card.getY() - (initHeight * zoomFactor - initHeight) / 2)));
                currentTimeline.getKeyFrames().add(
                    new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth * zoomFactor)));
                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.heightProperty(), initHeight * zoomFactor)));

                timelineProgressBar = new Timeline();

                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(progressIndicator.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {

                    turned = true;

                    card.setFill(new ImagePattern(image, 0, 100, 1, 1, true));

                    card.removeEventFilter(MouseEvent.ANY, enterEvent);
                    card.removeEventFilter(GazeEvent.ANY, enterEvent);

                    if (winner) {
                        onCorrectCardSelected();
                    } else {// bad card
                        onWrongCardSelected();
                    }
                });

                currentTimeline.play();
                timelineProgressBar.play();

            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                currentTimeline.stop();
                currentTimeline = new Timeline();

                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.xProperty(), card.getX() + (initWidth * zoomFactor - initWidth) / 2)));
                currentTimeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                    new KeyValue(card.yProperty(), card.getY() + (initHeight * zoomFactor - initHeight) / 2)));
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

    private void verifyProgressBarValue() {
        double finalSize = initWidth * 0.9 * gameContext.getConfiguration().getProgressBarSize() / 100;
        progressIndicator.setTranslateX(card.getX() + (initWidth - finalSize) * 1.15 / 2);
        progressIndicator.setTranslateY(card.getY() + (initHeight - finalSize) * 1.15 / 2);
        log.info("init : {} * {}; new : {} * {}", initWidth, initHeight, finalSize, finalSize);
        progressIndicator.setMinWidth(finalSize);
        progressIndicator.setMinHeight(finalSize);

        progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor()); // To change the color of the progress bar
    }
}
