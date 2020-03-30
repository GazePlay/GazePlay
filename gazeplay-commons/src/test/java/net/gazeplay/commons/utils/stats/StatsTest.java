package net.gazeplay.commons.utils.stats;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import mockit.MockUp;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
class StatsTest {

    @Mock
    private Scene mockScene;

    private Stats stats;

    @BeforeEach
    void setUp() {
        initMocks();
        stats = new Stats(mockScene, "testGame");
    }

    void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldInstantiateHeatMapData() {
        when(mockScene.getHeight()).thenReturn(30.0);
        when(mockScene.getWidth()).thenReturn(60.0);

        double[][] result = Stats.instantiateHeatMapData(mockScene, 3.0);

        assertEquals(10, result.length);
        assertEquals(20, result[0].length);
    }

    @Test
    void shouldSetTargetAOIList() {
        ArrayList<TargetAOI> testList = new ArrayList<>(List.of(
            new TargetAOI(1, 1, 2, 1000),
            new TargetAOI(2, 2, 2, 3000),
            new TargetAOI(3, 3, 2, 6000)
        ));

        stats.setTargetAOIList(testList);
        ArrayList<TargetAOI> resultList = stats.getTargetAOIList();

        assertEquals(2000, resultList.get(0).getDuration());
        assertEquals(3000, resultList.get(1).getDuration());
        assertEquals(0, resultList.get(2).getDuration());
    }

    @Test
    void shouldSetEmptyTargetAOIList() {
        ArrayList<TargetAOI> testList = new ArrayList<>();

        stats.setTargetAOIList(testList);
        ArrayList<TargetAOI> resultList = stats.getTargetAOIList();

        assertEquals(testList, resultList);
    }

    @Test
    void shouldTakeScreenshotWhenNewRoundIsReady() {
        Stats statsSpy = spy(stats);

        statsSpy.notifyNewRoundReady();

        verify(statsSpy).takeScreenShot();
    }

    @Test
    void shouldAddRoundDurationToReportOnNextRound() {
        long start = System.currentTimeMillis();
        stats.notifyNewRoundReady();

        long end = System.currentTimeMillis();
        stats.notifyNextRound();

        Long duration = end - start;

        assertTrue(stats.getOriginalDurationsBetweenGoals().contains(duration));
    }

    @Test
    void shouldCreateVideoRecording() {
        File buildDir = new File(System.getProperty("user.dir"), "build");

        new MockUp<GazePlayDirectories>() {
            @mockit.Mock
            public File getUserStatsFolder(String user) {
                return buildDir;
            }
        };

        stats.startVideoRecording();
        stats.endVideoRecording();

        File gameFolder = new File(buildDir, "testGame");
        File todayDirectory = new File(gameFolder, DateUtils.today());
        assertNotEquals(0, todayDirectory.list().length);
    }

    @Test
    void shouldCreateVideoRecordingWhenStartsAndStops() {
        Stats statsSpy = spy(stats);
        File buildDir = new File(System.getProperty("user.dir"), "build");

        Configuration mockConfig = mock(Configuration.class);
        when(mockConfig.isVideoRecordingEnabled()).thenReturn(true);

        new MockUp<GazePlayDirectories>() {
            @mockit.Mock
            public File getUserStatsFolder(String user) {
                return buildDir;
            }
        };
        new MockUp<ActiveConfigurationContext>() {
            @mockit.Mock
            public Configuration getInstance() {
                return mockConfig;
            }
        };

        statsSpy.start();
        statsSpy.stop();

        verify(statsSpy).startVideoRecording();
        verify(statsSpy).endVideoRecording();

        statsSpy.reset();

        verify(statsSpy, atLeastOnce()).start();
    }

    @Test
    void shouldIncrementHeatMapOnGazeMoved() {
        Stats statsSpy = spy(stats);

        statsSpy.start();
        statsSpy.gazeMoved(new Point2D(20.0, 40.0));

        verify(statsSpy).incrementHeatMap(20, 40);
        verify(statsSpy).incrementFixationSequence(20, 40);
    }
}
