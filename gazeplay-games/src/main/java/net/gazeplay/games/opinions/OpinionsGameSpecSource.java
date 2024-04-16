package net.gazeplay.games.opinions;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.cakes.CakesGameVariantGenerator;

public class OpinionsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Opinions").gameThumbnail("opinions")
                .category(GameCategories.Category.SELECTION).build(),
            new OpinionsGameVariantGenerator(), new OpinionsGameLauncher());
    }
}
