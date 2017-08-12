package utils.games;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

/**
 * Created by schwab on 12/08/2016.
 */
public class Portrait extends Circle {

    protected static final int maxX = 1000;
    protected static final int maxY = 500;
    protected int radius = 100;

    private final static Image[] photos = Utils.images(System.getProperties().getProperty("user.home")+ Utils.FILESEPARATOR +"GazePlay"+ Utils.FILESEPARATOR +"files"+ Utils.FILESEPARATOR +"images"+ Utils.FILESEPARATOR +"portraits");

    public Portrait(int radius) {

        super(radius);
        this.setPosition(newX(),newY());
        this.radius = radius;
        setFill(new ImagePattern(newPhoto(),0,0,1,1, true));
    }

    public void setPosition(double X, double Y){

        this.setCenterX(X);
        this.setCenterY(Y);
    }

    protected Image newPhoto(){

        return photos[((int)(photos.length*Math.random()))];

    }

    protected int newX(){

        return (int)(Math.random()*maxX)+radius;
    }

    protected int newY(){

        return (int)(Math.random()*maxY*2/3)+radius;
    }

}

