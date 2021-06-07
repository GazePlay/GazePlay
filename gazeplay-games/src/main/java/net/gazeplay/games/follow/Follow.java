package net.gazeplay.games.follow;

import javafx.animation.PauseTransition;
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

    double size;

    //gaze's position
    private double rx;
    private double ry;

    private double speed;

    private Rectangle RPlayer;

    Follow(final IGameContext gameContext, final Stats stats, final FollowGameVariant variant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

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

        speed = 1;

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
            if (dist>dimension2D.getWidth()/100) {
                gameContext.getChildren().remove(RPlayer);
                px = px + speed * x / Math.sqrt(dist);
                py = py + speed * y / Math.sqrt(dist);
                RPlayer.setX(px-size/2);
                RPlayer.setY(py-size/2);
                gameContext.getChildren().add(RPlayer);
            }
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
}
