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

    private final ArrayList<Rectangle> ListRec;

    private  final  ArrayList<EventItem> ListEI;

    Follow(final IGameContext gameContext, final Stats stats, final FollowGameVariant variant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        ListRec = new ArrayList<>();

        ListEI = new ArrayList<>();

        launch();
    }

    @Override
    public void launch() {
        gameContext.getChildren().clear();

        py = dimension2D.getHeight()/2;
        px = dimension2D.getWidth()/2;

        size = dimension2D.getHeight()/10;

        RPlayer = new Rectangle(px-size/2, py-size/2, size, size);
        RPlayer.setFill(new ImagePattern(new Image("data/biboule/images/Blue.png")));
        gameContext.getChildren().add(RPlayer);

        //increase the speed but decrease the accuracy
        speed = 2;

        //List of Wall
        Rectangle Wall = new Rectangle(dimension2D.getWidth()/3, dimension2D.getHeight()/5, dimension2D.getWidth()/5, dimension2D.getHeight()/6);
        Wall.setFill(new ImagePattern(new Image("data/follow/wall.png")));
        ListRec.add(Wall);
        gameContext.getChildren().add(Wall);

        //List of EventItem
        javafx.event.EventHandler<ActionEvent> event = e -> win();
        EventItem target = new EventItem(0, 0, size/2, size/2, new ImagePattern(new Image("data/follow/target.png")), event);
        ListEI.add(target);
        gameContext.getChildren().add(target.rectangle);

        startafterdelay(5000);

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
        double dist = x*x + y*y;
        PauseTransition next = new PauseTransition(Duration.millis(5));
        next.setOnFinished(nextevent -> {
            gameContext.getChildren().remove(RPlayer);
            if (dist>speed*dimension2D.getWidth()/100) {
                boolean bx = TestAllWall(px + speed * x / Math.sqrt(dist), py);
                boolean by = TestAllWall(px, py + speed * y / Math.sqrt(dist));
                if (bx) {
                    px = px + speed * x / Math.sqrt(dist);
                }
                if (by) {
                    py = py + speed * y / Math.sqrt(dist);
                }
            } else {
                boolean bx = TestAllWall(rx, py);
                boolean by = TestAllWall(px, ry);
                if (bx) {
                    px = rx;
                }
                if (by) {
                    py = ry;
                }
            }
            RPlayer.setX(px-size/2);
            RPlayer.setY(py-size/2);
            gameContext.getChildren().add(RPlayer);
            CheckEI();
            followthegaze();
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
        for (Rectangle Rec : ListRec){
            test = test && IsNotInWall(Rec, x, y, size);
        }
        return test;
    }

    private void win(){
        gameContext.updateScore(stats, this);

        gameContext.playWinTransition(500, actionEvent -> {
            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            gameContext.showRoundStats(stats, this);
        });
    }

    private void CheckEI(){
        for(EventItem EI : ListEI){
            if (!IsNotInWall(EI.rectangle, px, py, size)){
                EI.active();
            }
        }
    }
}
