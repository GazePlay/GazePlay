package net.gazeplay.games.cups2.action;

import javafx.animation.*;
import javafx.util.Callback;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.List;

public class Trick implements Action {
    private final List<Cup> cups;
    private final boolean direction;
    private final int indexA, indexB;

    public Trick(List<Cup> cups, int indexA, int indexB) {
        this.cups = cups;
        this.direction = indexA < indexB;
        this.indexA = Math.min(indexA, indexB);
        this.indexB = Math.max(indexA, indexB);
    }

    @Override
    public Type getType() {
        return Type.TRICK;
    }

    @Override
    public int simulate(int ballIndex) {
        return ballIndex;
    }

    @Override
    public void execute() {
        execute(null);
    }

    @Override
    public void execute(Callback<Void, Void> onFinish) {
        Cup cupA = cups.get(indexA);
        Cup cupB = cups.get(indexB);
        Cup ballCup = cupA.hasBall() ? cupA : cupB;
        Cup otherCup = cupA.hasBall() ? cupB : cupA;

        Callback<Void, Void> joinCallback = Action.joiner(onFinish, 3);
        double time = Config.ACTION_TRICK_TIME / Config.speedFactor;

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

        if (ballCup.hasBall()) {
            PauseTransition tricker = new PauseTransition(Duration.millis(time * 0.45));
            tricker.setOnFinished(e -> {
                double ballCupX = ballCup.getX() + ballCup.getTranslateX() + ballCup.getFitWidth() / 2;
                double otherCupX = otherCup.getX() + otherCup.getTranslateX() + otherCup.getFitWidth() / 2;
                double middleX = (ballCupX + otherCupX) / 2;
                double ballCupY = ballCup.getY() + ballCup.getTranslateY();
                double otherCupY = otherCup.getY() + otherCup.getTranslateY();
                ballCupY += ballCup.getFitHeight() * (ballCupY < otherCupY ? 0.8 : 0.2);
                otherCupY += ballCup.getFitHeight() * (ballCupY < otherCupY ? 0.2 : 0.8);

                ballCup.getBall().setCenterX(middleX);
                ballCup.getBall().setCenterY(ballCupY);
                ballCup.getBall().setVisible(true);

                TranslateTransition trick = new TranslateTransition(Duration.millis(time * 0.1), ballCup.getBall());
                trick.setOnFinished(ep -> {
                    ballCup.getBall().setVisible(false);
                    Cup.swapBall(ballCup, otherCup);
                    joinCallback.call(null);
                });
                trick.setByX(ballCupX - middleX);
                trick.setByY(otherCupY - ballCupY);
                trick.play();
            });
            tricker.play();
        } else
            joinCallback.call(null);

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
