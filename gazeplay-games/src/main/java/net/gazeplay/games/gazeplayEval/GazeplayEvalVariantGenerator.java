package net.gazeplay.games.gazeplayEval;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;
import static net.gazeplay.games.gazeplayEval.config.Const.ROOT_DIRECTORY;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class GazeplayEvalVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        File directoryPath = new File(ROOT_DIRECTORY);
        String[] content = directoryPath.list();
        ArrayList<GazeplayEvalGameVariant> game = Lists.newArrayList();

        if (content != null && content.length > 0)
            for (int i = 0; i < content.length; i++)
                game.add(i, new GazeplayEvalGameVariant(content[i]));

        return Sets.newLinkedHashSet(game);
    }
}
