package net.gazeplay.commons.gamevariants;

import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class DimensionDifficultyGameVariantTest {

    @Mock
    private Translator translator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenTranslatorShouldTranslateLabel() {
        DimensionDifficultyGameVariant gameVariant = new DimensionDifficultyGameVariant(123, 456, "easy");
        assertEquals("123x456 " + translator.translate("easy"), gameVariant.getLabel(translator));
    }
}
