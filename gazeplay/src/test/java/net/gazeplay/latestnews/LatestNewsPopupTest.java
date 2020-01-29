package net.gazeplay.latestnews;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import mockit.MockUp;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class LatestNewsPopupTest {

    @Mock
    Translator mockTranslator;
    @Mock
    private Configuration mockConfig;

    @BeforeEach
    void setup() {
        initMocks(this);
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

        List<String> linesEng = Collections.singletonList("<html>Eng<html/>");
        List<String> linesFra = Collections.singletonList("<html>Fra<html/>");
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
        Screen mockScreen = mock(Screen.class);
        when(mockScreen.getBounds()).thenReturn(new Rectangle2D(0, 0, 10, 10));

        new MockUp<Screen>() {
            @mockit.Mock
            public Screen getPrimary() {
                return mockScreen;
            }
        };

        assertEquals(new Dimension2D(7.5, 7.5), LatestNewsPopup.computePreferredDimension());
    }

    @Test
    void shouldDisplayIfNeeded() throws IOException, InterruptedException {
        List<String> lines = Arrays.asList("Implementation-Title: gazeplay", "Implementation-Version: 1.7");
        createMockManifest(lines);
        long mockLastTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(mockLastTime - 100));
        when(mockConfig.getLanguage()).thenReturn("eng");
        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(0L));
        when(mockConfig.isDebugEnabled()).thenReturn(false);
        when(mockTranslator.translate(ArgumentMatchers.<String>any())).thenReturn("some translation");

        Platform.runLater(() -> {
            LatestNewsPopup.displayIfNeeded(mockConfig, mockTranslator);
            assertNotEquals(mockLastTime - 100, mockConfig.getLatestNewsPopupShownTime().get());
            removeMockManifest();
        });

        waitForRunLater();
    }

    @Test
    void shouldNotDisplayIfShownRecently() {
        long lastTime = System.currentTimeMillis() - 100;
        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(lastTime));

        LatestNewsPopup.displayIfNeeded(mockConfig, mockTranslator);
        assertEquals(lastTime, mockConfig.getLatestNewsPopupShownTime().get());
    }

    @Test
    void shouldNotDisplayIfDebugEnabled() {
        long lastTime = 0;
        when(mockConfig.getLatestNewsPopupShownTime()).thenReturn(new SimpleLongProperty(lastTime));
        when(mockConfig.isDebugEnabled()).thenReturn(true);

        LatestNewsPopup.displayIfNeeded(mockConfig, mockTranslator);
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
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator);
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
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator);
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
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator);
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
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator);
            assertEquals("<html>Eng<html/>\r\n", latestNewsPopup.getOfflinePageContent());
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
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator);
            assertEquals("<html>Fra<html/>\r\n", latestNewsPopup.getOfflinePageContent());
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
            LatestNewsPopup latestNewsPopup = new LatestNewsPopup(mockConfig, mockTranslator);
            assertEquals("<html>Eng<html/>\r\n", latestNewsPopup.getOfflinePageContent());
            removeMockManifest();
        });

        waitForRunLater();
    }
}
