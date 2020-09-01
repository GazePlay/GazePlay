package net.gazeplay.commons.gamevariants;

import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class StringGameVariantTest {

    @Mock
    private Translator translator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(translator.translate(anyString())).thenReturn("translation");
    }

    @Test
    void givenTranslator_shouldTranslateLabel() {
        StringGameVariant gameVariant = new StringGameVariant("label", "value");
        assertEquals("translation", gameVariant.getLabel(translator));
        verify(translator, times(1)).translate("label");
    }
}
