package net.gazeplay.games.cooperativeGame;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class CooperativeGameKeyboardSpecSource implements GameSpecSource {

    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("CooperativeGameKeyboard").gameThumbnail("data/Thumbnails/chasedownChallenge.png")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new CooperativeGameVariantGenerator(), new CooperativeGameKeyboardLauncher());
    }
}
