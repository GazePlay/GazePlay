package net.gazeplay.games.whereisit.launcher;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItGameType;
import net.gazeplay.games.whereisit.WhereIsItStats;

public class WhereIsTheLetterGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.LETTERS.getGameName());
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.DimensionGameVariant gameVariant, Stats stats) {
        return new WhereIsIt(WhereIsItGameType.LETTERS, gameVariant.getWidth(),
            gameVariant.getHeight(), false, gameContext, stats);
    }

}
