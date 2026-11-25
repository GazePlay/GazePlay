package net.gazeplay.games.whereisit;

import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.CustomWhereIsItVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.whereisit.CustomGame;
import net.gazeplay.commons.whereisit.CustomGames;
import net.gazeplay.commons.whereisit.CustomScreen;
import net.gazeplay.games.whereisit.gamevariantgenerator.CustomWhereIsItVariantGenerator;
import net.gazeplay.games.whereisit.launcher.WhereIsItGameLauncher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * "Where is it?" played with a game made by the user (maintainer's request of 25/11/2026, point 86): its screens in an
 * order drawn at random, each with its grid, its pictures at their places and its right answer.
 */
@ExtendWith(ApplicationExtension.class)
class CustomWhereIsItTest {

    private static final Path PICTURES = Path.of("src/test/resources/images/portraits");

    private final IGameContext gameContext = mock(IGameContext.class, Answers.RETURNS_DEEP_STUBS);

    private final Configuration configuration = mock(Configuration.class);

    private final Stats stats = mock(Stats.class);

    private final Pane gameArea = new Pane();

    private final CustomGame game = new CustomGame();

    @BeforeEach
    void setUp() throws IOException {
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1600, 900));
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(configuration.getLanguage()).thenReturn("fra");
        when(configuration.getQuestionLength()).thenReturn(1000L);
        when(gameContext.getTranslator().translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        game.setName("Jeu de test");
        // 5 screens of every size, the right answer in turn, and one to complete
        for (int i = 0; i < CustomScreen.SIZES.size(); i++) {
            final int[] size = CustomScreen.SIZES.get(i);
            final CustomScreen screen = new CustomScreen();
            screen.setSize(size[0], size[1]);
            for (int place = 0; place < screen.getPlaces(); place++) {
                screen.setPicture(place, PICTURES.resolve(place % 2 == 0 ? "biscuit.jpg" : "biscuit2.jpg"));
            }
            screen.setRightAnswer(i % screen.getPlaces());
            screen.setSentence(i == 0 ? "" : "Où est-ce " + i + " ?");
            game.getScreens().add(screen);
        }
        game.getScreens().add(new CustomScreen());
        CustomGames.save(game);
    }

    @AfterEach
    void tearDown() throws IOException {
        CustomGames.delete(game);
    }

    private WhereIsIt launched() {
        return (WhereIsIt) new WhereIsItGameLauncher().createNewGame(gameContext,
            new CustomWhereIsItVariant(game.getFolder(), LivesVariant.WITHOUT), stats);
    }

    @Test
    void shouldGiveTheVariantsOfTheGamesOfTheUser() {
        final Set<IGameVariant> variants = new CustomWhereIsItVariantGenerator().getVariants();
        final List<CustomWhereIsItVariant> mine = variants.stream().map(CustomWhereIsItVariant.class::cast)
            .filter(variant -> variant.getFolder().equals(game.getFolder())).toList();
        // With or without hearts; no end to choose (maintainer's request of 25/11/2026)
        assertEquals(2, mine.size());
        for (final CustomWhereIsItVariant variant : mine) {
            assertEquals(variant, IGameVariant.toGameVariant(variant.toString()));
        }
        assertEquals("Jeu de test, LivesWithout", new CustomWhereIsItVariant(game.getFolder(), LivesVariant.WITHOUT)
            .getLabel(gameContext.getTranslator()));
    }

    @Test
    void shouldPlayEachScreenOnceInAnOrderDrawnAtRandom() throws Exception {
        final Set<List<String>> orders = new HashSet<>();
        for (int seed = 1; seed <= 6; seed++) {
            final WhereIsIt played = new WhereIsIt(gameContext, stats, CustomGames.load(game.getFolder()), false,
                seed * 7919.0);
            assertEquals(5, played.screensToFind());
            final List<String> order = new ArrayList<>();
            for (int screen = 1; screen <= 5; screen++) {
                assertEquals(screen, played.currentScreen());
                played.nextCustomScreen();
                order.add(played.getNbLines() + "x" + played.getNbColumns());
                assertEquals(screen == 5, played.screenFound());
            }
            assertEquals(5, new HashSet<>(order).size(), "each screen once " + order);
            orders.add(order);
        }
        assertTrue(orders.size() > 1, "not always the same order");
    }

    @Test
    void shouldShowTheScreenAsMadeByTheUser() throws Exception {
        final WhereIsIt played = launched();
        for (int i = 0; i < 5; i++) {
            played.nextCustomScreen();
            final CustomScreen screen = played.getCustomScreen();
            final RoundDetails round = WaitForAsyncUtils.asyncFx(() -> played.pickAndBuildRandomPictures(
                screen.getPlaces(), null, screen.getRightAnswer())).get();
            assertEquals(screen.getPlaces(), round.getPictureCardList().size());
            for (int place = 0; place < screen.getPlaces(); place++) {
                assertEquals(place == screen.getRightAnswer(), round.getPictureCardList().get(place).isWinner());
            }
            assertEquals(screen.getSentence(), round.getQuestion());
            assertNull(round.getQuestionSoundPath(), "no sound: no loudspeaker");
        }
    }

    @Test
    void shouldLaunchAScreenWithoutSentenceNorSound() throws Exception {
        final WhereIsIt played = launched();
        WaitForAsyncUtils.asyncFx(() -> {
            for (int i = 0; i < 5; i++) {
                played.launch();
                assertNotNull(played.getCustomScreen());
                played.dispose();
                gameArea.getChildren().clear();
            }
            return null;
        }).get();
    }

    @Test
    void shouldSayThatTheGameNoLongerExists() throws Exception {
        for (final IGameVariant variant : new IGameVariant[]{null,
            new CustomWhereIsItVariant("absent", LivesVariant.WITH),
            IGameVariant.toGameVariant("GroupedGameVariant:WhereIsIt:WhereIsItEnd5:2x2:-")}) {
            final WhereIsIt played = (WhereIsIt) new WhereIsItGameLauncher().createNewGame(gameContext, variant, stats);
            WaitForAsyncUtils.asyncFx(() -> {
                gameArea.getChildren().clear();
                played.launch();
                return null;
            }).get();
            assertNull(played.getCustomScreen());
            assertFalse(gameArea.getChildren().isEmpty(), "a message");
        }
    }
}
