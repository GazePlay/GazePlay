package net.gazeplay.games.ladder;

import javafx.animation.PauseTransition;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.ProgressButton;

import java.util.ArrayList;

public class Ladder implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final ReplayablePseudoRandom random;
    private final Dimension2D dimension2D;

    private int size;
    private int cross;

    private final ArrayList<Step> steps;
    private final ArrayList<Step> fall;
    private final Step[] start;

    private double ecartw;
    private double spacew;
    private double ecarth;
    private double spaceh;

    private Rectangle player;

    private double n;
    private int m;

    private double radius;

    private final ArrayList<ProgressButton> progressButtons;

    Ladder(IGameContext gameContext, Stats stats){
        this.gameContext = gameContext;
        this.stats = stats;
        random = new ReplayablePseudoRandom();
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        steps = new ArrayList<>();
        fall = new ArrayList<>();
        progressButtons = new ArrayList<>();
        start = new Step[5];
    }

    Ladder(IGameContext gameContext, Stats stats, double gameSeed){
        this.gameContext = gameContext;
        this.stats = stats;
        random = new ReplayablePseudoRandom(gameSeed);
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        steps = new ArrayList<>();
        fall = new ArrayList<>();
        progressButtons = new ArrayList<>();
        start = new Step[5];
    }

    @Override
    public void launch(){
        size = 10;
        cross = 3;

        ecartw = dimension2D.getWidth()*0.15;
        spacew = (dimension2D.getWidth()-2*ecartw)/4;
        ecarth = dimension2D.getHeight()*0.2;
        spaceh = (dimension2D.getHeight()-2*ecarth)/size;
        radius = ecarth/3;

        background();
        creation();
        button();

        player = new Rectangle(0,0,dimension2D.getHeight()/10, dimension2D.getHeight()/10);
        player.setFill(new ImagePattern(new Image("data/follow/Biboule.png")));
        //gameContext.getChildren().add(player);

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
        Step s;
        for (int i=0; i<5; i++){
            for (int j=0; j<size; j++){
                s = new Step(i,j,i,j+1);
                fall.add(s);
                if (j==0){
                    start[i] = s;
                }
            }
        }

        int y1;
        int y2;
        int[] old = new int[] {-1, -1, -1};
        for (int i=0; i<4; i++){
            for (int j=0; j<cross; j++){
                y1 = j*3 + random.nextInt(3) + 1;
                while (y1==old[j]){
                    y1 = j*3 + random.nextInt(3) + 1;
                }
                y2 = j*3 + random.nextInt(3) + 1;
                old[j] = y2;
                steps.add(new Step(i, y1, i+1,y2));
            }
        }

        for (Step step : fall){
            screencreation(step);
        }
        for (Step step : steps){
            screencreation(step);
        }
    }

    private void screencreation(Step step){
        Line line = new Line(ecartw+spacew*step.x1,ecarth+spaceh*step.y1,ecartw+spacew*step.x2,ecarth+spaceh*step.y2);
        line.setStrokeWidth(5);
        gameContext.getChildren().add(line);
    }

    private void button(){
        ProgressButton b;

        b = new ProgressButton();
        b.setLayoutX(ecartw - radius);
        b.setLayoutY(1.0/3* ecarth);
        b.getButton().setRadius(radius);
        b.assignIndicatorUpdatable(event -> {
            gameContext.getChildren().add(player);
            move(start[0], true);
            for (ProgressButton p : progressButtons){
                p.disable();
                p.setOpacity(1);
            }
        }, gameContext);
        b.active();
        progressButtons.add(b);
        gameContext.getChildren().add(b);
        gameContext.getGazeDeviceManager().addEventFilter(b);
    }

    private void move(Step step, boolean start){
        steps.remove(step);
        fall.remove(step);
        n = 0;
        if (step.x1==step.x2){
            m=15;
        } else {
            m=100;
        }
        PauseTransition wait = new PauseTransition(Duration.millis(10));
        wait.setOnFinished(event -> {
            if (n<m){
                n++;
                if (start){
                    player.setX(ecartw + ((step.x2- step.x1)*n/m + step.x1) *spacew - dimension2D.getHeight()/20);
                    player.setY(ecarth + ((step.y2- step.y1)*n/m + step.y1) *spaceh - dimension2D.getHeight()/20);
                } else {
                    player.setX(ecartw + ((step.x2- step.x1)*(m-n)/m + step.x1) *spacew - dimension2D.getHeight()/20);
                    player.setY(ecarth + ((step.y2- step.y1)*(m-n)/m + step.y1) *spaceh - dimension2D.getHeight()/20);
                }
                wait.play();
            } else {
                if (start){
                    find(step.x2, step.y2);
                } else {
                    find (step.x1, step.y1);
                }
            }
        });
        wait.play();
    }

    private void find(int x, int y){
        boolean search = true;
        for (Step step : steps){
            if (search && step.x1==x && step.y1==y){
                search = false;
                move(step, true);
            }
            else if (search && step.x2==x && step.y2==y){
                search = false;
                move(step, false);
            }
        }
        for (Step step : fall){
            if (search && step.x1==x && step.y1==y){
                search = false;
                move(step, true);
            }
        }
        if (search){
            win();
        }
    }

    private void win() {
        stats.stop();

        gameContext.updateScore(stats, this);

        gameContext.playWinTransition(500, actionEvent -> {
            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            gameContext.showRoundStats(stats, this);
        });
    }
}
