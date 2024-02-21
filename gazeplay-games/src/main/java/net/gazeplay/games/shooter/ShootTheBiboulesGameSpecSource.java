package net.gazeplay.games.shooter;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSpecSource;
import net.gazeplay.GameSummary;

public class ShootTheBiboulesGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("ShootTheBiboules").gameThumbnail("shootTheBiboules")
                .category(GameCategories.Category.ACTION_REACTION)
                .backgroundMusicUrl("https://opengameart.org/sites/default/files/TalkingCuteChiptune_0.mp3").build(),
            new ShootTheBiboulesGameLauncher());
    }
}
