package net.gazeplay.games.labyrinth;

import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.gameslocator.GameSpecSource;

public class LabyrinthGameSpecSource implements GameSpecSource {
    @Override
    public GameSpec getGameSpec() {
        return new GameSpec(
            new GameSummary("Labyrinth", "data/Thumbnails/labyrinth.png", GameCategories.Category.ACTION_REACTION),
            new LabyrinthGameVariantGenerator(), new LabyrinthGameLauncher());
    }
}
