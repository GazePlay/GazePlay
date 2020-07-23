package net.gazeplay.commons.gamevariants.generators;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoVariantGeneratorTest {

    @Test
    void shouldGetVariants() {
        NoVariantGenerator variantGenerator = new NoVariantGenerator();
        assertEquals(new LinkedHashSet<>(), variantGenerator.getVariants());
    }
}
