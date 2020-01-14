package net.gazeplay.games.egg;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;


@Slf4j
public class EggGame implements GameLifeCycle {

    private final IGameContext gameContext;

    private final Stats stats;

    private Egg egg;

    public EggGame(IGameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
    }

    @Override
    public void launch() {
        final Configuration config = gameContext.getConfiguration();

        egg = createEgg(config);

        gameContext.getChildren().add(egg);

        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {
        if (egg != null) {
            gameContext.getChildren().remove(egg);
            egg = null;
        }
    }

    private Egg createEgg(Configuration config) {
        javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final double EggHeight = gameDimension2D.getHeight() / 2;
        final double EggWidth = 3. * EggHeight / 4.;

        final int fixationlength = config.getFixationLength();

        double positionX = gameDimension2D.getWidth() / 2 - EggWidth / 2;
        double positionY = gameDimension2D.getHeight() / 2 - EggHeight / 2;

        Egg egg1 = new Egg(positionX, positionY, EggWidth, EggHeight, gameContext, stats, this, fixationlength);

        return egg1;
    }

}
