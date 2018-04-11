/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.gazeplay.games.order;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

    public void spawn(int nbTarget) {
        Timeline waitbeforestart = new Timeline();
        waitbeforestart.getKeyFrames().add(new KeyFrame(Duration.seconds(2)));
        for (int i = 0; i < nbTarget; i++) {
            Target t = new Target(100, randomPosGenerator, stats, Portrait.loadAllImages(), i + 1);
            gameContext.getChildren().add(t);
            waitbeforestart.play();
        }
    }
}
