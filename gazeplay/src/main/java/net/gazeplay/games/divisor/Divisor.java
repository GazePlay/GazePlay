package net.gazeplay.games.divisor;

import net.gazeplay.GameLifeCycle;
import javafx.scene.image.Image;
import net.gazeplay.GameContext;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.stats.Stats;

/**
 *
 * Created by givaudan on 15/02/2018.
 */
public class Divisor implements GameLifeCycle {
    private final GameContext gameContext;
    private final Stats stats;
    private final boolean lapin;

    public Divisor(GameContext gameContext, Stats stats, boolean lapin) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.lapin = lapin;
    }

    @Override
    public void launch() {
        Target target;
        if (lapin) {
            Image[] images = { new Image("data/divisor/images/1.png"), new Image("data/divisor/images/2.png"),
                    new Image("data/divisor/images/3.png"), new Image("data/divisor/images/4.png"),
                    new Image("data/divisor/images/5.png"), new Image("data/divisor/images/6.png"),
                    new Image("data/divisor/images/7.png") };
            target = new Target(gameContext, gameContext.getRandomPositionGenerator(), stats, images, 0,
                    System.currentTimeMillis(), this);
        } else {
            target = new Target(gameContext, gameContext.getRandomPositionGenerator(), stats, Portrait.loadAllImages(),
                    0, System.currentTimeMillis(), this);
        }
        gameContext.getChildren().add(target);
    }

    @Override
    public void dispose() {
        this.gameContext.getChildren().removeAll();
    }

}
