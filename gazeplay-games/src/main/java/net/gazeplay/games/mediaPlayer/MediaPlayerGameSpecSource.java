package net.gazeplay.games.mediaPlayer;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class MediaPlayerGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MediaPlayer").gameThumbnail("data/Thumbnails/gazeMedia.png")
                .category(GameCategories.Category.MULTIMEDIA).build(),
            new MediaPlayerGameLauncher());
    }
}
