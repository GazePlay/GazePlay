package net.gazeplay.games.cassebrique;

import javafx.animation.PauseTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class CasseBrique implements GameLifeCycle {

    final private IGameContext gameContext;
    final private Stats stats;
    final private CasseBriqueGameVariant variant;

    final private Dimension2D dimension2D;

    private Circle ball;

    private Rectangle barre;

    private double widthbarre;
    private double heightbarre;
    private double sizeball;

    private double rad;
    private double speed;

    CasseBrique(final IGameContext gameContext, final Stats stats, final CasseBriqueGameVariant variant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
    }

    public void launch(){
        widthbarre = dimension2D.getWidth()/5;
        heightbarre = dimension2D.getHeight()/35;
        sizeball = dimension2D.getHeight()*0.015;

        createball();
        createbarre();

        speed = 0;
        rad = 0;
        move();
        startafterdelay(5000);

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    public void dispose(){

    }

    private void ballfall(){
        speed = 0;
        rad = 0;
        gameContext.getChildren().remove(ball);
        createball();
        startafterdelay(4000);
    }

    private void createball(){
        ball = new Circle(dimension2D.getWidth()/2, dimension2D.getHeight() * 0.85, sizeball);
        ball.setFill(Color.RED);
        gameContext.getChildren().add(ball);
    }

    private void createbarre(){
        barre = new Rectangle(dimension2D.getWidth()/2 - widthbarre/2, dimension2D.getHeight() * 0.92 - heightbarre/2, widthbarre, heightbarre);
        barre.setFill(Color.BROWN);
        gameContext.getChildren().add(barre);
    }

    private void startafterdelay(int delay){
        PauseTransition wait = new PauseTransition(Duration.millis(delay));
        wait.setOnFinished(e -> speed = 4);
        wait.play();
    }

    private void move(){
        PauseTransition wait = new PauseTransition(Duration.millis(5));
        wait.setOnFinished(e -> {
            ball.setCenterX(ball.getCenterX() + speed * Math.sin(rad));
            ball.setCenterY(ball.getCenterY() + speed * Math.cos(rad));
            if (ball.getCenterY()>=dimension2D.getHeight()){
                ballfall();
            }
            wait.play();
        });
        wait.play();
    }
}
