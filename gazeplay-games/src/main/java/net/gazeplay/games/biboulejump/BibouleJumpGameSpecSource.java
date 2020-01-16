package net.gazeplay.games.biboulejump;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class BibouleJumpGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("BibJump").gameThumbnail("data/Thumbnails/biboulejump.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new BibouleJumpGameVariantGenerator(), new BibouleJumpGameLauncher());
    }
}
