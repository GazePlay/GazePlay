package net.gazeplay.games.follow;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class FollowGameVariantGenerator extends EnumGameVariantGenerator<FollowGameVariant> {

    public FollowGameVariantGenerator() {
        super(FollowGameVariant.values(), FollowGameVariant::getLabel);
    }
}
