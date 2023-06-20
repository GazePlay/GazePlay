package net.gazeplay.games.TowerDefense;

public class Map {

    static final int START = 8;
    static final int END = 9;
    static final int GRASS = 0;
    static final int ROAD = 1;
    static final int TURRET = 2;

    private int [][] map;
    private int nbCols;
    private int nbRows;
    private double screenWidth;
    private double screenHeight;
    private int tileWidth;
    private int tileHeight;

    private int startCol;
    private int startRow;


    public Map(int level){
        initLevel(level);
    }

    private void initLevel(int level){
        switch (level){
            case 1:
                initLevel1();
                break;
            default:
                initLevel1();
                break;
        }

        nbRows = map.length;
        nbCols = map[0].length;

        for (int row = 0; row < nbRows; row++) {
            for (int col = 0; col < nbCols; col++) {
                if(map[row][col]==START){
                    startCol = col;
                    startRow = row;
                }
            }
        }
    }

    private void initLevel1(){
        map = new int[][] {
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,0,0,},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,},
            {0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,1,0,0,},
            {0,0,0,0,1,2,0,2,0,1,0,0,0,0,0,0,0,1,2,0,},
            {0,0,0,0,1,0,0,0,0,1,0,0,1,1,1,0,0,1,1,1,},
            {0,0,0,2,1,0,0,0,0,1,2,0,1,0,1,0,0,0,0,1,},
            {9,1,1,1,1,0,0,0,0,1,0,0,1,0,1,0,2,1,1,1,},
            {0,0,2,0,0,0,0,0,0,1,0,2,1,0,1,0,0,1,0,0,},
            {0,0,0,0,0,0,0,0,0,1,1,1,1,0,1,1,1,1,0,0,},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,},
        };
    }

    public int[][] getMap(){
        return  map;
    }

    public int getTile(double x, double y){
        int row = (int) (y/tileHeight);
        int col = (int) (x/tileWidth);
        if(row<0 || row>=nbRows || col<0 || col>=nbCols){
            return GRASS;
        }

        return map[row][col];
    }

    public int getTileAbove(double x, double y){
        int row = (int) (y/tileHeight) - 1;
        if(row<0){
            return GRASS;
        }
        int col = (int) (x/tileWidth);

        return map[row][col];
    }

    public int getTileLeft(double x, double y){
        int row = (int) (y/tileHeight);
        int col = (int) (x/tileWidth) - 1;
        if(col<0){
            return GRASS;
        }

        return map[row][col];
    }

    public double getStartX() {
        return startCol*tileWidth;
    }

    public double getStartY() {
        return startRow*tileHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setScreenWidth(double screenWidth) {
        this.screenWidth = screenWidth;
        tileWidth = (int) (screenWidth/nbCols);
    }

    public void setScreenHeight(double screenHeight) {
        this.screenHeight = screenHeight;
        tileHeight = (int) (screenHeight/nbRows);
    }

}
