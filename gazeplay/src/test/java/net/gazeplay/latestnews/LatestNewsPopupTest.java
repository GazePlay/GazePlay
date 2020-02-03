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
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class LatestNewsPopupTest {

    @Mock
    private Translator mockTranslator;

    @Mock
    private Configuration mockConfig;

    @Mock
    private GazePlay gazePlay;

    @Mock
    private ScreenDimensionSupplier screenDimensionSupplier;

    @BeforeEach
    void setup() {
        initMocks(this);
        when(gazePlay.getCurrentScreenDimensionSupplier()).thenReturn(screenDimensionSupplier);
        when(screenDimensionSupplier.get()).thenReturn(new Dimension2D(1024, 768));
    }

    void createMockManifest(List<String> lines) throws IOException {
        new File("build/resources/test/META-INF").mkdir();
        Path file = Paths.get("build/resources/test/META-INF/MANIFEST.MF");
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    void removeMockManifest() {
        new File("build/resources/test/META-INF").delete();
    }

    void createMockOfflinePage() throws IOException {
        new File("build/resources/test/updates-popup").mkdir();
        Path fileEng = Paths.get("build/resources/test/updates-popup/offline-page-eng.html");
        Path fileFra = Paths.get("build/resources/test/updates-popup/offline-page-fra.html");

        List<String> linesEng = Collections.singletonList("<html>Eng { version }<html/>");
        List<String> linesFra = Collections.singletonList("<html>Fra { version }<html/>");
        Files.write(fileEng, linesEng, StandardCharsets.UTF_8);
        Files.write(fileFra, linesFra, StandardCharsets.UTF_8);
    }

    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(semaphore::release);
        semaphore.acquire();
    }

    @Test
    void shouldFindEnvInfo() {
        System.setProperty("os.name", "linux");
        System.setProperty("os.version", "1.2.3");
        System.setProperty("java.vm.vendor", "gazeplay");
        System.setProperty("java.vm.version", "11");

        assertEquals("linux 1.2.3 - gazeplay 11", LatestNewsPopup.findEnvInfo());
    }

    @Test
    void shouldComputePreferredDimension() {
        assertEquals(new Dimension2D(7.5, 7.5), LatestNewsPopup.computePreferredDimension(() -> new Dimension2D(10, 10)));
    }

    @Test
    void shouldDisplayIfNeeded() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("eng");
        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(0L));
        when(mockConfig.isLatestNewsDisplayForced()).thenReturn(true);
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup.displayIfNeeded(mockConfig, mockTranslator, screenDimensionSupplier);
            assertNotEquals(mockLastTime - 100, mockConfig.getLatestNewsPopupShownTime().get());
            removeMockManifest();
        });

        waitForRunLater();
    }

    @Test
    void shouldDisplayIfForced() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);

        // Show recently, but we still want to force it shown.
        long lastTime = System.currentTimeMillis() - 100;
        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(lastTime));

        when(mockConfig.getLanguage()).thenReturn("eng");
        when(mockConfig.isLatestNewsDisplayForced()).thenReturn(true);
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup.displayIfNeeded(mockConfig, mockTranslator, screenDimensionSupplier);
            assertNotEquals(lastTime, mockConfig.getLatestNewsPopupShownTime().get());
            removeMockManifest();
        });

        waitForRunLater();
    }

    @Test
    void shouldNotDisplayIfShownRecently() {
        long lastTime = System.currentTimeMillis() - 100;
        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(lastTime));

        LatestNewsPopup.displayIfNeeded(mockConfig, mockTranslator, screenDimensionSupplier);
        assertEquals(lastTime, mockConfig.getLatestNewsPopupShownTime().get());
    }

    @Test
    void shouldCreateFraDocumentURI() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("fra");
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator, screenDimensionSupplier);
            assertEquals("gazeplay-1-7-fra", latestNewsPopup.createDocumentUri());
            removeMockManifest();
        });

        waitForRunLater();
    }

    @Test
    void shouldCreateEngDocumentURI() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("eng");
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator, screenDimensionSupplier);
            assertEquals("gazeplay-1-7-eng", latestNewsPopup.createDocumentUri());
            removeMockManifest();
        });

        waitForRunLater();
    }

    @Test
    void shouldCreateEmptyDocumentURI() throws IOException, InterruptedException {
        List<String> lines = Collections.singletonList("Implementation-Title: gazeplay");
        createMockManifest(lines);
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("eng");
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator, screenDimensionSupplier);
            assertEquals("", latestNewsPopup.createDocumentUri());
            removeMockManifest();
        });

        waitForRunLater();
    }

    @Test
    void shouldGetOfflinePageContentEnglish() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);
        createMockOfflinePage();
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("eng");
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator, screenDimensionSupplier);
            assertTrue(latestNewsPopup.getOfflinePageContent().contains("<html>Eng 1.7<html/>"));
            removeMockManifest();
        });

        waitForRunLater();
    }

    @Test
    void shouldGetOfflinePageContentFrench() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);
        createMockOfflinePage();
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("fra");
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator, screenDimensionSupplier);
            assertTrue(latestNewsPopup.getOfflinePageContent().contains("<html>Fra 1.7<html/>"));
            removeMockManifest();
        });

        waitForRunLater();
    }

    @Test
    void shouldShowEnglishWhenNoOfflinePageAvailable() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);
        createMockOfflinePage();
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("esp");
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator, screenDimensionSupplier);
            assertTrue(latestNewsPopup.getOfflinePageContent().contains("<html>Eng 1.7<html/>"));
            removeMockManifest();
        });

        waitForRunLater();
    }

    @Test
    void shouldShowUnknownVersionWhenNoVersionAvailable() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay");
        createMockManifest(lines);
        createMockOfflinePage();
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("esp");
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator, screenDimensionSupplier);
            assertTrue(latestNewsPopup.getOfflinePageContent().contains("<html>Eng unknown version<html/>"));
            removeMockManifest();
        });

        waitForRunLater();
    }
}
