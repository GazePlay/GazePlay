package net.gazeplay.games.labyrinth;

import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

public class GameBox extends Parent {

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

    GameBox(final double height, final double width, final double coordX, final double coordY, final int wall, final int numCol, final int numRow) {

        this.x = coordX;
        this.y = coordY;
        this.numCol = numCol;
        this.numRow = numRow;

        final Rectangle r = new Rectangle();
        if (wall == 0) {
            r.setStrokeWidth(0.5);
            r.setStroke(Color.DARKORANGE);
        }
        r.setHeight(height);
        r.setWidth(width);
        r.setY(coordY);
        r.setX(coordX);

        indicator = createProgressIndicator(width, height);

        if (wall == 1) {
            isAWall = true;
            final Color colorWall = Color.MAROON;
            r.setFill(colorWall);
        } else {
            isAWall = false;
            final Color colorBackground = Color.BEIGE;
            r.setFill(colorBackground);
        }
        this.getChildren().addAll(r, indicator);
    }

    boolean isAWall() {
        return isAWall;
    }

    boolean isNextTo(final int i, final int j) {
        final boolean nextToInRow = (i == numRow) && (j == numCol - 1 || j == numCol + 1);
        final boolean nextToInCol = (j == numCol) && (i == numRow - 1 || i == numRow + 1);
        return (nextToInRow || nextToInCol);
    }

    private ProgressIndicator createProgressIndicator(final double width, final double height) {
        final ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(this.x + width * 0.05);
        indicator.setTranslateY(this.y + height * 0.2);
        indicator.setMinWidth(width * 0.9);
        indicator.setMinHeight(width * 0.9);
        indicator.setOpacity(0);
        return indicator;
    }

}
