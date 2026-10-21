package net.gazeplay.commons.gamevariants;

import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DimensionGameVariantTest {

    @Mock
    private Translator translator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void givenTranslatorShouldTranslateLabel() {
        DimensionGameVariant gameVariant = new DimensionGameVariant(123, 456);
        assertEquals("123x456", gameVariant.getLabel(translator));
    }
}
