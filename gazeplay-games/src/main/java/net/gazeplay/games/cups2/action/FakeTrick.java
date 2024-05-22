package net.gazeplay.games.cups2.action;

import javafx.animation.*;
import javafx.util.Callback;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.List;

public class FakeTrick implements Action {
    private final List<Cup> cups;
    private final boolean direction;
    private final int indexA, indexB;

    public FakeTrick(List<Cup> cups, int indexA, int indexB) {
        this.cups = cups;
        this.direction = indexA < indexB;
        this.indexA = Math.min(indexA, indexB);
        this.indexB = Math.max(indexA, indexB);
    }

    @Override
    public Type getType() {
        return Type.FAKE_TRICK;
    }

    @Override
    public int simulate(int ballIndex) {
        return ballIndex == indexA ? indexB : (ballIndex == indexB ? indexA : ballIndex);
    }

    @Override
    public void execute() {
        execute(null);
    }

    @Override
    public void execute(Callback<Void, Void> onFinish) {
        Cup cupA = cups.get(indexA);
        Cup cupB = cups.get(indexB);

        Callback<Void, Void> joinCallback = Action.joiner(onFinish, 2);
        double time = Config.ACTION_FAKE_TRICK_TIME / Config.speedFactor;

        SequentialTransition sta = new SequentialTransition(
            new PauseTransition(Duration.millis(time * 0.4)),
            new RotateTransition(Duration.millis(time * 0.15), cupA),
        new PauseTransition(Duration.millis(time * 0.3))
        );
        ((RotateTransition) sta.getChildren().get(1)).setByAngle(20);
        ((RotateTransition) sta.getChildren().get(1)).setInterpolator(Interpolator.LINEAR);
        sta.getChildren().get(1).setCycleCount(2);
        sta.getChildren().get(1).setAutoReverse(true);

        SequentialTransition stb = new SequentialTransition(
            new PauseTransition(Duration.millis(time * 0.4)),
            new RotateTransition(Duration.millis(time * 0.15), cupB),
            new PauseTransition(Duration.millis(time * 0.3))
        );
        ((RotateTransition) stb.getChildren().get(1)).setByAngle(-20);
        ((RotateTransition) stb.getChildren().get(1)).setInterpolator(Interpolator.LINEAR);
        stb.getChildren().get(1).setCycleCount(2);
        stb.getChildren().get(1).setAutoReverse(true);

        ParallelTransition pta = new ParallelTransition(
            Action.smoothArcTransition(Duration.millis(time), cupA, cupB, direction),
            sta
        );
        ParallelTransition ptb = new ParallelTransition(
            Action.smoothArcTransition(Duration.millis(time), cupB, cupA, !direction),
            stb
        );
        pta.setOnFinished(e -> joinCallback.call(null));
        ptb.setOnFinished(e -> joinCallback.call(null));
        pta.play();
        ptb.play();

        cups.set(indexA, cupB);
        cups.set(indexB, cupA);
    }
}
