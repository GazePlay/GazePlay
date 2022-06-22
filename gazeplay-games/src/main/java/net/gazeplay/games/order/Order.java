/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.order;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * @author vincent
 */
@Slf4j
public class Order implements GameLifeCycle {
    private final IGameContext gameContext;
    private final Stats stats;
    private final boolean inReplayMode;
    private int currentNum;
    private final int nbTarget;
    private final ReplayablePseudoRandom randomGenerator;
    @Getter
    private Target[] tabTarget;


    public Order(IGameContext gameContext, int nbTarget, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.currentNum = 0;
        this.nbTarget = nbTarget;
        //this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setCurrentGameSeed(randomGenerator.getSeed());
        this.inReplayMode = false;
    }

    public Order(IGameContext gameContext, int nbTarget, Stats stats, double gameSeed) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.currentNum = 0;
        this.nbTarget = nbTarget;
        //this.gameContext.startScoreLimiter();
        this.gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
        this.inReplayMode = true;
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.start();
        spawn();
    }

    public void enter(Target t) {
        handleAnswer(t, this.currentNum == t.getNum() - 1);

        if (this.currentNum == nbTarget) {
            for (Target target : tabTarget) {
                target.removeEvent();
            }
            gameContext.updateScore(stats, this);
            gameContext.playWinTransition(20, actionEvent -> Order.this.restart());
        }
    }

    private void handleAnswer(Target t, boolean correct) {

        Circle c = new Circle(t.getPos().getX(), t.getPos().getY(), t.getRadius());
        if (correct) {
            if (!inReplayMode) {
                stats.incrementNumberOfGoalsReached();
            }
            c.setFill(new ImagePattern(new Image("data/order/images/success.png"), 0, 0, 1, 1, true));
        } else {
            c.setFill(new ImagePattern(new Image("data/order/images/fail.png"), 0, 0, 1, 1, true));
            for (Target target : tabTarget) {
                target.removeEvent();
            }
        }
        this.gameContext.getChildren().add(c);

        Timeline pause = new Timeline();
        pause.getKeyFrames().add(new KeyFrame(Duration.seconds(1)));
        pause.setOnFinished(actionEvent -> {
            gameContext.getChildren().remove(c);
            if (!correct) {
                restart();
            }
        });

        this.gameContext.getChildren().remove(t);
        this.currentNum++;
        pause.play();
    }

    public void spawn() {
        tabTarget = new Target[nbTarget];
        Timeline timer = new Timeline();

        timer.setOnFinished(new EventHandler<>() {
            int i = 0;

            @Override
            public void handle(ActionEvent actionEvent) {
                Target t = new Target(Order.this, gameContext, i + 1, gameContext.getConfiguration().getFixationLength(), randomGenerator);
                gameContext.getChildren().add(t);
                tabTarget[i] = t;
                i++;
                if (i < nbTarget) {
                    timer.getKeyFrames()
                        .add(new KeyFrame(Duration.seconds(1)));
                    timer.play();
                } else {
                    stats.notifyNewRoundReady();
                    for (int j = 0; j < nbTarget; j++) {
                        tabTarget[j].addEvent();
                        stats.incrementNumberOfGoalsToReach();
                    }
                }
            }
        });
        timer.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        timer.play();
    }

    private void restart() {
        this.dispose();
        this.launch();
    }

    @Override
    public void dispose() {
        this.currentNum = 0;
        gameContext.clear();
    }

}
