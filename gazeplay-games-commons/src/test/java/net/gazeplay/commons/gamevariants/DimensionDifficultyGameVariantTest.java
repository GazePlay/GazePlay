package net.gazeplay.commons.gamevariants;

import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DimensionDifficultyGameVariantTest {

    @Mock
    private Translator translator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void givenTranslatorShouldTranslateLabel() {
        DimensionDifficultyGameVariant gameVariant = new DimensionDifficultyGameVariant(123, 456, "Easy");
        assertEquals("123x456 " + translator.translate("Easy"), gameVariant.getLabel(translator));
    }
}
