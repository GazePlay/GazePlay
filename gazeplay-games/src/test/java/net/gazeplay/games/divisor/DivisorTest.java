package net.gazeplay.games.divisor;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import net.gazeplay.GamePanelDimensionProvider;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.AnimationSpeedRatioSource;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.configuration.BackgroundStyle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.RandomPositionGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class DivisorTest {

    @Mock
    private IGameContext mockGameContext;

    @Mock
    private Configuration mockConfiguration;

    @Mock
    private GamePanelDimensionProvider mockProvider;

    // BackgroundStyle is an enum with constant-specific bodies, which the inline mock maker cannot mock
    private final BackgroundStyle backgroundStyle = BackgroundStyle.DARK;

    @Mock
    private Stats mockStats;

    private ObservableList<Node> children = FXCollections.observableArrayList();
    private final String sep = File.separator;
    private final String localDataFolder =
        System.getProperty("user.dir") + sep
            + "src" + sep
            + "test" + sep
            + "resources" + sep;

    private static void useActiveFilesDirectory(String filesDirectory) {
        Configuration activeConfiguration = mock(Configuration.class);
        when(activeConfiguration.getFileDir()).thenReturn(filesDirectory);
        ActiveConfigurationContext.setInstance(activeConfiguration);
    }

    @AfterEach
    void restoreActiveConfiguration() {
        ActiveConfigurationContext.switchToDefaultUser();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockGameContext.getConfiguration()).thenReturn(mockConfiguration);
        when(mockGameContext.getGamePanelDimensionProvider()).thenReturn(mockProvider);
        when(mockGameContext.getChildren()).thenReturn(children);
        when(mockGameContext.getRandomPositionGenerator()).thenReturn(new RandomPositionGenerator(new ReplayablePseudoRandom()) {
            @Override
            public Dimension2D getDimension2D() {
                return new Dimension2D(100, 200);
            }
        });
        when(mockGameContext.getGazeDeviceManager()).thenReturn(mock(GazeDeviceManager.class));
        when(mockGameContext.getAnimationSpeedRatioSource()).thenReturn(new AnimationSpeedRatioSource() {
            @Override
            public double getDurationRatio() {
                return 0;
            }

            @Override
            public DoubleProperty getSpeedRatioProperty() {
                return new SimpleDoubleProperty();
            }
        });

        when(mockProvider.getDimension2D()).thenReturn(new Dimension2D(1920, 1080));
        when(mockConfiguration.getBackgroundStyle()).thenReturn(backgroundStyle);
        when(mockConfiguration.isBackgroundEnabled()).thenReturn(true);
        when(mockConfiguration.getAnimationSpeedRatioProperty()).thenReturn(new SimpleDoubleProperty());
    }

    @Test
    void shouldLaunchRabbits() {
        Divisor divisor = new Divisor(mockGameContext, mockStats, true);

        divisor.launch();

        verify(mockProvider, atLeastOnce()).getDimension2D();
        assertEquals(2, children.size());

        Target target = (Target) children.get(1);
        assertTrue(target.getImgLib().pickRandomImage().getUrl().contains("rabbit"));
    }

    @Test
    void shouldLaunchCustom() {
        // Utils.getImagesSubdirectory("portraits") resolves to <files directory>/images/portraits
        useActiveFilesDirectory(localDataFolder);

        Divisor divisor = new Divisor(mockGameContext, mockStats, false);

        divisor.launch();

        assertEquals(1, children.size());

        Target target = (Target) children.get(0);
        assertTrue(target.getImgLib().pickRandomImage().getUrl().contains("biscuit"));
    }

    @Test
    void shouldLaunchDefault() {
        // An empty files directory in the temporary user.home (see build.gradle): no custom images
        useActiveFilesDirectory(new File(System.getProperty("user.home"), "empty-files").getAbsolutePath());

        Divisor divisor = new Divisor(mockGameContext, mockStats, false);

        divisor.launch();

        assertEquals(1, children.size());

        Target target = (Target) children.get(0);
        assertTrue(target.getImgLib().pickRandomImage().getUrl().contains("common/default/images"));
    }

    @Test
    void shouldRestart() {
        Divisor divisor = new Divisor(mockGameContext, mockStats, false);
        divisor.restart();

        verify(mockGameContext).clear();
        verify(mockGameContext).showRoundStats(any(), any());
    }

    @Test
    void shouldDispose() {
        Divisor divisor = new Divisor(mockGameContext, mockStats, false);
        divisor.dispose();

        verify(mockGameContext).clear();
    }

}
