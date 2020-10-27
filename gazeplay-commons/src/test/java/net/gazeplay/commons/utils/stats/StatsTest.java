package net.gazeplay.commons.utils.stats;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import mockit.MockUp;
import net.gazeplay.TestingUtils;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.FixationSequence;
import net.gazeplay.commons.utils.games.DateUtils;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit5.ApplicationExtension;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(ApplicationExtension.class)
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
        when(mockScene.getWidth()).thenReturn(1080d);
        when(mockScene.getHeight()).thenReturn(1920d);
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

        testList.get(0).setTimeEnded(2500);
        testList.get(1).setTimeEnded(5500);
        testList.get(2).setTimeEnded(6000);

        stats.setTargetAOIList(testList);
        ArrayList<TargetAOI> resultList = stats.getTargetAOIList();

        assertEquals(1500, resultList.get(0).getDuration());
        assertEquals(2500, resultList.get(1).getDuration());
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

        when(statsSpy.gameContextScene.getRoot()).thenReturn(new Pane());

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
        when(statsSpy.gameContextScene.getRoot()).thenReturn(new Pane());

        statsSpy.start();
        statsSpy.gazeMoved(new Point2D(20.0, 40.0));

        verify(statsSpy).incrementHeatMap(20, 40);
    }

    @Test
    void shouldSaveImageAsPNG() {
        BufferedImage image = new BufferedImage(30, 40, BufferedImage.TYPE_INT_RGB);
        File testFile = new File("testImage.png");

        Stats.saveImageAsPng(image, testFile);

        assertTrue(testFile.isFile());

        testFile.delete();
    }

    @Test
    void shouldSaveStats() throws InterruptedException {
        File buildDir = new File(System.getProperty("user.dir"), "build");
        BufferedImage image = new BufferedImage(30, 40, BufferedImage.TYPE_INT_RGB);
        Configuration mockConfig = mock(Configuration.class);
        when(mockConfig.isHeatMapDisabled()).thenReturn(false);
        when(mockConfig.isFixationSequenceDisabled()).thenReturn(false);

        new MockUp<GazePlayDirectories>() {
            @mockit.Mock
            public File getUserStatsFolder(String user) {
                return buildDir;
            }
        };
        new MockUp<SwingFXUtils>() {
            @mockit.Mock
            BufferedImage fromFXImage(Image wi, BufferedImage bi) {
                return image;
            }
        };
        new MockUp<ActiveConfigurationContext>() {
            public Configuration getInstance() {
                return mockConfig;
            }
        };

        when(stats.gameContextScene.getRoot()).thenReturn(new Pane());

        stats.start();
        stats.gazeMoved(new Point2D(30, 40));
        stats.gazeMoved(new Point2D(20, 50));

        Platform.runLater(() -> {
            try {
                stats.saveStats();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        TestingUtils.waitForRunLater();

        assertNotEquals(0, buildDir.list().length);
    }

    @Test
    void shouldIncrementNumberOfGoalsToReachByOne() {
        int initial = stats.getNbGoalsToReach();

        stats.incrementNumberOfGoalsToReach();

        int result = stats.getNbGoalsToReach();

        assertEquals(initial + 1, result);
    }

    @Test
    void shouldIncrementNumberOfGoalsToReachByAmount() {
        int initial = stats.getNbGoalsToReach();

        stats.incrementNumberOfGoalsToReach(5);

        int result = stats.getNbGoalsToReach();

        assertEquals(initial + 5, result);
    }

    @Test
    void shouldIncrementNumberOfGoalsReachedByOne() throws InterruptedException {
        int initial = stats.getNbGoalsReached();

        stats.setAccidentalShotPreventionPeriod(10L);
        stats.incrementNumberOfGoalsToReach();
        Thread.sleep(20);

        stats.incrementNumberOfGoalsReached();
        int result = stats.getNbGoalsReached();

        assertEquals(initial + 1, result);
    }

    @Test
    void shouldIncrementNumberOfUncountedGoalsReachedByOne() throws InterruptedException {
        int initial = stats.getNbUnCountedGoalsReached();

        stats.setAccidentalShotPreventionPeriod(50L);
        stats.incrementNumberOfGoalsToReach();
        Thread.sleep(20);

        stats.incrementNumberOfGoalsReached();
        int result = stats.getNbUnCountedGoalsReached();

        assertEquals(initial + 1, result);
    }

    @Test
    void shouldGetDefaultShotRatio() {
        int result = stats.getShotRatio();

        assertEquals(100, result);
    }

    @Test
    void shouldGetShotRatio() throws InterruptedException {
        stats.incrementNumberOfGoalsToReach(4);

        stats.setAccidentalShotPreventionPeriod(10L);
        Thread.sleep(20);

        stats.incrementNumberOfGoalsReached();

        int result = stats.getShotRatio();

        assertEquals(25, result);
    }
}
