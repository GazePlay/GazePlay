package magiccards;

import gaze.GazeEvent;
import gaze.GazeUtils;
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
import utils.games.Bravo;
import utils.games.Home;
import utils.games.Utils;

import java.util.Date;

/**
 * Created by schwab on 17/09/2016.
 */
public class Card extends Parent {

    protected static final float cardRatio = 0.75f;
    protected static final int minHeight = 30;
    protected static final float zoom_factor = 1.1f;
    protected static final double min_time = 500;//Math.sqrt(2)*1000;//0.5*1000;
    private Rectangle card;
    private boolean winner;
    private Image image;
    private boolean found;
    private boolean anim0ff = false;
    private long entry;
    //private double min_time;
    private double initWidth;
    private double initHeight;
    private Scene scene;
    private ChoiceBox choiceBox;
    private Group root;
    ProgressIndicator indicator;
    Bravo bravo = new Bravo();

    final static Image[] images = Utils.images(System.getProperty("user.home") +Utils.FILESEPARATOR+ "GazePlay"+Utils.FILESEPARATOR+"files"+Utils.FILESEPARATOR+"images"+Utils.FILESEPARATOR+"magiccards"+Utils.FILESEPARATOR);


    EventHandler<Event> enterEvent;
    boolean anniOff = true;

    public Card(int nbColomns, int nbLines, double x, double y, double width, double height, Image image, boolean winner, Scene scene, Group root, ChoiceBox choiceBox){

        this.entry = -1;
        this.winner = winner;
        this.initWidth=width;
        this.initHeight=height;
        this.scene = scene;
        this.choiceBox = choiceBox;
        this.root=root;
        card = new Rectangle(x, y, width, height);
        card.setFill(new ImagePattern(new Image("data/magiccards/images/red-card-game.png"),0,0,1,1, true));
        this.getChildren().add(card);
        this.image = image;
        indicator = new ProgressIndicator(0);
        indicator.setTranslateX(card.getX()+width*0.05);
        indicator.setTranslateY(card.getY()+height*0.2);
        indicator.setMinWidth(width*0.9);
        indicator.setMinHeight(width*0.9);
        indicator.setOpacity(0);
       // indicator.progressProperty().bind(slider.valueProperty().divide(127.0));
        this.getChildren().add(indicator);
        //indicator.setTranslateX(-15);

        this.getChildren().add(bravo);

        GazeUtils.addEventFilter(card);

        enterEvent = buildEvent();

        this.addEventFilter(MouseEvent.ANY, enterEvent);

        this.addEventFilter(GazeEvent.ANY, enterEvent);
    }

    public static void main(String[] args) {
        Application.launch(MagicCards.class, args);
    }

    private void enter(){

        Timeline timeline = new Timeline();

        card.setFill(new ImagePattern(image,0,0,1,1, true));
    }

    private EventHandler<Event> buildEvent() {
        return new EventHandler<Event>() {
            @Override
            public void handle(Event e) {

                //System.out.println(e.getEventType());

                if (found || anim0ff) {

                    return;
                }

                if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                    anim0ff = true;

                    indicator.setOpacity(0.5);

                    entry = (new Date()).getTime();

                    Timeline timeline = new Timeline();

                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(card.xProperty(), card.getX() - (initWidth*zoom_factor - initWidth)/2)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(card.yProperty(), card.getY() - (initHeight*zoom_factor - initHeight)/2)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth*zoom_factor)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(card.heightProperty(), initHeight*zoom_factor)));

                    timeline.play();

