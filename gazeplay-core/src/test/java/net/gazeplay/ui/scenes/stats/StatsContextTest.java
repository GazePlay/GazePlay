package net.gazeplay.ui.scenes.stats;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.ChiReport;
import net.gazeplay.commons.utils.stats.LevelsReport;
import net.gazeplay.commons.utils.stats.SavedStatsInfo;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.stats.ExplorationGamesStats;
import net.gazeplay.stats.HiddenItemsGamesStats;
import net.gazeplay.stats.SelectionGamesStats;
import net.gazeplay.stats.ShootGamesStats;
import net.gazeplay.ui.SystemFileManager;
import net.gazeplay.ui.menus.HelpDialog;
import net.gazeplay.ui.scenes.stats.StatsFigures.Figure;
import net.gazeplay.ui.scenes.stats.StatsFigures.View;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The statistics screen (refonte of 16/11/2026, maquette A): opens in the simple mode, the advanced mode shows the
 * detail and the views with data, in the look of the menus; the buttons.
 */
@ExtendWith(ApplicationExtension.class)
class StatsContextTest {

    @Mock
    private GazePlay mockGazePlay;

    @Mock
    private Translator mockTranslator;

    @Mock
    private Stats mockStats;

    private Configuration config;

    private final SavedStatsInfo mockSavedStatsInfo = new SavedStatsInfo(
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
        MockitoAnnotations.openMocks(this);
        when(mockGazePlay.getTranslator()).thenReturn(mockTranslator);
        when(mockGazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new javafx.geometry.Dimension2D(1920,
            1080));
        when(mockTranslator.currentLocale()).thenReturn(Locale.FRENCH);
        when(mockTranslator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockStats.getSavedStatsInfo()).thenReturn(mockSavedStatsInfo);
        when(mockStats.getFixationSequence()).thenReturn(new ArrayList<>(List.of(new LinkedList<>(),
            new LinkedList<>())));
        config = mock(Configuration.class);
        when(config.getEyeTracker()).thenReturn(EyeTracker.tobii_and_mouse.name());
        when(config.isFixationSequenceDisabled()).thenReturn(false);
        when(config.isAreaOfInterestDisabled()).thenReturn(true);
        when(config.getUserName()).thenReturn("Léa");
        ActiveConfigurationContext.setInstance(config);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private StatsContext screen(final Stats stats) throws Exception {
        return fx(() -> new StatsContext(mockGazePlay, new BorderPane(), stats));
    }

    /**
     * Maintainer's decision of 16/11/2026: the screen always opens in the simple mode; the switch shows the advanced
     * one and back.
     */
    @Test
    void shouldOpenInTheSimpleModeAndSwitchToTheAdvancedOne() throws Exception {
        final StatsContext screen = screen(mockStats);
        assertFalse(screen.getAdvanced().get());
        assertTrue(screen.getSimpleSwitch().isSelected());
        assertTrue(screen.getSimpleBody().isVisible());
        assertFalse(screen.getAdvancedBody().isVisible());

        fx(() -> {
            screen.getAdvancedSwitch().fire();
            return null;
        });
        assertTrue(screen.getAdvanced().get());
        assertFalse(screen.getSimpleBody().isVisible());
        assertTrue(screen.getAdvancedBody().isVisible());

        // A click on the chosen switch keeps it chosen
        fx(() -> {
            screen.getAdvancedSwitch().fire();
            return null;
        });
        assertTrue(screen.getAdvancedSwitch().isSelected());

        fx(() -> {
            screen.getSimpleSwitch().fire();
            return null;
        });
        assertFalse(screen.getAdvanced().get());
    }

    /**
     * The look of the menus, light or dark through the stylesheets: no color written in the screen.
     */
    @Test
    void shouldTakeTheLookOfTheMenus() throws Exception {
        final StatsContext screen = screen(mockStats);
        assertTrue(screen.getRoot().getStyleClass().contains("gp-menu"));
        assertTrue(screen.getRoot().getStyleClass().contains("gp-stats-screen"));
        assertTrue(screen.getRoot().getStyle() == null || screen.getRoot().getStyle().isEmpty());
    }

    /**
     * The game, its variant in clear, the profile and the date at the top.
     */
    @Test
    void shouldShowTheGameItsVariantAndTheProfile() throws Exception {
        when(mockStats.getCurrentGameNameCode()).thenReturn("WhereIsTheAnimal");
        when(mockStats.getCurrentGameVariant()).thenReturn("GroupedGameVariant:Farm:WhereIsItEnd10:2x3:-");
        final StatsContext screen = screen(mockStats);
        final List<String> texts = fx(() -> screen.getRoot().getTop().lookupAll(".label").stream()
            .map(node -> ((Label) node).getText()).toList());
        assertTrue(texts.contains("WhereIsTheAnimal"), texts.toString());
        assertTrue(texts.contains("Farm · WhereIsItEnd10 · 2 × 3"), texts.toString());
        assertTrue(texts.contains("StatsProfile Léa"), texts.toString());
    }

    @Test
    void shouldDescribeTheVariantWithoutTheNameOfTheGame() {
        assertEquals("WhereIsItEnd5 · 3 × 3",
            StatsFigures.variant("GroupedGameVariant:EggGame:WhereIsItEnd5:3x3:-", "EggGame", mockTranslator));
        // A variant of another kind keeps its own label
        assertEquals(net.gazeplay.commons.gamevariants.IGameVariant.toGameVariant("DimensionGameVariant:2:3")
            .getLabel(mockTranslator), StatsFigures.variant("DimensionGameVariant:2:3", "WhereIsIt", mockTranslator));
        assertNull(StatsFigures.variant(null, "WhereIsIt", mockTranslator));
        assertNull(StatsFigures.variant("SomethingUnknown", "WhereIsIt", mockTranslator));
    }

    @Test
    void shouldGiveTheFiguresOfEachKindOfGame() {
        final Locale fr = Locale.FRENCH;
        // Games that do not count the mistakes (-1)
        final ShootGamesStats shoot = mock(ShootGamesStats.class);
        when(shoot.getNbMistakes()).thenReturn(-1);
        assertEquals(List.of("TotalLength", "Shots", "HitRate", "ShotaverageLength"),
            StatsFigures.main(shoot, fr).stream().map(Figure::labelKey).toList());
        final ExplorationGamesStats exploration = mock(ExplorationGamesStats.class);
        when(exploration.getNbMistakes()).thenReturn(-1);
        assertEquals(List.of("TotalLength"),
            StatsFigures.main(exploration, fr).stream().map(Figure::labelKey).toList());
        assertTrue(StatsFigures.details(exploration, fr).isEmpty());
        final HiddenItemsGamesStats hidden = mock(HiddenItemsGamesStats.class);
        when(hidden.getNbMistakes()).thenReturn(-1);
        assertEquals(List.of("TotalLength", "HiddenItemsShot", "AverageLength"),
            StatsFigures.main(hidden, fr).stream().map(Figure::labelKey).toList());

        final SelectionGamesStats selection = mock(SelectionGamesStats.class);
        when(selection.getNbMistakes()).thenReturn(2);
        when(selection.getNbGoalsReached()).thenReturn(10);
        final List<Figure> figures = StatsFigures.main(selection, fr);
        assertEquals(List.of("TotalLength", "StatsSuccesses", "Mistakes", "AverageLength"),
            figures.stream().map(Figure::labelKey).toList());
        assertEquals("10", figures.get(1).value());
        assertTrue(figures.get(1).good());
        // A game that does not count the mistakes (-1) shows none
        when(selection.getNbMistakes()).thenReturn(-1);
        assertFalse(StatsFigures.main(selection, fr).stream().anyMatch(f -> f.labelKey().equals("Mistakes")));
        // A game that counts them shows 0 when there is none
        when(selection.getNbMistakes()).thenReturn(0);
        assertTrue(StatsFigures.main(selection, fr).contains(new Figure("Mistakes", "0", false)));
    }

    /**
     * 17/11/2026: the mistakes of every kind of game (before, only the selection games showed them: those of 30 games
     * were never seen), in the simple and the advanced mode; an exploration game with goals (La pièce, "Cherche")
     * shows its figures.
     */
    @Test
    void shouldShowTheMistakesOfEveryKindOfGame() {
        final Locale fr = Locale.FRENCH;
        final ShootGamesStats shoot = mock(ShootGamesStats.class);
        when(shoot.getNbMistakes()).thenReturn(3);
        assertEquals(List.of("TotalLength", "Shots", "HitRate", "Mistakes", "ShotaverageLength"),
            StatsFigures.main(shoot, fr).stream().map(Figure::labelKey).toList());
        assertTrue(StatsFigures.details(shoot, fr).contains(new Figure("Mistakes", "3", false)));

        final HiddenItemsGamesStats hidden = mock(HiddenItemsGamesStats.class);
        when(hidden.getNbMistakes()).thenReturn(1);
        assertEquals(List.of("TotalLength", "HiddenItemsShot", "Mistakes", "AverageLength"),
            StatsFigures.main(hidden, fr).stream().map(Figure::labelKey).toList());

        final Stats plain = mock(Stats.class);
        when(plain.getNbMistakes()).thenReturn(0);
        assertTrue(StatsFigures.main(plain, fr).contains(new Figure("Mistakes", "0", false)));

        final ExplorationGamesStats search = mock(ExplorationGamesStats.class);
        when(search.getNbGoalsToReach()).thenReturn(4);
        when(search.getNbGoalsReached()).thenReturn(3);
        when(search.getNbMistakes()).thenReturn(2);
        assertEquals(List.of("TotalLength", "StatsSuccesses", "Mistakes", "AverageLength"),
            StatsFigures.main(search, fr).stream().map(Figure::labelKey).toList());
        assertFalse(StatsFigures.details(search, fr).isEmpty());
    }

    @Test
    void shouldGiveTheFastestAndSlowestRounds() {
        final Stats stats = mock(Stats.class);
        when(stats.getOriginalDurationsBetweenGoals()).thenReturn(List.of(22_000L, 15_000L, 31_000L, 12_000L));
        final List<Figure> details = StatsFigures.details(stats, Locale.FRENCH);
        assertTrue(details.contains(new Figure("StatsFastest", "12,0 s (n° 4)", false)), details.toString());
        assertTrue(details.contains(new Figure("StatsSlowest", "31,0 s (n° 3)", false)), details.toString());
    }

    /**
     * GazePlay names its languages with three letters ("fra"): the numbers and dates take the language anyway.
     */
    @Test
    void shouldFormatTheNumbersInTheLanguageOfGazePlay() {
        assertEquals("fr", StatsFigures.javaLocale(Locale.of("fra", "FR")).getLanguage());
        assertEquals("FR", StatsFigures.javaLocale(Locale.of("fra", "FR")).getCountry());
        assertEquals("en", StatsFigures.javaLocale(Locale.of("eng")).getLanguage());
        assertEquals("18,4 s", StatsFigures.duration(18_400, StatsFigures.javaLocale(Locale.of("fra", "FR"))));
        assertEquals(Locale.FRENCH, StatsFigures.javaLocale(null));
    }

    @Test
    void shouldWriteTheDurationsForAPerson() {
        assertEquals("18,4 s", StatsFigures.duration(18_400, Locale.FRENCH));
        assertEquals("18.4 s", StatsFigures.duration(18_400, Locale.ENGLISH));
        assertEquals("3 min 42 s", StatsFigures.duration(222_000, Locale.FRENCH));
        assertEquals("1 h 02 min", StatsFigures.duration(3_725_000, Locale.FRENCH));
        assertEquals("0,0 s", StatsFigures.duration(-5, Locale.FRENCH));
    }

    /**
     * The views come from the data of the game, no longer from its name (the former screen missed the levels of the
     * dynamic "Where is the animal?").
     */
    @Test
    void shouldOfferOnlyTheViewsWithData() {
        final Stats empty = mock(Stats.class);
        assertEquals(List.of(View.HEATMAP), StatsFigures.views(empty, true));

        final Stats full = mock(Stats.class);
        when(full.getOriginalDurationsBetweenGoals()).thenReturn(List.of(1000L, 2000L));
        when(full.getFixationSequence()).thenReturn(new ArrayList<>(List.of(
            new LinkedList<>(List.of(new FixationPoint(1000, 200, 30, 40))), new LinkedList<>())));
        final LevelsReport levels = new LevelsReport();
        levels.addRoundLevel(1);
        when(full.getLevelsReport()).thenReturn(levels);
        when(full.getLevelsRounds()).thenReturn(levels.getOriginalLevelsPerRounds());
        final ChiReport chi = new ChiReport();
        chi.addChiObs(4.5);
        chi.addChiLevel(1);
        when(full.getChiReport()).thenReturn(chi);
        assertEquals(List.of(View.HEATMAP, View.DURATIONS, View.POSITIONS, View.LEVELS, View.CHI2),
            StatsFigures.views(full, true));
        // Without the fixation sequence (settings), no positions
        assertFalse(StatsFigures.views(full, false).contains(View.POSITIONS));
        assertEquals(View.DURATIONS, StatsFigures.firstAdvancedView(StatsFigures.views(full, true)));
        assertEquals(View.HEATMAP, StatsFigures.firstAdvancedView(List.of(View.HEATMAP)));
    }

    /**
     * Each view shows when its tab is chosen; the chi-squared table and the charts are built without error.
     */
    @Test
    void shouldShowEachViewOfItsTab() throws Exception {
        when(mockStats.getOriginalDurationsBetweenGoals()).thenReturn(List.of(22_000L, 15_000L, 19_000L));
        when(mockStats.computeRoundsDurationAverageDuration()).thenReturn(18_666L);
        when(mockStats.getFixationSequence()).thenReturn(new ArrayList<>(List.of(
            new LinkedList<>(List.of(new FixationPoint(1000, 200, 30, 40), new FixationPoint(2000, 300, 300, 400))),
            new LinkedList<>())));
        final LevelsReport levels = new LevelsReport();
        levels.addRoundLevel(1);
        levels.addRoundLevel(2);
        when(mockStats.getLevelsReport()).thenReturn(levels);
        when(mockStats.getLevelsRounds()).thenReturn(levels.getOriginalLevelsPerRounds());
        final ChiReport chi = new ChiReport();
        chi.addChiObs(4.5);
        chi.addChiLevel(1);
        when(mockStats.getChiReport()).thenReturn(chi);

        final StatsContext screen = screen(mockStats);
        assertEquals(View.DURATIONS, screen.getShownView());
        assertEquals(5, screen.getTabs().size());
        for (final View view : View.values()) {
            fx(() -> {
                screen.getTabs().get(view).fire();
                return null;
            });
            assertEquals(view, screen.getShownView());
            assertTrue(screen.getTabs().get(view).isSelected());
        }
    }

    /**
     * No "Continue" since 17/11/2026 (maintainer's request): the folder and the menu, the menu as the main button.
     */
    @Test
    void shouldOfferTheFolderAndTheMenuButNoContinue() throws Exception {
        final StatsContext screen = screen(mockStats);
        final List<String> texts = fx(() -> screen.getRoot().lookupAll(".gp-button").stream()
            .filter(javafx.scene.control.Button.class::isInstance).map(javafx.scene.control.Button.class::cast)
            .map(javafx.scene.control.Button::getText).toList());
        assertFalse(texts.contains("StatsContinue"), texts.toString());
        assertTrue(texts.contains("StatsMenu"), texts.toString());
        assertTrue(screen.getMenuButton().getStyleClass().contains("gp-button-primary"));
    }

    /**
     * The areas of interest and the scan path: in the advanced mode only, as the settings allow.
     */
    @Test
    void shouldOfferTheAreasOfInterestAndTheScanPathInTheAdvancedMode() throws Exception {
        final StatsContext screen = screen(mockStats);
        final Button areas = screen.getAreasOfInterestButton();
        assertFalse(areas.getParent().isVisible());
        fx(() -> {
            screen.getAdvancedSwitch().fire();
            return null;
        });
        assertTrue(areas.getParent().isVisible());
        assertTrue(areas.getParent().getChildrenUnmodifiable().contains(screen.getScanPathButton()));

        fx(() -> {
            areas.fire();
            return null;
        });
        verify(mockGazePlay).onDisplayAOI(mockStats);
        fx(() -> {
            screen.getScanPathButton().fire();
            return null;
        });
        verify(mockGazePlay).onDisplayScanpath(any());

        // Turned off in the settings: no buttons
        when(config.isAreaOfInterestDisabled()).thenReturn(false);
        when(config.isFixationSequenceDisabled()).thenReturn(true);
        final StatsContext without = screen(mockStats);
        assertNull(without.getAreasOfInterestButton().getParent());
        assertNull(without.getScanPathButton().getParent());
    }

    @Test
    void shouldOpenTheResultsFolderOfTheGamePlayed() throws Exception {
        // The files of the game were saved before midnight, the statistics screen is shown after
        final File savedFolder = new File(new File("statistics", "WhereIsTheShape"), "2026-10-23");
        when(mockStats.getSavedStatsInfo()).thenReturn(new SavedStatsInfo(
            new File(savedFolder, "heatmap.csv"), null, null, null, null, null, null));
        when(mockStats.getGameStatsOfTheDayDirectory())
            .thenReturn(new File(new File("statistics", "WhereIsTheShape"), "2026-10-24"));
        final StatsContext screen = screen(mockStats);
        try (MockedStatic<SystemFileManager> systemFileManager = mockStatic(SystemFileManager.class)) {
            screen.getFolderButton().fire();
            systemFileManager.verify(() -> SystemFileManager.openFolder(savedFolder));
        }
    }

    @Test
    void shouldOpenTheFolderOfTheDayWhenNoFileWasSaved() throws Exception {
        final File todayFolder = new File(new File("statistics", "WhereIsTheShape"), "2026-10-23");
        when(mockStats.getSavedStatsInfo()).thenReturn(null);
        when(mockStats.getGameStatsOfTheDayDirectory()).thenReturn(todayFolder);
        final StatsContext screen = screen(mockStats);
        try (MockedStatic<SystemFileManager> systemFileManager = mockStatic(SystemFileManager.class)) {
            screen.getFolderButton().fire();
            systemFileManager.verify(() -> SystemFileManager.openFolder(todayFolder));
        }
    }

    /**
     * A click on a view enlarges it to the whole screen, another click puts it back.
     */
    @Test
    void shouldEnlargeAViewAndPutItBack() throws Exception {
        final StatsContext screen = screen(mockStats);
        assertFalse(screen.getEnlarged().get());
        fx(() -> {
            screen.getEnlarged().set(true);
            return null;
        });
        assertFalse(screen.getSimpleBody().getChildren().get(0).isVisible(), "the tiles are hidden");
        // Changing the mode puts it back
        fx(() -> {
            screen.getAdvancedSwitch().fire();
            return null;
        });
        assertFalse(screen.getEnlarged().get());
    }

    /**
     * Maintainer's request of 5/11/2026: the "i" of each view opens a text explaining it.
     */
    @ParameterizedTest
    @ValueSource(strings = {"data/help/stats-heatmap-", "data/help/stats-chart-", "data/help/stats-details-",
        "data/help/stats-positions-", "data/help/stats-levels-", "data/help/stats-chi2-"})
    void shouldHaveTheExplanationOfEachViewInFrenchAndEnglish(String prefix) {
        for (String language : List.of("fra", "eng")) {
            List<String> lines = HelpDialog.read(prefix, language);
            assertNotNull(lines, prefix + language);
            assertTrue(lines.stream().anyMatch(line -> line.startsWith("# ")), prefix + language);
        }
    }

    /**
     * Only the images of the inputs that played the game (decision of the maintainer, October 2026).
     */
    @Test
    void shouldOfferOnlyTheImagesOfTheInputsUsed() {
        assertEquals(List.of("Mouse"), StatsContext.metricsShown(EyeTracker.mouse_control));
        assertEquals(List.of("Gaze"), StatsContext.metricsShown(EyeTracker.tobii));
        assertEquals(List.of("Mouse", "Gaze", "MouseAndGaze"), StatsContext.metricsShown(EyeTracker.tobii_and_mouse));
    }

    @Test
    void shouldGetChildren() throws Exception {
        final StatsContext context = screen(mockStats);
        assertEquals(context.getRoot().getChildren().size(), context.getChildren().size());
    }
}
