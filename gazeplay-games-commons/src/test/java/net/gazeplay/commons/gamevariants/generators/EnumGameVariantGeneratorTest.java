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
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

enum TestEnum {
    TEST1, TEST2
}

@RunWith(MockitoJUnitRunner.class)
class EnumGameVariantGeneratorTest {

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
        EnumGameVariantGenerator<TestEnum> variantGenerator
            = new EnumGameVariantGenerator<>(new TestEnum[]{TestEnum.TEST1, TestEnum.TEST2}, Objects::toString);

        Set<IGameVariant> result = variantGenerator.getVariants();
        ArrayList<String> expected = new ArrayList<>(List.of("TEST1", "TEST2"));

        for (IGameVariant variant : result) {
            assertTrue(expected.contains(variant.getLabel(translator)));
        }
    }
}
