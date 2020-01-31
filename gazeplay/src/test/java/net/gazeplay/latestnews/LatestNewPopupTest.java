package net.gazeplay.latestnews;

import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Dimension2D;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.screen.ScreenDimensionSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class LatestNewPopupTest {

    @Mock
    private GazePlay gazePlay;
    @Mock
    private Translator translator;
    @Mock
    private Configuration config;
    @Mock
    private ScreenDimensionSupplier screenDimensionSupplier;

    @BeforeEach
    void setup() {
        initMocks(this);
    }

    void createMockManifest(List<String> lines) throws IOException {
        new File("build/resources/test/META-INF").mkdir();
        Path file = Paths.get("build/resources/test/META-INF/MANIFEST.MF");
        Files.write(file, lines, StandardCharsets.UTF_8);

        MockitoAnnotations.initMocks(this);
        doReturn(screenDimensionSupplier).when(gazePlay).getCurrentScreenDimensionSupplier();
        doReturn(new Dimension2D(1024, 768)).when(screenDimensionSupplier).get();
            }

    @Test
    void shouldCreateDocumentURI() throws IOException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(config.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(config.getLanguage()).thenReturn("eng");
        when(translator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewPopup latestNewPopup = new LatestNewPopup(config, translator, screenDimensionSupplier);
            assertEquals("gazeplay-1-7-eng", latestNewPopup.createDocumentUri());
        });
    }
}
