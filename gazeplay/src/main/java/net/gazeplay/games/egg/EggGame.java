package net.gazeplay.games.egg;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by schwab on 17/09/2016.
 */
@Slf4j
public class EggGame implements GameLifeCycle {

    private final GameContext gameContext;

    private final Stats stats;

    private Egg egg;

    public EggGame(GameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
    }

    @Override
    public void launch() {
        final Configuration config = Configuration.getInstance();

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
