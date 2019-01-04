package net.gazeplay.games.divisor;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

/**
 * Created by givaudan on 15/02/2018.
 */
@Slf4j
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
            ImageLibrary imageLibrary = ImageUtils.createCustomizedImageLibrary(null, "divisor/rabbit/images");

            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            try {
                imageRectangle.setFill(new ImagePattern(new Image("data/divisor/images/Background.png")));
                int coef = (Configuration.getInstance().isBackgroundWhite()) ? 1 : 0;
                imageRectangle.setOpacity(1 - coef * 0.9);

            } catch (Exception e) {
                log.debug("File not found : {}", e.getMessage());
            }
            gameContext.getChildren().add(imageRectangle);
            this.gameContext.resetBordersToFront();

            target = new Target(gameContext, stats, imageLibrary, 0, System.currentTimeMillis(), this,
                    this.gameContext.getRandomPositionGenerator().newRandomPosition(100), lapin);
        } else {

            ImageLibrary imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubDirectory("divisor/basic"));
            target = new Target(gameContext, stats, imageLibrary, 0, System.currentTimeMillis(), this,
                    this.gameContext.getRandomPositionGenerator().newRandomPosition(100), lapin);
        }
        gameContext.getChildren().add(target);
    }

    public void restart() {
        this.dispose();
        this.launch();
    }

    @Override
    public void dispose() {
        this.gameContext.clear();
    }

}
