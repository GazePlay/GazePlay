package net.gazeplay.games.labyrinth;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of the Labyrinth (maintainer's choices of 9/11/2026): when it ends (buttons, see
 * {@link LabyrinthEnd}), and at the bottom the character (see {@link LabyrinthCharacter}), the level (see
 * {@link LabyrinthLevel}) and the way of moving (see {@link LabyrinthMove}).
 */
public class LabyrinthGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final LabyrinthCharacter character : LabyrinthCharacter.values()) {
            for (final LabyrinthLevel level : LabyrinthLevel.values()) {
                for (final LabyrinthMove move : LabyrinthMove.values()) {
                    for (final LabyrinthEnd end : LabyrinthEnd.values()) {
                        variants.add(new GroupedGameVariant(character.getKey(), level.getKey(), end.getKey(), null,
                            move.getKey()));
                    }
                }
            }
        }
        return variants;
    }
}
