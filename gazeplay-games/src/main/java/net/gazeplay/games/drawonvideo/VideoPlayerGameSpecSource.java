package net.gazeplay.games.drawonvideo;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class VideoPlayerGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("VideoPlayer").gameThumbnail("data/Thumbnails/youtube.png")
                .category(GameCategories.Category.MULTIMEDIA).build(),
            new VideoPlayerGameVariantGenerator(), new VideoPlayerGameLauncher());
    }
}
