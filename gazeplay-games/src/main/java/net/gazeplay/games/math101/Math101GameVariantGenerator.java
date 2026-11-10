package net.gazeplay.games.math101;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "Apprends les Maths" (maintainer's choices of 10/11/2026): the levels of the chosen topic as
 * buttons (see {@link MathLevel}), and at the bottom the topic (see {@link MathTopic}), when it ends (see
 * {@link MathEnd}) and what a wrong answer does (see {@link MathMistakes}).
 */
public class Math101GameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final MathTopic topic : MathTopic.values()) {
            for (final MathEnd end : MathEnd.values()) {
                for (final MathMistakes mistakes : MathMistakes.values()) {
                    for (final MathLevel level : topic.levels()) {
                        variants.add(new GroupedGameVariant(topic.getKey(), end.getKey(), level.getKey(), null,
                            mistakes.getKey()));
                    }
                }
            }
        }
        return variants;
    }
}
