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

    //If multi-goals game (like FCOIN)
    private int goals = 3;

    private boolean[] Listcoin;

    Follow(final IGameContext gameContext, final Stats stats, final FollowGameVariant variant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        ListWall = new ArrayList<>();

        ListEI = new ArrayList<>();

        Listcoin = new boolean[goals];

        launch();
    }

    @Override
    public void launch() {
        gameContext.getChildren().clear();

        canmove = true;

        py = dimension2D.getHeight()/2;
        px = dimension2D.getWidth()/2;

        size = dimension2D.getWidth()/50;

        for (int i=0; i<goals; i++){
            Listcoin[i]=false;
        }

        RPlayer = new Rectangle(px-size/2, py-size/2, size, size);
        RPlayer.setFill(new ImagePattern(new Image(/*"data/biboule/images/Blue.png"*/"data/follow/target.png")));
        gameContext.getChildren().add(RPlayer);

        //increase the speed but decrease the accuracy
        speed = 1.5;

        contour();
        if (variant.equals(FollowGameVariant.FKEY)){
            wallkey();
            itemkey();
        }
        else if (variant.equals(FollowGameVariant.FCOIN)){
            wallcoin();
            itemcoin();
        }
        else {
            log.error("Variant not found", variant);
        }

        /*//List of EventItem
        javafx.event.EventHandler<ActionEvent> eventwin = e -> {
            win();
        };
        EventItem target = new EventItem(2*size, 2*size, size/2, size/2, new ImagePattern(new Image("data/follow/target.png")), eventwin, true);
        ListEI.add(target);
        gameContext.getChildren().add(target.rectangle);
        javafx.event.EventHandler<ActionEvent> eventgoal0 = e -> {
            Listcoin[0]=true;
            multigoals();
        };
        javafx.event.EventHandler<ActionEvent> eventgoal1 = e -> {
            Listcoin[1]=true;
            multigoals();
        };
        javafx.event.EventHandler<ActionEvent> eventgoal2 = e -> {
            Listcoin[2]=true;
            multigoals();
        };
        EventItem sphere1 = new EventItem(dimension2D.getWidth() * 6/8, dimension2D.getHeight() * 2/7, size/3, size/3, new ImagePattern(new Image("data/follow/coin.png")), eventgoal0, true);
        ListEI.add(sphere1);
        gameContext.getChildren().add(sphere1.rectangle);
        EventItem sphere2 = new EventItem(dimension2D.getWidth() * 3/8, dimension2D.getHeight() * 6/7, size/3, size/3, new ImagePattern(new Image("data/follow/coin.png")), eventgoal1, true);
        ListEI.add(sphere2);
        gameContext.getChildren().add(sphere2.rectangle);
        EventItem sphere3 = new EventItem(dimension2D.getWidth() * 4/8, dimension2D.getHeight() * 1/7, size/3, size/3, new ImagePattern(new Image("data/follow/coin.png")), eventgoal2, true);
        ListEI.add(sphere3);
        gameContext.getChildren().add(sphere3.rectangle);*/

        startafterdelay(1000);

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose() {

    }

    private void followthegaze(){
        position();
        double x = rx - px;
        double y = ry - py;
        double dist = Math.sqrt(x*x + y*y);
        PauseTransition next = new PauseTransition(Duration.millis(5));
        next.setOnFinished(nextevent -> {
            if (canmove) {
                gameContext.getChildren().remove(RPlayer);
                if (dist > speed) {
                    boolean bx = TestAllWall(px + speed * x / Math.sqrt(dist), py);
                    boolean by = TestAllWall(px, py + speed * y / Math.sqrt(dist));
                    if (bx) {
                        px = px + speed * x / dist;
                    }
                    if (by) {
                        py = py + speed * y / dist;
                    }
                }
                RPlayer.setX(px - size / 2);
                RPlayer.setY(py - size / 2);
                gameContext.getChildren().add(RPlayer);
                CheckEI();
                followthegaze();
            }
        });
        next.play();
    }

    private void position(){
        rx = MouseInfo.getPointerInfo().getLocation().getX();
        ry = MouseInfo.getPointerInfo().getLocation().getY();
    }

    private void startafterdelay(int delay){
        PauseTransition Wait = new PauseTransition(Duration.millis(delay));
        Wait.setOnFinished(Waitevent -> followthegaze());
        Wait.play();
    }

    private boolean IsNotInWall(Rectangle Wall, double x, double y, double size){
        double Wx = Wall.getX() + size/2;
        double Wy = Wall.getY() + size/2;
        double Ww = Wall.getWidth();
        double Wh = Wall.getHeight();

        return (x+size<Wx) || (y+size<Wy) || (x>Wx+Ww) || (y>Wy+Wh);
    }

    private boolean TestAllWall(double x, double y){
        boolean test = true;
        for (Rectangle Rec : ListWall){
            test = test && IsNotInWall(Rec, x, y, size);
        }
        return test;
    }

    private void win(){
        canmove = false;

        gameContext.updateScore(stats, this);

        gameContext.playWinTransition(500, actionEvent -> {
            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            gameContext.showRoundStats(stats, this);
        });
    }

    private void CheckEI(){
        ArrayList<EventItem> Remove = new ArrayList<>();
        for(EventItem EI : ListEI){
            if (!IsNotInWall(EI.rectangle, px, py, size)){
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
        for (int i = 0; i<goals; i++){
            test = test && Listcoin[i];
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
    }

    private void wallcoin(){

    }

    private void itemkey(){
        int x = 32;
        int y = 18;
        double size = dimension2D.getWidth()/x;

        Rectangle DoorRED = new Rectangle(9*size, 4*size, size, size);
        DoorRED.setFill(new ImagePattern(new Image("data/follow/door1rouge.png")));
        ListWall.add(DoorRED);
        gameContext.getChildren().add(DoorRED);

        javafx.event.EventHandler<ActionEvent> eventkeyred = e -> {
            ListWall.remove(DoorRED);
            gameContext.getChildren().remove(DoorRED);
        };
        EventItem KeyRED = new EventItem(3*size, 7*size, size, size, new ImagePattern(new Image("data/follow/keyred.png")), eventkeyred, true);
        ListEI.add(KeyRED);
        gameContext.getChildren().add(KeyRED.rectangle);

        Rectangle DoorGREEN = new Rectangle(6*size, (y-2)*size, size, size);
        DoorGREEN.setFill(new ImagePattern(new Image("data/follow/door1verte.png")));
        ListWall.add(DoorGREEN);
        gameContext.getChildren().add(DoorGREEN);

        javafx.event.EventHandler<ActionEvent> eventkeygreen = e -> {
            ListWall.remove(DoorGREEN);
            gameContext.getChildren().remove(DoorGREEN);
        };
        EventItem KeyGREEN = new EventItem((x-3)*size, 4*size, size, size, new ImagePattern(new Image("data/follow/keygreen.png")), eventkeygreen, true);
        ListEI.add(KeyGREEN);
        gameContext.getChildren().add(KeyGREEN.rectangle);
    }

    private void itemcoin(){

    }
}
