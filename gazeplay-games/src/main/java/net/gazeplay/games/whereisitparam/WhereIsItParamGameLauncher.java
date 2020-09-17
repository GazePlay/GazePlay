package net.gazeplay.games.whereisitparam;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.utils.stats.Stats;

public class WhereIsItParamGameLauncher implements IGameLauncher<Stats, IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItParamGameType.CUSTOMIZED.getGameName());
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       IntGameVariant gameVariant, Stats stats) {
        return new WhereIsItParam(WhereIsItParamGameType.CUSTOMIZED, gameVariant.getNumber(), false, gameContext, stats);
    }
}
