package net.gazeplay.games.biboulejump;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class BibouleJump extends AnimationTimer implements GameLifeCycle {

    private static String DATA_PATH = "data/biboulejump";

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;

    private final double maxSpeed;
    private final double gravity;

    private Point2D mvmtVect;
    private Point2D gazeTarget;

    private long lastTickTime = 0;
    //Entities
    private Rectangle biboule;

    public BibouleJump(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();

        this.maxSpeed = 10.0;
        this.gravity = 0.1;

        Rectangle background = new Rectangle(0, 0, dimensions.getWidth(), dimensions.getHeight());
        background.setFill(Color.WHEAT);
        this.gameContext.getChildren().add(background);

        biboule = new Rectangle(dimensions.getWidth()/2.0, dimensions.getHeight()/2.0, dimensions.getHeight()/8, dimensions.getHeight()/8 );
        biboule.setFill(new ImagePattern(new Image("data/biboule/images/Blue.png")));
        this.gameContext.getChildren().add(biboule);

        gazeTarget = new Point2D(dimensions.getWidth()/2.0, dimensions.getHeight()/2.0);
        mvmtVect = Point2D.ZERO;

        EventHandler<Event> movementEvent = (Event event) -> {
            if (event.getEventType() == MouseEvent.MOUSE_MOVED) {
                gazeTarget = new Point2D(((MouseEvent)event).getX(), ((MouseEvent)event).getY());
            }else if (event.getEventType() == GazeEvent.GAZE_MOVED){
                gazeTarget = new Point2D(((GazeEvent)event).getX(), ((GazeEvent)event).getY());
            }
        };

        background.addEventFilter(MouseEvent.MOUSE_MOVED, movementEvent);
        background.addEventFilter(GazeEvent.GAZE_MOVED, movementEvent);
    }

    @Override
    public void launch() {
        generatePlatforms();

        this.start();
    }

    @Override
    public void dispose() {

    }

    private void generatePlatforms(){

    }

    @Override
    public void handle(long now) {
        if(lastTickTime == 0){
            lastTickTime = now;
        }
        double timeElapsed = ((double)now - (double)lastTickTime)/Math.pow(10.0, 6.0);
        lastTickTime = now;

        // Y velocity
        if(mvmtVect.getY() < maxSpeed){ //gravity
            mvmtVect = mvmtVect.add(0.0, gravity * timeElapsed);
        }

        if(biboule.getY() + biboule.getHeight() > dimensions.getHeight() && mvmtVect.getY() > 0){
            mvmtVect = mvmtVect.subtract(0.0, maxSpeed * 3);
        }

        // X velocity
        double distance = gazeTarget.distance(biboule.getX() + biboule.getWidth()/2.0, gazeTarget.getY());

        /*if(distance > Math.abs(mvmtVect.getX())) {
            mvmtVect = mvmtVect.add((gazeTarget.getX() > biboule.getX() + biboule.getWidth()/2.0 ? 0.5 : -0.5) * timeElapsed, 0.0);
        }else{
            mvmtVect = mvmtVect.add((gazeTarget.getX() > biboule.getX() + biboule.getWidth()/2.0 ? Math.abs(mvmtVect.getX()) - distance : -(Math.abs(mvmtVect.getX()) - distance)) * timeElapsed, 0.0);
        }*/

        double targetSpeed = clamp(maxSpeed, 0.0, distance);
        if(Math.abs(mvmtVect.getX()) > targetSpeed){

        }



        //Limit to max speed


        //Move biboule
        biboule.setX(biboule.getX() + mvmtVect.getX() * timeElapsed);
        biboule.setY(biboule.getY() + mvmtVect.getY() * timeElapsed);
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
