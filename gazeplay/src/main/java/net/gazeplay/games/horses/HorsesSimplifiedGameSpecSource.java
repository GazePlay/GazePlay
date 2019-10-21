package net.gazeplay.games.horses;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class HorsesSimplifiedGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(new GameSummary("Horses Simplified", "data/Thumbnails/horsesSimplified.png",
            GameCategories.Category.ACTION_REACTION), new HorsesSimplifiedGameVariantGenerator(), new HorsesSimplifiedGameLauncher());
    }
}
