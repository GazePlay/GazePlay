package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsItGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsItGameLauncher;

/**
 * Declares the game "WhereIsIt" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class WhereIsItGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsIt").gameThumbnail("whereIsIt")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsItGameVariantGenerator(), new WhereIsItGameLauncher());
    }
}
