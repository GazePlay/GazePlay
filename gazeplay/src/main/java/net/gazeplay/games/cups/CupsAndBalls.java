package net.gazeplay.games.cups;

import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.cups.utils.Action;
import net.gazeplay.games.cups.utils.PositionCup;
import net.gazeplay.games.cups.utils.Strategy;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class CupsAndBalls implements GameLifeCycle {

    private final GameContext gameContext;
    private final Stats stats;
    private Ball ball;
    private Cup cups[];
    private int nbCups;
    private final int nbLines;
    private final int nbColumns;
    private int nbExchanges;

    private javafx.geometry.Dimension2D dimension2D;
    private final int openCupSpeed = 1000;
    private final int exchangeCupDuration = 1000;
    private final int ballRadius = 20;

    private final Random random = new Random();
    Strategy strategy;
    ArrayList<Action> actions;

    public CupsAndBalls(GameContext gameContext, Stats stats, int nbCups) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.stats.start();
        this.nbCups = nbCups;
        this.cups = new Cup[nbCups];
        this.nbColumns = nbCups;
        this.nbLines = nbCups;
        this.nbExchanges = nbCups * nbCups;
    }

    public CupsAndBalls(GameContext gameContext, Stats stats, int nbCups, int nbExchanges) {
        this(gameContext, stats, nbCups);
        this.nbExchanges = nbExchanges;
    }

    private void init() {
        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        Image cupPicture = new Image("data/cups/images/cup.png");
        double imageWidth = dimension2D.getHeight() / (nbColumns * 1.5);
        double imageHeight = dimension2D.getHeight() / nbColumns;

        int ballInCup = random.nextInt(nbCups);
        Point posCup;
        for (int indexCup = 0; indexCup < cups.length; indexCup++) {
            PositionCup position = new PositionCup(indexCup, nbColumns / 2, nbColumns, nbLines, dimension2D.getHeight(),
                    dimension2D.getWidth(), imageWidth, imageHeight);
            posCup = position.calculateXY(position.getCellX(), position.getCellY());
            ImageView cupRectangle = new ImageView(cupPicture);
            cupRectangle.setX(posCup.getX());
            cupRectangle.setY(posCup.getY());
            cupRectangle.setFitWidth(imageWidth);
            cupRectangle.setFitHeight(imageHeight);
            cups[indexCup] = new Cup(cupRectangle, position, gameContext, stats, this, openCupSpeed);
            if (indexCup == ballInCup) {
                cups[indexCup].setWinner(true);
                cups[indexCup].giveBall(true);
                ball = new Ball(ballRadius, Color.RED, cups[indexCup]);
                cups[indexCup].setBall(ball);
                cups[indexCup].setBallRadius(ballRadius);
                gameContext.getChildren().add(ball.getItem());
            } else {
                cups[indexCup].giveBall(false);
                cups[indexCup].setBallRadius(ballRadius);
            }
            gameContext.getChildren().add(cupRectangle);
            cups[indexCup].getProgressIndicator().toFront();
        }
    }

    @Override
    public void launch() {
        init();
        TranslateTransition revealBallTransition = null;
        for (Cup cup : cups) {
            if (cup.containsBall()) {
                revealBallTransition = new TranslateTransition(Duration.millis(openCupSpeed), cup.getItem());
                revealBallTransition.setByY(-ballRadius * 8);
                revealBallTransition.setAutoReverse(true);
                revealBallTransition.setCycleCount(2);
            }
        }

        this.strategy = new Strategy(nbCups, nbExchanges, nbColumns, nbLines);
        this.actions = strategy.chooseStrategy();

        for (Cup cup : cups) {
            cup.setActionsToDo(actions.size());
        }
        if (revealBallTransition != null) {
            revealBallTransition.play();
            revealBallTransition.setOnFinished(e -> {
                ball.getItem().setVisible(false);
                createNewTransition(actions);
            });
        }

    }

    @Override
    public void dispose() {

    }

    private void createNewTransition(ArrayList<Action> actions) {
        int initCellX = actions.get(0).getInitCellX();
        int initCellY = actions.get(0).getInitCellY();
        int finalCellX = actions.get(0).getTargetCellX();
        int finalCellY = actions.get(0).getTargetCellY();

        ImageView cupToMove = null;
        Point initPos = null;
        Point newPos = null;
        for (int indexCup = 0; indexCup < nbCups; indexCup++) {
            Cup currentCup = cups[indexCup];
            if (currentCup.getPositionCup().getCellX() == initCellX
                    && currentCup.getPositionCup().getCellY() == initCellY) {
                cupToMove = currentCup.getItem();
                initPos = currentCup.getPositionCup().calculateXY(initCellX, initCellY);
                newPos = currentCup.getPositionCup().calculateXY(finalCellX, finalCellY);
                currentCup.getPositionCup().setCellX(finalCellX);
                currentCup.getPositionCup().setCellY(finalCellY);
                currentCup.progressBarUpdatePosition(newPos.getX(), newPos.getY());
                if (currentCup.containsBall()) {
                    currentCup.getBall().updatePosition(newPos.getX(), newPos.getY());
                }
            }
        }

        if (newPos == null || initPos == null || cupToMove == null) {
            log.error("The cup positions haven't been set up properly");
        }

        TranslateTransition movementTransition = new TranslateTransition(Duration.millis(exchangeCupDuration),
                cupToMove);
        movementTransition.setByX(newPos.getX() - initPos.getX());
        movementTransition.setByY(newPos.getY() - initPos.getY());

        movementTransition.setOnFinished(e -> {
            actions.remove(0);
            if (actions.size() > 0) {
                for (Cup cup : cups) {
                    cup.increaseActionsDone();
                }

                createNewTransition(actions);
            }
        });
        movementTransition.play();
    }

    public void openAllIncorrectCups() {
        TranslateTransition revealCup;
        for (Cup cup : cups) {
            if (!cup.containsBall() && !cup.isRevealed()) {
                revealCup = new TranslateTransition(Duration.millis(openCupSpeed), cup.getItem());
                revealCup.setByY(-ballRadius * 8);
                revealCup.play();
                cup.setRevealed(true);
            }
        }
    }

}
