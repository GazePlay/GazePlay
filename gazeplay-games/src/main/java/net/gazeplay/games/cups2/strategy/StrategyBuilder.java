package net.gazeplay.games.cups2.strategy;

import net.gazeplay.games.cups2.action.Action;
import net.gazeplay.games.cups2.utils.Cup;

import java.util.List;

public interface StrategyBuilder {
    enum Type {
        SIMPLE
    }

    Type getType();

    void computeActions(List<Action> actions, List<Cup> cups, int ballIndex);

    static StrategyBuilder newInstanceOf(Type type) {
        return switch (type) {
            case SIMPLE -> new SimpleStrategy();
        };
    }
}
