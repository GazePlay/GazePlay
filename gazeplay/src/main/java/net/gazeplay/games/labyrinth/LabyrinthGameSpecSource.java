package net.gazeplay.games.labyrinth;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class LabyrinthGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            GameSummary.builder().nameCode("Labyrinth").gameThumbnail("data/Thumbnails/labyrinth.png").category(GameCategories.Category.ACTION_REACTION).build(),
            new LabyrinthGameVariantGenerator(), new LabyrinthGameLauncher());
    }
}
