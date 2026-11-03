package net.gazeplay.games.TowerDefense;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.IntGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.TowerDefense.maps.MapOne;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Tower Defense" (reviewed on 3/11/2026, maintainer's rules): lost when the lives are over (no more "Bravo"), won
 * after the waves of the end, the waves on demand or automatic, the levels, the towers and the blizzard, the maps
 * drawn at random, the variants.
 */
@ExtendWith(ApplicationExtension.class)
class TowerDefenseTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();
    private final Pane root = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        root.resize(1600, 880);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getRoot()).thenReturn(root);
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1600, 880));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getAnimationSpeedRatio()).thenReturn(1.0);
        when(configuration.getFixationLength()).thenReturn(500);
        when(configuration.getProgressBarColor()).thenReturn("#FFFFFF");
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private TowerDefense launched(final TowerDefenseWaves waves, final TowerDefenseLevel level,
                                  final TowerDefenseEnd end, final double seed) throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final TowerDefense game = new TowerDefense(gameContext, stats, waves, level, end, seed);
            game.setManual();
            game.launch();
            return game;
        });
    }

    private TowerDefense launched(final TowerDefenseWaves waves, final TowerDefenseLevel level,
                                  final TowerDefenseEnd end) throws Exception {
        return launched(waves, level, end, 5);
    }

    private static void seconds(final TowerDefense game, final double seconds) {
        for (int i = 0; i < Math.round(seconds * TowerDefense.TICKS_PER_SECOND); i++) {
            game.tick();
        }
    }

    @Test
    void shouldLoseWhenTheLivesAreOverWithoutBravo() throws Exception {
        final TowerDefense game = launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.HARD,
            TowerDefenseEnd.ENDLESS);
        assertEquals(5, game.getLife());
        fx(() -> {
            game.sendWave();
            seconds(game, 120);
            return null;
        });
        assertEquals(0, game.getLife());
        assertEquals(TowerDefense.Phase.OVER, game.getPhase());
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        verify(stats, atLeastOnce()).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldWinWhenTheWavesOfTheEndAreHeld() throws Exception {
        final TowerDefense game = launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.EASY,
            TowerDefenseEnd.WAVES_10);
        fx(() -> {
            for (int wave = 1; wave <= 10; wave++) {
                assertEquals(TowerDefense.Phase.PLAY, game.getPhase());
                game.sendWave();
                // All its enemies come, and are removed at once (as if the towers had killed them)
                for (int i = 0; i < 12; i++) {
                    seconds(game, 1);
                    game.getEnemies().clear();
                }
                if (wave < 10) {
                    verify(gameContext, never()).playWinTransition(anyLong(), any());
                }
            }
            game.tick();
            return null;
        });
        assertEquals(10, game.getWaveCount());
        assertEquals(TowerDefense.Phase.OVER, game.getPhase());
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    @Test
    void shouldSendTheWavesOnDemandOrByThemselves() throws Exception {
        final TowerDefense onDemand = launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.NORMAL,
            TowerDefenseEnd.ENDLESS);
        fx(() -> {
            seconds(onDemand, 60);
            return null;
        });
        assertEquals(0, onDemand.getWaveCount(), "never by itself");

        final TowerDefense automatic = launched(TowerDefenseWaves.AUTOMATIC, TowerDefenseLevel.NORMAL,
            TowerDefenseEnd.ENDLESS);
        fx(() -> {
            seconds(automatic, TowerDefense.AUTOMATIC_WAVE_DELAY - 0.5);
            assertEquals(0, automatic.getWaveCount());
            seconds(automatic, 1);
            assertEquals(1, automatic.getWaveCount());
            // The next one waits for the end of this one, then for the delay
            seconds(automatic, 4);
            assertEquals(1, automatic.getWaveCount());
            automatic.getEnemies().clear();
            seconds(automatic, 2);
            automatic.getEnemies().clear();
            seconds(automatic, TowerDefense.AUTOMATIC_WAVE_DELAY + 0.5);
            assertEquals(2, automatic.getWaveCount());
            // The button still sends one sooner
            automatic.sendWave();
            assertEquals(3, automatic.getWaveCount());
            return null;
        });
    }

    @Test
    void shouldStartWithTheMoneyAndTheLivesOfTheLevel() throws Exception {
        for (final TowerDefenseLevel level : TowerDefenseLevel.values()) {
            final TowerDefense game = launched(TowerDefenseWaves.ON_DEMAND, level, TowerDefenseEnd.ENDLESS);
            assertEquals(level.getMoney(), game.getMoney(), 1e-9);
            assertEquals(level.getLives(), game.getLife());
        }
        assertEquals(15, TowerDefenseLevel.EASY.getLives());
        assertEquals(5, TowerDefenseLevel.HARD.getLives());
    }

    @Test
    void shouldBuildAndSellTowersWithTheMoney() throws Exception {
        final TowerDefense game = launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.HARD,
            TowerDefenseEnd.ENDLESS);
        final Point2D place = fx(() -> game.getMap().getTurretsTiles().getFirst());
        final Point2D other = fx(() -> game.getMap().getTurretsTiles().get(1));
        fx(() -> {
            game.createTower((int) place.getX(), (int) place.getY(), TowerDefense.BASIC_TOWER);
            return null;
        });
        assertEquals(15, game.getMoney(), 1e-9, "40 - 25");
        assertEquals(1, game.getTowers().size());
        fx(() -> {
            // Not enough money for a double tower (50)
            game.createTower((int) other.getX(), (int) other.getY(), TowerDefense.DOUBLE_TOWER);
            return null;
        });
        assertEquals(1, game.getTowers().size());
        fx(() -> {
            game.sellTower((int) place.getX(), (int) place.getY());
            return null;
        });
        assertEquals(40, game.getMoney(), 1e-9);
        assertTrue(game.getTowers().isEmpty());
    }

    @Test
    void shouldKeepTheShopAndItsCardInsideTheScreenAndSayWhatTheTowersDo() throws Exception {
        final TowerDefense game = launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.EASY,
            TowerDefenseEnd.ENDLESS);
        fx(() -> {
            // The places nearest to the right and to the bottom edges
            final Point2D right = game.getMap().getTurretsTiles().stream()
                .max(java.util.Comparator.comparingDouble(Point2D::getX)).orElseThrow();
            final Point2D bottom = game.getMap().getTurretsTiles().stream()
                .max(java.util.Comparator.comparingDouble(Point2D::getY)).orElseThrow();
            for (final Point2D place : new Point2D[]{right, bottom}) {
                game.createTowerSelection((int) place.getX(), (int) place.getY());
                assertNotNull(game.getShop());
                final javafx.geometry.Bounds bounds = game.getShop().getBoundsInParent();
                assertTrue(bounds.getMinX() >= -1 && bounds.getMaxX() <= root.getWidth() + 1, bounds.toString());
                assertTrue(bounds.getMinY() >= -1 && bounds.getMaxY() <= root.getHeight() + 1, bounds.toString());
            }
            // No progress indicator any more: the ring of the reviewed games
            assertTrue(gameArea.getChildren().stream().noneMatch(node -> node instanceof javafx.scene.control.ProgressIndicator));
            assertTrue(gameArea.getChildren().stream().anyMatch(node -> node instanceof net.gazeplay.components.FixationRing));
            return null;
        });
        final String cannon = game.describe(new net.gazeplay.games.TowerDefense.towers.CanonTower(0, 0,
            new java.util.ArrayList<>(), new java.util.ArrayList<>()));
        assertTrue(cannon.contains("TowerDefenseCanonName") && cannon.contains("TowerDefenseCanonRole"), cannon);
        assertTrue(cannon.contains(" 18 ") && cannon.contains(" 4 ") && cannon.contains("1.5 s") && cannon.contains(" 35 "),
            cannon);
    }

    /**
     * Found by the maintainer on 3/11/2026: with the mouse, the shop closed as soon as the mouse moved over a tower
     * of it (the mouse "left" the area under the towers).
     */
    @Test
    void shouldKeepTheShopOpenWhileTheMouseMovesOverItsTowers() throws Exception {
        final TowerDefense game = launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.EASY,
            TowerDefenseEnd.ENDLESS);
        fx(() -> {
            final Point2D place = game.getMap().getTurretsTiles().getFirst();
            game.createTowerSelection((int) place.getX(), (int) place.getY());
            final javafx.scene.Group shop = game.getShop();
            final javafx.scene.Node area = shop.getChildren().stream()
                .filter(node -> node instanceof javafx.scene.shape.Rectangle).findFirst().orElseThrow();
            final javafx.geometry.Bounds bounds = area.localToScene(area.getBoundsInLocal());
            // Over a tower: still inside the area
            javafx.event.Event.fireEvent(area, mouseExited(bounds.getCenterX(), bounds.getMinY() + 10));
            assertSame(shop, game.getShop(), "still open");
            // Out of the area: closed
            javafx.event.Event.fireEvent(area, mouseExited(bounds.getMaxX() + 30, bounds.getCenterY()));
            assertNull(game.getShop());
            return null;
        });
    }

    /**
     * Found by the maintainer with the Tobii on 3/11/2026: the fixation of a tower of the shop stopped after less
     * than a second (the gaze left the place of the tower, under the shop, whose exit stopped the ring).
     */
    @Test
    void shouldKeepTheFixationOfATowerWhenTheGazeLeavesThePlaceUnderTheShop() throws Exception {
        final TowerDefense game = launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.EASY,
            TowerDefenseEnd.ENDLESS);
        fx(() -> {
            final Point2D place = game.getMap().getTurretsTiles().getFirst();
            game.createTowerSelection((int) place.getX(), (int) place.getY());
            final net.gazeplay.components.FixationRing ring = gameArea.getChildren().stream()
                .filter(node -> node instanceof net.gazeplay.components.FixationRing)
                .map(node -> (net.gazeplay.components.FixationRing) node).findFirst().orElseThrow();
            final javafx.scene.Node tower = ((javafx.scene.layout.StackPane) game.getShop().getChildren().stream()
                .filter(node -> node instanceof javafx.scene.layout.StackPane).findFirst().orElseThrow())
                .getChildren().getFirst();
            // The rectangle of the place: 2 tiles around it, under the shop
            final javafx.scene.Node placeArea = gameArea.getChildren().stream()
                .filter(node -> node instanceof javafx.scene.shape.Rectangle rectangle
                    && Math.abs(rectangle.getX() - (place.getX() - 0.5) * root.getWidth() / 20) < 1
                    && Math.abs(rectangle.getY() - (place.getY() - 0.5) * root.getHeight() / 11) < 1)
                .findFirst().orElseThrow();
            javafx.event.Event.fireEvent(tower,
                new net.gazeplay.commons.gaze.devicemanager.GazeEvent(net.gazeplay.commons.gaze.devicemanager.GazeEvent.GAZE_ENTERED, 1, 0, 0));
            assertTrue(ring.isRunning());
            javafx.event.Event.fireEvent(placeArea,
                new net.gazeplay.commons.gaze.devicemanager.GazeEvent(net.gazeplay.commons.gaze.devicemanager.GazeEvent.GAZE_EXITED, -1, 0, 0));
            assertTrue(ring.isRunning(), "the place does not stop the fixation of the tower");
            javafx.event.Event.fireEvent(tower,
                new net.gazeplay.commons.gaze.devicemanager.GazeEvent(net.gazeplay.commons.gaze.devicemanager.GazeEvent.GAZE_EXITED, -1, 0, 0));
            assertFalse(ring.isRunning(), "the tower itself stops it");
            return null;
        });
    }

    /**
     * The picture selling a tower shows that tower (maintainer's request of 3/11/2026).
     */
    @Test
    void shouldShowTheTowerSoldOnItsSellPicture() throws Exception {
        final TowerDefense game = launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.EASY,
            TowerDefenseEnd.ENDLESS);
        fx(() -> {
            final Point2D place = game.getMap().getTurretsTiles().getFirst();
            game.createTower((int) place.getX(), (int) place.getY(), TowerDefense.CANON_TOWER);
            game.createTowerSelection((int) place.getX(), (int) place.getY());
            final String url = game.getShop().getChildren().stream()
                .filter(node -> node instanceof javafx.scene.image.ImageView)
                .map(node -> ((javafx.scene.image.ImageView) node).getImage().getUrl()).findFirst().orElseThrow();
            assertTrue(url.endsWith("sellCanon.png"), url);
            return null;
        });
    }

    private static javafx.scene.input.MouseEvent mouseExited(final double x, final double y) {
        return new javafx.scene.input.MouseEvent(javafx.scene.input.MouseEvent.MOUSE_EXITED, x, y, x, y,
            javafx.scene.input.MouseButton.NONE, 0, false, false, false, false, false, false, false, false, false,
            false, null);
    }

    @Test
    void shouldGiveTheCannonARoleOfItsOwn() {
        final var cannon = new net.gazeplay.games.TowerDefense.towers.CanonTower(0, 0, new java.util.ArrayList<>(),
            new java.util.ArrayList<>());
        final var basic = new net.gazeplay.games.TowerDefense.towers.BasicTower(0, 0, new java.util.ArrayList<>(),
            new java.util.ArrayList<>());
        assertTrue(cannon.damage > 3 * basic.damage, "much stronger");
        assertTrue(cannon.projSpeed < basic.projSpeed, "slower balls");
        assertTrue(cannon.range > basic.range, "from farther");
        assertTrue(cannon.cost > basic.cost, "dearer");
        assertTrue(cannon.projSize >= net.gazeplay.games.TowerDefense.towers.CanonTower.BALL_SIZE, "drawn dark");
    }

    @Test
    void shouldFreezeTheEnemiesWithTheBlizzardThenWaitForIt() throws Exception {
        final TowerDefense game = launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.EASY,
            TowerDefenseEnd.ENDLESS);
        fx(() -> {
            game.sendWave();
            seconds(game, 2.5);
            assertFalse(game.getEnemies().isEmpty());
            game.startBlizzard();
            assertTrue(game.getEnemies().stream().allMatch(enemy -> enemy.isFrozen()));
            final double x = game.getEnemies().getFirst().getX();
            final double y = game.getEnemies().getFirst().getY();
            seconds(game, TowerDefense.BLIZZARD_SECONDS - 0.5);
            assertEquals(x, game.getEnemies().getFirst().getX(), 1e-9);
            assertEquals(y, game.getEnemies().getFirst().getY(), 1e-9);
            seconds(game, 1);
            assertTrue(game.getEnemies().stream().noneMatch(enemy -> enemy.isFrozen()));
            // Not again before its delay
            game.startBlizzard();
            assertEquals(0, game.getBlizzardTicks());
            return null;
        });
    }

    @Test
    void shouldDrawTheMapAtRandom() throws Exception {
        final Set<Boolean> maps = new HashSet<>();
        for (int i = 1; i <= 20; i++) {
            maps.add(launched(TowerDefenseWaves.ON_DEMAND, TowerDefenseLevel.EASY, TowerDefenseEnd.ENDLESS,
                12_345_678.0 * i).getMap() instanceof MapOne);
        }
        assertEquals(Set.of(true, false), maps, "both maps");
    }

    @Test
    void shouldGiveTheVariantsAndReadOnesOfBefore() throws Exception {
        final Set<IGameVariant> variants = new TowerDefenseVariantGenerator().getVariants();
        assertEquals(TowerDefenseWaves.values().length * TowerDefenseLevel.values().length
            * TowerDefenseEnd.values().length, variants.size());
        final GroupedGameVariant variant = new GroupedGameVariant(TowerDefenseWaves.AUTOMATIC.getKey(),
            TowerDefenseLevel.HARD.getKey(), TowerDefenseEnd.WAVES_20.getKey(), null);
        assertTrue(variants.contains(variant));
        assertEquals(TowerDefenseWaves.AUTOMATIC, TowerDefenseGameLauncher.wavesOf(variant));
        assertEquals(TowerDefenseLevel.HARD, TowerDefenseGameLauncher.levelOf(variant));
        assertEquals(TowerDefenseEnd.WAVES_20, TowerDefenseGameLauncher.endOf(variant));
        final IGameVariant old = new IntGameVariant(1);
        assertEquals(TowerDefenseWaves.ON_DEMAND, TowerDefenseGameLauncher.wavesOf(old));
        assertEquals(TowerDefenseLevel.NORMAL, TowerDefenseGameLauncher.levelOf(old));
        assertEquals(TowerDefenseEnd.ENDLESS, TowerDefenseGameLauncher.endOf(null));
        final Stats saved = fx(() -> new TowerDefenseGameLauncher().createSavedStats(
            new javafx.scene.Scene(new Pane(), 800, 600), 0, 0, 0, null, null, null, null));
        assertNotNull(saved, "the replay has its statistics");
    }
}
