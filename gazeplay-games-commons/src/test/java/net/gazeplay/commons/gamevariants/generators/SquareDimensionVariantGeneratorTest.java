package net.gazeplay.commons.gamevariants.generators;

import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class SquareDimensionVariantGeneratorTest {

    @Mock
    private Translator translator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // This simply returns whatever we pass in
        when(translator.translate(anyString())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            return (String) args[0];
        });
    }

    @Test
    void shouldGetVariants() {
        SquareDimensionVariantGenerator variantGenerator
            = new SquareDimensionVariantGenerator(1, 3);

        Set<IGameVariant> result = variantGenerator.getVariants();
        ArrayList<String> expected = new ArrayList<>(List.of("1x1", "2x2", "3x3"));

        for (IGameVariant variant : result) {
            assertTrue(expected.contains(variant.getLabel(translator)));
        }
    }
}
