package net.gazeplay.ui.scenes.stats;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.chart.LineChart;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import mockit.MockUp;
import net.gazeplay.GazePlay;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.CustomButton;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.StatDisplayUtils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.stats.ExplorationGamesStats;
import net.gazeplay.stats.HiddenItemsGamesStats;
import net.gazeplay.stats.ShootGamesStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
class StatsContextTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Translator mockTranslator;

    @Mock
    private Stats mockStats;

    private SavedStatsInfo mockSavedStatsInfo = new SavedStatsInfo(
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv"),
        new File("file.csv")
    );

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(1920, 1080));
        when(mockTranslator.currentLocale()).thenReturn(Locale.ENGLISH);
        when(mockStats.getSavedStatsInfo()).thenReturn(mockSavedStatsInfo);
        when(mockStats.getFixationSequence()).thenReturn(new ArrayList<>(List.of(new LinkedList<>(), new LinkedList<>())));
    }

    @Test
    void shouldChangeWidth() {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));
        SimpleDoubleProperty widthProperty = new SimpleDoubleProperty(100);
        ImageView metrics = spy(new ImageView("bear.jpg"));
        new MockUp<StatDisplayUtils>() {
            @mockit.Mock
            public ImageView buildGazeMetrics(Stats stats, Region root) {
                return metrics;
            }
        };

        BorderPane rootSpy = spy(new BorderPane());
        when(rootSpy.widthProperty()).thenReturn(widthProperty);
        new StatsContext(mockGazePlay, rootSpy, mockStats, null);

        widthProperty.set(200);
        verify(metrics).setFitWidth(200 * 0.35);
    }

    @Test
    void shouldChangeHeight() {
        when(mockTranslator.currentLocale()).thenReturn(new Locale("ara"));
        SimpleDoubleProperty heightProperty = new SimpleDoubleProperty(100);
        ImageView metrics = spy(new ImageView("bear.jpg"));
        new MockUp<StatDisplayUtils>() {
            @mockit.Mock
            public ImageView buildGazeMetrics(Stats stats, Region root) {
                return metrics;
            }
        };

        BorderPane rootSpy = spy(new BorderPane());
        when(rootSpy.heightProperty()).thenReturn(heightProperty);
        new StatsContext(mockGazePlay, rootSpy, mockStats, null);

        heightProperty.set(200);
        verify(metrics).setFitHeight(200 * 0.35);
    }

    @Test
    void shouldAddAreaChartOnColorBandSelected() {
        ArrayList<LinkedList<FixationPoint>> fixationPoints = new ArrayList<>(List.of(
            new LinkedList<>(List.of(
                new FixationPoint(1234, 2345, 30, 40),
                new FixationPoint(4567, 1234, 40, 60)
            )),
            new LinkedList<>(List.of(
                new FixationPoint(1234, 2345, 30, 40),
                new FixationPoint(4567, 1234, 40, 60)
            ))
        )
        );
        when(mockStats.getFixationSequence()).thenReturn(fixationPoints);

        Configuration mockConfig = mock(Configuration.class);
        when(mockConfig.getAreaOfInterestDisabledProperty()).thenReturn(new SimpleBooleanProperty(true));

        new MockUp<ActiveConfigurationContext>() {
            @mockit.Mock
            public Configuration getInstance() {
                return mockConfig;
            }
        };

        BorderPane root = new BorderPane();
        new StatsContext(mockGazePlay, root, mockStats, null);

        BorderPane sidePane = (BorderPane) root.getCenter();
        StackPane centerStackPane = (StackPane) sidePane.getTop();
        VBox centerPane = (VBox) centerStackPane.getChildren().get(0);
        HBox controlPane = (HBox) sidePane.getBottom();
        RadioButton colorBands = (RadioButton) controlPane.getChildren().get(1);

        LineChart<String, Number> lineChart = (LineChart<String, Number>) centerPane.getChildren().get(2);
        assertTrue(centerPane.getChildren().contains(lineChart));

        colorBands.fire();
        assertFalse(centerPane.getChildren().contains(lineChart));

        colorBands.fire();
        assertTrue(centerPane.getChildren().contains(lineChart));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldAddAllToGridShootGames(boolean alignLeft) {
        BorderPane root = new BorderPane();
        StatsContext context = new StatsContext(mockGazePlay, root, mockStats, null);

        GridPane grid = new GridPane();
        ShootGamesStats stats = mock(ShootGamesStats.class);
        when(stats.getNbUnCountedGoalsReached()).thenReturn(3);
        context.addAllToGrid(stats, mockTranslator, grid, alignLeft);

        assertEquals(16, grid.getChildren().size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldAddAllToGridExplorationGames(boolean alignLeft) {
        BorderPane root = new BorderPane();
        StatsContext context = new StatsContext(mockGazePlay, root, mockStats, null);

        GridPane grid = new GridPane();
        ExplorationGamesStats stats = mock(ExplorationGamesStats.class);
        context.addAllToGrid(stats, mockTranslator, grid, alignLeft);

        assertEquals(2, grid.getChildren().size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldAddAllToGridHiddenItemsGames(boolean alignLeft) {
        BorderPane root = new BorderPane();
        StatsContext context = new StatsContext(mockGazePlay, root, mockStats, null);

        GridPane grid = new GridPane();
        HiddenItemsGamesStats stats = mock(HiddenItemsGamesStats.class);
        context.addAllToGrid(stats, mockTranslator, grid, alignLeft);

        assertEquals(12, grid.getChildren().size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldAddAllToGrid(boolean alignLeft) {
        BorderPane root = new BorderPane();
        StatsContext context = new StatsContext(mockGazePlay, root, mockStats, null);

        GridPane grid = new GridPane();
        Stats stats = mock(Stats.class);
        context.addAllToGrid(stats, mockTranslator, grid, alignLeft);

        assertEquals(12, grid.getChildren().size());
    }

    @Test
    void shouldCreateControlButtonPane() throws InterruptedException {
        BorderPane root = new BorderPane();
        StatsContext context = new StatsContext(mockGazePlay, root, mockStats, null);

        Configuration mockConfig = mock(Configuration.class);
        when(mockConfig.getAreaOfInterestDisabledProperty()).thenReturn(new SimpleBooleanProperty(true));
        when(mockConfig.isFixationSequenceDisabled()).thenReturn(false);
        RadioButton radioButton = new RadioButton();
        CustomButton button = new CustomButton("bear.jpg", 300);
        HBox result = context.createControlButtonPane(mockGazePlay, mockStats, mockConfig, radioButton, null, button, false);

        assertEquals(5, result.getChildren().size());

        CustomButton aoiButton = (CustomButton) result.getChildren().get(0);
        CustomButton scanPathButton = (CustomButton) result.getChildren().get(2);

        Platform.runLater(() -> aoiButton.fireEvent(TestingUtils.clickOnTarget(aoiButton)));
        TestingUtils.waitForRunLater();

        verify(mockGazePlay).onDisplayAOI(mockStats);

        Platform.runLater(() -> scanPathButton.fireEvent(TestingUtils.clickOnTarget(scanPathButton)));
        TestingUtils.waitForRunLater();

        verify(mockGazePlay).onDisplayScanpath(any());
    }

    @Test
    void shouldCreateControlButtonPaneNoContinueButton() {
        BorderPane root = new BorderPane();
        StatsContext context = new StatsContext(mockGazePlay, root, mockStats, null);

        Configuration mockConfig = mock(Configuration.class);
        when(mockConfig.getAreaOfInterestDisabledProperty()).thenReturn(new SimpleBooleanProperty(false));
        when(mockConfig.isFixationSequenceDisabled()).thenReturn(true);
        RadioButton radioButton = new RadioButton();
        HBox result = context.createControlButtonPane(mockGazePlay, mockStats, mockConfig, radioButton, null, null, false);

        assertEquals(1, result.getChildren().size());
    }

    @Test
    void shouldGetChildren() {
        BorderPane root = new BorderPane();
        StatsContext context = new StatsContext(mockGazePlay, root, mockStats, null);

        assertEquals(root.getChildren().size(), context.getChildren().size());
    }
}
