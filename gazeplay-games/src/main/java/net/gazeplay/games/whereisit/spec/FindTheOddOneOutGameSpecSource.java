package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.gamevariantgenerator.FindTheOddOneOutGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.FindTheOddOneOutGameLauncher;

/**
 * Declares the game "FindTheOddOneOut" to the menu: its name, thumbnail, categories, variants and launcher (see
 * {@code GameSpecSource}).
 */
public class FindTheOddOneOutGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("FindTheOddOneOut").gameThumbnail("findTheOddOneOut")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new FindTheOddOneOutGameVariantGenerator(), new FindTheOddOneOutGameLauncher());
    }
}
