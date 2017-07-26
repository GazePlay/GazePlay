package utils.games;

import gaze.GazeUtils;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.io.File;

/**
 * Created by schwab on 12/08/2016.
 */
public class Portrait extends Circle {

    protected static final int maxX = 1000;
    protected static final int maxY = 500;
    protected int radius = 100;

    private final Image[] photos = images();

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

    protected Image[] images(){

        String folder = System.getProperties().getProperty("user.home")+Utils.FILESEPARATOR+"GazePlay"+Utils.FILESEPARATOR+"files"+Utils.FILESEPARATOR+"images"+Utils.FILESEPARATOR+"portraits";

        if((new File(folder)).exists()) {

            return Utils.getImages(folder);
        }
        else{

            Image[] defaultImages = new Image[10];
            defaultImages[0] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/animal-807308_1920.png"));
            defaultImages[1] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/bulldog-1047518_1920.jpg"));
            defaultImages[2] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/businessman-607786_1920.png"));
            defaultImages[3] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/businessman-607834_1920.png"));
            defaultImages[4] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/crocodile-614386_1920.png"));
            defaultImages[5] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/goldfish-30837_1280.png"));
            defaultImages[6] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/graphic_missbone17.gif"));
            defaultImages[7] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/nurse-37322_1280.png"));
            defaultImages[8] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/owl-161583_1280.png"));
            defaultImages[9] = new Image(ClassLoader.getSystemResourceAsStream("data/common/default/images/pez-payaso-animales-el-mar-pintado-por-teoalmeyra-9844979.jpg"));
            return defaultImages;
        }


    }

}

