package net.gazeplay.games.follow;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class FollowEmmanuelGameVariantGenerator extends EnumGameVariantGenerator<FollowEmmanuelGameVariant> {

    public FollowEmmanuelGameVariantGenerator() {
        super(FollowEmmanuelGameVariant.values(), FollowEmmanuelGameVariant::getLabel);
    }
}
