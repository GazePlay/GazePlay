package net.gazeplay.games.TowerDefense;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public abstract class Map {

    public static final int START = 8;
    public static final int END = 9;
    public static final int GRASS = 0;
    public static final int ROAD = 1;
    public static final int TURRET = 2;

    private final ArrayList<Point2D> turretsTiles;
    protected int [][] map;
    private int nbCols;
    private int nbRows;
    private int startCol;
    private int startRow;

    public Map(){
        turretsTiles = new ArrayList<>();
        initLevel();
    }

    private void initLevel(){
        setStructure();
        nbRows = map.length;
        nbCols = map[0].length;

        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbCols; col++) {
                if(map[row][col]==START){
                    startCol = col;
                    startRow = row;
                } else if (map[row][col]==TURRET){
                    turretsTiles.add(new Point2D(col, row));
                }
            }
        }
    }
    // Look at the static variables above to understand a map
    public abstract void setStructure();

    public ArrayList<Point2D> getTurretsTiles() {
        return turretsTiles;
    }

    public int getTile(int col, int row){
        if(row<0 || row>=nbRows || col<0 || col>=nbCols){
            return GRASS;
        }
        return map[row][col];
    }

    public int getTileAbove(int col, int row){
        row = row - 1;
        return getTile(col, row);
    }

    public int getTileLeft(int col, int row){
        col = col - 1;
        return getTile(col, row);
    }

    public int getStartCol() {
        return startCol;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getNbCols() {
        return nbCols;
    }

    public int getNbRows() {
        return nbRows;
    }
}
