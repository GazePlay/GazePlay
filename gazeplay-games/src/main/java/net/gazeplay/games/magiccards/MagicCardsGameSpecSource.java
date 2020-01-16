package net.gazeplay.games.magiccards;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class MagicCardsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MagicCards").gameThumbnail("data/Thumbnails/magicCard.png")
                .category(GameCategories.Category.ACTION_REACTION)
                .category(GameCategories.Category.SELECTION).build(),
            new MagicCardsGameVariantGenerator(), new MagicCardsGameLauncher());
    }
}
