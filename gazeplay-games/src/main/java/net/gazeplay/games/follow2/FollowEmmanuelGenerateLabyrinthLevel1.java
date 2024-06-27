package net.gazeplay.games.follow2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class FollowEmmanuelGenerateLabyrinthLevel1 {

    private IGameContext gameContext;
    public Stats stats;
    public FollowEmmanuel followEmmanuel;
    private ArrayList<EventItemEmmanuel> listEI;
    private ArrayList<Rectangle> listWall;
    private double sizeWw, sizeWh;
    private EventHandler<ActionEvent> eventwin;
    private int[][] newMap;
    public Rectangle doorRED;

    public FollowEmmanuelGenerateLabyrinthLevel1(){}

    public void initiateLevel(){
        this.newMap = new int[][]{
          // 0  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        };
    }

    public int[][] generateLabyrinth(
        IGameContext gameContext,
        ArrayList<EventItemEmmanuel> listEI,
        ArrayList<Rectangle> listWall,
        double sizeWw,
        double sizeWh,
        EventHandler<ActionEvent> eventwin,
        Stats stats,
        FollowEmmanuel followEmmanuel){

        this.gameContext = gameContext;
        this.stats = stats;
        this.followEmmanuel = followEmmanuel;
        this.listEI = listEI;
        this.sizeWw = sizeWw;
        this.sizeWh = sizeWh;
        this.eventwin = eventwin;
        this.listWall = listWall;

        this.initiateLevel();
        newMap = this.choiceStart(newMap);
        newMap = this.placeEnd(newMap);
        return newMap;
    }

    public int[][] choiceStart(int[][] map){
        int nbRandom = new Random().nextInt(4);

        switch (nbRandom){
            case 0:
                map[7][13] = 0;
                map[8][13] = 0;
                break;
            case 1:
                map[6][14] = 0;
                map[6][15] = 0;
                break;
            case 2:
                map[7][16] = 0;
                map[8][16] = 0;
                break;
            case 3:
                map[9][14] = 0;
                map[9][15] = 0;
                break;
            default:
                log.info("Error for starter !");
                break;
        }

        return map;
    }

    public int[][] placeEnd(int[][] map){
        int nbRandom = new Random().nextInt(4);
        int x, y;

        switch (nbRandom){
            case 0:
                x = new Random().nextInt(4);
                y = new Random().nextInt(7);
                for (int i=0; i<=(4+x); i++){
                    map[i][4+y] = 1;
                }
                for (int j=0; j<=(4+y); j++){
                    map[4+x][j] = 1;
                }
                this.setupRubyDiagonallyTopLeftToBottomRight((4+x), (4+y));
                this.setupDoorTopLeft(map, (4+x), (4+y));
                this.setupCenterBottomKey(map);
                break;
            case 1:
                x = new Random().nextInt(4);
                y = new Random().nextInt(7);
                for (int i=0; i<=(4+x); i++){
                    map[i][25-y] = 1;
                }
                for (int j=(25-y); j<=29; j++){
                    map[4+x][j] = 1;
                }
                this.setupRubyDiagonallyTopRightToBottomLeft((4+x), (4+y));
                this.setupDoorTopRight(map, (4+x), (4+y));
                this.setupCenterBottomKey(map);
                break;
            case 2:
                x = new Random().nextInt(4);
                y = new Random().nextInt(7);
                for (int i=(11-x); i<=15; i++){
                    map[i][4+y] = 1;
                }
                for (int j=0; j<=(4+y); j++){
                    map[11-x][j] = 1;
                }
                this.setupRubyDiagonallyBottomLeftToTopRight((4+x), (4+y));
                this.setupDoorBottomLeft(map, (4+x), (4+y));
                this.setupCenterTopKey(map);
                break;
            case 3:
                x = new Random().nextInt(4);
                y = new Random().nextInt(7);
                for (int i=(11-x); i<=15; i++){
                    map[i][25-y] = 1;
                }
                for (int j=(25-y); j<=29; j++){
                    map[11-x][j] = 1;
                }
                this.setupRubyDiagonallyBottomRightToTopLeft((4+x), (4+y));
                this.setupDoorBottomRight(map, (4+x), (4+y));
                this.setupCenterTopKey(map);
                break;
        }

        return map;
    }

    public void setupRubyDiagonallyTopLeftToBottomRight(int maxX, int maxY){
        int x = new Random().nextInt(maxY-2);
        int y = new Random().nextInt(maxX-2);
        EventItemEmmanuel ruby = new EventItemEmmanuel((x+1) * sizeWw, (y+1) * sizeWh, 2 * sizeWw, 2 * sizeWh, new ImagePattern(new Image("data/follow2/ruby1RS.png")), eventwin, true);
        listEI.add(ruby);
        gameContext.getChildren().add(ruby);
    }

    public void setupRubyDiagonallyTopRightToBottomLeft(int maxX, int maxY){
        int x = new Random().nextInt(maxY-2);
        int y = new Random().nextInt(maxX-2);
        EventItemEmmanuel ruby = new EventItemEmmanuel((29-x) * sizeWw, (y+1) * sizeWh, 2 * sizeWw, 2 * sizeWh, new ImagePattern(new Image("data/follow2/ruby1RS.png")), eventwin, true);
        listEI.add(ruby);
        gameContext.getChildren().add(ruby);
    }

    public void setupRubyDiagonallyBottomLeftToTopRight(int maxX, int maxY){
        int x = new Random().nextInt(maxY-2);
        int y = new Random().nextInt(maxX-2);
        EventItemEmmanuel ruby = new EventItemEmmanuel((x+1) * sizeWw, (15-y) * sizeWh, 2 * sizeWw, 2 * sizeWh, new ImagePattern(new Image("data/follow2/ruby1RS.png")), eventwin, true);
        listEI.add(ruby);
        gameContext.getChildren().add(ruby);
    }

    public void setupRubyDiagonallyBottomRightToTopLeft(int maxX, int maxY){
        int x = new Random().nextInt(maxY-2);
        int y = new Random().nextInt(maxX-2);
        EventItemEmmanuel ruby = new EventItemEmmanuel((29-x) * sizeWw, (15-y) * sizeWh, 2 * sizeWw, 2 * sizeWh, new ImagePattern(new Image("data/follow2/ruby1RS.png")), eventwin, true);
        listEI.add(ruby);
        gameContext.getChildren().add(ruby);
    }

    public void setupCenterTopKey(int[][] map){

        int x = 4;
        int y = new Random().nextInt(5);
        for (int i=(12-y); i<=(17+y); i++){
            if ((i==(12-y)) || (i==(17+y))){
                for (int j=0; j<(x+1); j++){
                    map[j][i] = 1;
                }
            }
            map[x][i] = 1;
        }

        int randomPos = new Random().nextInt(3);
        int heightMax = 4;
        int weightMin = (12-y);
        int weightMax = (17+y);

        int randomPosDoor;

        switch (randomPos){
            case 0:
                randomPosDoor = new Random().nextInt(heightMax);
                map[randomPosDoor][weightMin] = 0;
                map[randomPosDoor+1][weightMin] = 0;
                break;

            case 1:
                randomPosDoor = new Random().nextInt(weightMax-weightMin);
                map[heightMax][weightMin+randomPosDoor] = 0;
                map[heightMax][weightMin+randomPosDoor+1] = 0;
                break;

            case 2:
                randomPosDoor = new Random().nextInt(heightMax);
                map[randomPosDoor][weightMax] = 0;
                map[randomPosDoor+1][weightMax] = 0;
                break;

            default:
                log.info("Error Door !");
                break;
        }

        this.setupKeyTop(14-y, 14+y);
    }

    public void setupCenterBottomKey(int[][] map){
        int x = 11;
        int y = new Random().nextInt(5);
        for (int i=(12-y); i<=(17+y); i++){
            if ((i==(12-y)) || (i==(17+y))){
                for (int j=x; j<=15; j++){
                    map[j][i] = 1;
                }
            }
            map[x][i] = 1;
        }

        int randomPos = new Random().nextInt(3);
        int heightMax = 11;
        int weightMin = (12-y);
        int weightMax = (17+y);

        int randomPosDoor;

        switch (randomPos){
            case 0:
                randomPosDoor = new Random().nextInt(4);
                map[14-randomPosDoor][weightMin] = 0;
                map[14-randomPosDoor+1][weightMin] = 0;
                break;

            case 1:
                randomPosDoor = new Random().nextInt(weightMax-weightMin);
                map[heightMax][weightMin+randomPosDoor] = 0;
                map[heightMax][weightMin+randomPosDoor+1] = 0;
                break;

            case 2:
                randomPosDoor = new Random().nextInt(4);
                map[14-randomPosDoor][weightMax] = 0;
                map[14-randomPosDoor+1][weightMax] = 0;
                break;

            default:
                log.info("Error Door !");
                break;
        }

        this.setupKeyBottom(14-y, 14+y);
    }

    public void setupKeyTop(int min, int max){
        int difference = max - min;
        int randomPos;
        if (difference == 0){
            randomPos = 0;
        }else {
            randomPos = new Random().nextInt(difference);
        }
        EventHandler<ActionEvent> eventkeyred = e -> {
            gameContext.updateScore(this.stats, this.followEmmanuel);
            listWall.remove(doorRED);
            gameContext.getChildren().remove(doorRED);
        };
        EventItemEmmanuel ruby = new EventItemEmmanuel((min+randomPos) * sizeWw, 2 * sizeWh, 2 * sizeWw, 2 * sizeWh, new ImagePattern(new Image("data/follow2/keyred.png")), eventkeyred, true);
        listEI.add(ruby);
        gameContext.getChildren().add(ruby);
    }

    public void setupKeyBottom(int min, int max){
        int difference = max - min;
        int randomPos;
        if (difference == 0){
            randomPos = 0;
        }else {
            randomPos = new Random().nextInt(difference);
        }
        EventHandler<ActionEvent> eventkeyred = e -> {
            gameContext.updateScore(this.stats, this.followEmmanuel);
            listWall.remove(doorRED);
            gameContext.getChildren().remove(doorRED);
        };
        EventItemEmmanuel ruby = new EventItemEmmanuel((min+randomPos) * sizeWw, 13 * sizeWh, 2 * sizeWw, 2 * sizeWh, new ImagePattern(new Image("data/follow2/keyred.png")), eventkeyred, true);
        listEI.add(ruby);
        gameContext.getChildren().add(ruby);
    }

    public void setupDoorTopLeft(int[][] map, int posX, int posY){
        int random = new Random().nextInt(2);
        int randomPosDoor;

        if (random == 0){
            randomPosDoor = new Random().nextInt(posY);

            doorRED = new Rectangle((randomPosDoor+1) * sizeWw, (posX+1) * sizeWh, 2 * sizeWw, sizeWh);
            doorRED.setFill(new ImagePattern(new Image("data/follow2/door1rouge.png")));
            listWall.add(doorRED);
            gameContext.getChildren().add(doorRED);

            map[posX][randomPosDoor] = 0;
            map[posX][randomPosDoor+1] = 0;
        }else {
            randomPosDoor = new Random().nextInt(posX);

            doorRED = new Rectangle((posY+1) * sizeWw, (randomPosDoor+1) * sizeWh, sizeWw, 2 * sizeWh);
            doorRED.setFill(new ImagePattern(new Image("data/follow2/door1rouge.png")));
            listWall.add(doorRED);
            gameContext.getChildren().add(doorRED);

            map[randomPosDoor][posY] = 0;
            map[randomPosDoor+1][posY] = 0;
        }
    }

    public void setupDoorTopRight(int[][] map, int posX, int posY){
        int random = new Random().nextInt(2);
        int randomPosDoor;

        if (random == 0){
            randomPosDoor = new Random().nextInt(posY);

            doorRED = new Rectangle((28-randomPosDoor+1) * sizeWw, (posX+1) * sizeWh, 2 * sizeWw, sizeWh);
            doorRED.setFill(new ImagePattern(new Image("data/follow2/door1rouge.png")));
            listWall.add(doorRED);
            gameContext.getChildren().add(doorRED);

            map[posX][28-randomPosDoor] = 0;
            map[posX][28-randomPosDoor+1] = 0;
        }else {
            randomPosDoor = new Random().nextInt(posX);

            doorRED = new Rectangle((29-posY+1) * sizeWw, (randomPosDoor+1) * sizeWh, sizeWw, 2 * sizeWh);
            doorRED.setFill(new ImagePattern(new Image("data/follow2/door1rouge.png")));
            listWall.add(doorRED);
            gameContext.getChildren().add(doorRED);

            map[randomPosDoor][29-posY] = 0;
            map[randomPosDoor+1][29-posY] = 0;
        }
    }

    public void setupDoorBottomLeft(int[][] map, int posX, int posY){
        int random = new Random().nextInt(2);
        int randomPosDoor;

        if (random == 0){
            randomPosDoor = new Random().nextInt(posY);

            doorRED = new Rectangle((randomPosDoor+1) * sizeWw, (15-posX+1) * sizeWh, 2 * sizeWw, sizeWh);
            doorRED.setFill(new ImagePattern(new Image("data/follow2/door1rouge.png")));
            listWall.add(doorRED);
            gameContext.getChildren().add(doorRED);

            map[15-posX][randomPosDoor] = 0;
            map[15-posX][randomPosDoor+1] = 0;
        }else {
            randomPosDoor = new Random().nextInt(posX);

            doorRED = new Rectangle((posY+1) * sizeWw, (14-randomPosDoor+1) * sizeWh, sizeWw, 2 * sizeWh);
            doorRED.setFill(new ImagePattern(new Image("data/follow2/door1rouge.png")));
            listWall.add(doorRED);
            gameContext.getChildren().add(doorRED);

            map[14-randomPosDoor][posY] = 0;
            map[14-randomPosDoor+1][posY] = 0;
        }
    }

    public void setupDoorBottomRight(int[][] map, int posX, int posY){
        int random = new Random().nextInt(2);
        int randomPosDoor;

        if (random == 0){
            randomPosDoor = new Random().nextInt(posY);

            doorRED = new Rectangle((28-randomPosDoor+1) * sizeWw, (15-posX+1) * sizeWh, 2 * sizeWw, sizeWh);
            doorRED.setFill(new ImagePattern(new Image("data/follow2/door1rouge.png")));
            listWall.add(doorRED);
            gameContext.getChildren().add(doorRED);

            map[15-posX][28-randomPosDoor] = 0;
            map[15-posX][28-randomPosDoor+1] = 0;
        }else {
            randomPosDoor = new Random().nextInt(posX);

            doorRED = new Rectangle((29-posY+1) * sizeWw, (14-randomPosDoor+1) * sizeWh, sizeWw, 2 * sizeWh);
            doorRED.setFill(new ImagePattern(new Image("data/follow2/door1rouge.png")));
            listWall.add(doorRED);
            gameContext.getChildren().add(doorRED);

            map[14-randomPosDoor][29-posY] = 0;
            map[14-randomPosDoor+1][29-posY] = 0;
        }
    }
}
