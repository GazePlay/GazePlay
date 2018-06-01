package net.gazeplay.games.labyrinth;

import java.util.Random;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

public class Cheese extends Parent {

    public Rectangle cheese;
    private final Labyrinth gameInstance;

    public int indexY;
    public int indexX;

    private int tour; // Only for the user test

    private Random r;

    public boolean alreadyCheese;

    public Cheese(double entiereRecX, double entiereRecY, double width, double height, Labyrinth gameInstance) {

        this.gameInstance = gameInstance;
        alreadyCheese = false;
        cheese = new Rectangle(entiereRecX, entiereRecY, width, height);
        cheese.setFill(new ImagePattern(new Image("data/labyrinth/images/cheese.png"), 5, 5, 1, 1, true));
        indexY = 0;
        indexX = 0;
        cheese.setMouseTransparent(true);
        tour = 0; // Only for the user test
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

        indexY = y;
        indexX = x;

        if (tour == 0) {
            indexY = 5;
            indexX = 4;
        } else if (tour == 1) {
            indexY = 2;
            indexX = 4;
        } else if (tour == 2) {
            indexY = 3;
            indexX = 8;
        }
        tour++;

        double coordX = gameInstance.positionX(indexX) - gameInstance.adjustmentCaseWidth;
        double coordY = gameInstance.positionY(indexY) + gameInstance.adjustmentCaseHeight;

        cheese.setX(coordX);
        cheese.setY(coordY);
        cheese.setMouseTransparent(true);

    }

    public boolean isTheCheese(int i, int j) {
        return (i == indexY && j == indexX);
    }

}
