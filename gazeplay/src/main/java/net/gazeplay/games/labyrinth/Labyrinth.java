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
    public int bestScore;

    protected GameBox[][] walls;
    private int[][] wallsPlacement;

    protected final int nbBoxesLine = 7;
    protected final int nbBoxesColumns = 12;

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

    protected int version;

    public Labyrinth(GameContext gameContext, Stats stats, int version) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;
        this.version = version;
        Configuration config = Configuration.getInstance();
        fixationlength = config.getFixationLength();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
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
            mouse = new MouseTransparentArrows(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext,
                    stats, this);
            break;
        case 2:
            mouse = new MouseArrowsV2(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            break;
        case 3:
            mouse = new MouseArrowsV3(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            break;
        case 4:
            mouse = new MouseV4(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
            break;
        default:
            mouse = new MouseV4(entiereRecX, entiereRecY, caseWidth, caseHeight * 0.8, gameContext, stats, this);
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
        walls = new GameBox[nbBoxesLine][nbBoxesColumns];
        for (int i = 0; i < nbBoxesLine; i++) { // i = rows number = Coord Y
            for (int j = 0; j < nbBoxesColumns; j++) { // j = columns number = Coord X
                GameBox g = new GameBox(caseHeight, caseWidth, entiereRecX + j * caseWidth,
                        entiereRecY + i * caseHeight, wallsPlacement[i][j], j, i);
                walls[i][j] = g;
                gameContext.getChildren().add(this.walls[i][j]);
            }
        }
    }

    /*
     * public GameBox[][] constructionOfStructure() { GameBox[][] gb = new GameBox[nbCasesLignes][nbCasesColonne]; for
     * (int i = 0; i < nbCasesLignes; i++) { for (int j = 0; j < nbCasesColonne; j++) { gb[i][j] =
     * walls[i][j].clone(false); } } return gb; }
     */

    private int[][] constructionWallMatrix() {
        int[][] tab = { { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1 },
                { 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1 }, { 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1 },
                { 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0 },
                { 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0 }, };
        return tab;

    }

    // 1 if there is a wall, 0 otherwise
    /*
     * private int[][] constructionWallMatrix() { int[][] tab = { { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0,
     * 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1 }, { 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1, 0, 1, 1 }, { 0, 1, 0, 0, 0, 1,
     * 0, 0, 0, 1, 1, 1, 0, 0, 1 }, { 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1 }, { 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0,
     * 0, 0, 1, 0 }, { 1, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0 }, { 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 1, 1, 0 }, {
     * 0, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0 } }; return tab;
     * 
     * }
     */

    public boolean isFreeForMouse(int i, int j) {
        if (i >= nbBoxesLine || j >= nbBoxesColumns) {
            return false;
        }
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
            System.out.println("bestScore : " + bestScore + "\nyour score : " + mouse.nbMove + "\n difference "
                    + (mouse.nbMove - bestScore));
            mouse.nbMove = 0;
        }
    }

    // public int min(int i1, int i2, int i3, int i4, int i5) {
    // return Math.min(i1, Math.min(i2, Math.min(i3, Math.min(i4, i5))));
    // }

    /*
     * public int chemin(GameBox deb, GameBox end, GameBox[][] gb, int val) { System.out.println("indice Ligne " +
     * deb.numRow + "   indice Col: " + deb.numCol);
     * 
     * // gb[deb.numRow][deb.numCol].visited = true; int val0 = 1000, val1 = 1000, val2 = 1000, val3 = 1000, val4 =
     * 1000;
     * 
     * if (deb.equals(end)) { System.out.println("Passage ");
     * 
     * return val; } if (deb.numCol == end.numCol && deb.numRow == end.numRow) {
     * System.out.println("C'est quand meme treschelou"); val0 = val; }
     * 
     * if (deb.numCol < nbCasesColonne - 1 && isFreeForMouse(deb.numRow, deb.numCol + 1)) { GameBox[][] gb1 =
     * copyGameBox(gb); GameBox g = gb1[deb.numRow][deb.numCol + 1]; if (!gb1[deb.numRow][deb.numCol + 1].visited) {
     * 
     * gb1[deb.numRow][deb.numCol + 1].visited = true; val1 = chemin(gb1[deb.numRow][deb.numCol + 1], end, gb1, val +
     * 1); } if (gb[deb.numRow][deb.numCol + 1].visited) { System.out.println("C'est un probleme 1 _________________");
     * 
     * }
     * 
     * } if (deb.numCol > 0 && isFreeForMouse(deb.numRow, deb.numCol - 1)) { GameBox[][] gb2 = copyGameBox(gb); GameBox
     * g = gb2[deb.numRow][deb.numCol - 1]; if (!gb2[deb.numRow][deb.numCol - 1].visited) { gb2[deb.numRow][deb.numCol -
     * 1].visited = true; val2 = chemin(gb2[deb.numRow][deb.numCol - 1], end, gb2, val + 1); } if
     * (gb[deb.numRow][deb.numCol - 1].visited) { System.out.println("C'est un probleme 2_________________");
     * 
     * } } if (deb.numRow < nbCasesLignes - 1 && isFreeForMouse(deb.numRow + 1, deb.numCol)) { GameBox[][] gb3 =
     * copyGameBox(gb); GameBox g = gb3[deb.numRow + 1][deb.numCol]; if (!gb3[deb.numRow + 1][deb.numCol].visited) {
     * 
     * gb3[deb.numRow + 1][deb.numCol].visited = true; val3 = chemin(gb3[deb.numRow + 1][deb.numCol], end, gb3, val +
     * 1); } if (gb[deb.numRow + 1][deb.numCol].visited) { System.out.println("C'est un probleme 3_________________");
     * 
     * } } if (deb.numRow > 0 && isFreeForMouse(deb.numRow - 1, deb.numCol)) { GameBox[][] gb4 = copyGameBox(gb);
     * GameBox g = gb4[deb.numRow - 1][deb.numCol]; if (!gb4[deb.numRow - 1][deb.numCol].visited) { gb4[deb.numRow -
     * 1][deb.numCol].visited = true; val4 = chemin(gb4[deb.numRow - 1][deb.numCol], end, gb4, val + 1); } if
     * (gb[deb.numRow - 1][deb.numCol].visited) { System.out.println("C'est un probleme 4 _________________");
     * 
     * } } return min(val0, val1, val2, val3, val4); // } }
     */

    /*
     * private int[][] dijkstra(GameBox deb) { int[][] distances = new int[nbCasesLignes][nbCasesColonne];
     * ArrayList<GameBox> gb = new ArrayList<>(); for (int i = 0; i < nbCasesLignes; i++) { for (int j = 0; j <
     * nbCasesColonne; j++) { gb.add(new GameBox(j, i, walls[i][j].isAWall(), false)); } }
     * 
     * while (!gb.isEmpty()) { } return distances; }
     * 
     * private GameBox[][] copyGameBox(GameBox[][] g) { GameBox[][] gb = new GameBox[nbCasesLignes][nbCasesColonne]; for
     * (int i = 0; i < nbCasesLignes; i++) { for (int j = 0; j < nbCasesColonne; j++) { gb[i][j] = new GameBox(j, i,
     * g[i][j].isAWall(), g[i][j].visited); } } return gb; }
     */

}
