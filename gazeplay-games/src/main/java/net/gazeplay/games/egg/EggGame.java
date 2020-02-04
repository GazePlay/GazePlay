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

    private final int numberOfTurns;

    public EggGame(final IGameContext gameContext, final Stats stats, final int numOfTurns) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.numberOfTurns = numOfTurns;
    }

    @Override
    public void launch() {
        final Configuration config = gameContext.getConfiguration();

        final Egg egg = createEgg(config);

        gameContext.getChildren().add(egg);

        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    private Egg createEgg(final Configuration config) {
        final javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final double EggHeight = gameDimension2D.getHeight() / 2;
        final double EggWidth = 3. * EggHeight / 4.;

        final int fixationlength = config.getFixationLength();

        final double positionX = gameDimension2D.getWidth() / 2 - EggWidth / 2;
        final double positionY = gameDimension2D.getHeight() / 2 - EggHeight / 2;

        return new Egg(positionX, positionY, EggWidth, EggHeight, gameContext, stats, this, fixationlength, numberOfTurns);
    }

}
