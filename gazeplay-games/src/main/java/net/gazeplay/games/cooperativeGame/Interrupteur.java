package net.gazeplay.games.cooperativeGame;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

import java.util.ArrayList;

public class Interrupteur extends Parent {

    private Rectangle interrupteur;
    private ArrayList<Rectangle> portes;
    private boolean isInterrupteurActivated;
    private final IGameContext gameContext;
    private final CooperativeGame gameInstance;
    private final EventHandler<Event> enterEvent;
    private final ProgressIndicator progressIndicator;
    private Timeline timelineProgressBar;
    private final double slow;
    private final ImagePattern offButton;
    private final ImagePattern onButton;



    public Interrupteur(Rectangle interrupteur, IGameContext gameContext, CooperativeGame gameInstance){
        this.interrupteur = interrupteur;
        this.portes = new ArrayList<>();
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.isInterrupteurActivated = false;
        this.offButton = new ImagePattern(new Image("data/cooperativeGame/pushButtonOff.png"));
        this.onButton = new ImagePattern(new Image("data/cooperativeGame/pushButtonOn.png"));
        this.interrupteur.maxHeight(interrupteur.getHeight());
        this.interrupteur.maxWidth(interrupteur.getWidth());
        this.interrupteur.setFill(offButton);
        gameContext.getChildren().add(this.interrupteur);
        this.enterEvent = buildEvent();
        this.progressIndicator = createProgressIndicator(interrupteur.getWidth()+10, interrupteur.getHeight()+10);
        gameContext.getGazeDeviceManager().addEventFilter(this.progressIndicator);
        this.progressIndicator.addEventFilter(GazeEvent.ANY, enterEvent);
        this.progressIndicator.addEventFilter(MouseEvent.ANY, enterEvent);
        this.progressIndicator.toFront();
        this.slow = 0.7;
        gameContext.getChildren().add(this.progressIndicator);
    }

    private ProgressIndicator createProgressIndicator(final double width, final double height) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(interrupteur.getX() + width * 0.05);
        indicator.setTranslateY(interrupteur.getY() + height * 0.2);
        indicator.setMinWidth(width);
        indicator.setMinHeight(height);
        indicator.setOpacity(0);
        return indicator;
    }

    private EventHandler<Event> buildEvent() {
        return e -> {
            if (e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == MouseEvent.MOUSE_ENTERED) {

                for (Cat dog : gameInstance.dogs){
                    dog.speed = dog.speed * slow;
                    System.out.println("entered : " + dog.speed);
                }

                progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
                progressIndicator.setOpacity(1);
                progressIndicator.setProgress(0);
                timelineProgressBar = new Timeline();
                timelineProgressBar.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()),
                    new KeyValue(progressIndicator.progressProperty(), 1)));

                timelineProgressBar.setOnFinished(actionEvent -> {
                        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
                    if (!this.isInterrupteurActivated){
                        this.isInterrupteurActivated = true;
                        this.interrupteur.setFill(onButton);
                        for (Rectangle porte : portes) {
                            porte.setX(porte.getX() + dimension2D.getWidth() + 100);
                        }
                    }else{
                        this.isInterrupteurActivated = false;
                        this.interrupteur.setFill(offButton);
                        for (Rectangle porte : portes) {
                            porte.setX(porte.getX()-dimension2D.getWidth() - 100);
                            if (gameInstance.isCollidingWithASpecificObstacle(porte,gameInstance.cat.hitbox)){
                                gameInstance.endOfGame(false);
                                break;
                            }else{
                                for (Cat dog: gameInstance.dogs){
                                    if (gameInstance.isCollidingWithASpecificObstacle(porte,dog.hitbox)){
                                        dog.hitbox.setX(dog.initPosX);
                                        dog.hitbox.setY(dog.initPosY);
                                    }
                                }
                            }
                        }
                    }
                });
                timelineProgressBar.play();
            }
            if (e.getEventType() == GazeEvent.GAZE_EXITED || e.getEventType() == MouseEvent.MOUSE_EXITED){

                for (Cat dog : gameInstance.dogs){
                    dog.speed = dog.speed / slow;
                    System.out.println("exited : " + dog.speed);
                }
                timelineProgressBar.stop();
                progressIndicator.setOpacity(0);
                progressIndicator.setProgress(0);
            }
        };
    }



    public void createDoorAroundAnObject(Rectangle object){

        double width = 50;
        double height = 50;

        Rectangle leftDoor = new Rectangle(object.getX()-width*2, object.getY()-height/2, width,object.getHeight()+height);
        Rectangle rightDoor = new Rectangle(object.getX()+object.getWidth()+width, object.getY()-height/2, width,object.getHeight()+height);

        Rectangle upDoor = new Rectangle(object.getX()-width*2, object.getY()-height-height/2, object.getWidth()+width*3+width, height);
        Rectangle downDoor = new Rectangle(object.getX()-width*2, object.getY()+object.getHeight()+height/2, object.getWidth()+width*3+width, height);


        this.portes.add(upDoor);
        this.portes.add(leftDoor);
        this.portes.add(rightDoor);
        this.portes.add(downDoor);
    }








    public Rectangle getInterrupteur() {
        return interrupteur;
    }

    public void setInterrupteur(Rectangle interrupteur) {
        this.interrupteur = interrupteur;
    }

    public ArrayList<Rectangle> getPortes() {
        return portes;
    }

    public void setPortes(ArrayList<Rectangle> portes) {
        this.portes = portes;
    }


}
