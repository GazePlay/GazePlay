package net.gazeplay.games.ladder;

import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;

public class Ladder implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;

    private int size;
    private int cross;

    private final ArrayList<Step> steps;

    Ladder(IGameContext gameContext, Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
        random = new ReplayablePseudoRandom();
        steps = new ArrayList<>();
    }

    Ladder(IGameContext gameContext, Stats stats, double gameSeed){
        this.gameContext = gameContext;
        this.stats = stats;
        random = new ReplayablePseudoRandom(gameSeed);
        steps = new ArrayList<>();
    }

    @Override
    public void launch(){
        size = 10;
        cross = 3;

        background();
        creation();
        button();

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose(){

    }

    private void background(){

    }

    private void creation(){
        for (int i=0; i<5; i++){
            for (int j=0; j<size; j++){
                steps.add(new Step(i,j,i,j+1));
            }
        }

        int y1 = 0;
        int y2 = 0;
        for (int i=0; i<4; i++){
            for (int j=0; j<cross; j++){
                y1 = y1 + random.nextInt(size-cross+j-y1) + 1;
                y2 = y2 + random.nextInt(size-cross+j-y1) + 1;
                steps.add(new Step(i, y1, i+1,y2));
            }
        }
    }

    private void button(){

    }
}
