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
import javafx.util.Duration;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * @author vincent
 */
public class Spawner {
    private final RandomPositionGenerator randomPosGenerator;
    private final Stats stats;
    private final GameContext gameContext;

    public Spawner(RandomPositionGenerator randomPosGenerator, Stats stats, GameContext gameContext) {
        this.randomPosGenerator = randomPosGenerator;
        this.stats = stats;
        this.gameContext = gameContext;
    }

    public void spawn(int nbTarget, Order game) {
        Target[] tabTarget = new Target[nbTarget];
        Timeline timer = new Timeline();
        timer.getKeyFrames().add(new KeyFrame(Duration.seconds(1)));
        timer.setOnFinished(new EventHandler<ActionEvent>() {
            int i = 0;

            @Override
            public void handle(ActionEvent actionEvent) {
                Target t = new Target(100, randomPosGenerator, stats, Portrait.createImageLibrary(), game, gameContext,
                        i + 1);
                gameContext.getChildren().add(t);
                tabTarget[i] = t;
                i++;
                if (i < nbTarget) {
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
}
