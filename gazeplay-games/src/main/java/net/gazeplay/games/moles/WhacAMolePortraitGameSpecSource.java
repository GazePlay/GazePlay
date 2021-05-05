package net.gazeplay.games.moles;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class WhacAMolePortraitGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhacAmolePortrait").gameThumbnail("data/Thumbnails/mole.png")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new WhacAMolePortraitGameLauncher());
    }
}
