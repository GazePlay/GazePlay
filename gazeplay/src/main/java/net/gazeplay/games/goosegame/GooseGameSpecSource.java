package net.gazeplay.games.goosegame;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class GooseGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("GooseGame", "data/Thumbnails/goosegame.png", GameCategories.Category.ACTION_REACTION),
            new GooseGameVariantGenerator(), new GooseGameLauncher());
    }
}
