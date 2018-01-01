package net.gazeplay.games.magiccards;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
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
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.commons.gaze.GazeEvent;
import net.gazeplay.commons.gaze.GazeUtils;
import net.gazeplay.commons.gaze.configuration.Configuration;
import net.gazeplay.commons.gaze.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.HiddenItemsGamesStats;

/**
 * Created by schwab on 17/09/2016.
 */
@Slf4j
public class Card extends Parent {

    protected static final float cardRatio = 0.75f;
    protected static final int minHeight = 30;
    protected static final float zoom_factor = 1.1f;
    protected static double mintime;

    private final Rectangle card;
    private final boolean winner;
    private final Image image;

    private boolean turned = false;// true if the card has been turned
    final int nbLines;
    final int nbColumns;
    private double initWidth;
    private double initHeight;
    private final Scene scene;
    private final GameContext gameContext;
    private final ProgressIndicator indicator;
    private Timeline timelineProgressBar;
    final HiddenItemsGamesStats stats;

    private static Image[] images;

    final EventHandler<Event> enterEvent;

    public Card(int nbColumns, int nbLines, double x, double y, double width, double height, Image image,
            boolean winner, GameContext gameContext, HiddenItemsGamesStats stats) {

        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        mintime = config.getFixationlength();
        this.winner = winner;// true if it is the good card
        this.initWidth = width;
        this.initHeight = height;
        this.gameContext = gameContext;
        this.scene = gameContext.getScene();

        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.stats = stats;
        card = new Rectangle(x, y, width, height);
        card.setFill(new ImagePattern(new Image("data/magiccards/images/red-card-game.png"), 0, 0, 1, 1, true));
        this.getChildren().add(card);
        this.image = image;
        indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX() + width * 0.05);
        indicator.setTranslateY(card.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        this.getChildren().add(indicator);

        GazeUtils.addEventFilter(card);

        enterEvent = buildEvent();

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        this.addEventFilter(GazeEvent.ANY, enterEvent);
    }

    public static void main(String[] args) {
        Application.launch(MagicCards.class, args);
    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                if (turned)
                    return;

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    indicator.setOpacity(1);
                    indicator.setProgress(0);

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

                    timelineProgressBar.getKeyFrames()
                            .add(new KeyFrame(new Duration(mintime), new KeyValue(indicator.progressProperty(), 1)));

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

                                stats.incNbGoals();

                                int final_zoom = 2;

                                indicator.setOpacity(0);

                                Timeline timeline = new Timeline();

                                timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                                        new KeyValue(card.widthProperty(), card.getWidth() * final_zoom)));
                                timeline.getKeyFrames().add(new KeyFrame(new Duration(1000),
                                        new KeyValue(card.heightProperty(), card.getHeight() * final_zoom)));
                                timeline.getKeyFrames()
                                        .add(new KeyFrame(new Duration(1000), new KeyValue(card.xProperty(),
                                                (scene.getWidth() - card.getWidth() * final_zoom) / 2)));
                                timeline.getKeyFrames()
                                        .add(new KeyFrame(new Duration(1000), new KeyValue(card.yProperty(),
                                                (scene.getHeight() - card.getHeight() * final_zoom) / 2)));

                                timeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {

                                        gameContext.playWinTransition(500, new EventHandler<ActionEvent>() {

                                            @Override
                                            public void handle(ActionEvent actionEvent) {
                                                gameContext.clear();
                                                Card.addCards(gameContext, nbLines, nbColumns, stats);
                                                // HomeUtils.home(scene, root, choiceBox, stats);
                                                stats.start();

                                                gameContext.onGameStarted();
                                            }
                                        });
                                    }
                                });

                                timeline.play();

                            } else {// bad card

                                Timeline timeline = new Timeline();

                                timeline.getKeyFrames()
                                        .add(new KeyFrame(new Duration(2000), new KeyValue(card.opacityProperty(), 0)));

                                timeline.play();

                                indicator.setOpacity(0);
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

                    indicator.setOpacity(0);
                    indicator.setProgress(0);
                }
            }
        };
    }

    public static void addCards(GameContext gameContext, int nbLines, int nbColumns, HiddenItemsGamesStats stats) {

        Scene scene = gameContext.getScene();

        images = Utils.images(Utils.getImagesFolder() + "magiccards" + Utils.FILESEPARATOR);
        double cardHeight = computeCardHeight(scene, nbLines);
        double cardWidth = cardHeight * cardRatio;
        double width = computeCardWidth(scene, nbColumns) - cardWidth;

        int winner = (int) (nbColumns * nbLines * Math.random());
        int k = 0;
        Card winCard = null;

        for (int i = 0; i < nbColumns; i++)
            for (int j = 0; j < nbLines; j++) {

                if (k++ == winner) {
                    winCard = new Card(nbColumns, nbLines, width / 2 + (width + cardWidth) * i,
                            minHeight / 2 + (minHeight + cardHeight) * j, cardWidth, cardHeight, getRandomImage(), true,
                            gameContext, stats);
                    gameContext.getChildren().add(winCard);
                } else {
                    Card card = new Card(nbColumns, nbLines, width / 2 + (width + cardWidth) * i,
                            minHeight / 2 + (minHeight + cardHeight) * j, cardWidth, cardHeight,
                            new Image("data/common/images/error.png"), false, gameContext, stats);
                    gameContext.getChildren().add(card);
                }
            }
        winCard.toFront();
        stats.start();
    }

    private static Image getRandomImage() {

        int value = (int) Math.floor(Math.random() * images.length);

        return images[value];
    }

    private static double computeCardHeight(Scene scene, int nbLines) {

        return scene.getHeight() * 0.9 / nbLines;
    }

    private static double computeCardWidth(Scene scene, int nbColumns) {

        return scene.getWidth() / nbColumns;
    }

    public Rectangle getCard() {
        return card;
    }

}
