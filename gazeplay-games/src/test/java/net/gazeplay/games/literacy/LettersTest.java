package net.gazeplay.games.literacy;

import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.scene.layout.Pane;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.games.ImageUtils;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * "Identifier les lettres" (reviewed on 12/11/2026, maintainer's rules): the cards of each level (a third of the letter
 * asked, the other letters alike or not), the writing, a card found or wrong after the fixation of the settings, the
 * picture uncovered, the end, the variants and those of before the review.
 */
@ExtendWith(ApplicationExtension.class)
class LettersTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    private final Configuration configuration = mock(Configuration.class, withSettings().stubOnly());

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1600, 900));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(translator.currentLocale()).thenReturn(Locale.of("fra", "FR"));
        when(configuration.getFixationLength()).thenReturn(500);
        when(configuration.getGameStartDelay()).thenReturn(0);
    }

    private static <T> T fx(final Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private Letters launched(final LettersLevel level, final LettersCase letterCase, final LettersEnd end)
        throws Exception {
        return fx(() -> {
            gameArea.getChildren().clear();
            final Letters game = new Letters(gameContext, stats, level, letterCase, LettersPictures.ANIMALS, end, 4);
            game.setManual();
            game.launch();
            game.step(0.02);
            return game;
        });
    }

    private static void steps(final Letters game, final double seconds) {
        for (double t = 0; t < seconds; t += 0.02) {
            game.step(0.02);
        }
    }

    private static void enter(final Letters.Spot spot) {
        Event.fireEvent(spot.getNode(), new GazeEvent(GazeEvent.GAZE_ENTERED, 1, 0, 0));
    }

    private static void exit(final Letters.Spot spot) {
        Event.fireEvent(spot.getNode(), new GazeEvent(GazeEvent.GAZE_EXITED, -1, 0, 0));
    }

    @Test
    void shouldPutAThirdOfTheLetterAskedAndTheOtherLettersOfTheLevel() throws Exception {
        for (final LettersLevel level : LettersLevel.values()) {
            final Letters game = launched(level, LettersCase.CAPITALS, LettersEnd.ENDLESS);
            assertEquals(level.getCards(), game.getCards().size());
            final long asked = game.getCards().stream().filter(card -> card.getLetter() == game.getAsked()).count();
            assertEquals(level.getTargets(), asked, level.name());
            final long others = game.getCards().stream().map(Letters.Card::getLetter)
                .filter(letter -> letter != game.getAsked()).distinct().count();
            assertEquals(level.getOthers(), others, level.name());
        }
        assertEquals(2, LettersLevel.EASY.getTargets());
        assertEquals(4, LettersLevel.NORMAL.getTargets());
        assertEquals(7, LettersLevel.HARD.getTargets());
    }

    @Test
    void shouldChooseOtherLettersAlikeOnlyAtTheHardLevel() {
        final ReplayablePseudoRandom random = new ReplayablePseudoRandom(5);
        for (final LettersCase letterCase : LettersCase.values()) {
            for (int i = 0; i < 300; i++) {
                final char asked = Letters.pickTarget(LettersLevel.EASY, letterCase, random);
                final List<Character> easy = Letters.otherLetters(asked, LettersLevel.EASY, letterCase, random);
                assertEquals(1, easy.size());
                assertTrue(Letters.lookalikes(asked, letterCase).indexOf(easy.get(0)) < 0, asked + " " + easy);
                assertNotEquals(asked, (char) easy.get(0));
                final char hard = Letters.pickTarget(LettersLevel.HARD, letterCase, random);
                final List<Character> others = Letters.otherLetters(hard, LettersLevel.HARD, letterCase, random);
                assertEquals(3, others.size());
                assertTrue(Letters.lookalikes(hard, letterCase).indexOf(others.get(0)) >= 0, hard + " " + others);
                assertFalse(others.contains(hard));
            }
        }
        // Seen on a capture (12/11/2026): an "O" with a "G" at the easy level, alike as capitals
        assertTrue(Letters.lookalikes('g', LettersCase.CAPITALS).indexOf('o') >= 0);
        assertTrue(Letters.lookalikes('g', LettersCase.BOTH).indexOf('o') >= 0);
    }

    @Test
    void shouldWriteTheLettersOfTheVariant() throws Exception {
        final Letters capitals = launched(LettersLevel.NORMAL, LettersCase.CAPITALS, LettersEnd.ENDLESS);
        assertTrue(capitals.getCards().stream().allMatch(card -> Character.isUpperCase(card.getWritten().charAt(0))));
        final Letters small = launched(LettersLevel.NORMAL, LettersCase.SMALL, LettersEnd.ENDLESS);
        assertTrue(small.getCards().stream().allMatch(card -> Character.isLowerCase(card.getWritten().charAt(0))));
        final Letters both = launched(LettersLevel.HARD, LettersCase.BOTH, LettersEnd.ENDLESS);
        assertTrue(both.getCards().stream().anyMatch(card -> Character.isUpperCase(card.getWritten().charAt(0))));
        assertTrue(both.getCards().stream().anyMatch(card -> Character.isLowerCase(card.getWritten().charAt(0))));
    }

    @Test
    void shouldSayTheLetterAskedAndAgainOnTheLoudspeaker() throws Exception {
        final Letters game = launched(LettersLevel.EASY, LettersCase.CAPITALS, LettersEnd.ENDLESS);
        fx(() -> {
            final String question = "/quest/" + Character.toUpperCase(game.getAsked()) + ".mp3";
            verify(gameContext.getSoundManager()).add(argThat(path -> path.startsWith("data/literacy/sounds/fra/")
                && path.endsWith(question)));
            enter(game.getReplay());
            steps(game, 0.6);
            verify(gameContext.getSoundManager(), times(2)).add(argThat(path -> path.endsWith(question)));
            return null;
        });
    }

    @Test
    void shouldTurnACardOfTheLetterAfterTheFixation() throws Exception {
        final Letters game = launched(LettersLevel.EASY, LettersCase.CAPITALS, LettersEnd.ENDLESS);
        fx(() -> {
            final Letters.Card right = game.getCards().stream().filter(card -> card.getLetter() == game.getAsked())
                .findFirst().orElseThrow();
            enter(right.getSpot());
            steps(game, 0.4);
            assertFalse(right.isFound());
            steps(game, 0.15);
            assertTrue(right.isFound());
            assertFalse(right.isVisible());
            assertEquals(LettersLevel.EASY.getTargets() - 1, game.getLeft());
            return null;
        });
    }

    @Test
    void shouldCountAMistakeForACardOfAnotherLetterAndLetItBeTriedAgain() throws Exception {
        final Letters game = launched(LettersLevel.EASY, LettersCase.CAPITALS, LettersEnd.ENDLESS);
        fx(() -> {
            final Letters.Card wrong = game.getCards().stream().filter(card -> card.getLetter() != game.getAsked())
                .findFirst().orElseThrow();
            enter(wrong.getSpot());
            steps(game, 0.6);
            verify(stats).incrementNumberOfMistakes();
            assertTrue(wrong.isVisible());
            // Still looked at: no second mistake
            steps(game, 1.0);
            verify(stats, times(1)).incrementNumberOfMistakes();
            exit(wrong.getSpot());
            enter(wrong.getSpot());
            steps(game, 0.6);
            verify(stats, times(2)).incrementNumberOfMistakes();
            return null;
        });
    }

    @Test
    void shouldUncoverThePictureThenAskAnotherLetter() throws Exception {
        final Letters game = launched(LettersLevel.NORMAL, LettersCase.SMALL, LettersEnd.ENDLESS);
        fx(() -> {
            final char first = game.getAsked();
            game.getCards().stream().filter(card -> card.getLetter() == first).toList().forEach(game::choose);
            assertEquals(1, game.getUncovered());
            assertEquals(Letters.Phase.PICTURE, game.getPhase());
            assertTrue(game.getCards().stream().noneMatch(Letters.Card::isVisible));
            verify(stats).incrementNumberOfGoalsReached();
            steps(game, Letters.PICTURE + 0.05);
            assertEquals(Letters.Phase.PLAY, game.getPhase());
            assertNotEquals(first, game.getAsked());
            assertEquals(LettersLevel.NORMAL.getCards(), game.getCards().size());
            verify(stats, times(2)).incrementNumberOfGoalsToReach();
            return null;
        });
    }

    @Test
    void shouldEndWithBravoAfterThePicturesOfTheVariant() throws Exception {
        final Letters game = launched(LettersLevel.EASY, LettersCase.BOTH, LettersEnd.PICTURES_3);
        fx(() -> {
            for (int i = 0; i < 3; i++) {
                final char asked = game.getAsked();
                game.getCards().stream().filter(card -> card.getLetter() == asked).toList().forEach(game::choose);
                steps(game, Letters.PICTURE + 0.05);
            }
            assertEquals(Letters.Phase.OVER, game.getPhase());
            verify(gameContext).playWinTransition(anyLong(), any());
            return null;
        });
    }

    @Test
    void shouldWaitForTheDelayOfTheSettings() throws Exception {
        when(configuration.getGameStartDelay()).thenReturn(1000);
        final Letters game = launched(LettersLevel.EASY, LettersCase.CAPITALS, LettersEnd.ENDLESS);
        fx(() -> {
            final Letters.Card right = game.getCards().stream().filter(card -> card.getLetter() == game.getAsked())
                .findFirst().orElseThrow();
            enter(right.getSpot());
            steps(game, 0.9);
            assertFalse(right.isFound());
            steps(game, 0.7);
            assertTrue(right.isFound());
            return null;
        });
    }

    @Test
    void shouldSayWhenThereIsNoImageOfTheUser() throws Exception {
        assertEquals(0, ImageUtils.listImageFiles(GazePlayDirectories.getImagesDirectory()).size(),
            "no images left by another test");
        final Letters game = fx(() -> {
            gameArea.getChildren().clear();
            final Letters g = new Letters(gameContext, stats, LettersLevel.EASY, LettersCase.CAPITALS,
                LettersPictures.MY_IMAGES, LettersEnd.ENDLESS, 4);
            g.setManual();
            g.launch();
            return g;
        });
        assertTrue(game.getCards().isEmpty());
        verify(translator).translate("NoUserImages");
    }

    @Test
    void shouldRemoveEverythingWhenDisposed() throws Exception {
        final Letters game = launched(LettersLevel.HARD, LettersCase.CAPITALS, LettersEnd.ENDLESS);
        fx(() -> {
            game.dispose();
            assertTrue(gameArea.getChildren().isEmpty());
            assertEquals(Letters.Phase.OVER, game.getPhase());
            return null;
        });
    }

    @Test
    void shouldReadTheVariantsAndThoseOfBeforeTheReview() {
        assertEquals(2 * 3 * 4 * 3, new LettersGameVariantGenerator().getVariants().size());
        final Letters grouped = LettersGameLauncher.create(gameContext, new GroupedGameVariant("LettersMyImages",
            "LettersSmall", "Hard", UserImagesVariant.IMAGES, "LettersEnd5"), stats, 1);
        assertEquals(LettersLevel.HARD, grouped.getLevel());
        assertEquals(LettersCase.SMALL, grouped.getLetterCase());
        assertEquals(LettersPictures.MY_IMAGES, grouped.getPictures());
        assertEquals(LettersEnd.PICTURES_5, grouped.getEnd());
        // "DimensionGameVariant:3:3" of before the review: hard
        assertEquals(LettersLevel.HARD,
            LettersGameLauncher.create(gameContext, new DimensionGameVariant(3, 3), stats, 1).getLevel());
        assertEquals(LettersLevel.EASY,
            LettersGameLauncher.create(gameContext, new DimensionGameVariant(2, 2), stats, 1).getLevel());
    }
}
