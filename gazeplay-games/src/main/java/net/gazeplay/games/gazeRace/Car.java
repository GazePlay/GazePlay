package net.gazeplay.games.gazeRace;

import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;

public class Car extends Rectangle {

    private IGameContext gameContext;
    private GazeRace gameInstance;
    protected boolean isDestroyed;
    private String direction;
    protected double speed;
    public Car(double x, double y, double width, double height, IGameContext gameContext, GazeRace gameInstance, double speed, String direction) {
        super(x, y, width, height);
        this.gameContext = gameContext;
        this.gameInstance = gameInstance;
        this.isDestroyed = false;
        this.speed = speed;
        this.direction = direction;
        setFill(Color.RED);
        moveCar(this.direction);
    }

    private void moveCar(String direction){

        AnimationTimer carAnimation = new AnimationTimer() {
            int nbframes = 0;
            @Override
            public void handle(long now) {

                if (nbframes == 60){
                    nbframes = 0;
                }
                gameInstance.willCollideWithAnObstacle(direction,speed,Car.this);
                if (!isDestroyed){


                    if (direction.compareToIgnoreCase("left") == 0){
                        setX(getX()-speed);
                    }else if (direction.compareToIgnoreCase("right") == 0){
                        setX(getX()+speed);
                    }else if (direction.compareToIgnoreCase("down") == 0){
                        setY(getY() - speed);
                    } else if (direction.compareToIgnoreCase("up") == 0){
                        setY(getY()+speed);
                    }
                }else{
                    this.stop();
                }

                nbframes++;
            }
        };

        carAnimation.start();

    }

}
