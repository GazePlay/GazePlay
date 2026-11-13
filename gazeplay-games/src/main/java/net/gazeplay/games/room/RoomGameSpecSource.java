package net.gazeplay.games.room;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

/**
 * Declares "La pièce" (name code Room) to the menu: its name, thumbnail, categories, variants (see
 * {@link RoomGameVariantGenerator}) and launcher (see {@code GameSpecSource}).
 */
public class RoomGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Room").gameThumbnail("home")
                .category(GameCategories.Category.SELECTION)
                .category(GameCategories.Category.ACTION_REACTION)
                .build(),
            new RoomGameVariantGenerator(), new RoomGameLauncher());
    }
}
