package net.gazeplay.games.cassebrique;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class CasseBrique implements GameLifeCycle {

    private IGameContext gameContext;
    private Stats stats;
    private CasseBriqueGameVariant variant;

    private Circle ball;

    private Rectangle barre;

    CasseBrique(final IGameContext gameContext, final Stats stats, final CasseBriqueGameVariant variant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;
    }

    public void launch(){


        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
        gameContext.onGameStarted(2000);
    }

    public void dispose(){

    }



}
