package net.gazeplay.games.surviveAgainstRobots;

import javafx.animation.AnimationTimer;
import javafx.scene.shape.Rectangle;
import net.gazeplay.IGameContext;

import java.util.Random;

public class Bonus extends Rectangle {

    protected boolean isDestroyed;
    private boolean isStopped;
    private final SurviveAgainstRobots gameInstance;
    private final double spawningDuration;
    private final IGameContext gameContext;
    protected AnimationTimer timer;
    private final double slowfactor;
    public Bonus(double x, double y, double width, double height, SurviveAgainstRobots gameInstance, IGameContext gameContext) {
        super(x, y, width, height);
        this.isDestroyed = false;
        this.isStopped = false;
        this.gameInstance = gameInstance;
        this.spawningDuration = 8;
        this.gameContext = gameContext;
        this.slowfactor = 0.5;
        generateRandomBonus();
    }

    private void generateRandomBonus() {
        int randomIndex = new Random().nextInt(BonusEnum.values().length);
        BonusEnum bonus = BonusEnum.values()[randomIndex];

        startTimerWithEffect(bonus);


    }


    private void startTimerWithEffect(BonusEnum bonusEnum){

        setFill(bonusEnum.getImagePattern());
        gameContext.getChildren().add(this);
        gameInstance.obstacles.add(this);

        timer = new AnimationTimer() {
            int nbframes = 0;
            int nbSeconds = 0;
            @Override
            public void handle(long now) {
                if (isDestroyed && !isStopped){
                    if (bonusEnum == BonusEnum.SLOW){
                        applySlow();
                    }
                }
                if (this.nbSeconds == spawningDuration){
                    isStopped = true;
                }

                if (isDestroyed){
                    removeObject();
                }
                if (isStopped){
                    applySlow();
                    removeObject();
                    stop();
                }
                if (nbframes == 60){
                    nbframes = 0;
                    nbSeconds++;
                }
                    nbframes++;
            }
        };
        timer.start();
    }

    private void applySlow(){
        double robotSpeed = gameInstance.robotSpeed;
        if(isDestroyed && !isStopped){
            for (Robot robot : gameInstance.robots){
                if (robot.speed == robotSpeed){
                    robot.speed = robot.speed * slowfactor;
                }
            }
        }
        if (isStopped){
            for (Robot robot : gameInstance.robots){
                if (robot.speed != robotSpeed){
                    robot.speed = robot.speed / slowfactor;
                }
            }
        }
    }

    private void removeObject(){
        gameContext.getChildren().remove(this);
        gameInstance.obstacles.remove(this);
        gameInstance.bonuses.remove(this);
    }
}
