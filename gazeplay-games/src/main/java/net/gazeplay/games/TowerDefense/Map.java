package net.gazeplay.games.TowerDefense;

import javafx.geometry.Point2D;

import java.util.ArrayList;

public class Map {

    static final int START = 8;
    static final int END = 9;
    static final int GRASS = 0;
    static final int ROAD = 1;
    static final int TURRET = 2;

    private final ArrayList<Point2D> turretsTiles;
    private int [][] map;
    private int nbCols;
    private int nbRows;
    private int startCol;
    private int startRow;

    public Map(int level){
        turretsTiles = new ArrayList<>();
        initLevel(level);
    }

    private void initLevel(int level){
        switch (level) {
            case 1 -> initLevel1();
            default -> initLevel1();
        }

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

    private void initLevel1(){
        // Look at the static variables above to understand the map
        map = new int[][] {
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,0,0,},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,},
            {0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,1,0,0,},
            {0,0,0,0,1,2,0,2,0,1,0,0,0,2,0,0,0,1,2,0,},
            {0,0,0,0,1,0,0,0,0,1,0,0,1,1,1,0,0,1,1,1,},
            {0,0,0,2,1,0,0,0,0,1,0,0,1,0,1,0,0,0,0,1,},
            {9,1,1,1,1,0,0,0,0,1,0,0,1,0,1,0,2,1,1,1,},
            {0,0,2,0,0,0,0,0,0,1,0,2,1,0,1,0,0,1,0,0,},
            {0,0,0,0,0,0,0,0,0,1,1,1,1,0,1,1,1,1,0,0,},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        };
    }

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
