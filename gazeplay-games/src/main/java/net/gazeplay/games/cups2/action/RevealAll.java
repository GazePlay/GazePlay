package net.gazeplay.games.cups2.action;

import javafx.util.Callback;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.ArrayList;
import java.util.List;

public class RevealAll implements Action {
    private final List<Action> reveals = new ArrayList<>();

    public RevealAll(List<Cup> cups) {
        for (Cup cup : cups)
            reveals.add(new Reveal(cup));
    }

    @Override
    public Type getType() {
        return Type.REVEAL_ALL;
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
        Callback<Void, Void> joinCallback = Action.joiner(onFinish, reveals.size());
        for (Action reveal : reveals)
            reveal.execute(joinCallback);
    }
}
