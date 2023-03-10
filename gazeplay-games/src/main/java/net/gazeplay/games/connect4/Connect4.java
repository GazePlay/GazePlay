package net.gazeplay.games.connect4;

import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class Connect4 implements GameLifeCycle {
    private final Stats stats;
    private final IGameContext gameContext;

    private final int nbRows = 6;
    private final int nbColumns = 7;
    private double gridWidth;
    private double gridHeight;
    private double gridXOffset;
    private double gridYOffset;
    private double cellSize;
    private int[][] grid;

    Connect4(final IGameContext gameContext, final Stats stats){
        this.stats = stats;
        this.gameContext = gameContext;
        grid = new int[nbColumns][nbRows];
        grid[5][2] = 2;
        grid[4][2] = 1;
        grid[3][2] = 2;
        grid[2][2] = 1;
    }

    Connect4(final IGameContext gameContext, final Stats stats, double gameSeed){
        this.stats = stats;
        this.gameContext = gameContext;
    }

    @Override
    public void launch() {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        cellSize = Math.min(dimension2D.getWidth()*0.8/nbColumns, dimension2D.getHeight()*0.8/nbRows);

        gridWidth = cellSize*nbColumns;
        gridHeight = cellSize*nbRows;
        gridXOffset = (dimension2D.getWidth() - cellSize*nbColumns)/2;
        gridYOffset = (dimension2D.getHeight() - cellSize*nbRows)/2;
        Rectangle r = new Rectangle(gridXOffset,gridYOffset,gridWidth,gridHeight);
        r.setFill(Color.BLUE);
        gameContext.getChildren().add(r);

        updateGrid();
//        Circle circle = new Circle((dimension2D.getWidth() - cellSize*0.4)/2,(dimension2D.getHeight() - cellSize*0.4)/2,cellSize*0.4);
//        circle.setFill(Color.ORANGE);
//        gameContext.getChildren().add(circle);
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    public void clearGrid(){
        for(int i = 0; i<nbColumns; i++){
            for(int j = 0; j<nbRows; j++){
                grid[i][j] = 0;
            }
        }
    }

    public void updateGrid(){
        Color col;
        for(int i = 0; i<nbColumns; i++){
            for(int j = 0; j<nbRows; j++){
                switch(grid[i][j]){
                    case 0:
                        col = Color.WHITE;
                        break;
                    case 1:
                        col = Color.ORANGE;
                        break;
                    case 2:
                        col = Color.RED;
                        break;
                    default:
                        col = Color.WHITE;
                }
                double radius = cellSize*0.4;
//                double x = gridXOffset + i*cellSize + (cellSize - radius*2)/2 + radius;
//                double y = gridYOffset + j*cellSize + (cellSize - radius*2)/2 + radius;
                double x = gridXOffset + i*cellSize + 0.5*cellSize;
                double y = gridYOffset + j*cellSize + 0.5*cellSize;

                Circle c = new Circle(x,y,radius);
                c.setFill(col);
                gameContext.getChildren().add(c);
            }
        }
    }


}
