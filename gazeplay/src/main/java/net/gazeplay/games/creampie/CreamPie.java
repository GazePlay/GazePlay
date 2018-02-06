package net.gazeplay.games.creampie;

import javafx.scene.image.Image;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.Portrait;
import net.gazeplay.commons.utils.RandomPositionGenerator;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Created by schwab on 12/08/2016.
 */
public class CreamPie implements GameLifeCycle {

    private final GameContext gameContext;

    private final Stats stats;

    private final Hand hand;

    private final Target target;

    public CreamPie(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;

        final Image[] availableImages = Portrait.loadAllImages();
        final RandomPositionGenerator randomPositionGenerator = gameContext.getRandomPositionGenerator();

        hand = new Hand();
        target = new Target(randomPositionGenerator, hand, stats, gameContext, availableImages);

        gameContext.getChildren().add(target);
        gameContext.getChildren().add(hand);
    }

    @Override
    public void launch() {
        hand.recomputePosition();

        gameContext.getRoot().widthProperty().addListener((obs, oldVal, newVal) -> {
            hand.recomputePosition();
        });
        gameContext.getRoot().heightProperty().addListener((obs, oldVal, newVal) -> {
            hand.recomputePosition();
        });

    }

    @Override
    public void dispose() {

    }
}
