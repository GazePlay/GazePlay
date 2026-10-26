package net.gazeplay.games.follow;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

/**
 * Gives the variants of the game "Follow" proposed in the menu.
 */
public class FollowGameVariantGenerator extends EnumGameVariantGenerator<FollowGameVariant> {

    public FollowGameVariantGenerator() {
        super(FollowGameVariant.values(), FollowGameVariant::getLabel);
    }
}
