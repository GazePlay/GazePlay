package net.gazeplay.games.labyrinth;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.Random;

class Cheese extends Parent {

    private final Rectangle cheese;
    private final Labyrinth gameInstance;

    private int indexY;
    private int indexX;

    private Random r;

    Cheese(final double entiereRecX, final double entiereRecY, final double width, final double height, final Labyrinth gameInstance) {
        this.gameInstance = gameInstance;
        cheese = new Rectangle(entiereRecX, entiereRecY, width, height);
        cheese.setFill(new ImagePattern(new Image("data/labyrinth/images/cheese.png"), 5, 5, 1, 1, true));
        indexY = 0;
        indexX = 0;
        cheese.setMouseTransparent(true);
    }

    void beginCheese() {
        r = new Random();
        moveCheese();
        this.getChildren().add(cheese);
    }

    void moveCheese() {
        int x, y;
        do {
            y = r.nextInt(gameInstance.nbBoxesLine);
            x = r.nextInt(gameInstance.nbBoxesColumns);
        } while (!gameInstance.isFreeForCheese(y, x));

        indexY = y;
        indexX = x;

        final double coordX = gameInstance.positionX(indexX) - gameInstance.adjustmentCaseWidth;
        final double coordY = gameInstance.positionY(indexY) + gameInstance.adjustmentCaseHeight;

        cheese.setX(coordX);
        cheese.setY(coordY);
        cheese.setMouseTransparent(true);

    }

    boolean isTheCheese(final int i, final int j) {
        return (i == indexY && j == indexX);
    }

}
