package net.gazeplay.games.cups2.action;

import javafx.animation.*;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Callback;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.CupsAndBalls;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.List;

public class FakeCycle implements Action {
    private final List<Cup> cups;
    private final int start;
    private final int end;
    private final boolean direction;
    private final boolean upOrDown;

    public FakeCycle(List<Cup> cups, int indexA, int indexB, boolean upOrDown) {
        this.cups = cups;
        this.start = Math.min(indexA, indexB);
        this.end = Math.max(indexA, indexB);
        this.direction = indexA < indexB;
        this.upOrDown = upOrDown;
    }

    @Override
    public Type getType() {
        return Type.FAKE_CYCLE;
    }

    @Override
    public int simulate(int ballIndex) {
        int dir = direction ? 0 : 1;
        if (start + dir <= ballIndex && ballIndex <= end - 1 + dir)
            return start + dir  + (ballIndex - (start + dir) + (direction ? -1 : 1) + (end - start)) % (end - start);
        else
            return ballIndex;
    }

    @Override
    public void execute() {
        execute(null);
    }

    @Override
    public void execute(Callback<Void, Void> onFinish) {
        Callback<Void, Void> joinCallback = Action.joiner(onFinish, end - start + 1);
        double distance = (direction ? -1 : 1) * (cups.get(1).getX() - cups.get(0).getX());

        PathTransition fpt;
        if (direction)
            fpt = fakedSmoothArcTransition(start, end, upOrDown);
        else
            fpt = fakedSmoothArcTransition(end, start, upOrDown);
        fpt.setOnFinished(e -> joinCallback.call(null));
        fpt.play();

        double time = fpt.getDuration().toMillis();

        TranslateTransition ftt = new TranslateTransition(Duration.millis(time), cups.get(direction ? end : start));
        ftt.setByX(distance);
        ftt.setInterpolator(Interpolator.EASE_BOTH);
        ftt.setOnFinished(e -> joinCallback.call(null));
        ftt.play();
        new Timeline(new KeyFrame(
            Duration.millis(time * 0.5),
            e -> ftt.setRate(-1)
        )).play();

        for (Cup cup : cups.subList(start + 1, end)) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(time), cup);
            tt.setByX(distance);
            tt.setInterpolator(Interpolator.EASE_BOTH);
            tt.setOnFinished(e -> joinCallback.call(null));
            tt.play();
        }

        if (direction)
            cups.add(end - 1, cups.remove(start));
        else
            cups.add(start + 1, cups.remove(end));
    }

    PathTransition fakedSmoothArcTransition(int fromCup, int toCup, boolean upOrDown) {
        Cup cupA = cups.get(fromCup);
        Cup cupB = cups.get(toCup);
        if (upOrDown)
            cupA.toBack();
        else
            cupA.toFront();
        int dir = direction ? -1 : 1;
        double maxHeight = CupsAndBalls.getGameContext().getGamePanelDimensionProvider().getDimension2D().getHeight();
        double heightSide = 0.5 + Math.pow(Math.abs(fromCup - toCup), 0.8) * (Config.CUP_MARGIN / 100) * (upOrDown ? -1 : 1);
        return new PathTransition(
            Duration.millis(Math.pow(Math.abs(fromCup - toCup), 0.25) * Config.ACTION_FAKE_CYCLE_TIME / Config.getSpeedFactor()),
            new Path(
                new MoveTo(cupA.getX() + cupA.getFitWidth() / 2, cupA.getY() + cupA.getFitHeight() / 2),
                new CubicCurveTo(
                    cupA.getX() + cupA.getFitWidth() / 2, Math.max(0, Math.min(cupA.getY() + cupA.getFitHeight() * heightSide, maxHeight)),
                    cupB.getX() + cupB.getFitWidth() / 2, Math.max(0, Math.min(cupB.getY() + cupB.getFitHeight() * heightSide, maxHeight)),
                    cups.get(toCup + dir).getX() + cupB.getFitWidth() / 2, cupB.getY() + cupB.getFitHeight() / 2
                )
            ),
            cupA
        );
    }
}
