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
        this.stats.setCurrentGameSeed(randomGenerator.getSeed());

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

    public Labyrinth(final IGameContext gameContext, final Stats stats, final LabyrinthGameVariant variant, double gameSeed) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;

        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

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
        gameContext.setLimiterAvailable();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final Rectangle recJeu = new Rectangle(entiereRecX, entiereRecY, entiereRecWidth, entiereRecHeight);
        gameContext.getChildren().add(recJeu);

        chooseAnim();

        this.wallsPlacement = constructionWallMatrix();
        walls = creationLabyrinth();

        if (doAnim) {
            //The animation of the creation of the labyrinthe
            int delay = 20;     //The delay between each dig (in ms)
            anim(delay);
        } else {
            // Creation of cheese
            cheese = new Cheese(entiereRecX, entiereRecY, dimension2D.getWidth() / 15, dimension2D.getHeight() / 15, this, randomGenerator);
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
            case ANLOOK_DESTINATION:
                return new MouseTransparentArrows(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            case ANLOOK_LOCAL_ARROWS:
                return new MouseArrowsV2(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            case ANLOOK_GLOBAL_ARROWS:
                return new MouseArrowsV3(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            case ANSELECT_THEN_LOOK_DESTINATION:
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
        /*return new int[][]
            {
                {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1},
                {0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1},
                {0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0},
                {1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            };

         */
        //Initialisation
        int[][] lab = new int[nbBoxesLine][nbBoxesColumns];
        int[][] ret = new int[nbBoxesLine][nbBoxesColumns];
        for (int i = 0; i < nbBoxesLine; i++) {
            for (int j = 0; j < nbBoxesColumns; j++) {
                lab[i][j] = 1;
                ret[i][j] = 1;
            }
        }
        lab[0][0] = 0;
        ret[0][0] = 0;

        //Goals to dig
        int nbObj = (int) (1.5 * Math.cbrt(nbBoxesColumns * nbBoxesLine));
        int objX, objY;     //My goal to reach
        int robX, robY;     //My digger robot
        int r;              //My rand var
        int rmax = 10;      //My bond of my rand var
        int noise = (int) (Math.sqrt(nbObj));
        for (int i = 0; i < nbObj; i++) {
            objX = (int) (nbBoxesLine * (i % (int) (Math.sqrt(nbObj)) / (Math.sqrt(nbObj) - 2) + (randomGenerator.nextDouble() - 0.5) / noise));
            if (objX < 0) {
                objX = 0;
            }
            if (objX >= nbBoxesLine) {
                objX = nbBoxesLine - 1;
            }
            objY = (int) (nbBoxesColumns * ((int) (i / Math.sqrt(nbObj)) / (Math.sqrt(nbObj) - 2) + (randomGenerator.nextDouble() - 0.5) / noise));
            if (objY < 0) {
                objY = 0;
            }
            if (objY >= nbBoxesColumns) {
                objY = nbBoxesColumns - 1;
            }
            robX = objX;
            robY = objY;
            while (lab[robX][robY] == 1) {
                robX = randomGenerator.nextInt(nbBoxesLine);
                robY = randomGenerator.nextInt(nbBoxesColumns);
            }
            while (lab[objX][objY] == 1) {
                r = randomGenerator.nextInt(rmax);
                if (r == 0) {    //Je m'éloigne en x
                    if (robX > objX) {
                        if (robX != nbBoxesLine - 1) {
                            robX++;
                        }
                    } else {
                        if (robX != 0) {
                            robX--;
                        }
                    }
                } else if (r == 1) {   //Je m'éloigne en y
                    if (robY > objY) {
                        if (robY != nbBoxesColumns - 1) {
                            robY++;
                        }
                    } else {
                        if (robY != 0) {
                            robY--;
                        }
                    }
                } else if (r < rmax / 2 + 1) {    //Je m'approche en x
                    if (robX > objX) {
                        if (robX != 0) {
                            robX--;
                        }
                    } else {
                        if (robX != nbBoxesLine - 1) {
                            robX++;
                        }
                    }
                } else {      //Je m'approche en y
                    if (robY > objY) {
                        if (robY != 0) {
                            robY--;
                        }
                    } else {
                        if (robY != nbBoxesColumns - 1) {
                            robY++;
                        }
                    }
                }
                if (lab[robX][robY] == 1) {
                    listAnim.add(robX);
                    listAnim.add(robY);
                }
                lab[robX][robY] = 0;
            }
        }

        //Return
        if (doAnim) {
            return ret;
        }
        return lab;
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
        }
    }

    void anim(int delay) {
        if (!listAnim.isEmpty()) {
            PauseTransition wait = new PauseTransition(Duration.millis(delay));
            wait.setOnFinished(waitEvent -> {
                int x = listAnim.remove(0);
                int y = listAnim.remove(0);
                wallsPlacement[x][y] = 0;
                final GameBox g = new GameBox(caseHeight, caseWidth, entiereRecX + y * caseWidth,
                    entiereRecY + x * caseHeight, wallsPlacement[x][y], y, x);
                walls[x][y] = g;
                gameContext.getChildren().add(g);
                anim(delay);
            });
            wait.play();
        } else {
            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

            // Creation of cheese
            cheese = new Cheese(entiereRecX, entiereRecY, dimension2D.getWidth() / 15, dimension2D.getHeight() / 15, this, randomGenerator);
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
    }

    private void chooseAnim() {
        doAnim = variant.getLabel().startsWith("Anime");
    }

}
