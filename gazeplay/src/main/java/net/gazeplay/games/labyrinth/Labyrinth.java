package net.gazeplay.games.labyrinth;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;

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
    public final double fixationlength;

    protected GameBox[][] walls;
    private int[][] wallsPlacement;

    protected final int nbCasesLignes = 10;
    protected final int nbCasesColonne = 15;

    private final double entiereRecX;
    private final double entiereRecY;
    private final double entiereRecWidth;
    private final double entiereRecHeigth;

    final double caseHeight;
    final double caseWidth;
    final double adjustmentCaseWidth;
    final double adjustmentCaseHeight;

    protected Cheese cheese;
    private Mouse mouse;

    public Labyrinth(GameContext gameContext, Stats stats) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;
        Configuration config = Configuration.getInstance();
        fixationlength = config.getFixationLength();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);

        entiereRecX = dimension2D.getWidth() * 0.2;
        entiereRecY = dimension2D.getHeight() * 0.05;
        entiereRecWidth = dimension2D.getWidth() * 0.6;
        entiereRecHeigth = dimension2D.getHeight() * 0.9;

        caseWidth = entiereRecWidth / nbCasesColonne;
        caseHeight = entiereRecHeigth / nbCasesLignes;
        adjustmentCaseWidth = caseWidth / 4;
        adjustmentCaseHeight = caseHeight / 4;

    }

    @Override
    public void launch() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final Configuration config = Configuration.getInstance();

        Rectangle recJeu = new Rectangle(entiereRecX, entiereRecY, entiereRecWidth, entiereRecHeigth);
        // recJeu.setFill(colorBackground);
        gameContext.getChildren().add(recJeu);

        this.wallsPlacement = constructionWallMatrix();
        creationLabyrinth(recJeu, dimension2D);

        /*
         * choice of versions 0 : La souris n'avance que d'une case à le fois, elle va a la case ou le joueur regarde 1
         * : 4 fleches regroupées à droite du labyrinth 2 : 4 fleches regroupees à gauche du labyrinth 3 : 4 fleches
         * entourant la souris (celle pour aller en haut au dessus de la souris) 4 : 4 fleches entourant le labyrinth 5
         * : Regarder la souris puis la souris va suivre les deplacements du regard. Notion de "prendre" et de "poser"
         * la souris
         */

        int version = 0;

        // Creation of cheese
        cheese = new Cheese(entiereRecX, entiereRecY, dimension2D.getWidth() / 15, dimension2D.getHeight() / 15, this);

        // Creation of the mouse
        mouse = new MouseV0(entiereRecX - adjustmentCaseWidth, entiereRecY + adjustmentCaseHeight, caseWidth * 0.8,
                caseHeight * 0.8, gameContext, stats, this);
        gameContext.getChildren().add(mouse);

        // launch of cheese
        cheese.beginCheese();
        gameContext.getChildren().add(cheese);

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
        double y = entiereRecY + i * caseHeight + adjustmentCaseHeight;
        return y;
    }

    private void creationLabyrinth(Rectangle recTotal, Dimension2D dim2D) {
        walls = new GameBox[nbCasesLignes][nbCasesColonne];
        for (int i = 0; i < nbCasesLignes; i++) { // i = rows number = Coord Y
            for (int j = 0; j < nbCasesColonne; j++) { // j = columns number = Coord X
                GameBox g = new GameBox(caseHeight, caseWidth, entiereRecX + j * caseWidth,
                        entiereRecY + i * caseHeight, wallsPlacement[i][j], j, i);
                walls[i][j] = g;
                gameContext.getChildren().add(this.walls[i][j]);
            }
        }
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

    public boolean isFreeForMouse(int i, int j) {
        return (!walls[i][j].isAWall());
    }

    public boolean isFreeForCheese(int i, int j) {
        return (!walls[i][j].isAWall() && !mouse.isTheMouse(i, j));
    }

    public void testIfCheese(int i, int j) {
        if (cheese.isTheCheese(i, j)) {
            stats.incNbGoals();
            stats.notifyNewRoundReady();
            cheese.moveCheese();
        }
    }

}
