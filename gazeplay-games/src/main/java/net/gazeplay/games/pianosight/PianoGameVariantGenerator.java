package net.gazeplay.games.pianosight;

import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Gives the variants of the Piano (maintainer's choices of 13/11/2026): the mode at the top (see {@link PianoMode}),
 * the end of the songs as buttons (see {@link PianoEnd}), a single value in the free mode; one keyboard (a single
 * sub-group, its row is not shown).
 */
public class PianoGameVariantGenerator implements IGameVariantGenerator {

    static final String KEYBOARD = "PianoOctave";

    @Override
    public Set<IGameVariant> getVariants() {
        final Set<IGameVariant> variants = new LinkedHashSet<>();
        for (final PianoEnd end : new PianoEnd[]{PianoEnd.SONGS_1, PianoEnd.SONGS_3, PianoEnd.SONGS_5,
            PianoEnd.ENDLESS}) {
            variants.add(new GroupedGameVariant(PianoMode.SONG.getKey(), KEYBOARD, end.getKey(), null));
        }
        variants.add(new GroupedGameVariant(PianoMode.FREE.getKey(), KEYBOARD, PianoEnd.FREE.getKey(), null));
        return variants;
    }
}
