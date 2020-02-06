package net.gazeplay.games.labyrinth;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class Labyrinth extends Parent implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    public final double fixationlength;

    private GameBox[][] walls;
    private int[][] wallsPlacement;

    final int nbBoxesLine = 7;
    final int nbBoxesColumns = 12;

    final double entiereRecX;
    final double entiereRecY;
    final double entiereRecWidth;
    final double entiereRecHeight;

    final double caseHeight;
    final double caseWidth;
    final double adjustmentCaseWidth;
    final double adjustmentCaseHeight;

    private Cheese cheese;
    private Mouse mouse;

    private final LabyrinthGameVariant variant;

    public Labyrinth(final IGameContext gameContext, final Stats stats, final LabyrinthGameVariant variant) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;
        final Configuration config = gameContext.getConfiguration();
        fixationlength = config.getFixationLength();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.debug("dimension2D = {}", dimension2D);

        entiereRecX = dimension2D.getWidth() * 0.25;
        entiereRecY = dimension2D.getHeight() * 0.15;
        entiereRecWidth = dimension2D.getWidth() * 0.6;
        entiereRecHeight = dimension2D.getHeight() * 0.7;

        caseWidth = entiereRecWidth / nbBoxesColumns;
        caseHeight = entiereRecHeight / nbBoxesLine;
        adjustmentCaseWidth = caseWidth / 6;
        adjustmentCaseHeight = caseHeight / 6;

    }

    public GameBox getBoxAt(final int i, final int j) {
        return walls[i][j];
    }

    @Override
    public void launch() {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final Rectangle recJeu = new Rectangle(entiereRecX, entiereRecY, entiereRecWidth, entiereRecHeight);
        gameContext.getChildren().add(recJeu);

        this.wallsPlacement = constructionWallMatrix();
        walls = creationLabyrinth();

        // Creation of cheese
        cheese = new Cheese(entiereRecX, entiereRecY, dimension2D.getWidth() / 15, dimension2D.getHeight() / 15, this);
        mouse = createMouse();

        gameContext.getChildren().add(mouse);

        // launch of cheese
        cheese.beginCheese();
        gameContext.getChildren().add(cheese);

        stats.notifyNewRoundReady();
    }

    private Mouse createMouse() {
        // Creation of the mouse
        switch (variant) {
            case LOOK_DESTINATION:
                return new MouseTransparentArrows(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            case LOOK_LOCAL_ARROWS:
                return new MouseArrowsV2(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            case LOOK_GLOBAL_ARROWS:
                return new MouseArrowsV3(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            case SELECT_THEN_LOOK_DESTINATION:
                return new MouseV4(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            default:
                throw new IllegalArgumentException("Unsupported variant ID");
        }
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    protected double positionX(final int j) {
        return entiereRecX + j * caseWidth;
    }

    protected double positionY(final int i) {
        return entiereRecY + i * caseHeight;
    }

    private GameBox[][] creationLabyrinth() {
        final GameBox[][] walls = new GameBox[nbBoxesLine][nbBoxesColumns];
        for (int i = 0; i < nbBoxesLine; i++) { // i = rows number = Coord Y
            for (int j = 0; j < nbBoxesColumns; j++) { // j = columns number = Coord X
                final GameBox g = new GameBox(caseHeight, caseWidth, entiereRecX + j * caseWidth,
                    entiereRecY + i * caseHeight, wallsPlacement[i][j], j, i);
                walls[i][j] = g;
                gameContext.getChildren().add(g);
            }
        }
        return walls;
    }

    private int[][] constructionWallMatrix() {
        return new int[][]
            {
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1},
                {0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1},
                {0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
                {1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            };
    }

    boolean isFreeForMouse(final int i, final int j) {
        if (i >= nbBoxesLine || j >= nbBoxesColumns) {
            return false;
        }
        return (!walls[i][j].isAWall());
    }

    boolean isFreeForCheese(final int i, final int j) {
        return (!walls[i][j].isAWall() && !mouse.isTheMouse(i, j));
    }

    void testIfCheese(final int i, final int j) {
        if (cheese.isTheCheese(i, j)) {
            stats.incNbGoals();
            stats.notifyNewRoundReady();
            cheese.moveCheese();
            mouse.nbMove = 0;
        }
    }

}
