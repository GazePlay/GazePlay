package net.gazeplay.games.labyrinth;

import java.util.Random;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class Cheese extends Parent {

    public Rectangle cheese;
    private final Labyrinth gameInstance;

    public int cheeseNumRow;
    public int cheeseNumCol;

    private Random r;

    public boolean alreadyCheese;

    public Cheese(double entiereRecX, double entiereRecY, double width, double height, Labyrinth gameInstance) {

        this.gameInstance = gameInstance;
        alreadyCheese = false;
        cheese = new Rectangle(entiereRecX, entiereRecY, width, height);
        cheese.setFill(new ImagePattern(new Image("data/labyrinth/images/cheese.png"), 5, 5, 1, 1, true));
        cheeseNumRow = 0;
        cheeseNumCol = 0;
        cheese.setMouseTransparent(true);
    }

    public void beginCheese() {
        r = new Random();
        moveCheese();
        this.getChildren().add(cheese);
    }

    public void moveCheese() {
        int x, y;
        do {
            y = r.nextInt(gameInstance.nbCasesLignes);
            x = r.nextInt(gameInstance.nbCasesColonne);
        } while (!gameInstance.isFreeForCheese(y, x));

        cheeseNumRow = y;
        cheeseNumCol = x;

        double coordX = gameInstance.positionX(x) - gameInstance.adjustmentCaseWidth;
        double coordY = gameInstance.positionY(y) + gameInstance.adjustmentCaseHeight;

        cheese.setX(coordX);
        cheese.setY(coordY);
        cheese.setMouseTransparent(true);

    }

    public boolean isTheCheese(int i, int j) {
        return (i == cheeseNumRow && j == cheeseNumCol);
    }

}
