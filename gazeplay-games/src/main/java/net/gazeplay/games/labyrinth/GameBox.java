package net.gazeplay.games.labyrinth;

import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

public class GameBox extends Parent {

    private final Rectangle r;

    @Getter
    private final ProgressIndicator indicator;

    private final double x;

    private final double y;

    protected final int numCol; // j

    protected final int numRow; // i

    private final boolean isAWall;

    @Getter
    @Setter
    private boolean nextToTheMouse;

    GameBox(double height, double width, double coordX, double coordY, int wall, int numCol, int numRow) {

        this.x = coordX;
        this.y = coordY;
        this.numCol = numCol;
        this.numRow = numRow;

        this.r = new Rectangle();
        if (wall == 0) {
            this.r.setStrokeWidth(0.5);
            this.r.setStroke(Color.DARKORANGE);
        }
        this.r.setHeight(height);
        this.r.setWidth(width);
        this.r.setY(coordY);
        this.r.setX(coordX);

        indicator = createProgressIndicator(width, height);

        if (wall == 1) {
            isAWall = true;
            Color colorWall = Color.MAROON;
            r.setFill(colorWall);
        } else {
            isAWall = false;
            Color colorBackground = Color.BEIGE;
            r.setFill(colorBackground);
        }
        this.getChildren().addAll(r, indicator);
    }

    boolean isAWall() {
        return isAWall;
    }

    boolean isNextTo(int i, int j) {
        boolean nextToInRow = (i == numRow) && (j == numCol - 1 || j == numCol + 1);
        boolean nextToInCol = (j == numCol) && (i == numRow - 1 || i == numRow + 1);
        return (nextToInRow || nextToInCol);
    }

    private ProgressIndicator createProgressIndicator(double width, double height) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(this.x + width * 0.05);
        indicator.setTranslateY(this.y + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

}
