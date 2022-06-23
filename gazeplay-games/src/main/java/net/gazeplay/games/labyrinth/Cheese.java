package net.gazeplay.games.labyrinth;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import net.gazeplay.commons.random.ReplayablePseudoRandom;

class Cheese extends Parent {

    private final Rectangle cheese;
    private final Labyrinth gameInstance;

    private int indexY;
    private int indexX;
    boolean isCheese;

    private ReplayablePseudoRandom randomGenerator;

    Cheese(final double entiereRecX, final double entiereRecY, final double width, final double height, final Labyrinth gameInstance, ReplayablePseudoRandom random) {
        this.gameInstance = gameInstance;
        this.isCheese=true;
        cheese = new Rectangle(entiereRecX, entiereRecY, width, height);
        //cheese.setFill(new ImagePattern(new Image("data/labyrinth/images/cheese.png"), 5, 5, 1, 1, true));
        indexY = 0;
        indexX = 0;
        cheese.setMouseTransparent(true);
        this.randomGenerator = random;
    }
    void setToHouse()
    {
        this.isCheese=false;
    }
    void beginCheese() {
        moveCheese();
        if(this.isCheese)
        {
            cheese.setFill(new ImagePattern(new Image("data/labyrinth/images/cheese.png"), 5, 5, 1, 1, true));
        }
        else
        {
            cheese.setFill(new ImagePattern(new Image("data/labyrinth/images/house.png"), 5, 5, 1, 1, true));
        }
        this.getChildren().add(cheese);
    }

    void moveCheese() {
        int x, y;
        do {
            y = randomGenerator.nextInt(gameInstance.nbBoxesLine);
            x = randomGenerator.nextInt(gameInstance.nbBoxesColumns);
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
