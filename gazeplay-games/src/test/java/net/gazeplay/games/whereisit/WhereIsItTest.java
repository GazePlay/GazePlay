package net.gazeplay.games.whereisit;

import javafx.geometry.Dimension2D;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.difficulty.SourceSet;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class WhereIsItTest {
    // Test Mocks
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext mockGameContext;

    @Mock
    Stats mockStats;

    @Mock
    Configuration mockConfig;

    private static final String FILESEPARATOR = File.separator;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        when(mockConfig.getLanguage()).thenReturn("eng");
        when(mockGameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(800, 600));
        when(mockGameContext.getConfiguration()).thenReturn(mockConfig);
    }

    @ParameterizedTest
    @EnumSource(value = WhereIsItGameType.class, mode = EnumSource.Mode.EXCLUDE, names = {"CUSTOMIZED"})
    void shouldPickAndBuildRandomPictures(final WhereIsItGameType gameType) {
        final WhereIsIt whereIsIt = new WhereIsIt(gameType, 2, 2, mockGameContext, mockStats);

        final ReplayablePseudoRandom random = new ReplayablePseudoRandom();
        final RoundDetails randomPictures = whereIsIt.pickAndBuildRandomPictures(4, random, 0);
        assertEquals(4, randomPictures.getPictureCardList().size());
    }

    @Test
    void shouldPickAndBuildRandomCustomPictures() {
        final WhereIsIt whereIsIt = new WhereIsIt(WhereIsItGameType.CUSTOMIZED, 2, 2, mockGameContext, mockStats);
        final String currentDir = System.getProperty("user.dir") +
            FILESEPARATOR + "src" +
            FILESEPARATOR + "test" +
            FILESEPARATOR + "resources";
        when(mockConfig.getWhereIsItDir()).thenReturn(currentDir);

        final ReplayablePseudoRandom random = new ReplayablePseudoRandom();
        final RoundDetails randomPictures = whereIsIt.pickAndBuildRandomPictures(4, random, 0);
        assertEquals(4, randomPictures.getPictureCardList().size());
    }

    /**
     * The whole name of a folder is compared: "I" (in "whereIsTheLetter") used to let every letter in the vowels, and
     * "pig" the pigeon in the farm.
     */
    @Test
    void shouldKeepOnlyTheFoldersNamedInTheVariant() {
        final Set<String> letters = Set.of("data/whereIsTheLetter/images/A", "data/whereIsTheLetter/images/B",
            "data/whereIsTheLetter/images/I");
        assertEquals(Set.of("data/whereIsTheLetter/images/A", "data/whereIsTheLetter/images/I"),
            WhereIsIt.filterFolders(letters, Set.of("A", "E", "I", "O", "U", "Y")));

        final Set<String> animals = Set.of("data/whereIsTheAnimal/images/pig", "data/whereIsTheAnimal/images/pigeon",
            "data/whereIsTheAnimal/images/seahorse", "data/whereIsTheAnimal/images/horse");
        assertEquals(Set.of("data/whereIsTheAnimal/images/pig", "data/whereIsTheAnimal/images/horse"),
            WhereIsIt.filterFolders(animals, Set.of("pig", "horse")));

        assertEquals(letters, WhereIsIt.filterFolders(letters, Set.of()));
    }

    /**
     * Each variant of the games shows exactly the folders listed for it in variants.json.
     */
    @ParameterizedTest
    @EnumSource(value = WhereIsItGameType.class, mode = EnumSource.Mode.EXCLUDE,
        names = {"CUSTOMIZED", "FIND_ODD", "NUMBERS", "ANIMALS_DYNAMIC"})
    void shouldShowExactlyTheFoldersOfEachVariant(final WhereIsItGameType gameType) throws Exception {
        final String resources = "data/" + gameType.getResourcesDirectoryName();
        final Set<String> variant = new SourceSet(resources + "/variants.json").getResources(gameType.getVariant());
        final Set<String> folders = WhereIsIt.filterFolders(
            ResourceFileManager.getResourceFolders(resources + "/images/"), variant);
        final Set<String> names = folders.stream().map(folder -> new File(folder).getName()).collect(Collectors.toSet());
        assertEquals(variant, names, "Every name of the variant has a folder of pictures, and no other folder is shown");
    }

    /**
     * The sounds of "Where is the sound?" do not depend on the language; the voices of the questions only exist in
     * some languages.
     */
    @Test
    void shouldGiveTheSoundsInEveryLanguage() {
        final WhereIsIt sounds = new WhereIsIt(WhereIsItGameType.SOUNDS_ALL, 2, 2, mockGameContext, mockStats);
        assertEquals("data/whereIsTheSound/sounds/cow.mp3", sounds.getPathSound("cow", "deu", sounds.drawVoice("deu")));
        assertNull(sounds.drawVoice("deu"));

        final WhereIsIt animals = new WhereIsIt(WhereIsItGameType.ANIMALS_FARM, 2, 2, mockGameContext, mockStats);
        assertNull(animals.getPathSound("cow", "deu", animals.drawVoice("deu")));
        assertEquals("data/whereIsTheAnimal/sounds/fra/cow.w.fra.mp3", animals.getPathSound("cow", "fra", "w"));

        final WhereIsIt odd = new WhereIsIt(WhereIsItGameType.FIND_ODD, 2, 2, mockGameContext, mockStats);
        assertEquals("data/findTheOddOneOut/sound/eng/odd.m.eng.mp3", odd.getPathSound("odd", "eng", "m"));
        assertNull(odd.getPathSound("odd", "chn", odd.drawVoice("chn")), "No voice of the odd one out in Chinese");
    }

    @Test
    void shouldSayTheQuestionOfTheOddOneOut() {
        final WhereIsIt odd = new WhereIsIt(WhereIsItGameType.FIND_ODD, 2, 2, mockGameContext, mockStats);
        final RoundDetails round = odd.pickAndBuildRandomPictures(4, new ReplayablePseudoRandom(), 0);
        assertNotNull(round.getQuestionSoundPath());
    }

    /**
     * The other pictures of the odd one out are all different (the folders hold 8 pictures, the largest grid 8 other
     * places).
     */
    @Test
    void shouldNotShowTheSamePictureTwiceInTheOddOneOut() {
        final WhereIsIt odd = new WhereIsIt(WhereIsItGameType.FIND_ODD, 3, 3, mockGameContext, mockStats);
        final ReplayablePseudoRandom random = new ReplayablePseudoRandom();
        for (int round = 0; round < 5; round++) {
            final List<String> pictures = odd.pickAndBuildRandomPictures(9, random, 4).getPictureCardList().stream()
                .map(PictureCard::getImagePath).toList();
            assertEquals(9, new HashSet<>(pictures).size(), "Pictures of a round: " + pictures);
        }
    }

    @Test
    void shouldNotAskTheSameAnswerTwiceInARow() {
        final WhereIsIt colors = new WhereIsIt(WhereIsItGameType.COLORS_EASY, 1, 2, mockGameContext, mockStats);
        final ReplayablePseudoRandom random = new ReplayablePseudoRandom(0.42);
        String previous = null;
        for (int round = 0; round < 30; round++) {
            final int winner = random.nextInt(2);
            final RoundDetails details = colors.pickAndBuildRandomPictures(2, random, winner);
            final String answer = new File(details.getPictureCardList().get(winner).getImagePath()).getParentFile().getName();
            assertNotEquals(previous, answer, "Round " + round);
            previous = answer;
        }
    }

    /**
     * The pictures stay above the band of the question, which leaves the configuration button out of the grid.
     */
    @Test
    void shouldLayTheGridAboveTheBand() {
        final GameSizing sizing = new GameSizingComputer(3, 3).computeGameSizing(new Dimension2D(1366, 768));
        assertTrue(sizing.cellY(2) + sizing.cellHeight() <= sizing.bandTop() + 0.001);
        assertTrue(sizing.cellX(2) + sizing.cellWidth() <= 1366 + 0.001);
        // Configuration button: a tenth of the width, 30 pixels above the bottom
        assertTrue(sizing.bandHeight() >= 136.6 + 30);
        assertTrue(sizing.bandLeft() > 136.6);
        assertTrue(sizing.bandRight() > sizing.bandLeft());
    }

    /**
     * From the 5th replay of a level, the threshold index used to be outside the 4 critical values
     * (IndexOutOfBoundsException): the threshold stays the one of the first replays.
     */
    @Test
    void shouldKeepTheChiSquaredThresholdForManyReplays() {
        for (int replays = 1; replays <= 20; replays++) {
            assertEquals(0.45, WhereIsIt.chi2Threshold(replays));
        }
    }

}
