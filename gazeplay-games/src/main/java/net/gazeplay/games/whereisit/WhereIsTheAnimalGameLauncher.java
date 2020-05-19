package net.gazeplay.games.whereisit;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

public class WhereIsTheAnimalGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new WhereIsItStats(scene, WhereIsItGameType.ANIMALNAME.getGameName());
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.IntGameVariant gameVariant, Stats stats) {
        return new WhereIsIt(WhereIsItGameType.ANIMALNAME, gameVariant.getNumber(), false, gameContext, stats);
    }

}
