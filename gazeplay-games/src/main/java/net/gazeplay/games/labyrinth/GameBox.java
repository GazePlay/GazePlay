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

    private double x;
    private double y;
    protected int numCol; // j
    protected int numRow; // i

    private boolean isAWall;

    public boolean wasNextToTheMouse;

    public boolean visited;

    public GameBox(double height, double width, double coordX, double coordY, int wall, int nc, int nr) {

        this.x = coordX;
        this.y = coordY;
        this.numCol = nc;
        this.numRow = nr;

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
            r.setFill(colorWall);
        } else {
            isAWall = false;
            r.setFill(colorBackground);
        }
        this.getChildren().addAll(r, indicator);
    }

    public GameBox(int x, int y, boolean wall, boolean visit) {
        numCol = x;
        numRow = y;
        if (visit) {
            visited = true;
        } else {
            visited = false;
        }
        isAWall = wall;

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
        indicator.setTranslateX(this.x + width * 0.05);
        indicator.setTranslateY(this.y + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

    public GameBox clone(Boolean b) {
        return new GameBox(this.numCol, this.numRow, this.isAWall, b);
    }

    public boolean equals(GameBox g) {
        return (this.numCol == g.numCol && this.numRow == g.numRow);
    }

}
