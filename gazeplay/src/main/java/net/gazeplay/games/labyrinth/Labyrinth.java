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

    protected final double entiereRecX;
    protected final double entiereRecY;
    protected final double entiereRecWidth;
    protected final double entiereRecHeight;

    final double caseHeight;
    final double caseWidth;
    final double adjustmentCaseWidth;
    final double adjustmentCaseHeight;

    protected Cheese cheese;
    private Mouse mouse;

    private int version;

    public Labyrinth(GameContext gameContext, Stats stats, int version) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;
        this.version = version;
        Configuration config = Configuration.getInstance();
        fixationlength = config.getFixationLength();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);

        entiereRecX = dimension2D.getWidth() * 0.2;
        entiereRecY = dimension2D.getHeight() * 0.05;
        // entiereRecX = dimension2D.getWidth() * 0.25;
        // entiereRecY = dimension2D.getHeight() * 0.1;
        entiereRecWidth = dimension2D.getWidth() * 0.6;
        entiereRecHeight = dimension2D.getHeight() * 0.9;

        caseWidth = entiereRecWidth / nbCasesColonne;
        caseHeight = entiereRecHeight / nbCasesLignes;
        adjustmentCaseWidth = caseWidth / 4;
        adjustmentCaseHeight = caseHeight / 4;

    }

    @Override
    public void launch() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle recJeu = new Rectangle(entiereRecX, entiereRecY, entiereRecWidth, entiereRecHeight);
        gameContext.getChildren().add(recJeu);

        this.wallsPlacement = constructionWallMatrix();
        creationLabyrinth(recJeu, dimension2D);

        // Creation of cheese
        cheese = new Cheese(entiereRecX, entiereRecY, dimension2D.getWidth() / 15, dimension2D.getHeight() / 15, this);

        // Creation of the mouse
        switch (version) {
        case 0:
            mouse = new MouseV0(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            break;
        case 1:
            mouse = new MouseArrowsV1(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            break;
        case 2:
            mouse = new MouseArrowsV2(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            break;
        case 3:
            mouse = new MouseArrowsV3(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            break;
        case 4:
            mouse = new MouseArrowsV2(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            break;
        default:
            mouse = new MouseV0(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            break;
        }

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
        double x = entiereRecX + j * caseWidth; // - adjustmentCaseWidth;
        return x;
    }

    protected double positionY(int i) {
        double y = entiereRecY + i * caseHeight;// + adjustmentCaseHeight;
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
