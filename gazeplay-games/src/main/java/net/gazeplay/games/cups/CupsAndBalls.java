package net.gazeplay.games.cups;

import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.games.cups.utils.Action;
import net.gazeplay.games.cups.utils.PositionCup;
import net.gazeplay.games.cups.utils.Strategy;

import java.awt.*;
import java.util.ArrayList;

@Slf4j
public class CupsAndBalls implements GameLifeCycle {

    private final IGameContext gameContext;
    private final Stats stats;
    private Ball ball;
    private final Cup[] cups;
    private final int nbCups;
    private final int nbLines;
    private final int nbColumns;
    private int nbExchanges;

    private final int openCupSpeed = 1000;
    private final int ballRadius = 20;

    private final ReplayablePseudoRandom randomGenerator;
    private ArrayList<Action> actions;
    private final ArrayList<TargetAOI> targetAOIList;

    public CupsAndBalls(final IGameContext gameContext, final Stats stats, final int nbCups) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbCups = nbCups;
        this.cups = new Cup[nbCups];
        this.nbColumns = nbCups;
        this.nbLines = nbCups;
        this.nbExchanges = nbCups * nbCups;
        this.targetAOIList = new ArrayList<>();
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setCurrentGameSeed(randomGenerator.getSeed());
    }

    public CupsAndBalls(final IGameContext gameContext, final Stats stats, final int nbCups, double gameSeed, String parameter) {
        super();
        this.gameContext = gameContext;
        this.stats = stats;
        this.nbCups = nbCups;
        this.cups = new Cup[nbCups];
        this.nbColumns = nbCups;
        this.nbLines = nbCups;
        this.nbExchanges = nbCups * nbCups;
        this.targetAOIList = new ArrayList<>();
        gameContext.startScoreLimiter();
        gameContext.startTimeLimiter();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
    }

    public CupsAndBalls(final IGameContext gameContext, final Stats stats, final int nbCups, final int nbExchanges) {
        this(gameContext, stats, nbCups);
        this.nbExchanges = nbExchanges;
    }

    public CupsAndBalls(final IGameContext gameContext, final Stats stats, final int nbCups, final int nbExchanges, double gameSeed, String parameter) {
        this(gameContext, stats, nbCups, gameSeed, parameter);
        this.nbExchanges = nbExchanges;
    }

    private void init() {
        final javafx.geometry.Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        final Image cupPicture = new Image("data/cups/images/cup.png");
        final double imageWidth = dimension2D.getHeight() / (nbColumns * 1.5);
        final double imageHeight = dimension2D.getHeight() / nbColumns;

        final int ballInCup = randomGenerator.nextInt(nbCups);
        Point posCup;
        for (int indexCup = 0; indexCup < cups.length; indexCup++) {
            final PositionCup position = new PositionCup(indexCup, nbColumns / 2, nbColumns, nbLines, dimension2D.getHeight(),
                dimension2D.getWidth(), imageWidth, imageHeight);
            posCup = position.calculateXY(position.getCellX(), position.getCellY());
            final ImageView cupRectangle = new ImageView(cupPicture);
            cupRectangle.setX(posCup.getX());
            cupRectangle.setY(posCup.getY());
            cupRectangle.setFitWidth(imageWidth);
            cupRectangle.setFitHeight(imageHeight);
            cups[indexCup] = new Cup(cupRectangle, position, gameContext, stats, this, openCupSpeed);
            if (indexCup == ballInCup) {
                final long startTime = System.currentTimeMillis();
                final TargetAOI targetAOI = new TargetAOI(posCup.getX(), posCup.getY() + imageHeight / 2, (int) ((imageWidth + imageHeight) / 3), startTime);
                targetAOI.setTimeEnded(startTime + openCupSpeed);
                targetAOIList.add(targetAOI);
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
        gameContext.setLimiterAvailable();
        init();
        TranslateTransition revealBallTransition = null;
        for (final Cup cup : cups) {
            if (cup.containsBall()) {
                revealBallTransition = new TranslateTransition(Duration.millis(openCupSpeed), cup.getItem());
                revealBallTransition.setByY(-ballRadius * 8);
                revealBallTransition.setAutoReverse(true);
                revealBallTransition.setCycleCount(2);
            }
        }

        Strategy strategy = new Strategy(nbCups, nbExchanges, nbColumns, nbLines, randomGenerator);
        this.actions = strategy.chooseStrategy();

        for (final Cup cup : cups) {
            cup.setActionsToDo(actions.size());
        }
        if (revealBallTransition != null) {
            revealBallTransition.setOnFinished(e -> {
                ball.getItem().setVisible(false);
                createNewTransition(actions);
            });
            revealBallTransition.play();
        }
        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    @Override
    public void dispose() {
        stats.setTargetAOIList(targetAOIList);
    }

    private void createNewTransition(final ArrayList<Action> actions) {
        final int initCellX = actions.get(0).getInitCellX();
        final int initCellY = actions.get(0).getInitCellY();
        final int finalCellX = actions.get(0).getTargetCellX();
        final int finalCellY = actions.get(0).getTargetCellY();

        ImageView cupToMove = null;
        Point initPos = null;
        Point newPos = null;
        for (int indexCup = 0; indexCup < nbCups; indexCup++) {
            final Cup currentCup = cups[indexCup];
            if (currentCup.getPositionCup().getCellX() == initCellX
                && currentCup.getPositionCup().getCellY() == initCellY) {
                cupToMove = currentCup.getItem();
                initPos = currentCup.getPositionCup().calculateXY(initCellX, initCellY);
                newPos = currentCup.getPositionCup().calculateXY(finalCellX, finalCellY);
                currentCup.getPositionCup().setCellX(finalCellX);
                currentCup.getPositionCup().setCellY(finalCellY);
                currentCup.progressBarUpdatePosition(newPos.getX(), newPos.getY());
                if (currentCup.containsBall()) {
                    final TargetAOI targetAOI = new TargetAOI(newPos.getX(), newPos.getY() + currentCup.getItem().getFitHeight() / 2, (int) ((currentCup.getItem().getFitWidth() + currentCup.getItem().getFitHeight()) / 3), System.currentTimeMillis());
                    targetAOIList.add(targetAOI);
                    currentCup.getBall().updatePosition(newPos.getX(), newPos.getY());
                }
            }
        }

        if (newPos == null || initPos == null || cupToMove == null) {
            throw new IllegalStateException("The cup positions haven't been set up properly");
        }

        final int exchangeCupDuration = 1000;
        final TranslateTransition movementTransition = new TranslateTransition(
            Duration.millis(exchangeCupDuration), cupToMove);
        movementTransition.setByX(newPos.getX() - initPos.getX());
        movementTransition.setByY(newPos.getY() - initPos.getY());

        movementTransition.setOnFinished(e -> {
            actions.remove(0);
            if (actions.size() > 0) {
                for (final Cup cup : cups) {
                    cup.increaseActionsDone();

                }

                createNewTransition(actions);
            } else {
                gameContext.firstStart();
                gameContext.onGameStarted(2000);
            }
        });

        movementTransition.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        targetAOIList.get(targetAOIList.size() - 1).setTimeEnded(System.currentTimeMillis());
        movementTransition.play();
    }

    public void openAllIncorrectCups() {
        TranslateTransition revealCup;
        for (final Cup cup : cups) {
            if (!cup.containsBall() && !cup.isRevealed()) {
                revealCup = new TranslateTransition(Duration.millis(openCupSpeed), cup.getItem());
                revealCup.setByY(-ballRadius * 8);
                revealCup.play();
                cup.setRevealed(true);
            }
        }
    }
}
