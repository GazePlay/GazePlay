package net.gazeplay.games.cups2.action;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Callback;
import javafx.util.Duration;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.List;

public class Cycle implements Action {
    private final List<Cup> cups;
    private final int start;
    private final int end;
    private final boolean direction;
    private final boolean upOrDown;

    public Cycle(List<Cup> cups, int indexA, int indexB, boolean upOrDown) {
        this.cups = cups;
        this.start = Math.min(indexA, indexB);
        this.end = Math.max(indexA, indexB);
        this.direction = indexA < indexB;
        this.upOrDown = upOrDown;
    }

    @Override
    public Type getType() {
        return Type.CYCLE;
    }

    @Override
    public int simulate(int ballIndex) {
        if (start <= ballIndex && ballIndex <= end)
            return start + (ballIndex - start + (direction ? -1 : 1) + (end - start + 1)) % (end - start + 1);
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

        PathTransition pt;
        if (direction)
            pt = Action.smoothArcTransition(Duration.millis(Config.ACTION_CYCLE_TIME / Config.speedFactor), cups.get(start), cups.get(end), upOrDown);
        else
            pt = Action.smoothArcTransition(Duration.millis(Config.ACTION_CYCLE_TIME / Config.speedFactor), cups.get(end), cups.get(start), upOrDown);
        pt.setOnFinished(e -> joinCallback.call(null));
        pt.play();

        int dir = direction ? 1 : 0;
        for (Cup cup : cups.subList(start + dir, end + dir)) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(Config.ACTION_EXCHANGE_TIME / Config.speedFactor), cup);
            tt.setByX(distance);
            tt.setInterpolator(Interpolator.EASE_BOTH);
            tt.setOnFinished(e -> joinCallback.call(null));
            tt.play();
        }

        if (direction)
            cups.add(end, cups.remove(start));
        else
            cups.add(start, cups.remove(end));
    }
}
