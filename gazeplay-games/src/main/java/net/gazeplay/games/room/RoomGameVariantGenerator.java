package net.gazeplay.games.room;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of "La pièce" (maintainer's choices of 13/11/2026): the mode at the top (see {@link RoomMode}), the
 * room as buttons (see {@link RoomPlace}), and at the bottom the end (see {@link RoomEnd}) and the level (see
 * {@link RoomLevel}); the exploration has the four rooms, without end.
 */
public class RoomGameVariantGenerator implements IGameVariantGenerator {

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final RoomEnd end : RoomEnd.values()) {
            for (final RoomLevel level : RoomLevel.values()) {
                for (final RoomPlace place : RoomPlace.values()) {
                    variants.add(new GroupedGameVariant(RoomMode.SEARCH.getKey(), end.getKey(), place.getKey(), null,
                        level.getKey()));
                }
            }
        }
        for (final RoomPlace place : RoomPlace.values()) {
            if (place != RoomPlace.ALL) {
                variants.add(new GroupedGameVariant(RoomMode.EXPLORE.getKey(), RoomEnd.ENDLESS.getKey(),
                    place.getKey(), null, RoomLevel.PICTURE.getKey()));
            }
        }
        return variants;
    }
}
