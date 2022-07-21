package net.gazeplay.commons.gamevariants.generators;

import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class IntListVariantGeneratorTest {

    @Mock
    private Translator translator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGetVariants() {
        IntListVariantGenerator variantGenerator = new IntListVariantGenerator("choose_text", 1, 2, 23, 46, 5);
        Set<IGameVariant> result = variantGenerator.getVariants();
        ArrayList<String> expected = new ArrayList<>(List.of("1", "2", "23", "46", "5"));

        for (IGameVariant variant : result) {
            assertTrue(expected.contains(variant.getLabel(translator)));
        }
    }
}
