package net.gazeplay.latestnews;

import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class LatestNewPopupTest {

    @Mock
    Translator mockTanslator;
    @Mock
    private Configuration mockConfig;

    @BeforeEach
    void setup() {
        initMocks(this);
    }

    @Test
    void shouldCreateDocumentURI() {
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("eng");
        when(mockTanslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewPopup latestNewPopup = new LatestNewPopup(mockConfig, mockTanslator);
            assertEquals("gazeplay-1-7-eng", latestNewPopup.createDocumentUri());
        });
    }
}
