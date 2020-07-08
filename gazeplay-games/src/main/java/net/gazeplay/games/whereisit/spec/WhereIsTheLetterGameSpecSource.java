package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.WhereIsItGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsTheLetterGameLauncher;

public class WhereIsTheLetterGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheLetter").gameThumbnail("data/Thumbnails/Where-is-the-Letter.png")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.LITERACY)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsItGameVariantGenerator(), new WhereIsTheLetterGameLauncher());
    }
}
