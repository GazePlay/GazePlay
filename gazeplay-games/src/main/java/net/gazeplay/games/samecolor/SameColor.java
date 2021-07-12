package net.gazeplay.games.samecolor;

import javafx.event.ActionEvent;
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

import java.beans.EventHandler;
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

    private double width;
    private double height;

    private int[] place;

    private List<Color> colorList;

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

        width = dimension2D.getWidth() / gameVariant.getWidth();
        height = dimension2D.getHeight() / gameVariant.getHeight();

        nbgoalsreached = 0;
        nbgoals = gameVariant.getHeight()*gameVariant.getWidth()/2;

        place = new int[nbgoals*2];
        for (int i=0; i<nbgoals; i++){
            place[2*i] = i;
            place[2*i+1] = i;
        }
        shuffle(20);

        background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());

        gameContext.getChildren().add(background);
        background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));

        DoubleRec test = new DoubleRec(0,0,dimension2D.getWidth()/2, dimension2D.getHeight()/2, width, height/2, Color.RED);
        gameContext.getChildren().addAll(test.rec1, test.rec2);
        doubleRecList.add(test);

        javafx.event.EventHandler<ActionEvent> eventwin = e -> {
            stats.incrementNumberOfGoalsReached();
            nbgoalsreached++;
            testwin();
        };

        for (DoubleRec doubleRec : doubleRecList){
            doubleRec.setEvent(eventwin, gameContext.getGazeDeviceManager(), gameContext, doubleRecList);
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

    private void shuffle(int n){
        if (n>0){
            int a = random.nextInt(nbgoals*2);
            int b = random.nextInt(nbgoals*2);

            int temp = place[a];
            place[a] = place[b];
            place[b] = temp;

            shuffle(n-1);
        }
    }
}
