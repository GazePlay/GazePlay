package net.gazeplay.games.cooperativeGame;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

public class MovingWall extends Rectangle {

    private boolean direction;
    private final CooperativeGame gameInstance;
    private Timeline verticalTimeline;
    private Timeline horizontalTimeline;
    protected boolean resetPos;
    private final IGameContext gameContext;
    private final EventHandler<Event> enterEvent;
    private float speed;


    MovingWall(final double x, final double y, final double width, final double height, CooperativeGame gameInstance, IGameContext gameContext, boolean resetPos, float speed) {
        super(x, y, width, height);
        this.direction = true;
        this.gameInstance = gameInstance;
        this.resetPos = resetPos;
        this.gameContext = gameContext;
        this.verticalTimeline = new Timeline();
        this.horizontalTimeline = new Timeline();
        this.speed = speed;
        this.enterEvent = buildEvent();
        gameContext.getGazeDeviceManager().addEventFilter(this);
        this.addEventFilter(GazeEvent.ANY, enterEvent);
        this.addEventFilter(MouseEvent.ANY, enterEvent);
    }

    public void animationVertical(double fromY, double toY){
        boolean frombottom;
        if (fromY > toY) {
            double temp = fromY;
            fromY = toY;
            toY = temp;
            frombottom = true;

        }else{
            frombottom = false;
        }
        double finalToY = toY;
        double finalFromY = fromY;
        verticalTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            if (this.direction){
                gameInstance.willCollideWithAnObstacle("down",this.speed,this);
                gameInstance.willCollideWithAnObstacle("up",this.speed,this);
                if (frombottom){
                    if (this.getY() + this.getHeight() >= finalToY+getHeight()) {
                        this.direction = false;
                    }
                }else{
                    if (this.getY() + this.getHeight() >= finalToY) {
                        this.direction = false;
                    }
                }
                this.setY(this.getY() + this.speed);
            } else {
                gameInstance.willCollideWithAnObstacle("up",this.speed,this);
                gameInstance.willCollideWithAnObstacle("down",this.speed,this);
                if (this.getY() <= finalFromY) {
                    this.direction = true;
                }
                this.setY(this.getY() - this.speed);
            }
        }));
        verticalTimeline.setCycleCount(Animation.INDEFINITE);
        verticalTimeline.play();
    }

    public void animationHorizontal(double fromX, double toX){
        boolean fromRight;
        if (fromX > toX) {
            double temp = fromX;
            fromX = toX;
            toX = temp;
            fromRight = true;

        }else{
            fromRight = false;
        }
        double finalToX = toX;
        double finalFromX = fromX;
        horizontalTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            if (this.direction){

                gameInstance.willCollideWithAnObstacle("right",this.speed,this);
                gameInstance.willCollideWithAnObstacle("left",this.speed,this);

                if (fromRight){
                    if (this.getX() + this.getWidth() >= finalToX+getWidth()){
                        this.direction = false;
                    }
                }else{
                    if (this.getX() + this.getWidth() >= finalToX){
                        this.direction = false;
                    }
                }

                this.setX(this.getX()+this.speed);
            }else{
                gameInstance.willCollideWithAnObstacle("right",this.speed,this);
                gameInstance.willCollideWithAnObstacle("left",this.speed,this);

                if (this.getX() <= finalFromX){
                    this.direction = true;
                }
                this.setX(this.getX()-this.speed);
            }
        }));
        horizontalTimeline.setCycleCount(Animation.INDEFINITE);
        horizontalTimeline.play();
    }

    private EventHandler<Event> buildEvent() {
        return e -> {
            if (gameInstance.endOfLevel){
                verticalTimeline.stop();
                horizontalTimeline.stop();
            }else{
                if (e.getEventType() == GazeEvent.GAZE_ENTERED || e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    this.setFill(Color.BLUE);
                    verticalTimeline.pause();
                    horizontalTimeline.pause();
                }
                if (e.getEventType() == GazeEvent.GAZE_EXITED || e.getEventType() == MouseEvent.MOUSE_EXITED){
                    this.setFill(Color.RED);
                    horizontalTimeline.play();
                    verticalTimeline.play();
                }
            }
        };
    }




}

