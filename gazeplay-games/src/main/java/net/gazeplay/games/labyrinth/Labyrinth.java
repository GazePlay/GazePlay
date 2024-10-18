package net.gazeplay.games.labyrinth;

import javafx.animation.PauseTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class Labyrinth extends Parent implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    public final double fixationlength;

    private GameBox[][] walls;
    private int[][] wallsPlacement;

    int nbBoxesLine = 4;
    int nbBoxesColumns = 6;
    int iteration = 1;
    int actualSeed;

    double entiereRecX;
    double entiereRecY;
    double entiereRecWidth;
    double entiereRecHeight;

    double caseHeight;
    double caseWidth;
    double adjustmentCaseWidth;
    double adjustmentCaseHeight;

    private Cheese cheese;
    private Mouse mouse;

    private final LabyrinthGameVariant variant;

    private final ReplayablePseudoRandom randomGenerator;
    private List<Integer> listAnim = new ArrayList<Integer>();

    private boolean doAnim;

    public Labyrinth(final IGameContext gameContext, final Stats stats, final LabyrinthGameVariant variant) {
        super();

        this.gameContext = gameContext;
        this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.stats = stats;

        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());

        this.variant = variant;
        final Configuration config = gameContext.getConfiguration();
        fixationlength = config.getFixationLength();
    }

    public Labyrinth(final IGameContext gameContext, final Stats stats, final LabyrinthGameVariant variant, double gameSeed) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;

        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        this.variant = variant;
        final Configuration config = gameContext.getConfiguration();
        fixationlength = config.getFixationLength();
    }

    public GameBox getBoxAt(final int i, final int j) {
        return walls[i][j];
    }

    public void prepareLaunch(){
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

    @Override
    public void launch() {
        this.prepareLaunch();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final Rectangle recJeu = new Rectangle(entiereRecX, entiereRecY, entiereRecWidth, entiereRecHeight);
        gameContext.getChildren().add(recJeu);

        this.wallsPlacement = constructionWallMatrix();
        walls = creationLabyrinth();

        // Creation of cheese
        cheese = new Cheese(entiereRecX, entiereRecY, caseHeight, caseHeight * 0.8, this, randomGenerator);
        mouse = createMouse();
        gameContext.getChildren().add(mouse);

        // launch of cheese
        cheese.beginCheese();
        gameContext.getChildren().add(cheese);
        gameContext.start();
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    private Mouse createMouse() {
        // Creation of the mouse
        Mouse mouse = new MouseArrowsV2(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
        mouse.setImage();
        return mouse;
    }

    public void nextLvl(){
        if (this.iteration < 7){
            this.iteration ++;
            this.gameContext.getChildren().clear();
            this.nbBoxesColumns += 1;
            this.nbBoxesLine += 1;
            this.launch();
        }else {
            this.dispose();
        }

    }
    @Override
    public void dispose() {
        this.gameContext.getChildren().clear();
        this.gameContext.showRoundStats(stats, this);
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

    public int generateSeed(){
        Random seed = new Random();
        return seed.nextInt(2);
    }

    private int[][] constructionWallMatrix() {
        this.actualSeed =  this.generateSeed();
        switch (this.iteration){
            case 1:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 0, 0, 0, 0, 1},
                            {0, 1, 1, 1, 0, 0},
                            {0, 0, 0, 1, 1, 1},
                            {1, 1, 0, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0},
                            {0, 0, 0, 1, 1, 0},
                            {1, 1, 0, 1, 1, 0},
                            {1, 0, 0, 0, 1, 0}
                        };
                }

            case 2:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 1, 0},
                            {0, 1, 0, 1, 0, 0, 0},
                            {0, 0, 0, 1, 1, 1, 1},
                            {0, 1, 0, 1, 0, 0, 0},
                            {0, 1, 0, 0, 0, 1, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 0, 0, 0, 0, 0, 0},
                            {0, 1, 1, 1, 1, 0, 1},
                            {0, 1, 0, 0, 0, 0, 0},
                            {0, 0, 0, 1, 0, 1, 0},
                            {1, 1, 1, 0, 0, 1, 0}
                        };
                }

            case 3:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 0, 0, 0},
                            {0, 1, 0, 0, 0, 1, 1, 0},
                            {0, 1, 1, 1, 0, 0, 0, 1},
                            {0, 0, 0, 0, 0, 1, 0, 0},
                            {1, 1, 1, 1, 1, 1, 1, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 0, 0, 0, 1, 1},
                            {0, 1, 1, 1, 1, 0, 1, 1},
                            {0, 0, 0, 1, 1, 0, 0, 0},
                            {0, 1, 0, 0, 1, 1, 0, 1},
                            {1, 1, 1, 0, 1, 0, 0, 0}
                        };
                }

            case 4:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 1, 0, 0, 0},
                            {0, 1, 1, 1, 0, 0, 0, 1, 0},
                            {0, 1, 0, 0, 0, 1, 1, 1, 1},
                            {0, 1, 0, 1, 0, 0, 0, 0, 0},
                            {0, 0, 0, 1, 1, 1, 0, 1, 1},
                            {0, 1, 1, 0, 0, 0, 0, 1, 1},
                            {0, 0, 0, 0, 1, 1, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 0, 0, 0, 0, 1, 1, 1, 1},
                            {0, 1, 1, 1, 0, 1, 0, 0, 0},
                            {0, 0, 0, 1, 0, 0, 0, 1, 0},
                            {1, 1, 0, 1, 1, 1, 1, 0, 0},
                            {1, 0, 0, 0, 1, 1, 1, 0, 1},
                            {1, 0, 1, 0, 0, 1, 1, 0, 1},
                            {0, 0, 1, 1, 0, 1, 0, 0, 0}
                        };
                }

            case 5:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0, 0, 1, 0, 0},
                            {0, 0, 0, 1, 1, 1, 0, 0, 0, 1},
                            {0, 1, 0, 0, 0, 0, 1, 1, 1, 0},
                            {0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
                            {0, 1, 1, 1, 1, 1, 1, 0, 1, 1},
                            {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1},
                            {1, 1, 0, 0, 0, 0, 0, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0, 0, 1, 1, 1},
                            {0, 0, 0, 1, 1, 1, 0, 0, 1, 1},
                            {0, 1, 1, 0, 0, 1, 1, 0, 1, 1},
                            {0, 0, 0, 0, 0, 1, 1, 0, 0, 0},
                            {0, 1, 1, 1, 0, 1, 1, 0, 1, 1},
                            {0, 1, 0, 0, 0, 1, 1, 0, 0, 0},
                            {1, 1, 0, 1, 0, 0, 0, 1, 1, 0},
                            {1, 1, 0, 1, 1, 1, 0, 1, 0, 0}
                        };
                }

            case 6:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 1},
                            {0, 1, 0, 0, 0, 0, 1, 1, 1, 0, 0},
                            {0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 0},
                            {0, 1, 1, 1, 0, 1, 0, 0, 0, 1, 1},
                            {0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                            {0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0},
                            {1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0},
                            {1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                            {0, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1},
                            {0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1},
                            {0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0},
                            {0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1},
                            {0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1},
                            {0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1},
                            {1, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0}
                        };
                }

            case 7:
                if (this.actualSeed == 0){
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
                            {0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1},
                            {0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0},
                            {0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1},
                            {0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0},
                            {0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0},
                            {0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 1, 0},
                            {0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0}
                        };
                }else {
                    return new int[][]
                        {
                            {0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
                            {0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1},
                            {0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0},
                            {0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1},
                            {0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},
                            {0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0},
                            {1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0},
                            {0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0},
                            {0, 1, 1, 0, 1, 1, 1, 1, 0, 0, 0, 0}
                        };
                }

            default:
                return new int[][]
                    {
                        {0, 0},
                        {0, 0}
                    };
        }
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
            stats.incrementNumberOfGoalsReached();
            gameContext.updateScore(stats, this);
            cheese.moveCheese();
            stats.incrementNumberOfGoalsToReach();
            mouse.nbMove = 0;
            this.nextLvl();
        }
    }
}
