package net.gazeplay.games.cups2.model;

import com.google.common.collect.ImmutableList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.CupsAndBalls;
import net.gazeplay.games.cups2.action.Action;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.*;
import java.util.concurrent.Semaphore;

public class PlayerModel {
    // Represents the player's skill level, in order to adapt the game difficulty using AdaptiveStrategy's action building

    static final double MIN_GAZE_DISTANCE = 80;  // in pixels

    // TODO: This class should be completed with more metrics such as a proper way to the estimate player's relation to
    //  the number of cups a better balanced estimation for the speed factor, and probably else

    private final List<Performance> performanceHistory = new ArrayList<>();
    private Performance currentPerf;
    private final Semaphore performanceLock = new Semaphore(1);
    private int totalMeasurements = 0;
    private final HashMap<Action.Type, Integer> actionMeasurements = new HashMap<>();

    private boolean lostTrackOfBall = false;  // True whenether the player didn't whatch the cup with the ball for a while
    private int lostTrackOfBallCooldown = Config.PLAYER_BALL_TRACKING_COOLDOWN;
    private Action lastAction = null;
    private double currentActionPerf = 0;
    private int currentActionMeasurements = 0;

    public List<Performance> getPerformanceHistory() {
        return ImmutableList.copyOf(performanceHistory);
    }

    private Point2D lastGazePosition = null;  // Last position given by the gaze-tracking device in order to treat other metrics
    private final Semaphore lastGazePositionLock = new Semaphore(1);

    private void setLastGazePosition(Point2D position) {
        try {
            lastGazePositionLock.acquire();
        } catch (InterruptedException e) {
            return;
        }
        lastGazePosition = position;
        lastGazePositionLock.release();
    }

    private Optional<Point2D> getLastGazePosition() {
        try {
            lastGazePositionLock.acquire();
        } catch (InterruptedException e) {
            return Optional.empty();
        }
        Point2D position = lastGazePosition;
        lastGazePositionLock.release();
        return Optional.ofNullable(position);
    }


    // Circle that shows some internal states of lostTrackOfBall and gaze tracking, only for testing purposes
    Circle debugGazePoint = null;

    public PlayerModel() {
        super();
        CupsAndBalls.getGameContext().getGazeDeviceManager().addGazeMotionListener(this::gazeListener);

        if (Config.DEBUG) {
            debugGazePoint = new Circle(MIN_GAZE_DISTANCE, Paint.valueOf("lime"));
            debugGazePoint.setVisible(true);
            debugGazePoint.setOpacity(0.2);
            CupsAndBalls.getGameContext().getChildren().add(debugGazePoint);
        }

        trackingLoop();
    }

    public void dispose() {
        CupsAndBalls.getGameContext().getGazeDeviceManager().removeGazeMotionListener(this::gazeListener);
    }

    private void gazeListener(Point2D position) {
        // Updates the gaze position given by the gaze-tracking device
        setLastGazePosition(position);
        if (debugGazePoint != null) {
            debugGazePoint.toFront();
            debugGazePoint.setRadius(MIN_GAZE_DISTANCE * Math.sqrt(Config.getSpeedFactor()));
            debugGazePoint.setCenterX(position.getX());
            debugGazePoint.setCenterY(position.getY());
        }
    }

    private void trackingLoop() {
        // Executed every tick (60 TPS) to update the player's performance metrics

        try {
            performanceLock.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        totalMeasurements++;
        currentActionMeasurements++;
        if (lastAction != null && CupsAndBalls.getCurrentAction() != null)
            actionMeasurements.put(
                CupsAndBalls.getCurrentAction().getType(),
                actionMeasurements.getOrDefault(CupsAndBalls.getCurrentAction().getType(), 0) + 1
            );

        Optional<Point2D> optPosition = getLastGazePosition();
        if (optPosition.isPresent()) {
            Point2D position = optPosition.get();
            boolean ballUnderGaze = CupsAndBalls.getGameContext().getChildren().stream().filter(
                node -> node instanceof Cup && node.intersects((new Circle(
                    position.getX(), position.getY(),
                    MIN_GAZE_DISTANCE * Math.sqrt(Config.getSpeedFactor())
                )).getBoundsInLocal())
            ).anyMatch(cup -> ((Cup) cup).hasBall());
            if (ballUnderGaze) {
                currentActionPerf++;
                if (CupsAndBalls.getCurrentPhase() == CupsAndBalls.Phase.OBSERVATION && CupsAndBalls.getCurrentAction() != null) {
                    currentPerf.ballTracking++;
                    if (!lostTrackOfBall)
                        currentPerf.getActionsPerf().put(
                            CupsAndBalls.getCurrentAction().getType(),
                            currentPerf.getActionsPerf().getOrDefault(CupsAndBalls.getCurrentAction().getType(), 0.) + 1
                        );
                }
            }
        }

        performanceLock.release();

        // lostTrackOfBall logic, updated after each action
        if (lastAction != null && lastAction != CupsAndBalls.getCurrentAction()) {
            if (currentActionPerf / currentActionMeasurements < (lastAction.hasBall() ? 0.5 : 0.2))
                lostTrackOfBallCooldown = Math.max(lostTrackOfBallCooldown - 1, 0);
            else
                lostTrackOfBallCooldown = Math.min(lostTrackOfBallCooldown + 1, Config.PLAYER_BALL_TRACKING_COOLDOWN);
            if (lostTrackOfBallCooldown % Config.PLAYER_BALL_TRACKING_COOLDOWN == 0)
                lostTrackOfBall = lostTrackOfBallCooldown == 0;
            currentActionPerf = 0;
            currentActionMeasurements = 0;
            if (debugGazePoint != null)
                debugGazePoint.setFill(Paint.valueOf(lostTrackOfBall ? "red" : "lime"));
            currentPerf.speedPerf *= lostTrackOfBall ? 0.9 : 1.05;
            currentPerf.nbCupsPerf *= lostTrackOfBall ? 0.9 : 1.05;
        }
        lastAction = CupsAndBalls.getCurrentAction();

        new Timeline(new KeyFrame(
            Duration.millis(1000 / 61d),  // 60 TPS
            e -> trackingLoop()
        )).play();
    }

    public void finishRound() {
        // Executed at the beginning of a new round to save the previous one's performance metrics

        try {
            performanceLock.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        if (totalMeasurements > 0 && lastAction != null) {
            currentPerf.ballTracking /= totalMeasurements;
            for (Map.Entry<Action.Type, Integer> entry : actionMeasurements.entrySet())
                currentPerf.getActionsPerf().put(
                    entry.getKey(),
                    currentPerf.getActionsPerf().get(entry.getKey()) / entry.getValue()
                );
            performanceHistory.add(currentPerf);
        }

        performanceLock.release();
    }


    public void newRound(RoundInstance round) {
        // Set up the player's performance metrics for a new round

        try {
            performanceLock.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        // Resetting performance
        totalMeasurements = 0;
        for (Action.Type type : round.actionPool)
            actionMeasurements.put(type, 0);
        lostTrackOfBall = false;
        lostTrackOfBallCooldown = Config.PLAYER_BALL_TRACKING_COOLDOWN;
        currentActionPerf = 0;
        currentActionMeasurements = 0;
        currentPerf = new Performance(round);

        performanceLock.release();
    }

    public void selectedRightCup() {
    }

    public void selectedWrongCup() {
        currentPerf.speedPerf *= 0.8;
//        currentPerf.nbCupsPerf *= 0.8;
    }
}
