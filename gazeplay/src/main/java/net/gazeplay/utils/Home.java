package net.gazeplay.utils;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

/**
 * Created by schwab on 22/12/2016.
 */
public class Home extends Rectangle{

    public Home(double X, double Y, double width, double heigth){

        super(X, Y, width, heigth);

        this.setFill(new ImagePattern(new Image("data/common/images/home-button.png"),0,0,1,1, true));
    }
}
