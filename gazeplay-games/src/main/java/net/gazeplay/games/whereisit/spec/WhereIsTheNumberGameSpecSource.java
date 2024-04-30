package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsItGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsTheNumberGameLauncher;

public class WhereIsTheNumberGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheNumber").gameThumbnail("whereIsTheNumber")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.LITERACY)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsItGameVariantGenerator(), new WhereIsTheNumberGameLauncher());
    }
}
