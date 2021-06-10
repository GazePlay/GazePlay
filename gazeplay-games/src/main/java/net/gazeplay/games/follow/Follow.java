package net.gazeplay.games.follow;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.awt.*;
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
    double size;

    //gaze's position
    private double rx;
    private double ry;

    //speed of the player
    private double speed;

    //player square
    private Rectangle RPlayer;

    private boolean canmove;

    private final ArrayList<Rectangle> ListWall;

    private final  ArrayList<EventItem> ListEI;

    private Rectangle Gaze;


    Follow(final IGameContext gameContext, final Stats stats, final FollowGameVariant variant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        ListWall = new ArrayList<>();

        ListEI = new ArrayList<>();

        launch();
    }

    @Override
    public void launch() {
        gameContext.getChildren().clear();

        canmove = true;

        py = dimension2D.getHeight()/2;
        px = dimension2D.getWidth()/2;

        size = dimension2D.getWidth()/50;

        dallage();

        RPlayer = new Rectangle(px-size/2, py-size/2, size, size);
        RPlayer.setFill(new ImagePattern(new Image("data/biboule/images/Blue.png")));
        gameContext.getChildren().add(RPlayer);

        //increase the speed but decrease the accuracy
        speed = 2;

        contour();
        if (variant.equals(FollowGameVariant.FKEY)){
            wallkey();
            itemkey();
        }
        else if (variant.equals(FollowGameVariant.FCOIN)){
            InitWallItemCoin();
        }
        else {
            log.error("Variant not found : " + variant.getLabel());
        }

        Gaze = new Rectangle(0, 0, dimension2D.getWidth()/200, dimension2D.getWidth()/200);
        Gaze.setFill(new ImagePattern(new Image("data/follow/ruby1.png")));
        gameContext.getChildren().add(Gaze);

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
                        if (IsInWall(Wall, tx, py, size)){
                            if (x>0){
                                tx = Wall.getX() - size/2;
                            } else {
                                tx = Wall.getX()+Wall.getWidth() + size/2;
                            }
                        }
                        if (IsInWall(Wall, px, ty, size)){
                            if (y>0){
                                ty = Wall.getY() - size/2;
                            } else {
                                ty = Wall.getY()+Wall.getHeight() + size/2;
                            }
                        }
                    }

                    px = tx;
                    py = ty;
                }
                RPlayer.setX(px - size / 2);
                RPlayer.setY(py - size / 2);
                CheckEI();
                followthegaze();
            }
        });
        next.play();
    }

    private void position(){
        rx = MouseInfo.getPointerInfo().getLocation().getX();
        ry = MouseInfo.getPointerInfo().getLocation().getY();
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
        gameContext.getChildren().remove(Gaze);

        dispose();

        canmove = false;

        gameContext.updateScore(stats, this);

        gameContext.playWinTransition(500, actionEvent -> {

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            gameContext.showRoundStats(stats, this);
        });
    }

    private void CheckEI(){
        ArrayList<EventItem> Remove = new ArrayList<>();
        for(EventItem EI : ListEI){
            if (IsInWall(EI.rectangle, px, py, size)){
                if (EI.remove) {
                    Remove.add(EI);
                    gameContext.getChildren().remove(EI.rectangle);
                }
                EI.active();
            }
        }
        ListEI.removeAll(Remove);
    }

    private void multigoals(){
        boolean test = true;
        for (EventItem EI : ListEI){
            test = test && EI.multigoals;
        }
        if (test){
            win();
        }
    }

    private void contour(){
        int x = 32;
        int y = 18;
        double w = dimension2D.getWidth();
        double h = w*9/16;
        double size = w/x;
        Rectangle W;
        for (int i=0; i<x; i++){
            W = new Rectangle(i*size, 0, size, size);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
            W = new Rectangle(i*size, h-size, size, size);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
        }
        for (int i=1; i<y-1; i++){
            W = new Rectangle(0, i*size, size, size);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
            W = new Rectangle(w-size, i*size, size, size);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
        }
    }

    private void wallkey(){
        int x = 32;
        int y = 18;
        double size = dimension2D.getWidth()/x;
        Rectangle W;

        for (int i=1; i<10; i++){
            W = new Rectangle(i*size, 5*size, size, size);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
        }
        for (int i=1; i<4; i++){
            W = new Rectangle(9*size, i*size, size, size);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
        }
        for (int i=6; i<y-2; i++){
            W = new Rectangle(6*size, i*size, size, size);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
        }
        for (int i=4; i<y-1; i++){
            W = new Rectangle((x-9)*size, i*size, size, size);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
        }
        for (int i=1; i<y-4; i++){
            W = new Rectangle((x-5)*size, i*size, size, size);
            W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
            ListWall.add(W);
            gameContext.getChildren().add(W);
        }
    }

    private void itemkey(){
        int x = 32;
        int y = 18;
        double size = dimension2D.getWidth()/x;

        {
            Rectangle DoorRED = new Rectangle(9 * size, 4 * size, size, size);
            DoorRED.setFill(new ImagePattern(new Image("data/follow/door1rouge.png")));
            ListWall.add(DoorRED);
            gameContext.getChildren().add(DoorRED);

            javafx.event.EventHandler<ActionEvent> eventkeyred = e -> {
                ListWall.remove(DoorRED);
                gameContext.getChildren().remove(DoorRED);
                //Maybe add a song
            };
            EventItem KeyRED = new EventItem(3 * size, 7 * size, size, size, new ImagePattern(new Image("data/follow/keyred.png")), eventkeyred, true);
            ListEI.add(KeyRED);
            gameContext.getChildren().add(KeyRED.rectangle);
        }

        {
            Rectangle DoorGREEN = new Rectangle(6 * size, (y - 2) * size, size, size);
            DoorGREEN.setFill(new ImagePattern(new Image("data/follow/door1verte.png")));
            ListWall.add(DoorGREEN);
            gameContext.getChildren().add(DoorGREEN);

            javafx.event.EventHandler<ActionEvent> eventkeygreen = e -> {
                ListWall.remove(DoorGREEN);
                gameContext.getChildren().remove(DoorGREEN);
                //Maybe add a sound
            };
            EventItem KeyGREEN = new EventItem((x - 3) * size, 4 * size, size, size, new ImagePattern(new Image("data/follow/keygreen.png")), eventkeygreen, true);
            ListEI.add(KeyGREEN);
            gameContext.getChildren().add(KeyGREEN.rectangle);
        }

        javafx.event.EventHandler<ActionEvent> eventwin = e -> win();
        EventItem Ruby = new EventItem(3 * size, 2 * size, size, size, new ImagePattern(new Image("data/follow/ruby1.png")), eventwin, true);
        ListEI.add(Ruby);
        gameContext.getChildren().add(Ruby.rectangle);
    }

    private void InitWallItemCoin(){
        int x = 32;
        int y = 18;
        double size = dimension2D.getWidth()/x;

        int Map[][] = new int[][]
            {
                {1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 1},
                {0, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 0, 1},
                {0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1},
                {0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0},
                {1, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0},
                {1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                {0, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 2, 2, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0},
                {0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                {0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0},
                {0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0},
                {1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1}
            };

        Rectangle W;
        EventItem Coin;

        for (int i=0; i<x-2; i++){
            for (int j=0; j<y-2; j++){
                if (Map[j][i]==1){
                    W = new Rectangle((i+1)*size, (j+1)*size, size, size);
                    W.setFill(new ImagePattern(new Image("data/follow/wall1.png")));
                    ListWall.add(W);
                    gameContext.getChildren().add(W);
                }
                else if(Map[j][i]==0) {
                    Coin = new EventItem((i+1)*size, (j+1)*size, size, size, new ImagePattern(new Image("data/follow/coin.png")), e-> {multigoals(); /*Maybe add a song*/}, true, false);
                    ListEI.add(Coin);
                    gameContext.getChildren().add(Coin.rectangle);
                }
            }
        }
    }

    private void dallage(){
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
}
