package net.gazeplay.ui.scenes.stats;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class StatsContextFactoryTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Translator mockTranslator;

    @Mock
    private Stats mockStats;

    @Mock
    private Configuration mockConfig;

    private SavedStatsInfo mockSavedStatsInfo = new SavedStatsInfo(
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv")
    );

    @AfterEach
    void restoreGlobalState() {
        ActiveConfigurationContext.switchToDefaultUser();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(1920, 1080));
        when(mockTranslator.currentLocale()).thenReturn(Locale.ENGLISH);
        when(mockStats.getSavedStatsInfo()).thenReturn(mockSavedStatsInfo);
        when(mockConfig.getAreaOfInterestDisabledProperty()).thenReturn(new SimpleBooleanProperty(true));
        when(mockConfig.isAreaOfInterestDisabled()).thenReturn(true);
        when(mockStats.getFixationSequence()).thenReturn(new ArrayList<>(List.of(new LinkedList<>(), new LinkedList<>())));

        ActiveConfigurationContext.setInstance(mockConfig);
    }

    @Test
    void shouldCreateNewInstance() {
        StatsContext result = StatsContextFactory.newInstance(mockGazePlay, mockStats);

        BorderPane pane = (BorderPane) result.getRoot().getChildren().get(1);
        HBox box = (HBox) pane.getChildren().get(1);

        assertEquals(3, result.getRoot().getChildren().size());
        // Including the button of the results folder
        assertEquals(5, box.getChildren().size());
    }

    @Test
    void shouldCreateNewInstanceWithContinueButton() {
        CustomButton button = new CustomButton("bear.jpg", 300);
        StatsContext result =
            StatsContextFactory.newInstance(mockGazePlay, mockStats, button);

        BorderPane pane = (BorderPane) result.getRoot().getChildren().get(1);
        HBox box = (HBox) pane.getChildren().get(1);

        assertEquals(3, result.getRoot().getChildren().size());
        // Including the button of the results folder
        assertEquals(6, box.getChildren().size());
        assertEquals(button, box.getChildren().get(5));
    }
}
