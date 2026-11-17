package net.gazeplay.ui.scenes.stats;

import javafx.geometry.Dimension2D;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.gaze.EyeTracker;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.FixationPoint;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class ScanpathViewTest {

    @Mock
    private GazePlay gazePlay;

    @Mock
    private Stats stats;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(gazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(1920, 1080));
        when(gazePlay.getTranslator()).thenReturn(mock(Translator.class));
        when(stats.getStartTime()).thenReturn(10_000L);
        // Mouse first, then gaze (FixationSequence); in memory, x holds the vertical coordinate and y the horizontal one
        final LinkedList<FixationPoint> gaze = new LinkedList<>(List.of(
            new FixationPoint(11_000, 300, 200, 100),
            new FixationPoint(13_000, 500, 400, 300),
            new FixationPoint(17_000, 200, 600, 500)));
        final LinkedList<FixationPoint> mouse = new LinkedList<>(List.of(
            new FixationPoint(12_000, 800, 100, 900)));
        when(stats.getFixationSequence()).thenReturn(new ArrayList<>(List.of(mouse, gaze)));
    }

    @Test
    void shouldOfferWhatTheInputsRecorded() {
        assertEquals(List.of(ScanpathView.Shown.MOUSE), ScanpathView.shownChoices(EyeTracker.mouse_control));
        assertEquals(List.of(ScanpathView.Shown.GAZE), ScanpathView.shownChoices(EyeTracker.tobii));
        assertEquals(List.of(ScanpathView.Shown.GAZE, ScanpathView.Shown.MOUSE, ScanpathView.Shown.BOTH),
            ScanpathView.shownChoices(EyeTracker.tobii_and_mouse));
    }

    /**
     * The gaze is shown first, all its fixations, at their place (x and y the right way round) and their moment from
     * the start of the game.
     */
    @Test
    void shouldShowAllTheFixationsOfTheGazeFirst() {
        final ScanpathView view = new ScanpathView(gazePlay, stats);

        assertEquals(ScanpathView.Shown.GAZE, view.getShown());
        assertEquals(-1, view.getCurrent());
        assertTrue(view.getShowAllButton().isSelected());
        final List<ScanpathView.Fixation> fixations = view.getFixations();
        assertEquals(3, fixations.size());
        assertEquals(100, fixations.get(0).x());
        assertEquals(200, fixations.get(0).y());
        assertEquals(1000, fixations.get(0).time());
        assertEquals(300, fixations.get(0).duration());
    }

    @Test
    void shouldMergeBothSourcesInTheOrderOfTheirTime() {
        final ScanpathView view = new ScanpathView(gazePlay, stats);

        view.show(ScanpathView.Shown.BOTH);

        final List<ScanpathView.Fixation> fixations = view.getFixations();
        assertEquals(4, fixations.size());
        assertFalse(fixations.get(1).gaze());
        assertEquals(2000, fixations.get(1).time());
    }

    @Test
    void shouldGoThroughTheFixationsOneByOne() {
        final ScanpathView view = new ScanpathView(gazePlay, stats);

        view.showFixation(1);
        assertEquals(1, view.getCurrent());
        assertFalse(view.getShowAllButton().isSelected());

        view.showFixation(10);
        assertEquals(2, view.getCurrent());

        assertEquals(0, view.fixationAt(0));
        assertEquals(1, view.fixationAt(3500));
        assertEquals(2, view.fixationAt(7000));

        view.showAll();
        assertEquals(-1, view.getCurrent());
        assertTrue(view.getShowAllButton().isSelected());
    }

    @Test
    void shouldShowAnEmptySourceWithoutFailing() {
        when(stats.getFixationSequence()).thenReturn(new ArrayList<>(List.of(new LinkedList<>(), new LinkedList<>())));

        final ScanpathView view = new ScanpathView(gazePlay, stats);

        assertTrue(view.getFixations().isEmpty());
        assertTrue(view.getPlayButton().isDisabled());
    }
}
