package net.gazeplay.games.whereisit.gamevariantgenerator;

import net.gazeplay.commons.gamevariants.CustomWhereIsItVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;
import net.gazeplay.commons.whereisit.CustomGame;
import net.gazeplay.commons.whereisit.CustomGames;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Where is it?": each game made by the user that can be played, with or without hearts. The menu
 * shows its own window instead of the variants; they are used by the shortcuts and the replay.
 */
public class CustomWhereIsItVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final CustomGame game : CustomGames.list()) {
            if (game.playableScreens().isEmpty()) {
                continue;
            }
            for (final String lives : LivesVariant.options(true)) {
                variants.add(new CustomWhereIsItVariant(game.getFolder(), lives));
            }
        }
        return variants;
    }
}
