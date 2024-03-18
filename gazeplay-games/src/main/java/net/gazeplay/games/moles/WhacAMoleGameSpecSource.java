package net.gazeplay.games.moles;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class WhacAMoleGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhacAMole").gameThumbnail("mole")
                .category(GameCategories.Category.ACTION_REACTION).build(),
            new MolesGameVariantGenerator(), new WhacAMoleGameLauncher());
    }
}
