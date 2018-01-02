package net.gazeplay.games.magiccards;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.GazeEvent;
import net.gazeplay.commons.gaze.GazeUtils;
import net.gazeplay.commons.utils.stats.HiddenItemsGamesStats;

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

    private final Scene scene;
    private final GameContext gameContext;

    private final double initWidth;
    private final double initHeight;

    private final MagicCards gameInstance;

    /**
     * true if the card has been turned
     */
    private boolean turned = false;

    private final ProgressIndicator progressIndicator;

    private Timeline timelineProgressBar;
    final HiddenItemsGamesStats stats;

    final EventHandler<Event> enterEvent;

    public Card(double positionX, double positionY, double width, double height, Image image, boolean winner,
            GameContext gameContext, HiddenItemsGamesStats stats, MagicCards gameInstance, int fixationlength) {

        this.card = new Rectangle(positionX, positionY, width, height);
        this.card.setFill(new ImagePattern(new Image("data/magiccards/images/red-card-game.png"), 0, 0, 1, 1, true));

        this.image = image;
        this.winner = winner; // true if it is the good card

        this.gameContext = gameContext;
        this.scene = gameContext.getScene();

        this.stats = stats;

        this.fixationlength = fixationlength;

        this.gameInstance = gameInstance;

        this.initWidth = width;
        this.initHeight = height;

        this.getChildren().add(card);

        this.progressIndicator = createProgressIndicator(width, height);
        this.getChildren().add(this.progressIndicator);

        this.enterEvent = buildEvent();

        GazeUtils.addEventFilter(card);

        this.addEventFilter(MouseEvent.ANY, enterEvent);
        this.addEventFilter(GazeEvent.ANY, enterEvent);
    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX() + width * 0.05);
        indicator.setTranslateY(card.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private void onCorrectCardSelected() {

        stats.incNbGoals();

        int final_zoom = 2;

        progressIndicator.setOpacity(0);

        gameInstance.removeAllIncorrectCards();

        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(
                new KeyFrame(new Duration(1000), new KeyValue(card.widthProperty(), card.getWidth() * final_zoom)));
        timeline.getKeyFrames().add(
                new KeyFrame(new Duration(1000), new KeyValue(card.heightProperty(), card.getHeight() * final_zoom)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(card.xProperty(), (scene.getWidth() - card.getWidth() * final_zoom) / 2)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(card.yProperty(), (scene.getHeight() - card.getHeight() * final_zoom) / 2)));

        timeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent actionEvent) {
                        gameInstance.dispose();

                        gameContext.clear();

                        gameInstance.launch();

                        stats.start();

                        gameContext.onGameStarted();
                    }
                });
            }
        });

        timeline.play();
    }

    private void onWrongCardSelected() {

        Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(card.opacityProperty(), 0)));

        timeline.play();

        progressIndicator.setOpacity(0);
    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (turned)
                    return;

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    progressIndicator.setOpacity(1);
                    progressIndicator.setProgress(0);

                    Timeline timelineCard = new Timeline();

                    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.xProperty(), card.getX() - (initWidth * zoom_factor - initWidth) / 2)));
                    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.yProperty(), card.getY() - (initHeight * zoom_factor - initHeight) / 2)));
                    timelineCard.getKeyFrames().add(
                            new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth * zoom_factor)));
                    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.heightProperty(), initHeight * zoom_factor)));

                    timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(fixationlength),
                            new KeyValue(progressIndicator.progressProperty(), 1)));

                    timelineCard.play();

                    timelineProgressBar.play();

                    timelineProgressBar.setOnFinished(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {

                            turned = true;

                            card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));

                            card.removeEventFilter(MouseEvent.ANY, enterEvent);
                            card.removeEventFilter(GazeEvent.ANY, enterEvent);

                            if (winner) {
                                onCorrectCardSelected();
                            } else {// bad card
                                onWrongCardSelected();
                            }
                        }
                    });
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    Timeline timeline = new Timeline();

                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.xProperty(), card.getX() + (initWidth * zoom_factor - initWidth) / 2)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.yProperty(), card.getY() + (initHeight * zoom_factor - initHeight) / 2)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(new Duration(1), new KeyValue(card.heightProperty(), initHeight)));

                    timeline.play();

                    timelineProgressBar.stop();

                    progressIndicator.setOpacity(0);
                    progressIndicator.setProgress(0);
                }
            }
        };
    }

}
