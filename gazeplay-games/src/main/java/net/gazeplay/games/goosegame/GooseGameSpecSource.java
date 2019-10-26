package net.gazeplay.games.goosegame;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class GooseGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("GooseGame").gameThumbnail("data/Thumbnails/goosegame.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new GooseGameVariantGenerator(), new GooseGameLauncher());
    }
}
