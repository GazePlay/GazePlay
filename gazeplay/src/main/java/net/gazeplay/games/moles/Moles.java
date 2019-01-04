package net.gazeplay.games.moles;

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
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.*;

@Slf4j
public class Moles extends Parent implements GameLifeCycle {

    public final int nbHoles = 10;

    @Data
    @AllArgsConstructor
    public class RoundDetails {
        public final List<MolesChar> molesList;
    }

    GameContext gameContext;

    private final Stats stats;

    public Rectangle terrain;

    public int nbMolesWhacked;

    public int nbMolesOut;

    private Label lab;

    public RoundDetails currentRoundDetails;

    public Moles(GameContext gameContext, Stats stats) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.debug("dimension2D = {}", dimension2D);

    }

    @Override
    public void launch() {

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final Configuration config = Configuration.getInstance();

        Rectangle imageFond = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageFond.setFill(new ImagePattern(new Image("data/whackmole/images/molesGround.jpg")));
        int coef = (Configuration.getInstance().isBackgroundWhite()) ? 1 : 0;

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(coef * 0.9);

        imageFond.setEffect(colorAdjust);

        gameContext.getChildren().add(imageFond);

        List<MolesChar> molesList = initMoles(config);
        currentRoundDetails = new RoundDetails(molesList);
        this.getChildren().addAll(molesList);
        gameContext.getChildren().add(this);

        Rectangle imageFondTrans = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageFondTrans.setFill(new ImagePattern(new Image("data/whackmole/images/molesGroundTransparent.png")));

        imageFondTrans.setEffect(colorAdjust);

        gameContext.getChildren().add(imageFondTrans);

        this.nbMolesWhacked = 0;

        /* Score display */
        lab = new Label();
        String s = "Score:" + nbMolesWhacked;
        lab.setText(s);
        Color col = (Configuration.getInstance().isBackgroundWhite()) ? Color.BLACK : Color.WHITE;
        lab.setTextFill(col);
        lab.setFont(Font.font(dimension2D.getHeight() / 14));
        lab.setLineSpacing(10);
        lab.setLayoutX(0.4 * dimension2D.getWidth());
        lab.setLayoutY(0.08 * dimension2D.getHeight());
        gameContext.getChildren().add(lab);

        stats.notifyNewRoundReady();
        this.gameContext.resetBordersToFront();

        play(dimension2D);

    }

    /* Moles get out randomly */
    private synchronized void play(Dimension2D gameDim2D) {

        nbMolesOut = 0;
        Random r = new Random();

        Timer minuteur = new Timer();
        TimerTask tache = new TimerTask() {
            public void run() {

                int n = r.nextInt();
                if (nbMolesOut <= 3) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut <= 4) && (n % 4 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut <= 5) && (n % 8 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut <= 6) && (n % 12 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut <= 7) && (n % 16 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut <= 8) && (n % 20 == 0)) {
                    chooseMoleToOut(r);
                } else if ((nbMolesOut <= 9) && (n % 24 == 0)) {
                    chooseMoleToOut(r);
                }
            }
        };

        minuteur.schedule(tache, 0, 500);

    }

    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.molesList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.molesList);
                currentRoundDetails.molesList.removeAll(currentRoundDetails.molesList);
            }
            currentRoundDetails = null;
        }
    }

    /* Select a mole not out for the moment and call "getOut()" */
    public void chooseMoleToOut(Random r) {
        if (this.currentRoundDetails == null) {
            return;
        }
        int indice;
        do {
            indice = r.nextInt(nbHoles);
        } while (!currentRoundDetails.molesList.get(indice).canGoOut);
        MolesChar m = currentRoundDetails.molesList.get(indice);
        m.getOut();
    }

    private double[][] CreationTableauPlacement(double width, double height, double distTrans) {
        double tabPlacement[][] = new double[10][2];

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
        double height = gameDimension2D.getHeight();
        double width = gameDimension2D.getWidth();
        double distTrans = computeDistTransMole(gameDimension2D);

        double place[][] = CreationTableauPlacement(width, height, distTrans);

        /* Creation and placement of moles in the field */
        for (int i = 0; i < place.length; i++) {
            result.add(new MolesChar(place[i][0], place[i][1], moleWidth, moleHeight, distTrans, gameContext, stats,
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

    public void OneMoleWhacked() {
        nbMolesWhacked++;
        String s = "Score:" + nbMolesWhacked;
        stats.incNbGoals();
        stats.notifyNewRoundReady();
        lab.setText(s);
    }

}
