package net.gazeplay.games.labyrinth;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class Labyrinth extends Parent implements GameLifeCycle {

    GameContext gameContext;
    private final Stats stats;

    private Rectangle[][] walls;

    private final int nbCasesLignes = 10;
    private final int nbCasesColonne = 15;

    private final Color colorWall = Color.MAROON;
    private final Color colorBackground = Color.BEIGE;

    public Labyrinth(GameContext gameContext, Stats stats) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);

    }

    @Override
    public void launch() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        double x = dimension2D.getWidth() * 0.2;
        double y = dimension2D.getHeight() * 0.05;
        double width = dimension2D.getWidth() * 0.6;
        double height = dimension2D.getHeight() * 0.9;
        Rectangle recJeu = new Rectangle(x, y, width, height);
        recJeu.setFill(colorBackground);

        gameContext.getChildren().add(recJeu);

        this.walls = creationLabyrinth(recJeu, dimension2D);

        fillWalls();

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    private Rectangle[][] creationLabyrinth(Rectangle recTotal, Dimension2D dim2D) {

        // Rectangle[numero de ligne][numero de colonne]
        Rectangle[][] rec = new Rectangle[nbCasesLignes][nbCasesColonne];

        double heigth = recTotal.getHeight() / nbCasesLignes;
        double width = recTotal.getWidth() / nbCasesColonne;

        for (int i = 0; i < nbCasesLignes; i++) { // Pour chaque ligne

            for (int j = 0; j < nbCasesColonne; j++) { // Pour chaque colonne
                Rectangle r = new Rectangle();
                r.setHeight(heigth);
                r.setWidth(width);
                r.setY(recTotal.getY() + i * heigth);
                r.setX(recTotal.getX() + j * width);
                rec[i][j] = r;
            }
        }

        return rec;
    }

    // 1 if there is a wall, 0 otherwise
    private int[][] constructionWallMatrix() {
        int[][] tab = { { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };
        return tab;

    }

    private void fillWalls() {

        for (int i = 0; i < 5; i++) {
            walls[i][1].setFill(colorWall);
            walls[4][1 + i].setFill(colorWall);
            walls[4 + i][3].setFill(colorWall);
            walls[1 + i][5].setFill(colorWall);
            walls[1 + i][9].setFill(colorWall);
        }

        for (int i = 0; i < 3; i++) {
            walls[8][1 + i].setFill(colorWall);
            walls[9][6 + i].setFill(colorWall);
            walls[7 + i][8].setFill(colorWall);
            walls[3][9 + i].setFill(colorWall);
            walls[1 + i][11].setFill(colorWall);
            walls[7][11 + i].setFill(colorWall);
            walls[5 + i][13].setFill(colorWall);
            walls[1 + i][14].setFill(colorWall);
        }
        walls[6][0].setFill(colorWall);
        walls[1][4].setFill(colorWall);
        walls[2][6].setFill(colorWall);
        walls[6][5].setFill(colorWall);
        walls[6][6].setFill(colorWall);
        walls[2][8].setFill(colorWall);
        walls[5][8].setFill(colorWall);
        walls[2][13].setFill(colorWall);
        walls[4][14].setFill(colorWall);

        for (int i = 0; i < nbCasesLignes; i++) {
            for (int j = 0; j < nbCasesColonne; j++) {
                if (!isAWall(i, j)) {
                    walls[i][j].setFill(colorBackground);
                }
                gameContext.getChildren().add(this.walls[i][j]);
            }
        }

    }

    // Return true is the case Walls[i][j] is not a wall, false if it's a wall
    public boolean isAWall(int i, int j) {
        return (walls[i][j].getFill() == colorWall);
    }

}
