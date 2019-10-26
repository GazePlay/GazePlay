package net.gazeplay.games.videogrid;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GameSpecSource;

public class VideoGridGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("VideoGrid").gameThumbnail("data/Thumbnails/videogrid.png").category(GameCategories.Category.SELECTION).category(GameCategories.Category.UTILITY).build(),
            new VideoGridGameVariantGenerator(), new VideoGridGameLauncher());
    }
}