                    anim0ff = false;


                } else if (e.getEventType() == GazeEvent.GAZE_MOVED || e.getEventType() == MouseEvent.MOUSE_MOVED) {

                    //System.out.println("MOVE");

                    long now = (new Date()).getTime();

                    indicator.setProgress((now - entry)/min_time);

                    if (entry != -1 && (now - entry) > min_time) {

                        //System.out.println("GAGNÉ");

                        found = true;

                        card.setFill(new ImagePattern(image,0,0,1,1, true));

                        if(winner){

                            int final_zoom = 2;

                            indicator.setOpacity(0);

                            Timeline timeline = new Timeline();

                            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(card.widthProperty(), card.getWidth()*final_zoom)));
                            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(card.heightProperty(), card.getHeight()*final_zoom)));
                            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(card.xProperty(), (scene.getWidth()-card.getWidth()*final_zoom)/2)));
                            timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(card.yProperty(), (scene.getHeight()-card.getHeight()*final_zoom)/2)));



                            timeline.onFinishedProperty().set(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent actionEvent) {


                                    for(Node N : root.getChildren()){

                                        System.out.println(N.getClass());

                                        if( (N instanceof Home)) {

                                            System.out.println("OK");
                                            N.setTranslateX(-10000);
                                            N.setOpacity(0);
                                            N.removeEventFilter(MouseEvent.ANY, enterEvent);
                                        }
                                    }

                                    SequentialTransition sequence = bravo.win();
                                    sequence.setOnFinished(new EventHandler<ActionEvent>() {

                                        @Override
                                        public void handle(ActionEvent actionEvent) {
                                            Utils.clear(scene, root, choiceBox);
                                            Card.addCards(root, scene, choiceBox,2, 2);
                                            Utils.home(scene, root, choiceBox);
                                        }
                                    });
                                }
                            });

                            timeline.play();

                        }
                        else{//bad card

                            Timeline timeline = new Timeline();

                            timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), new KeyValue(card.opacityProperty(), 0)));

                            timeline.play();

                            indicator.setOpacity(0);

                        }

                    } else {


                    }
                } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {

                    anim0ff = true;

                    Timeline timeline = new Timeline();

                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(card.xProperty(), card.getX() + (initWidth*zoom_factor - initWidth)/2)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(card.yProperty(), card.getY() + (initHeight*zoom_factor - initHeight)/2)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(card.widthProperty(), initWidth)));
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(card.heightProperty(), initHeight)));

                    timeline.play();
                    entry = -1;
                    anim0ff = false;
                    indicator.setOpacity(0);
                }
            }
        };

    }


    public static void addCards(Group root, Scene scene, ChoiceBox cbxGames, int nbColumns, int nbLines) {

        double cardHeight = computeCardHeight(scene, nbLines);
        double cardWidth = cardHeight * cardRatio;
        double width = computeCardWidth(scene, nbColumns) - cardWidth;

        System.out.println(cardHeight);
        System.out.println(cardWidth);

        int winner = (int)(nbColumns * nbLines * Math.random());
        int k = 0;
        Card winCard = null;


        for (int i = 0 ; i < nbColumns ; i++)
            for (int j = 0 ; j < nbLines ; j++){

                if(k++==winner) {
                    winCard = new Card(nbColumns,  nbLines, width / 2 + (width + cardWidth) * i, minHeight / 2 + (minHeight + cardHeight) * j, cardWidth, cardHeight, getRandomImage(), true, scene, root, cbxGames);
                    root.getChildren().add(winCard);
                }
                else {
                    Card card = new Card(nbColumns,  nbLines, width / 2 + (width + cardWidth) * i, minHeight / 2 + (minHeight + cardHeight) * j, cardWidth, cardHeight, new Image("data/magiccards/images/error.png"), false, scene, root, cbxGames);
                    root.getChildren().add(card);
                }
            }
        winCard.toFront();
    }

    private static Image getRandomImage() {

        //Image[] images = Utils.getImages(System.getProperty("user.home") +Utils.FILESEPARATOR+ "GazePlay"+Utils.FILESEPARATOR+"files"+Utils.FILESEPARATOR+"images"+Utils.FILESEPARATOR+"magiccards"+Utils.FILESEPARATOR);

        int value = (int)Math.floor(Math.random()*images.length);

        return images[value];
    }

    private static double computeCardHeight(Scene scene, int nbLines ){

        return scene.getHeight()*0.9/ nbLines;
    }

    private static double computeCardWidth(Scene scene, int nbColumns){

        return scene.getWidth()/ nbColumns;
    }





}
