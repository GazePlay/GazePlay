package net.gazeplay.games.cooperativeGame;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;


public class CooperativeGame extends Parent implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private boolean endOfLevel;
    private Cat cat;
    private int level;

    public CooperativeGame(final IGameContext gameContext, Stats stats, int level){
        this.gameContext = gameContext;
        this.stats = stats;
        this.level = level;
    }



    @Override
    public void launch() {
        this.endOfLevel = false;
        gameContext.setLimiterAvailable();
        setLevel(level);
        gameContext.firstStart();
        System.out.println("cat posX:" + cat.cat.getX());


    }

    private void setLevel(final int i){

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double width = dimension2D.getWidth() / 7;
        final double height = dimension2D.getHeight() / 7;
        this.cat = new CatMovement(width*2, height*2, width+1,height+1,gameContext,stats,this);
        gameContext.getChildren().add(this.cat);
        this.cat.toFront();
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }
}
