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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

enum TestEnum {
    TEST1, TEST2
}

@RunWith(MockitoJUnitRunner.class)
class EnumGameVariantTest {

    @Mock
    private Translator translator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(translator.translate(anyString())).thenReturn("translation");
    }

    @Test
    void givenTranslatorShouldTranslateLabel() {
        EnumGameVariant<TestEnum> gameVariant = new EnumGameVariant<>(TestEnum.TEST1, Object::toString);
        assertEquals("translation", gameVariant.getLabel(translator));
        verify(translator, times(1)).translate("TEST1");
    }
}
