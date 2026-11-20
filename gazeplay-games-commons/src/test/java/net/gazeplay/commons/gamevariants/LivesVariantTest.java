package net.gazeplay.commons.gamevariants;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The choice "with hearts / without hearts" of the games with lives (16/11/2026).
 */
class LivesVariantTest {

    @Test
    void shouldOfferBothChoicesOnlyWhereTheLivesApply() {
        // Without hearts first (20/11/2026): the easiest, chosen when the window of the variants opens
        assertEquals(List.of(LivesVariant.WITHOUT, LivesVariant.WITH), LivesVariant.options(true));
        assertEquals(List.of(LivesVariant.WITHOUT), LivesVariant.options(false));
    }

    @Test
    void shouldReadTheChoiceOfAVariantAndKeepTheHeartsOfTheVariantsOfBefore() {
        assertTrue(LivesVariant.withLives(IGameVariant.toGameVariant("GroupedGameVariant:A:B:2x2:-:LivesWith")));
        assertFalse(LivesVariant.withLives(IGameVariant.toGameVariant("GroupedGameVariant:A:B:2x2:-:End5:LivesWithout")));
        // Before the switch, or another kind of variant: with hearts
        assertTrue(LivesVariant.withLives(IGameVariant.toGameVariant("GroupedGameVariant:A:B:2x2:-:End5")));
        assertTrue(LivesVariant.withLives(IGameVariant.toGameVariant("DimensionGameVariant:2:2")));
        assertTrue(LivesVariant.withLives(null));
    }
}
