package net.gazeplay.games.moles;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.BackgroundStyleVisitor;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Moles extends Parent implements GameLifeCycle {

    @Data
    @AllArgsConstructor
    public static class RoundDetails {
        public final List<MolesChar> molesList;
    }

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    private int nbMolesWhacked;

    @Getter
    private AtomicInteger nbMolesOut = new AtomicInteger(0);

    private Label lab;

    private RoundDetails currentRoundDetails;
  
    @Getter
    @Setter
    private ArrayList<TargetAOI> targetAOIList;
    private double moleRadius;

    private Timer minuteur;

    Moles(IGameContext gameContext, Stats stats) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        targetAOIList = new ArrayList<>();
        moleRadius = 0;
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
    }

    @Override
    public void launch() {

        if (currentRoundDetails != null) {
            if (currentRoundDetails.molesList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.molesList);
                currentRoundDetails.molesList.clear();
            }
            currentRoundDetails = null;
        }
        targetAOIList.clear();
        gameContext.getChildren().clear();

        gameContext.setLimiterAvailable();
        gameContext.start();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final Configuration config = gameContext.getConfiguration();

        Rectangle imageFond = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageFond.setFill(new ImagePattern(new Image("data/whackmole/images/molesGround.jpg")));
        adjustBackground(imageFond);
        gameContext.getChildren().add(imageFond);

        List<MolesChar> molesList = initMoles(config);
        currentRoundDetails = new RoundDetails(molesList);
        this.getChildren().addAll(molesList);
        gameContext.getChildren().add(this);

        Rectangle imageFondTrans = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageFondTrans.setFill(new ImagePattern(new Image("data/whackmole/images/molesGroundTransparent.png")));
        adjustBackground(imageFondTrans);
        gameContext.getChildren().add(imageFondTrans);

        this.nbMolesWhacked = 0;

        /* Score display */
        lab = new Label();
        String s = "Score:" + nbMolesWhacked;
        lab.setText(s);
        Color col = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Color>() {
            @Override
            public Color visitLight() {
                return Color.BLACK;
            }

            @Override
            public Color visitDark() {
                return Color.WHITE;
            }
        });
        lab.setTextFill(col);
        lab.setFont(Font.font(dimension2D.getHeight() / 14));
        lab.setLineSpacing(10);
        lab.setLayoutX(0.4 * dimension2D.getWidth());
        lab.setLayoutY(0.08 * dimension2D.getHeight());
        gameContext.getChildren().add(lab);

        this.gameContext.resetBordersToFront();
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        play();

    }

    void adjustBackground(Rectangle image) {
        int backgroundStyleCoef = gameContext.getConfiguration().getBackgroundStyle().accept(new BackgroundStyleVisitor<Integer>() {
            @Override
            public Integer visitLight() {
                return 2;
            }

            @Override
            public Integer visitDark() {
                return 0;
            }
        });

        ColorAdjust colorAdjust = new ColorAdjust();

        if (gameContext.getConfiguration().isBackgroundEnabled()) {
            colorAdjust.setBrightness(backgroundStyleCoef * 0.25); //0.5 or 0
        } else {
            colorAdjust.setBrightness(backgroundStyleCoef - 1); //1 or -1
        }

        image.setEffect(colorAdjust);
    }

    /* Moles get out randomly */
    private synchronized void play() {

        nbMolesOut = new AtomicInteger(0);
        ReplayablePseudoRandom r = new ReplayablePseudoRandom();

        minuteur = new Timer();
        TimerTask tache = new TimerTask() {
            public void run() {

                int n = (int) r.random();
                if (nbMolesOut.get() <= 3) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut.get() <= 4) && (n % 4 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut.get() <= 5) && (n % 8 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut.get() <= 6) && (n % 12 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut.get() <= 7) && (n % 16 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut.get() <= 8) && (n % 20 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut.get() <= 9) && (n % 24 == 0)) {
                    chooseMoleToOut(r);
                }
            }
        };

        minuteur.schedule(tache, 0, 500);

    }

    @Override
    public void dispose() {
        stats.setTargetAOIList(targetAOIList);
        if (currentRoundDetails != null) {
            if (currentRoundDetails.molesList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.molesList);
                currentRoundDetails.molesList.clear();
            }
            currentRoundDetails = null;
        }
    }

    /* Select a mole not out for the moment and call "getOut()" */
    private void chooseMoleToOut(ReplayablePseudoRandom r) {
        if (this.currentRoundDetails == null) {
            return;
        }
        int indice;
        do {
            int nbHoles = 10;
            indice = r.nextInt(nbHoles);
        } while (!currentRoundDetails.molesList.get(indice).canGoOut);
        MolesChar m = currentRoundDetails.molesList.get(indice);
        final TargetAOI targetAOI = new TargetAOI(m.getPositionX(), m.getPositionY(), (int)moleRadius/3,
            System.currentTimeMillis());
        targetAOIList.add(targetAOI);
        m.setTargetAOIListIndex(targetAOIList.size()-1);
        m.getOut();
        stats.incrementNumberOfGoalsToReach();
    }

    private double[][] creationTableauPlacement(double width, double height, double distTrans) {
        double[][] tabPlacement = new double[10][2];

        tabPlacement[0][0] = 0.05 * width;
        tabPlacement[0][1] = 0.190 * height + distTrans;
        tabPlacement[1][0] = 0.382 * width;
        tabPlacement[1][1] = 0.185 * height + distTrans;
        tabPlacement[2][0] = 0.75 * width;
        tabPlacement[2][1] = 0.097 * height + distTrans;
        tabPlacement[3][0] = 0.22 * width;
        tabPlacement[3][1] = 0.345 * height + distTrans;
        tabPlacement[4][0] = 0.62 * width;
        tabPlacement[4][1] = 0.29 * height + distTrans;
        tabPlacement[5][0] = 0.468 * width;
        tabPlacement[5][1] = 0.465 * height + distTrans;
        tabPlacement[6][0] = 0.837 * width;
        tabPlacement[6][1] = 0.42 * height + distTrans;
        tabPlacement[7][0] = 0.059 * width;
        tabPlacement[7][1] = 0.531 * height + distTrans;
        tabPlacement[8][0] = 0.28 * width;
        tabPlacement[8][1] = 0.63 * height + distTrans;
        tabPlacement[9][0] = 0.67 * width;
        tabPlacement[9][1] = 0.59 * height + distTrans;

        return tabPlacement;
    }

    private List<MolesChar> initMoles(Configuration config) {
        javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        ArrayList<MolesChar> result = new ArrayList<>();

        double moleHeight = computeMoleHeight(gameDimension2D);

        double moleWidth = computeMoleWidth(gameDimension2D);
        this.moleRadius = moleWidth;
        double height = gameDimension2D.getHeight();
        double width = gameDimension2D.getWidth();
        double distTrans = computeDistTransMole(gameDimension2D);

        double[][] place = creationTableauPlacement(width, height, distTrans);

        /* Creation and placement of moles in the field */
        for (double[] doubles : place) {
            result.add(new MolesChar(doubles[0], doubles[1], moleWidth, moleHeight, distTrans, gameContext,
                this));
        }

        return result;
    }

    private static double computeDistTransMole(Dimension2D gameDimension2D) {
        return gameDimension2D.getHeight() * 0.16;
    }

    private static double computeMoleHeight(Dimension2D gameDimension2D) {
        return gameDimension2D.getHeight() * 0.14;
    }

    private static double computeMoleWidth(Dimension2D gameDimension2D) {
        return gameDimension2D.getWidth() * 0.13;
    }

    void oneMoleWhacked() {

        nbMolesWhacked++;
        String s = "Score:" + nbMolesWhacked;
        stats.incrementNumberOfGoalsReached();
        EventHandler<ActionEvent> limiterEndEventHandler = e -> {
            minuteur.cancel();
            minuteur.purge();
        };
        gameContext.updateScore(stats,this, limiterEndEventHandler, limiterEndEventHandler);
        lab.setText(s);

    }
}
