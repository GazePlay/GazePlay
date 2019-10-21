package net.gazeplay.games.draw;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class ScribbleGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("Scribble", "data/Thumbnails/gribouille.png", GameCategories.Category.ACTION_REACTION),
            new ScribbleGameLauncher());
    }
}
