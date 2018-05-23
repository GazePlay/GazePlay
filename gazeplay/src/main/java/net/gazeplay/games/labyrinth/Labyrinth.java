package net.gazeplay.games.labyrinth;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class Labyrinth extends Parent implements GameLifeCycle {

    GameContext gameContext;
    private final Stats stats;

    private Rectangle[][] walls;
    private int[][] wallsPlacement;

    protected final int nbCasesLignes = 10;
    protected final int nbCasesColonne = 15;

    private final Color colorWall = Color.MAROON;
    private final Color colorBackground = Color.BEIGE;

    private final double entiereRecX;
    private final double entiereRecY;
    private final double entiereRecWidth;
    private final double entiereRecHeigth;

    private final double caseHeigth;
    private final double caseWidth;
    double adjustmentCaseWidth;
    double adjustmentCaseHeight;

    private Rectangle cheese;
    private Mouse mouse;

    public Labyrinth(GameContext gameContext, Stats stats) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);

        entiereRecX = dimension2D.getWidth() * 0.2;
        entiereRecY = dimension2D.getHeight() * 0.05;
        entiereRecWidth = dimension2D.getWidth() * 0.6;
        entiereRecHeigth = dimension2D.getHeight() * 0.9;

        caseWidth = entiereRecWidth / nbCasesColonne;
        caseHeigth = entiereRecHeigth / nbCasesLignes;
        adjustmentCaseWidth = caseWidth / 4;
        adjustmentCaseHeight = caseHeigth / 4;

    }

    @Override
    public void launch() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final Configuration config = Configuration.getInstance();

        Rectangle recJeu = new Rectangle(entiereRecX, entiereRecY, entiereRecWidth, entiereRecHeigth);
        recJeu.setFill(colorBackground);
        gameContext.getChildren().add(recJeu);

        this.wallsPlacement = constructionWallMatrix();
        this.walls = creationLabyrinth(recJeu, dimension2D);
        fillWalls();

        // Creation of the mouse
        mouse = new Mouse(entiereRecX - adjustmentCaseWidth, entiereRecY + adjustmentCaseHeight,
                dimension2D.getWidth() / 15, dimension2D.getHeight() / 15, gameContext, stats, this);
        gameContext.getChildren().add(mouse);

        // Creation of cheese
        Random r = new Random();
        cheese = new Rectangle(entiereRecX, entiereRecY, dimension2D.getWidth() / 15, dimension2D.getHeight() / 15);
        cheese.setFill(new ImagePattern(new Image("data/labyrinth/images/cheese.png"), 5, 5, 1, 1, true));
        moveCheese(r);
        gameContext.getChildren().add(cheese);

        Timer minuteur = new Timer();
        TimerTask tache = new TimerTask() {
            public void run() {
                moveCheese(r);
            }
        };

        minuteur.schedule(tache, 0, 2000);

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    protected double positionX(int j) {
        double x = entiereRecX + j * caseWidth - adjustmentCaseWidth;
        return x;
    }

    protected double positionY(int i) {
        double y = entiereRecY + i * caseHeigth + adjustmentCaseHeight;
        return y;
    }

    private Rectangle[][] creationLabyrinth(Rectangle recTotal, Dimension2D dim2D) {

        // Rectangle[numero de ligne][numero de colonne]
        Rectangle[][] rec = new Rectangle[nbCasesLignes][nbCasesColonne];

        for (int i = 0; i < nbCasesLignes; i++) { // Pour chaque ligne

            for (int j = 0; j < nbCasesColonne; j++) { // Pour chaque colonne
                Rectangle r = new Rectangle();
                r.setHeight(caseHeigth);
                r.setWidth(caseWidth);
                r.setY(entiereRecY + i * caseHeigth);
                r.setX(entiereRecX + j * caseWidth);
                rec[i][j] = r;
            }
        }

        return rec;
    }

    // 1 if there is a wall, 0 otherwise
    private int[][] constructionWallMatrix() {
        int[][] tab = { { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1 }, { 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1 },
                { 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 1 }, { 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1 },
                { 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0 }, { 1, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0 },
                { 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0 }, { 0, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0 } };
        return tab;

    }

    private void fillWalls() {

        for (int i = 0; i < nbCasesLignes; i++) {
            for (int j = 0; j < nbCasesColonne; j++) {
                if (wallsPlacement[i][j] == 1) {
                    walls[i][j].setFill(colorWall);
                } else {
                    walls[i][j].setFill(colorBackground);
                }
                gameContext.getChildren().add(this.walls[i][j]);
            }
        }
    }

    // Return true is the case Walls[i][j] is a wall, false if it is not a wall
    public boolean isAWall(int i, int j) {
        return (wallsPlacement[i][j] == 1);
    }

    private void moveCheese(Random r) {
        int x, y;
        do {
            y = r.nextInt(nbCasesLignes);
            x = r.nextInt(nbCasesColonne);
        } while (isAWall(y, x));

        double coordX = positionX(x);
        double coordY = positionY(y);

        cheese.setX(coordX);
        cheese.setY(coordY);

    }

}
