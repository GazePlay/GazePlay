package net.gazeplay.games.gazeplayEvalTest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.ArrayList;
import java.util.Set;

public class GazeplayEvalTestVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {

        ArrayList<GazeplayEvalGameVariant> game = Lists.newArrayList();
        game.add(0, new GazeplayEvalGameVariant("Test"));

        return Sets.newLinkedHashSet(game);
    }

}
