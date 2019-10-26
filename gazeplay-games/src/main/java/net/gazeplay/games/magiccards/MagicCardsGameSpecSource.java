package net.gazeplay.games.magiccards;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class MagicCardsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MagicCards").gameThumbnail("data/Thumbnails/magicCard.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new MagicCardsGameVariantGenerator(), new MagicCardsGameLauncher());
    }
}
