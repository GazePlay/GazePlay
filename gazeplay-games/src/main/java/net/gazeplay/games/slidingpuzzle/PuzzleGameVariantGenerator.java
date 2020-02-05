package net.gazeplay.games.slidingpuzzle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gazeplay.GameSpec;

public class PuzzleGameVariantGenerator extends GameSpec.EnumGameVariantGenerator<PuzzleGameVariantGenerator.PuzzleGameVariant> {

    public PuzzleGameVariantGenerator() {
        super(PuzzleGameVariant.values(), PuzzleGameVariant::getLabel);
    }

    @RequiredArgsConstructor
    public enum PuzzleGameVariant {
        NUMBERS("Numbers", "data/sliding-puzzle/tiles/tile"),
        MONA_LISA("Mona Lisa", "data/sliding-puzzle/monalisa/p"),
        FISH("Fish", "data/sliding-puzzle/fish/p"),
        BIBOULE("Biboule", "data/sliding-puzzle/biboule/p");

        @Getter
        private final String label;

        @Getter
        private final String resourcesPath;

    }

}
