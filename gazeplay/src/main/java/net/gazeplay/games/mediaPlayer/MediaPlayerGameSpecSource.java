package net.gazeplay.games.mediaPlayer;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class MediaPlayerGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("MediaPlayer").gameThumbnail("data/Thumbnails/gazeMedia.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new MediaPlayerGameLauncher());
    }
}
