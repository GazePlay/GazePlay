package net.gazeplay.games.mediaPlayer;

import javafx.scene.Scene;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.GameSpec;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.pet.PetStats;

public class MediaPlayerGameLauncher implements GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant> {
    @Override
    public Stats createNewStats(Scene scene) {
        return new Stats(scene,"mediaPlayer");
    }

    @Override
    public GameLifeCycle createNewGame(IGameContext gameContext,
                                       GameSpec.DimensionGameVariant gameVariant, Stats stats) {
        return new GazeMediaPlayer(gameContext, stats);
    }

}
