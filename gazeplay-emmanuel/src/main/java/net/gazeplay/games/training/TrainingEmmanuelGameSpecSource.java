package net.gazeplay.games.training;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class TrainingEmmanuelGameSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("TrainingEmmanuel").gameThumbnail("follow").category(GameCategories.Category.LOGIC_MATHS).build(),
            new TrainingEmmanuelGameLauncher());
    }
}
