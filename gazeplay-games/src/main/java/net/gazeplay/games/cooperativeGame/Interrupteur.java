package net.gazeplay.games.cooperativeGame;

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

/**
 * A class representing a switch that can activate or deactivate connected doors.
 */
public class Interrupteur extends Parent {

    /**
     The main interrupteur rectangle object that players can interact with.
     */
    private Rectangle interrupteur;

    /**
     A list of associated portes rectangles that will be opened when the interrupteur is activated.
     */
    private ArrayList<Rectangle> portes;

    /**
     A boolean flag indicating whether the interrupteur is currently activated or not.
     */
    private boolean isInterrupteurActivated;

    /**
     The game context in which the interrupteur object exists.
     */
    private final IGameContext gameContext;

    /**
     The instance of the cooperative game that this interrupteur object is a part of.
     */
    private final SprintFinish gameInstance;

    /**
     The event handler that will be triggered when the interrupteur is gazed at or clicked on.
     */
    private final EventHandler<Event> enterEvent;

    /**
     A progress indicator that displays the amount of time remaining until the interrupteur is activated.
     */
    private final ProgressIndicator progressIndicator;

    /**
     A timeline object that controls the progress bar animation.
     */
    protected Timeline timelineProgressBar;

    /**
     A factor that determines the rate at which the interrupteur activation progresses.
     */
    private final double slow;

    /**
     The image pattern of the interrupteur button when it is in the "off" state.
     */
    private final ImagePattern offButton;

    /**
     The image pattern of the interrupteur button when it is in the "on" state.
     */
    private final ImagePattern onButton;





    /**
     * Creates a new Interrupteur object with the given parameters.
     *
     * @param interrupteur the rectangle representing the switch
     * @param gameContext the game context in which the switch is located
     * @param gameInstance the instance of the cooperative game
     */
    public Interrupteur(Rectangle interrupteur, IGameContext gameContext, SprintFinish gameInstance){
        this.interrupteur = interrupteur;
        this.portes = new ArrayList<>();
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.isInterrupteurActivated = false;
        this.offButton = new ImagePattern(new Image("data/cooperativeGame/pushButtonOff.png"));
        this.onButton = new ImagePattern(new Image("data/cooperativeGame/pushButtonOn.png"));
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


    /**
     Creates a new ProgressIndicator with the given width and height, and sets its properties.
     @param width the width of the ProgressIndicator
     @param height the height of the ProgressIndicator
     @return the created ProgressIndicator
     */
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
            if (gameInstance.gameTimerEnded){
                if (!gameInstance.endOfLevel){
                    if (!gameInstance.catNotKeyboard){
                        if (e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                            initTimerInterrupteur();
                        }
                        if (e.getEventType() == GazeEvent.GAZE_EXITED || e.getEventType() == MouseEvent.MOUSE_EXITED){
                            stopTimerInterrupteur();
                        }
                    }
                }else{
                    stopTimerInterrupteur();
                }
            }
        };
    }

    protected void stopTimerInterrupteur(){
        if (timelineProgressBar != null){
            timelineProgressBar.stop();
            progressIndicator.setOpacity(0);
            progressIndicator.setProgress(0);
            for (Cat dog : gameInstance.dogs){
                dog.speed = dog.speed / slow;
            }
        }
    }

    protected void initTimerInterrupteur(){

        for (Cat dog : gameInstance.dogs){
            dog.speed = dog.speed * slow;
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
                // Move the doors out of bounds
                this.isInterrupteurActivated = true;
                this.interrupteur.setFill(onButton);
                for (Rectangle porte : portes) {
                    porte.setX(porte.getX() + dimension2D.getWidth() + 500);
                }
            }else{
                this.isInterrupteurActivated = false;
                this.interrupteur.setFill(offButton);

                // Move the doors to their initial position
                for (Rectangle porte : portes) {
                    porte.setX(porte.getX()-dimension2D.getWidth() - 500);
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

            if (gameInstance.catNotKeyboard){
                stopTimerInterrupteur();
            }

        });
        timelineProgressBar.play();

    }


    /**
     * Creates doors around a given object by creating 4 rectangles representing each side of the door.
     * Adds the rectangles to the list of doors.
     * @param object the object around which the doors are created
     */
    public void createDoorAroundAnObject(Rectangle object){

        // Define the width and height of the doors
        double width = 50;
        double height = 50;

        // Create 4 rectangles representing the doors around the object
        Rectangle leftDoor = new Rectangle(object.getX()-width*2, object.getY()-height/2, width,object.getHeight()+height);
        Rectangle rightDoor = new Rectangle(object.getX()+object.getWidth()+width, object.getY()-height/2, width,object.getHeight()+height);

        Rectangle upDoor = new Rectangle(object.getX()-width*2, object.getY()-height-height/2, object.getWidth()+width*3+width, height);
        Rectangle downDoor = new Rectangle(object.getX()-width*2, object.getY()+object.getHeight()+height/2, object.getWidth()+width*3+width, height);

        // Add the rectangles to the list of doors

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
