package net.gazeplay.games.goosegame;

import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;


public class GooseGame implements GameLifeCycle {

    private static final int WIDTH = 9;
    private static final int HEIGHT = 7;

    private final GameContext gameContext;
    private final Stats stats;
    private final Dimension2D dimensions;
    private final Configuration config;
    private final int nbPlayers;

    private GridPane grid;

    public GooseGame(GameContext gameContext, Stats stats, int nbPlayers) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbPlayers = nbPlayers;

        this.dimensions = gameContext.getGamePanelDimensionProvider().getDimension2D();
        this.config = Configuration.getInstance();

        grid = new GridPane();
        double squareWidth = WIDTH * dimensions.getWidth() < HEIGHT * dimensions.getHeight() ? dimensions.getWidth() / WIDTH : dimensions.getHeight() / HEIGHT;

        //Generate spiral grid
        int x = 0;
        int y = HEIGHT - 1;
        int dx = 1;
        int dy = 0;
        int minX = 0;
        int minY = 0;
        int maxX = WIDTH - 1;
        int maxY = HEIGHT - 2;

        Polyline spiralBorder = new Polyline();
        spiralBorder.getPoints().addAll(0., squareWidth*HEIGHT,
                squareWidth*WIDTH, squareWidth*HEIGHT,
                squareWidth*WIDTH, 0.,
                0., 0.,
                0., squareWidth*(HEIGHT - 1));

        Square previousSquare = null;
        for(int i = 1; i < WIDTH * HEIGHT + 1; i++){
            Square newSquare = new Square(i, squareWidth-2, previousSquare);
            if(previousSquare != null){
                previousSquare.setNextSquare(newSquare);
            }
            grid.add(newSquare, x, y);
            previousSquare = newSquare;

            x += dx;
            y += dy;

            if(dx == 1 && x == maxX){
                maxX--;
                dy = -1;
                dx = 0;
                spiralBorder.getPoints().add(x * squareWidth);
                spiralBorder.getPoints().add(y * squareWidth);
            }else if(dy == -1 && y == minY){
                minY++;
                dy = 0;
                dx = -1;
                spiralBorder.getPoints().add(x * squareWidth);
                spiralBorder.getPoints().add((y + 1) * squareWidth);
            }else if(dx == -1 && x == minX){
                minX++;
                dy = 1;
                dx = 0;
                spiralBorder.getPoints().add((x + 1) * squareWidth);
                spiralBorder.getPoints().add((y + 1) * squareWidth);
            }else if(dy == 1 && y == maxY){
                maxY--;
                dy = 0;
                dx = 1;
                spiralBorder.getPoints().add((x + 1) * squareWidth);
                spiralBorder.getPoints().add(y * squareWidth);
            }
        }

        BorderPane bpane = new BorderPane();
        bpane.setCenter(grid);

        spiralBorder.setStrokeWidth(5);
        spiralBorder.setStroke(Color.RED);

        gameContext.getChildren().addAll(bpane, spiralBorder);
    }

    @Override
    public void launch() {

    }

    @Override
    public void dispose() {

    }
}
