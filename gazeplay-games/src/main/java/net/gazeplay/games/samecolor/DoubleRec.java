package net.gazeplay.games.samecolor;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DoubleRec {

    public Rectangle rec1;
    public Rectangle rec2;

    public Color color;

    public boolean isin1;
    public boolean isin2;

    DoubleRec(double x1, double y1, double x2, double y2, double width, double height, Color color){
        this.color = color;
        rec1 = new Rectangle(x1, y1, width, height);
        rec2 = new Rectangle(x2, y2, width, height);
        rec1.setFill(color);
        rec2.setFill(color);
        isin1 = false;
        isin2 = false;
    }
}
