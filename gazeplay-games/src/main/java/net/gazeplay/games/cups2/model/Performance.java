package net.gazeplay.games.cups2.model;

import lombok.Getter;
import net.gazeplay.games.cups2.action.Action;

import java.util.HashMap;

@Getter
public class Performance {
    // Performance of a player on a round of the game

    RoundInstance round;  // The round config and actions the performance is based on

    double ballTracking;  // Ratio of time spent on the ball over the round's time
    double selectionFixations;  // Focus on the cup with the ball during selection phase
    double selectionTime;  // Time for the player to select a cup

    final HashMap<Action.Type, Double> actionsPerf = new HashMap<>();  // Performance for each action type observed
    double speedPerf;  // Performance related to speed of the round
    double nbCupsPerf;  // Performance related to the number of cups in the round

    public Performance(RoundInstance round) {
        this.round = round;
        for (Action.Type type : Action.Type.values())
            actionsPerf.put(type, 0.0);
        speedPerf = 1;
        nbCupsPerf = 1;
    }
}
