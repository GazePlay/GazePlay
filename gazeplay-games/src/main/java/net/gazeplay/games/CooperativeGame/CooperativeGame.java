package net.gazeplay.games.CooperativeGame;

import javafx.scene.Parent;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.IntGameVariant;
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

        cat = createCat();
        gameContext.getChildren().add(cat);
        gameContext.start();


    }

    private Cat createCat(){
        return new CatMovement(10, 10, 50,50,gameContext,stats,this);
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }
}
