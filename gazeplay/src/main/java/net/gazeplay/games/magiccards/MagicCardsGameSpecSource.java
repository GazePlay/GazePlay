package net.gazeplay.games.magiccards;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class MagicCardsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("MagicCards", "data/Thumbnails/magicCard.png", GameCategories.Category.ACTION_REACTION),
            new MagicCardsGameVariantGenerator(), new MagicCardsGameLauncher());
    }
}
