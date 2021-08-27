package net.gazeplay.games.ladder;

import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;

public class Ladder implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;
    private final Dimension2D dimension2D;

    private int size;
    private int cross;

    private final ArrayList<Step> steps;

    private double ecartw;
    private double spacew;
    private double ecarth;
    private double spaceh;

    Ladder(IGameContext gameContext, Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
        random = new ReplayablePseudoRandom();
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        steps = new ArrayList<>();
    }

    Ladder(IGameContext gameContext, Stats stats, double gameSeed){
        this.gameContext = gameContext;
        this.stats = stats;
        random = new ReplayablePseudoRandom(gameSeed);
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        steps = new ArrayList<>();
    }

    @Override
    public void launch(){
        size = 10;
        cross = 3;

        ecartw = dimension2D.getWidth()*0.15;
        spacew = (dimension2D.getWidth()-2*ecartw)/4;
        ecarth = dimension2D.getHeight()*0.1;
        spaceh = (dimension2D.getHeight()-2*ecarth)/size;

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
        Rectangle back = new Rectangle(0,0,dimension2D.getWidth(), dimension2D.getHeight());
        back.setFill(Color.WHITE);
        gameContext.getChildren().add(back);
    }

    private void creation(){
        for (int i=0; i<5; i++){
            for (int j=0; j<size; j++){
                steps.add(new Step(i,j,i,j+1));
            }
        }

        int y1;
        int y2;
        for (int i=0; i<4; i++){
            y1 = 0;
            y2 = 0;
            for (int j=0; j<cross; j++){
                y1 = y1 + random.nextInt(size-cross+j-y1-1) + 1;
                y2 = y2 + random.nextInt(size-cross+j-y1-1) + 1;
                steps.add(new Step(i, y1, i+1,y2));
            }
        }

        for (Step step : steps){
            screencreation(step);
        }
    }

    private void screencreation(Step step){
        Line line = new Line(ecartw+spacew*step.x1,ecarth+spaceh*step.y1,ecartw+spacew*step.x2,ecarth+spaceh*step.y2);
        gameContext.getChildren().add(line);
    }

    private void button(){

    }
}
