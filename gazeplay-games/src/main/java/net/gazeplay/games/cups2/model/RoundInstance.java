package net.gazeplay.games.cups2.model;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.gazeplay.games.cups2.Config;
import net.gazeplay.games.cups2.action.Action;

import java.util.List;

@Getter
public class RoundInstance {
    double speedFactor;
    int nbCups;
    List<Action.Type> actionPool;
    int nbActions;

    public RoundInstance(List<Action.Type> actionPool, int nbActions) {
        this.speedFactor = Config.getSpeedFactor();
        this.nbCups = Config.getNbCups();
        this.actionPool = ImmutableList.copyOf(actionPool);
        this.nbActions = nbActions;
    }
}
