package net.gazeplay.games.divisor;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
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
    private final boolean isRabbit;

    public Divisor(final IGameContext gameContext, final Stats stats, final boolean isRabbit) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.isRabbit = isRabbit;
        this.gameContext.startTimeLimiter();
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        final Target target;
        final ImageLibrary imageLibrary;


        if (isRabbit) {
            imageLibrary = ImageUtils.createCustomizedImageLibrary(null, "divisor/rabbit/images");
            initBackground();
            gameContext.resetBordersToFront();
        } else {
            imageLibrary = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("portraits"));
        }

        this.stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.incrementNumberOfGoalsToReach(15);

         target = new Target(gameContext, stats, imageLibrary, 0,System.currentTimeMillis(), this,
            this.gameContext.getRandomPositionGenerator().newRandomPosition(100), isRabbit);

        gameContext.getChildren().add(target);
        gameContext.firstStart();
    }

    private void initBackground() {
        if (gameContext.getConfiguration().isBackgroundEnabled()) {
            Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
            Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
            imageRectangle.setFill(new ImagePattern(new Image("data/divisor/images/Background.png")));
            double imageRectangleOpacity = gameContext.getConfiguration().getBackgroundStyle().accept(
                new BackgroundStyleVisitor<>() {
                    @Override
                    public Double visitLight() {
                        return 0.5;
                    }

                    @Override
                    public Double visitDark() {
                        return 1.d;
                    }
            });
            imageRectangle.setOpacity(imageRectangleOpacity);
            gameContext.getChildren().add(imageRectangle);
        }
    }

    public void restart() {
        this.dispose();
        //this.launch();
        //gameContext.onGameStarted();
        gameContext.showRoundStats(stats, this);
    }

    @Override
    public void dispose() {
        this.gameContext.clear();
    }

}
