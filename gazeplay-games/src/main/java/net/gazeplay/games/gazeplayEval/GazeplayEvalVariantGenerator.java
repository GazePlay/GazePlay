package net.gazeplay.games.gazeplayEval;

import net.gazeplay.commons.eval.EvalLibrary;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * One variant per evaluation imported in the settings, in the order of their names.
 */
public class GazeplayEvalVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        Set<IGameVariant> variants = new LinkedHashSet<>();
        for (String name : EvalLibrary.names()) {
            variants.add(new GazeplayEvalGameVariant(name));
        }
        return variants;
    }
}
