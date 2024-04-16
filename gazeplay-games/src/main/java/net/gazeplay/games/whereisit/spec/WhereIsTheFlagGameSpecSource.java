package net.gazeplay.games.whereisit.spec;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;
import net.gazeplay.games.whereisit.gamevariantgenerator.WhereIsTheFlagGameVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsTheFlagGameLauncher;

public class WhereIsTheFlagGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("WhereIsTheFlag").gameThumbnail("whereIsTheFlag")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.MEMORIZATION).build(),
            new WhereIsTheFlagGameVariantGenerator(), new WhereIsTheFlagGameLauncher());
    }
}
