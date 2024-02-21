package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsTheSoundGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsTheSoundGameLauncher;

public class WhereIsTheSoundGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheSound").gameThumbnail("whereIsTheSound")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsTheSoundGameVariantGenerator(), new WhereIsTheSoundGameLauncher());
    }
}
