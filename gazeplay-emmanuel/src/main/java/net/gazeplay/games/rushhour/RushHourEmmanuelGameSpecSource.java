package net.gazeplay.games.rushhour;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class RushHourEmmanuelGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("RushHourEmmanuel").gameThumbnail("rushHour")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LOGIC_MATHS).build(),
            new RushHourEmmanuelVariantGenerator(), new RushHourEmmanuelGameLauncher());
    }
}
