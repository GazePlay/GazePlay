package net.gazeplay.games.cassebrique;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares the game "CasseBrique" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class CasseBriqueGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("CasseBrique").gameThumbnail("cassebrique")
                .category(GameCategories.Category.ACTION_REACTION)
                .build(),
            new CasseBriqueGameVariantGenerator(), new CasseBriqueGameLauncher());
    }
}
