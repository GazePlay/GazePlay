package net.gazeplay;

import javafx.scene.Scene;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public interface IGameLauncher<T extends Stats, V extends IGameVariant> {

    T createNewStats(Scene scene);

    GameLifeCycle createNewGame(IGameContext gameContext, V gameVariant, T stats);
}
