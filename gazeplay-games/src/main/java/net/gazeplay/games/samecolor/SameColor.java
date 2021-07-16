package net.gazeplay.games.samecolor;

import javafx.event.ActionEvent;
import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
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

    private final Dimension2D dimension2D;

    private final ImageLibrary backgroundImage;

    private int nbgoals;

    private final List<DoubleRec> doubleRecList;

    private int[] place;

    private Color[] colorList;

    SameColor(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant) {

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom();
        this.stats.setGameSeed(random.getSeed());

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        this.backgroundImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions"), random);

        doubleRecList = new ArrayList<>();
    }

    SameColor(IGameContext gameContext, Stats stats, DimensionGameVariant gameVariant, double gameSeed) {

        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;

        this.random = new ReplayablePseudoRandom(gameSeed);

        this.dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        this.backgroundImage = ImageUtils.createImageLibrary(Utils.getImagesSubdirectory("opinions"), random);

        doubleRecList = new ArrayList<>();
    }

    public void launch(){
        gameContext.getChildren().clear();
        doubleRecList.clear();

        double width = dimension2D.getWidth() / gameVariant.getWidth();
        double height = dimension2D.getHeight() / gameVariant.getHeight();

        nbgoals = gameVariant.getHeight()*gameVariant.getWidth()/2;

        colorList = new Color[]
            {
              Color.AQUA, Color.BLUE, Color.CHARTREUSE, Color.CORAL, Color.CRIMSON, Color.DARKORANGE, Color.DARKORCHID,
                Color.DEEPPINK, Color.FUCHSIA, Color.GOLD, Color.PLUM, Color.PURPLE, Color.RED, Color.TOMATO, Color.SIENNA,
                Color.SEAGREEN, Color.NAVY, Color.MISTYROSE
            };

        place = new int[nbgoals*2];
        for (int i=0; i<nbgoals; i++){
            place[2*i] = i;
            place[2*i+1] = i;
        }
        shuffle();

        Rectangle background = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        background.widthProperty().bind(gameContext.getRoot().widthProperty());
        background.heightProperty().bind(gameContext.getRoot().heightProperty());

        gameContext.getChildren().add(background);
        background.setFill(new ImagePattern(backgroundImage.pickRandomImage()));

        int a, b;
        for (int i=0; i<nbgoals; i++){
            a = -1;
            b = -1;
            for (int j=0; j<place.length && b<0; j++){
                if (place[j]==i){
                    if (a<0){
                        a = j;
                    } else {
                        b = j;
                    }
                }
            }
            doubleRecList.add(new DoubleRec(width * (a % gameVariant.getWidth()), height * (a / gameVariant.getWidth()), width * (b % gameVariant.getWidth()), height * (b / gameVariant.getWidth()), width, height, colorList[i]));
        }

        javafx.event.EventHandler<ActionEvent> eventwin = e -> {
            stats.incrementNumberOfGoalsReached();
            testwin();
        };

        for (DoubleRec doubleRec : doubleRecList){
            gameContext.getChildren().addAll(doubleRec.rec1, doubleRec.rec2);
            doubleRec.setEvent(eventwin, gameContext.getGazeDeviceManager(), gameContext, doubleRecList, random);
        }

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    public void dispose(){
        doubleRecList.clear();
    }

    private void testwin(){
        if (doubleRecList.isEmpty()){
            stats.stop();

            gameContext.updateScore(stats, this);

            gameContext.playWinTransition(500, actionEvent -> {

                gameContext.getGazeDeviceManager().clear();

                gameContext.clear();

                gameContext.showRoundStats(stats, this);
            });
        }
    }

    private void shuffle(){
        for (int i=0; i<20; i++){
            int a = random.nextInt(nbgoals*2);
            int b = random.nextInt(nbgoals*2);

            int temp = place[a];
            place[a] = place[b];
            place[b] = temp;

            a = random.nextInt(colorList.length);
            b = random.nextInt(colorList.length);

            Color Ctemp = colorList[a];
            colorList[a] = colorList[b];
            colorList[b] = Ctemp;
        }
    }
}
