package net.gazeplay.games.cooperativeGame;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;


public class CooperativeGame extends Parent implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private boolean endOfLevel;
    private Cat cat;
    private int level;
    private ArrayList<Rectangle> obstacles;

    public CooperativeGame(final IGameContext gameContext, Stats stats, int level){
        this.gameContext = gameContext;
        this.stats = stats;
        this.level = level;
        this.obstacles = new ArrayList<>();
    }



    @Override
    public void launch() {
        this.endOfLevel = false;
        gameContext.setLimiterAvailable();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        background.setFill(Color.WHITE);
        gameContext.getChildren().add(background);
        initGameBox();
        setLevel(level);
        gameContext.firstStart();
        System.out.println("cat posX:" + cat.cat.getX());


    }

    private void setLevel(final int i){

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        this.cat = new CatMovement(100, 100, 75,75,gameContext,stats,this, 10, this.obstacles);
        gameContext.getChildren().add(this.cat.cat);

        this.cat.cat.toFront();
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }



    private void initGameBox(){

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Rectangle upWall = new Rectangle(0,0,dimension2D.getWidth(),50);
        upWall.setFill(Color.BLACK);

        Rectangle downWall = new Rectangle(0,dimension2D.getHeight()-50,dimension2D.getWidth(),50);
        downWall.setFill(Color.BLACK);

        Rectangle leftWall = new Rectangle(0,0,50,dimension2D.getHeight());
        leftWall.setFill(Color.BLACK);

        Rectangle rightWall = new Rectangle(dimension2D.getWidth()-50,0,50,dimension2D.getHeight());
        rightWall.setFill(Color.BLACK);

        this.obstacles.add(upWall);
        this.obstacles.add(downWall);
        this.obstacles.add(leftWall);
        this.obstacles.add(rightWall);

        for (int i = 0; i < obstacles.size(); i++){
            gameContext.getChildren().add(obstacles.get(i));
            obstacles.get(i).toFront();
        }

    }
}
