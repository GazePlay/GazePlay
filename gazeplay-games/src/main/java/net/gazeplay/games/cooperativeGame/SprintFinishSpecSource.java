package net.gazeplay.games.cooperativeGame;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class SprintFinishSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("SprintFinish").gameThumbnail("data/Thumbnails/SprintToTheFinish.png")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new SprintFinishVariantGenerator(), new SprintFinishLauncher());
    }
}
