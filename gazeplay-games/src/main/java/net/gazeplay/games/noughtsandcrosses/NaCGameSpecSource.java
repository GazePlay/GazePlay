package net.gazeplay.games.noughtsandcrosses;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class NaCGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("NaC").gameThumbnail("data/Thumbnails/NaC.png")
                .category(GameCategories.Category.LOGIC_MATHS)
                .category(GameCategories.Category.SELECTION)
                .build(),
            new NaCGameVariantGenerator(), new NaCGameLauncher());
    }
}
