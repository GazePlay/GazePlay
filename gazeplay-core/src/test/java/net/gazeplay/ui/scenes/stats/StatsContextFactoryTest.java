package net.gazeplay.ui.scenes.stats;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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

    private final SavedStatsInfo mockSavedStatsInfo = new SavedStatsInfo(
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv")
    );

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(1920, 1080));
        when(mockTranslator.currentLocale()).thenReturn(Locale.ENGLISH);
        when(mockStats.getSavedStatsInfo()).thenReturn(mockSavedStatsInfo);
        when(mockConfig.getAreaOfInterestDisabledProperty()).thenReturn(new SimpleBooleanProperty(true));
        when(mockStats.getFixationSequence()).thenReturn(new ArrayList<>(List.of(new LinkedList<>(), new LinkedList<>())));
    }

    @Test
    void shouldCreateNewInstance() {
        StatsContext result = StatsContextFactory.newInstance(mockGazePlay, mockStats);

        BorderPane pane = (BorderPane) result.getRoot().getChildren().get(1);
        HBox box = (HBox) pane.getChildren().get(1);

        assertEquals(3, result.getRoot().getChildren().size());
        assertEquals(4, box.getChildren().size());
    }

    @Test
    void shouldCreateNewInstanceWithContinueButton() {
        CustomButton button = new CustomButton("bear.jpg", 300);
        StatsContext result =
            StatsContextFactory.newInstance(mockGazePlay, mockStats, button);

        BorderPane pane = (BorderPane) result.getRoot().getChildren().get(1);
        HBox box = (HBox) pane.getChildren().get(1);

        assertEquals(3, result.getRoot().getChildren().size());
        assertEquals(5, box.getChildren().size());
        assertEquals(button, box.getChildren().get(4));
    }
}
