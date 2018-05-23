package net.gazeplay.games.labyrinth;

import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameBox extends Parent {

    private final Color colorWall = Color.MAROON;
    private final Color colorBackground = Color.BEIGE;

    protected Rectangle r;

    protected ProgressIndicator indicator;

    protected double x;
    protected double y;
    protected int numCol; // j
    protected int numRow; // i

    private boolean isAWall;

    public boolean wasNextToTheMouse;

    public GameBox(double height, double width, double coordX, double coordY, int wall, int nc, int nr) {

        x = coordX;
        y = coordY;
        numCol = nc;
        numRow = nr;

        r = new Rectangle();
        r.setHeight(height);
        r.setWidth(width);
        r.setY(coordY);
        r.setX(coordX);

        indicator = createProgressIndicator(width, height);

        if (wall == 1) {
            isAWall = true;
            r.setFill(colorWall);
        } else {
            isAWall = false;
            r.setFill(colorBackground);
        }
        this.getChildren().addAll(r, indicator);
    }

    public boolean isAWall() {
        return isAWall;
    }

    public boolean isNextTo(int i, int j) {
        boolean nextToInRow = (i == numRow) && (j == numCol - 1 || j == numCol + 1);
        boolean nextToInCol = (j == numCol) && (i == numRow - 1 || i == numRow + 1);
        return (nextToInRow || nextToInCol);
    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(x + width * 0.05);
        indicator.setTranslateY(y + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        indicator.setStyle("-fx-fill:null");
        return indicator;
    }

}
