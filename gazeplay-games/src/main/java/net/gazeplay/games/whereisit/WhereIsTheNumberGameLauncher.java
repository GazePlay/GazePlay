package net.gazeplay.games.whereisit;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class WhereIsTheNumberGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.NUMBERS.getGameName());
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.DimensionGameVariant gameVariant, Stats stats) {
        return new WhereIsIt(WhereIsIt.WhereIsItGameType.NUMBERS, gameVariant.getWidth(),
            gameVariant.getHeight(), false, gameContext, stats);
    }

}
