package net.gazeplay.games.follow;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;

@Slf4j
public class Follow implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    @Getter
    @Setter
    private FollowGameVariant variant;

    private final Dimension2D dimension2D;

    //player's position
    private double px;
    private double py;

    //size of the player square
    double sizeP;

    //gaze's position
    private double rx;
    private double ry;

    //speed of the player
    private double speed;

    //player square
    private Rectangle RPlayer;

    //If the player can move or not
    private boolean canmove;

    //List of walls
    private final ArrayList<Rectangle> ListWall;

    //List of EventItems
    private final  ArrayList<EventItem> ListEI;

    //Pointer of the gaze
    private Rectangle Gaze;

    //number position item (x,y)
    private int x;
    private int y;

    //size wall/item
    private double sizeWw;
    private double sizeWh;

    //score with coins
    private int score;
    private int scoretoreach;


    Follow(final IGameContext gameContext, final Stats stats, final FollowGameVariant variant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        ListWall = new ArrayList<>();

        ListEI = new ArrayList<>();

        //launch();
    }

    @Override
    public void launch() {
        gameContext.getChildren().clear();

        score = 0;
        scoretoreach = 0;

        canmove = true;

        x=32;
        y=18;

        sizeWw = dimension2D.getWidth()/x;
        sizeWh = dimension2D.getHeight()/y;

        py = dimension2D.getHeight()/2;
        px = dimension2D.getWidth()/2;

        sizeP = dimension2D.getWidth()/50;

        //Make the paving of the floor
        paving();

        RPlayer = new Rectangle(px-sizeP/2, py-sizeP/2, sizeP, sizeP);
        RPlayer.setFill(new ImagePattern(new Image("data/follow/Biboule.png")));
        gameContext.getChildren().add(RPlayer);

        //increase the speed but decrease the accuracy
        speed = 4;

        //Make the border of the screen
        contour();

        if (variant.equals(FollowGameVariant.FKEY)){
            FKEY();
        }
        else if (variant.equals(FollowGameVariant.FCOIN)){
            FCOIN();
        }
        else {
            log.error("Variant not found : " + variant.getLabel());
        }

        Gaze = new Rectangle(0, 0, dimension2D.getWidth()/200, dimension2D.getWidth()/200);
        Gaze.setFill(new ImagePattern(new Image("data/follow/ruby1.png")));
        gameContext.getChildren().add(Gaze);

        {
            Scene gameContextScene = gameContext.getPrimaryScene();

            EventHandler<GazeEvent> recordGazeMovements = e -> {
                Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                rx = toSceneCoordinate.getX();
                ry = toSceneCoordinate.getY();

            };

            EventHandler<MouseEvent> recordMouseMovements = e -> {
                Point2D toSceneCoordinate = gameContextScene.getRoot().localToScene(e.getX(), e.getY());
                rx = toSceneCoordinate.getX();
                ry = toSceneCoordinate.getY();

            };

            gameContextScene.getRoot().addEventFilter(GazeEvent.GAZE_MOVED, recordGazeMovements);
            gameContextScene.getRoot().addEventFilter(MouseEvent.MOUSE_MOVED, recordMouseMovements);
        }

        startafterdelay();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose() {
        stats.stop();
        ListEI.clear();
        ListWall.clear();
    }

    private void followthegaze(){
        position();
        double x = rx - px;
        double y = ry - py;
        double dist = Math.sqrt(x*x + y*y);
        PauseTransition next = new PauseTransition(Duration.millis(5));
        next.setOnFinished(nextevent -> {
            if (canmove) {
                if (dist > speed) {
                    double tx = px + speed * x / dist;
                    double ty = py + speed * y / dist;

                    for (Rectangle Wall : ListWall){
                        if (IsInWall(Wall, tx, py, sizeP)){
                            if (x>0){
                                tx = Wall.getX() - sizeP/2;
                            } else {
                                tx = Wall.getX()+Wall.getWidth() + sizeP/2;
                            }
                        }
                        if (IsInWall(Wall, px, ty, sizeP)){
                            if (y>0){
                                ty = Wall.getY() - sizeP/2;
                            } else {
                                ty = Wall.getY()+Wall.getHeight() + sizeP/2;
                            }
                        }
                    }

                    px = tx;
                    py = ty;
                }
                RPlayer.setX(px - sizeP / 2);
                RPlayer.setY(py - sizeP / 2);
                CheckEI();
                followthegaze();
            }
        });
        next.play();
    }

    private void position(){
        Gaze.setX(rx);
        Gaze.setY(ry);
    }

    private void startafterdelay(){
        PauseTransition Wait = new PauseTransition(Duration.millis(1000));
        Wait.setOnFinished(Waitevent -> followthegaze());
        Wait.play();
    }

    private boolean IsInWall(Rectangle Wall, double x, double y, double size){
        double Wx = Wall.getX() + size/2;
        double Wy = Wall.getY() + size/2;
        double Ww = Wall.getWidth();
        double Wh = Wall.getHeight();

        return (x+size>Wx) && (y+size>Wy) && (x<Wx+Ww) && (y<Wy+Wh);
    }

    private void win(){
        stats.stop();

        gameContext.updateScore(stats, this);

        gameContext.playWinTransition(500, actionEvent -> {

            canmove = false;

            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            gameContext.showRoundStats(stats, this);
        });
    }

    private void CheckEI(){
        ArrayList<EventItem> Remove = new ArrayList<>();
        for(EventItem EI : ListEI){
            if (IsInWall(EI, px, py, sizeP)){
                if (EI.remove) {
                    Remove.add(EI);
                    gameContext.getChildren().remove(EI);
                }
                EI.active();
            }
        }
        ListEI.removeAll(Remove);
    }

    private void multigoals(){
        if (score>=scoretoreach){
            win();
        }
    }

    private void contour(){
        Rectangle W;
        for (int i=0; i<x; i++){
            W = new Rectangle(i*sizeWw, 0, sizeWw, sizeWh);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
            W = new Rectangle(i*sizeWw, dimension2D.getHeight()-sizeWh, sizeWw, sizeWh);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
        }
        for (int i=1; i<y-1; i++){
            W = new Rectangle(0, i*sizeWh, sizeWw, sizeWh);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
            W = new Rectangle(dimension2D.getWidth()-sizeWw, i*sizeWh, sizeWw, sizeWh);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
        }
    }

    private void paving(){
        int x = 32;
        int y = 18;
        double size = dimension2D.getWidth()/x;
        Rectangle D;

        for (int i=0; i<x; i++){
            for (int j=0; j<y; j++){
                D = new Rectangle(i*size, j*size, size, size);
                D.setFill(new ImagePattern(new Image("data/follow/slab1.png")));
                gameContext.getChildren().add(D);
            }
        }
    }

    private void build(int[][] Map){
        Rectangle W;
        EventItem Coin;

        for (int i=0; i<x-2; i++){
            for (int j=0; j<y-2; j++){
                if (Map[j][i]==1){
                    W = new Rectangle((i+1)*sizeWw, (j+1)*sizeWh, sizeWw, sizeWh);
                    W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
                    ListWall.add(W);
                    gameContext.getChildren().add(W);
                }
                else if(Map[j][i]==2) {
                    scoretoreach++;
                    Coin = new EventItem((i+1)*sizeWw, (j+1)*sizeWh, sizeWw, sizeWh, new ImagePattern(new Image("data/follow/coin.png")), e-> {stats.incrementNumberOfGoalsReached(); score++; multigoals(); /*Maybe add a song*/}, true);
                    ListEI.add(Coin);
                    gameContext.getChildren().add(Coin);
                }
            }
        }
    }

    private void FKEY(){
        int[][] Map = new int[][]
            {
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}
            };

        build(Map);

        {
            Rectangle DoorRED = new Rectangle(9 * sizeWw, 3 * sizeWh, sizeWw, 2*sizeWh);
            DoorRED.setFill(new ImagePattern(new Image("data/follow/door1rouge.png")));
            ListWall.add(DoorRED);
            gameContext.getChildren().add(DoorRED);

            javafx.event.EventHandler<ActionEvent> eventkeyred = e -> {
                ListWall.remove(DoorRED);
                gameContext.getChildren().remove(DoorRED);
                //Maybe add a song
                stats.incrementNumberOfGoalsReached();
            };
            EventItem KeyRED = new EventItem(3 * sizeWw, 7 * sizeWh, sizeWw, sizeWh, new ImagePattern(new Image("data/follow/keyred.png")), eventkeyred, true);
            ListEI.add(KeyRED);
            gameContext.getChildren().add(KeyRED);
        }

        {
            Rectangle DoorGREEN = new Rectangle(6 * sizeWw, (y - 3) * sizeWh, sizeWw, 2*sizeWh);
            DoorGREEN.setFill(new ImagePattern(new Image("data/follow/door1verte.png")));
            ListWall.add(DoorGREEN);
            gameContext.getChildren().add(DoorGREEN);

            Rectangle DoorGREEN2 = new Rectangle((x-9) * sizeWw, (y - 7) * sizeWh, sizeWw, 2*sizeWh);
            DoorGREEN2.setFill(new ImagePattern(new Image("data/follow/door1verte.png")));
            ListWall.add(DoorGREEN2);
            gameContext.getChildren().add(DoorGREEN2);

            javafx.event.EventHandler<ActionEvent> eventkeygreen = e -> {
                ListWall.remove(DoorGREEN);
                gameContext.getChildren().remove(DoorGREEN);
                ListWall.remove(DoorGREEN2);
                gameContext.getChildren().remove(DoorGREEN2);
                //Maybe add a sound
                stats.incrementNumberOfGoalsReached();
            };
            EventItem KeyGREEN = new EventItem((x - 3) * sizeWw, 4 * sizeWh, sizeWw, sizeWh, new ImagePattern(new Image("data/follow/keygreen.png")), eventkeygreen, true);
            ListEI.add(KeyGREEN);
            gameContext.getChildren().add(KeyGREEN);
        }

        javafx.event.EventHandler<ActionEvent> eventwin = e -> {
            stats.incrementNumberOfGoalsReached();
            win();
        };
        EventItem Ruby = new EventItem(2 * sizeWw, 2 * sizeWh, 2*sizeWw, 2*sizeWh, new ImagePattern(new Image("data/follow/ruby1.png")), eventwin, true);
        ListEI.add(Ruby);
        gameContext.getChildren().add(Ruby);

        javafx.event.EventHandler<ActionEvent> eventtrap = e -> {
            Rectangle WallTrap = new Rectangle((x-9) * sizeWw, sizeWh, sizeWw, 3* sizeWh);
            WallTrap.setFill(new ImagePattern(new Image("data/follow/jailbar1.png")));
            gameContext.getChildren().add(WallTrap);
            ListWall.add(WallTrap);
            //Maybe add a sound
        };
        EventItem Trap = new EventItem((x-8) * sizeWw, 5 * sizeWh, 3 * sizeWw, sizeWh, new ImagePattern(new Image("data/follow/nothing.png")), eventtrap, true);
        ListEI.add(Trap);
        gameContext.getChildren().add(Trap);
    }

    private void FCOIN(){

        int[][] Map = new int[][]
            {
                {1, 2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1},
                {2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1},
                {2, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 1, 2, 1},
                {2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1},
                {2, 2, 2, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 2, 2, 1, 1, 1, 2, 2, 1, 2, 2, 1, 2, 2},
                {1, 1, 1, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 2, 1, 1, 1, 2, 2, 1, 2, 2},
                {1, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 2, 1, 1, 2, 2, 1, 2},
                {1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 2},
                {2, 2, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 2, 0, 0, 2, 1, 1, 2, 2, 2, 1, 2, 2, 2, 1, 1, 1, 2},
                {2, 2, 1, 2, 2, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 1, 2, 2, 2, 1, 2, 2, 2},
                {2, 1, 1, 2, 1, 2, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 1, 1, 2, 1, 2, 2, 2, 2, 2, 2, 2},
                {2, 2, 2, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 2, 1, 1, 1, 2, 2, 1, 2},
                {2, 2, 2, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 2},
                {1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 1, 1, 2, 2, 2},
                {1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1}
            };

        build(Map);
    }
}
