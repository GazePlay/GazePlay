package net.gazeplay.games.gazeRace;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;

public class Player extends Rectangle {

    protected double speed;
    private GazeRace gameInstance;
    private final IGameContext gameContext;
    protected int health;
    protected boolean isDead;
    private Rectangle target;
    private final EventHandler<Event> enterEvent;
    protected boolean isInvincible;
    protected AnimationTimer playerAnimationMovement;
    //private ImagePattern playerImage = new ImagePattern(new Image(""));
    private boolean direction;

    public Player(double x, double y, double width, double height,  GazeRace gameInstance, IGameContext gameContext, double speed, int health, boolean direction) {
        super(x, y, width, height);
        this.gameInstance = gameInstance;
        this.gameContext = gameContext;
        this.speed = speed;
        this.target = new Rectangle(x,y,10,10);
        this.health = health;
        this.isDead = false;
        this.isInvincible = false;
        this.direction = direction;
        if (direction){
            setFill(new ImagePattern(new Image("data/gazeRace/carH.png")));
        }else{
            setFill(new ImagePattern(new Image("data/gazeRace/carV.png")));
        }

        final Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(100.0);
        lighting.setSpecularConstant(0.0);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);
        lighting.setLight(new Light.Distant(30, 30, Color.BLUE));

        this.setEffect(lighting);

        this.enterEvent = buildEvent();
        gameContext.getPrimaryScene().addEventFilter(GazeEvent.ANY, enterEvent);
        gameContext.getPrimaryScene().addEventFilter(MouseEvent.ANY, enterEvent);

        playerAnimationMovement = new AnimationTimer() {
            int nbframe = 0;

            @Override
            public void handle(long now) {

                if(!isDead){

                    if(health == 0){
                        isDead = true;
                    }
                    playerMoveMouse();
                    if(nbframe == 60){
                        nbframe = 0;

                    }
                }else{
                    this.stop();
                }


                nbframe++;
            }
        };

        playerAnimationMovement.start();

    }

    protected void startTimerInvincible(){

        isInvincible = true;

        AnimationTimer invincibleAnimation = new AnimationTimer() {
            int nbframes = 0;
            int nbSecond = 0;
            boolean blink = true;
            @Override
            public void handle(long now) {

                if (nbframes == 20 || nbframes == 40 || nbframes == 60){
                    if (blink){
                        setFill(Color.TRANSPARENT);
                        blink = false;
                    }else{
                        if (direction){
                            setFill(new ImagePattern(new Image("data/gazeRace/carH.png")));
                        }else{
                            setFill(new ImagePattern(new Image("data/gazeRace/carV.png")));
                        }
                        blink = true;
                    }
                }


                if (nbSecond == 4){
                    isInvincible = false;
                    if (direction){
                        setFill(new ImagePattern(new Image("data/gazeRace/carH.png")));
                    }else{
                        setFill(new ImagePattern(new Image("data/gazeRace/carV.png")));
                    }
                    stop();
                }


                if (nbframes == 60){
                    nbframes = 0;
                    nbSecond++;
                }
                nbframes++;
            }
        };
        invincibleAnimation.start();
    }

    private EventHandler<Event> buildEvent(){
        return e -> {

            if (e.getEventType() == GazeEvent.GAZE_MOVED ){
                this.target.setX(((GazeEvent)e).getX());
                this.target.setY(((GazeEvent)e).getY());
            }else if (e.getEventType() == MouseEvent.MOUSE_MOVED){
                this.target.setX(((MouseEvent)e).getX());
                this.target.setY(((MouseEvent)e).getY());
            }
        };
    }

    /**
     * Moves the player towards their target when controlled by the mouse.
     */
    private void playerMoveMouse(){
        double dx = target.getX() - this.getX()- this.getWidth()/2;
        double dy = target.getY() - this.getY()- this.getHeight()/2;

        double distance = Math.sqrt(dx * dx + dy * dy);

        if(distance > speed) {
            double vx = dx / distance * speed;
            double vy = dy / distance * speed;

            String directionX = vx > 0 ? "right" : "left";
            String directionY = vy > 0 ? "down" : "up";

            if(Math.abs(vx) > Math.abs(vy)) {


                if(!gameInstance.willCollideWithAnObstacle(directionX, speed, this)) {
                    this.setX(this.getX() + vx);
                }
                if(!gameInstance.willCollideWithAnObstacle(directionY, speed, this)) {
                    this.setY(this.getY() + vy);
                }
            } else {

                if(!gameInstance.willCollideWithAnObstacle(directionY, speed, this)) {
                    this.setY(this.getY() + vy);
                }
                if(!gameInstance.willCollideWithAnObstacle(directionX, speed, this)) {
                    this.setX(this.getX() + vx);
                }
            }
        }else{
            gameInstance.willCollideWithAnObstacle(" ", speed, this);
        }
    }

}
