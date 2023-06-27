package net.gazeplay.games.gazeRace;

import javafx.animation.AnimationTimer;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;

import java.util.ArrayList;

public class Car extends Rectangle {


    protected boolean isDestroyed;
    private String direction;
    protected double speed;
    public Car(double x, double y, double width, double height, double speed, String direction, Color c, Enum<GazeRaceVariant> gameVariant) {
        super(x, y, width, height);

        this.isDestroyed = false;
        this.speed = speed;
        this.direction = direction;






        if (gameVariant.equals(GazeRaceVariant.HORIZONTAL)){
            if (direction.compareToIgnoreCase("right") == 0 || direction.compareToIgnoreCase("left") == 0){
                setFill(new ImagePattern(new Image("data/gazeRace/carH.png")));
                if (direction.compareToIgnoreCase("left") == 0){
                    this.setRotate(180);
                }
            }
        }else{
            if (direction.compareToIgnoreCase("up") == 0 || direction.compareToIgnoreCase("down") == 0){
                setFill(new ImagePattern(new Image("data/gazeRace/carV.png")));
                if (direction.compareToIgnoreCase("up") == 0){
                    this.setRotate(180);
                }
            }
        }

        final Lighting lighting = new Lighting();
        lighting.setDiffuseConstant(100.0);
        lighting.setSpecularConstant(0.0);
        lighting.setSpecularExponent(0.0);
        lighting.setSurfaceScale(0.0);
        lighting.setLight(new Light.Distant(30, 30, c));

        this.setEffect(lighting);

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
