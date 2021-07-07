package net.gazeplay.games.samecolor;

import javafx.geometry.Dimension2D;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

@Slf4j
public class SameColor implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private final DimensionGameVariant gameVariant;

    private final ReplayablePseudoRandom random;

    private final Translator translator;

    private final Dimension2D dimension2D;

    private final ReplayablePseudoRandom randomGenerator;

    private final ImageLibrary backgroundImage;

    private Rectangle background;

    SameColor(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant) {

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom();
        this.stats.setGameSeed(random.getSeed());

        this.translator = gameContext.getTranslator();

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        this.randomGenerator = new ReplayablePseudoRandom();

        this.backgroundImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions"), randomGenerator);
    }

    SameColor(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant, double gameSeed) {

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom(gameSeed);

        this.translator = gameContext.getTranslator();

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);

        this.backgroundImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions"), randomGenerator);
    }

    public void launch(){
        gameContext.getChildren().clear();

        background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());

        gameContext.getChildren().add(background);
        background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    public void dispose(){

    }
}
