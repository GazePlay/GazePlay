package net.gazeplay.games.cups;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class CupsBallsGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("CupsBalls").gameThumbnail("data/Thumbnails/passpass.png").category(GameCategories.Category.MEMORIZATION).build(),
            new CupsBallsGameVariantGenerator(), new CupsBallsGameLauncher());
    }
}
