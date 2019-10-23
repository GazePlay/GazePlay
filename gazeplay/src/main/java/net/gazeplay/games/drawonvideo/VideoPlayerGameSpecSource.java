package net.gazeplay.games.drawonvideo;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class VideoPlayerGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("VideoPlayer").gameThumbnail("data/Thumbnails/youtube.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new VideoPlayerGameVariantGenerator(), new VideoPlayerGameLauncher());
    }
}
