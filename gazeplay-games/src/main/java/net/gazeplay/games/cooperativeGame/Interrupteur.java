package net.gazeplay.games.cooperativeGame;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
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
    private IGameContext gameContext;
    private CooperativeGame gameInstance;
    private final EventHandler<Event> enterEvent;
    private final ProgressIndicator progressIndicator;
    private Timeline timelineProgressBar;




    public Interrupteur(Rectangle interrupteur, IGameContext gameContext, CooperativeGame gameInstance){
        this.interrupteur = interrupteur;
        this.portes = new ArrayList<>();
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.isInterrupteurActivated = false;
        this.progressIndicator = createProgressIndicator(120, 120);
        gameContext.getChildren().add(this.progressIndicator);
        this.progressIndicator.toFront();
        this.enterEvent = buildEvent();
        gameContext.getGazeDeviceManager().addEventFilter(this.interrupteur);
        this.interrupteur.addEventFilter(GazeEvent.ANY, enterEvent);
        this.interrupteur.addEventFilter(MouseEvent.ANY, enterEvent);
    }

    private ProgressIndicator createProgressIndicator(final double width, final double height) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(interrupteur.getX() + width * 0.05);
        indicator.setTranslateY(interrupteur.getY() + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    private EventHandler<Event> buildEvent() {
        return e -> {
            if (e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                this.interrupteur.setFill(Color.BLUE);
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
                        for (Rectangle porte : portes) {
                            porte.setX(porte.getX() + dimension2D.getWidth() + 100);
                        }
                    }else{
                        this.isInterrupteurActivated = false;
                        for (Rectangle porte : portes) {
                            porte.setX(porte.getX()-dimension2D.getWidth() - 100);
                        }
                    }
                });
                timelineProgressBar.play();
            }
            if (e.getEventType() == GazeEvent.GAZE_EXITED || e.getEventType() == MouseEvent.MOUSE_EXITED){
                this.interrupteur.setFill(Color.RED);
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
