package net.gazeplay.commons.gamevariants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The variant of GazePlay Eval goes to the JVM of the game as text (--variant) and must come back unchanged, whatever
 * the name of the evaluation (accents, spaces, apostrophes).
 */
class GazeplayEvalGameVariantTest {

    @Test
    void shouldComeBackFromItsText() {
        GazeplayEvalGameVariant variant = new GazeplayEvalGameVariant("Tests sons et vidéos d'été");

        IGameVariant parsed = IGameVariant.toGameVariant(variant.toString());

        assertEquals(variant, parsed);
        assertEquals("GazeplayEvalGameVariant:Tests sons et vidéos d'été", variant.toString());
        assertEquals("Tests sons et vidéos d'été", variant.getLabel(null));
    }
}
