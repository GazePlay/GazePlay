package net.gazeplay.games.samecolor;

import javafx.geometry.Dimension2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.ImageLibrary;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.List;

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

    private int nbgoals;
    private int nbgoalsreached;

    private List<DoubleRec> doubleRecList;

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

        doubleRecList = new ArrayList<>();
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

        doubleRecList = new ArrayList<>();
    }

    public void launch(){
        gameContext.getChildren().clear();
        doubleRecList.clear();

        nbgoalsreached = 0;
        nbgoals = gameVariant.getHeight()*gameVariant.getWidth()/2;

        background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());

        gameContext.getChildren().add(background);
        background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));

        DoubleRec test = new DoubleRec(0,0,dimension2D.getWidth()/2, dimension2D.getHeight()/2, dimension2D.getWidth()/2, dimension2D.getHeight()/2, Color.RED);
        gameContext.getChildren().addAll(test.rec1, test.rec2);
        doubleRecList.add(test);

        for (DoubleRec doubleRec : doubleRecList){
            doubleRec.rec1.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
                doubleRec.isin1 = true;
                sameselect(doubleRec);
            });
            doubleRec.rec2.addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
                doubleRec.isin2 = true;
                sameselect(doubleRec);
            });
            //doubleRec.rec1.addEventFilter(MouseEvent.MOUSE_EXITED, e -> doubleRec.isin1 = false);
            //doubleRec.rec2.addEventFilter(MouseEvent.MOUSE_EXITED, e -> doubleRec.isin2 = false);

            doubleRec.rec1.addEventHandler(GazeEvent.GAZE_ENTERED,e -> {
                doubleRec.isin1 = true;
                sameselect(doubleRec);
            });
            doubleRec.rec2.addEventHandler(GazeEvent.GAZE_ENTERED, e -> {
                doubleRec.isin2 = true;
                sameselect(doubleRec);
            });
            //doubleRec.rec1.addEventHandler(GazeEvent.GAZE_EXITED, e -> doubleRec.isin1 = false);
            //doubleRec.rec2.addEventHandler(GazeEvent.GAZE_EXITED, e -> doubleRec.isin2 = false);
            gameContext.getGazeDeviceManager().addEventFilter(doubleRec.rec1);
            gameContext.getGazeDeviceManager().addEventFilter(doubleRec.rec2);
        }

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    public void dispose(){
        doubleRecList.clear();
    }

    private void testwin(){
        if (nbgoalsreached >= nbgoals){
            stats.stop();

            gameContext.updateScore(stats, this);

            gameContext.playWinTransition(500, actionEvent -> {

                gameContext.getGazeDeviceManager().clear();

                gameContext.clear();

                gameContext.showRoundStats(stats, this);
            });
        }
    }

    private void sameselect(DoubleRec doubleRec){
        if (doubleRec.isin1 && doubleRec.isin2){
            gameContext.getChildren().removeAll(doubleRec.rec1, doubleRec.rec2);
            doubleRecList.remove(doubleRec);
            stats.incrementNumberOfGoalsReached();
            nbgoalsreached++;
            testwin();
        }
    }
}
