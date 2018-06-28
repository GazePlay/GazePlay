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
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * @author vincent
 */
@Slf4j
public class Order implements GameLifeCycle {
    private final GameContext gameContext;
    private final Stats stats;
    private int currentNum;
    private final int nbTarget;

    public Order(GameContext gameContext, int nbTarget, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.currentNum = 0;
        this.nbTarget = nbTarget;
    }

    @Override
    public void launch() {
        spawn();
        this.stats.notifyNewRoundReady();
    }

    public void enter(Target t) {
        handleAnswer(t, this.currentNum == t.getNum() - 1);

        if (this.currentNum == nbTarget) {
            gameContext.playWinTransition(20, new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    Order.this.restart();
                }
            });
        }
    }

    private void handleAnswer(Target t, boolean correct) {
        stats.incNbGoals();

        Circle c = new Circle(t.getPos().getX(), t.getPos().getY(), t.getRadius());
        if (correct == true) {
            c.setFill(new ImagePattern(new Image("data/order/images/success.png"), 0, 0, 1, 1, true));
        } else {
            c.setFill(new ImagePattern(new Image("data/order/images/fail.png"), 0, 0, 1, 1, true));
        }
        this.gameContext.getChildren().add(c);

        Timeline pause = new Timeline();
        pause.getKeyFrames().add(new KeyFrame(Duration.seconds(1)));
        pause.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Order.this.gameContext.getChildren().remove(c);
                if (correct == false) {
                    Order.this.restart();
                }
            }
        });

        this.gameContext.getChildren().remove(t);
        this.currentNum++;
        pause.play();
    }

    public void spawn() {
        Target[] tabTarget = new Target[nbTarget];
        Timeline timer = new Timeline();

        timer.setOnFinished(new EventHandler<ActionEvent>() {
            int i = 0;

            @Override
            public void handle(ActionEvent actionEvent) {
                Target t = new Target(Order.this, gameContext, i + 1);
                gameContext.getChildren().add(t);
                tabTarget[i] = t;
                i++;
                if (i < nbTarget) {
                    timer.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(Configuration.getInstance().getSpeedEffects() * 1)));
                    timer.play();
                } else {
                    for (int j = 0; j < nbTarget; j++) {
                        tabTarget[j].addEvent();
                    }
                }
            }
        });
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
