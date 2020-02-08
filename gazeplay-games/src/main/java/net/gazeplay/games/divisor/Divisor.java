package net.gazeplay.games.divisor;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Created by givaudan on 15/02/2018.
 */
@Slf4j
public class Divisor implements GameLifeCycle {
    private final IGameContext gameContext;
    private final Stats stats;
    private final boolean lapin;

    public Divisor(final IGameContext gameContext, final Stats stats, final boolean lapin) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.lapin = lapin;
    }

    @Override
    public void launch() {
        final Target target;

        if (lapin) {
            final ImageLibrary imageLibrary = ImageUtils.createCustomizedImageLibrary(null, "divisor/rabbit/images");

            final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            final Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            try {
                imageRectangle.setFill(new ImagePattern(new Image("data/divisor/images/Background.png")));
                final int coef = (gameContext.getConfiguration().isBackgroundWhite()) ? 1 : 0;
                imageRectangle.setOpacity(1 - coef * 0.9);

            } catch (final Exception e) {
                log.debug("File not found : {}", e.getMessage());
            }
            gameContext.getChildren().add(imageRectangle);
            this.gameContext.resetBordersToFront();

            target = new Target(gameContext, stats, imageLibrary, 0, System.currentTimeMillis(), this,
                this.gameContext.getRandomPositionGenerator().newRandomPosition(100), lapin);
        } else {

            final ImageLibrary imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubDirectory("divisor/basic"));
            target = new Target(gameContext, stats, imageLibrary, 0, System.currentTimeMillis(), this,
                this.gameContext.getRandomPositionGenerator().newRandomPosition(100), lapin);
        }
        gameContext.getChildren().add(target);
    }

    public void restart() {
        this.dispose();
        gameContext.showRoundStats(stats, this);
    }

    @Override
    public void dispose() {
        this.gameContext.clear();
    }

}
