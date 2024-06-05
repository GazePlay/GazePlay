package net.gazeplay.games.cups2.model;

import com.google.common.collect.ImmutableList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.CupsAndBalls;
import net.gazeplay.games.cups2.action.Action;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.*;
import java.util.concurrent.Semaphore;

public class PlayerModel {

    private final List<Performance> performanceHistory = new ArrayList<>();
    private Performance currentPerf;
    private final Semaphore performanceLock = new Semaphore(1);
    private int totalMeasurements = 0;
    private final HashMap<Action.Type, Integer> actionMeasurements = new HashMap<>();

    private boolean lostTrackOfBall = false;
    private int lostTrackOfBallCooldown = Config.PLAYER_BALL_TRACKING_COOLDOWN;
    private Action lastAction = null;
    private double currentActionPerf = 0;
    private int currentActionMeasurements = 0;

    public List<Performance> getPerformanceHistory() {
        return ImmutableList.copyOf(performanceHistory);
    }

    private Point2D lastGazePosition = null;
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


    public PlayerModel() {
        super();
        CupsAndBalls.getGameContext().getGazeDeviceManager().addGazeMotionListener(this::gazeListener);
        trackingLoop();
    }

    public void dispose() {
        CupsAndBalls.getGameContext().getGazeDeviceManager().removeGazeMotionListener(this::gazeListener);
    }

    private void gazeListener(Point2D position) {
        setLastGazePosition(position);
    }

    private void trackingLoop() {
        try {
            performanceLock.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        totalMeasurements++;
        currentActionMeasurements++;
        if (lastAction != null)
            actionMeasurements.put(
                CupsAndBalls.getCurrentAction().getType(),
                actionMeasurements.get(CupsAndBalls.getCurrentAction().getType()) + 1
            );

        Optional<Point2D> optPosition = getLastGazePosition();
        if (optPosition.isPresent()) {
            Point2D position = optPosition.get();
            boolean ballUnderGaze = CupsAndBalls.getGameContext().getChildren().stream().filter(
                node -> node instanceof Cup && node.contains(position)
            ).anyMatch(cup -> ((Cup) cup).hasBall());
            if (ballUnderGaze && CupsAndBalls.getCurrentPhase() == CupsAndBalls.Phase.OBSERVATION) {
                currentPerf.ballTracking++;
                if (!lostTrackOfBall)
                    currentPerf.getActionsPerf().put(
                        CupsAndBalls.getCurrentAction().getType(),
                        currentPerf.getActionsPerf().get(CupsAndBalls.getCurrentAction().getType()) + 1
                    );
            }
        }

        performanceLock.release();

        // lostTrackOfBall logic, updated after each action
        if (lastAction != null && lastAction != CupsAndBalls.getCurrentAction()) {
            if (currentActionPerf / currentActionMeasurements < 0.5)
                lostTrackOfBallCooldown = Math.max(lostTrackOfBallCooldown - 1, 0);
            else
                lostTrackOfBallCooldown = Math.min(lostTrackOfBallCooldown + 1, Config.PLAYER_BALL_TRACKING_COOLDOWN);
            if (lostTrackOfBallCooldown % Config.PLAYER_BALL_TRACKING_COOLDOWN == 0)
                lostTrackOfBall = lostTrackOfBallCooldown == 0;
            currentActionPerf = 0;
            currentActionMeasurements = 0;
        }
        lastAction = CupsAndBalls.getCurrentAction();

        new Timeline(new KeyFrame(
            Duration.millis(1000 / 30d),  // 60 TPS
            e -> trackingLoop()
        )).play();
    }


    public void newRound(RoundInstance round) {
        try {
            performanceLock.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        // Packing everything up
        if (totalMeasurements > 0) {
            currentPerf.ballTracking /= totalMeasurements;
            for (Map.Entry<Action.Type, Integer> entry : actionMeasurements.entrySet())
                currentPerf.getActionsPerf().put(
                    entry.getKey(),
                    currentPerf.getActionsPerf().get(entry.getKey()) / entry.getValue()
                );
            performanceHistory.add(currentPerf);
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
}
