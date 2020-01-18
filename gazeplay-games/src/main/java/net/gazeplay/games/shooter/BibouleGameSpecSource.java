package net.gazeplay.games.shooter;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class BibouleGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Biboule").gameThumbnail("data/Thumbnails/biboules.png").category(GameCategories.Category.ACTION_REACTION)
                .backgroundMusicUrl("https://opengameart.org/sites/default/files/TalkingCuteChiptune_0.mp3").build(),
            new ShooterGameLauncher());
    }
}
