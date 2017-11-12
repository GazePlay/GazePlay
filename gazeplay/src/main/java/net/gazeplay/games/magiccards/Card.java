package net.gazeplay.games.magiccards;

import gaze.GazeEvent;
import gaze.GazeUtils;
import gaze.configuration.Configuration;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.utils.Bravo;
import net.gazeplay.utils.Home;
import net.gazeplay.utils.HomeUtils;
import utils.games.Utils;
import net.gazeplay.utils.stats.HiddenItemsGamesStats;

/**
 * Created by schwab on 17/09/2016.
 */
public class Card extends Parent {

    protected static final float cardRatio = 0.75f;
    protected static final int minHeight = 30;
    protected static final float zoom_factor = 1.1f;
    protected static double min_time;
    private Rectangle card;
    private boolean winner;
    private Image image;
    private boolean toTurn = false;// true if the card is about to be turned (ie player gaze is on it and progress bar
                                   // is increasing)
    private boolean turned = false;// true if the card has been turned
    int nbLines;
    int nbColumns;
    private double initWidth;
    private double initHeight;
    private Scene scene;
    private ChoiceBox choiceBox;
    private Group root;
    ProgressIndicator indicator;
    HiddenItemsGamesStats stats;
    Bravo bravo = Bravo.getBravo();

    private static Image[] images;

    EventHandler<Event> enterEvent;
    boolean anniOff = true;

    public Card(int nbColumns, int nbLines, double x, double y, double width, double height, Image image,
            boolean winner, Scene scene, Group root, ChoiceBox choiceBox, HiddenItemsGamesStats stats) {

        min_time = new Configuration().fixationlength;
        this.winner = winner;// true if it is the good card
        this.initWidth = width;
        this.initHeight = height;
        this.scene = scene;
        this.choiceBox = choiceBox;
        this.root = root;
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

        // this.getChildren().add(bravo);

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

                    SequentialTransition sequence = new SequentialTransition();

                    Timeline timelineCard = new Timeline();

                    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(indicator.progressProperty(), 0)));
                   timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.xProperty(), card.getX() - (initWidth * zoom_factor - initWidth) / 2)));
                    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.yProperty(), card.getY() - (initHeight * zoom_factor - initHeight) / 2)));
                    timelineCard.getKeyFrames().add(
                            new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth * zoom_factor)));
                    timelineCard.getKeyFrames().add(new KeyFrame(new Duration(1),
                            new KeyValue(card.heightProperty(), initHeight * zoom_factor)));

                    Timeline timelineProgressBar = new Timeline();

                    timelineProgressBar.getKeyFrames()
                            .add(new KeyFrame(new Duration(min_time), new KeyValue(indicator.progressProperty(), 1)));

                    sequence.getChildren().addAll(timelineCard, timelineProgressBar);

                    sequence.play();

                    toTurn = true;

                    sequence.setOnFinished(new EventHandler<ActionEvent>() {

                        @Override
                        public void handle(ActionEvent actionEvent) {

                            if (!toTurn) {// la carte n'est plus à tourner quand l'évènement est terminé.

                                return;
                            }

                            toTurn = false;

                            turned = true;

                            card.setFill(new ImagePattern(image, 0, 0, 1, 1, true));

                            card.removeEventFilter(MouseEvent.ANY, enterEvent);
                            card.removeEventFilter(GazeEvent.ANY, enterEvent);

                            if (winner) {

                                stats.incNbGoals();

                                int final_zoom = 2;

                                indicator.setOpacity(0);

                                Timeline timeline = new Timeline();

                                for (Node N : root.getChildren()) {// clear all but images and reward

                                    if ((N instanceof Card && card != ((Card) N).getCard() && !(N instanceof Bravo))
                                            || (N instanceof Home)) {// we put outside screen Home and cards

                                        N.setTranslateX(-10000);
                                        N.setOpacity(0);
                                        N.removeEventFilter(MouseEvent.ANY, enterEvent);
                                        N.removeEventFilter(GazeEvent.ANY, enterEvent);
                                    } else {// we keep only Bravo and winning card
                                    }
                                }

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

                                        SequentialTransition sequence = bravo.win();
                                        bravo.toFront();
                                        sequence.setOnFinished(new EventHandler<ActionEvent>() {

                                            @Override
                                            public void handle(ActionEvent actionEvent) {
                                                HomeUtils.clear(scene, root, choiceBox);
                                                Card.addCards(root, scene, choiceBox, nbLines, nbColumns, stats);
                                                HomeUtils.home(scene, root, choiceBox, stats);
                                                stats.start();
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

                    toTurn = false;

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
                    indicator.setOpacity(0);
                    indicator.setProgress(0);
                }
            }
        };
    }

    public static void addCards(Group root, Scene scene, ChoiceBox cbxGames, int nbLines, int nbColumns,
            HiddenItemsGamesStats stats) {

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
                            scene, root, cbxGames, stats);
                    root.getChildren().add(winCard);
                } else {
                    Card card = new Card(nbColumns, nbLines, width / 2 + (width + cardWidth) * i,
                            minHeight / 2 + (minHeight + cardHeight) * j, cardWidth, cardHeight,
                            new Image("data/magiccards/images/error.png"), false, scene, root, cbxGames, stats);
                    root.getChildren().add(card);
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
