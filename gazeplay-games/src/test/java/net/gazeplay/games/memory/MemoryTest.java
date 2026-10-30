package net.gazeplay.games.memory;

import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.GroupedGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.LevelsReport;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Memory (reviewed on 30/10/2026, one card in the menu instead of six): the variants, the cards of a round, a pair
 * found or not, the dynamic difficulty, the pictures of the user.
 */
@ExtendWith(ApplicationExtension.class)
class MemoryTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    private Memory launched;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(100);
        when(stats.getLevelsReport()).thenReturn(mock(LevelsReport.class));
    }

    @AfterEach
    void stopTheGame() throws Exception {
        if (launched != null) {
            WaitForAsyncUtils.asyncFx(launched::dispose).get();
        }
    }

    private Memory launchedGame(String content, String cards, String size) throws Exception {
        String folder = MemoryContent.USER_IMAGES.getKey().equals(content) ? MemoryContent.USER_IMAGES_FOLDER : null;
        launched = WaitForAsyncUtils.asyncFx(() -> {
            Memory memory = new Memory(gameContext, stats, new GroupedGameVariant(content, cards, size, folder), 1234);
            memory.launch();
            return memory;
        }).get();
        return launched;
    }

    /**
     * What the cards show at the top, the cards hidden or visible at the bottom, and for each the 5 sizes and the
     * dynamic difficulty; the variants are written and read back (command line of the game, replay).
     */
    @Test
    void shouldOfferTheSixFormerGamesAndTheUserPictures() {
        Set<IGameVariant> variants = new MemoryGameVariantGenerator().getVariants();
        assertEquals(4 * 2 * 6, variants.size());
        Map<String, Long> byGroup = variants.stream().map(GroupedGameVariant.class::cast)
            .collect(Collectors.groupingBy(GroupedGameVariant::getGroup, Collectors.counting()));
        assertEquals(Set.of("MemoryContentImages", "MemoryContentLetters", "MemoryContentNumbers",
            "MemoryContentUserImages"), byGroup.keySet());
        for (IGameVariant variant : variants) {
            assertEquals(variant, IGameVariant.toGameVariant(variant.toString()), variant.toString());
            GroupedGameVariant grouped = (GroupedGameVariant) variant;
            assertEquals(grouped.getGroup().equals("MemoryContentUserImages") ? "Memory" : null,
                UserImagesVariant.folderOf(variant));
        }
    }

    /**
     * A round of letters, face down, 2 x 3: 6 cards, 3 pairs of distinct letters drawn by the game; one goal to reach
     * per pair.
     */
    @Test
    void shouldDealTheCardsOfARound() throws Exception {
        Memory memory = launchedGame("MemoryContentLetters", MemoryGameVariantGenerator.HIDDEN, "2x3");

        assertEquals(6, memory.getCards().size());
        Map<Integer, Long> byId = memory.getCards().stream()
            .collect(Collectors.groupingBy(MemoryCard::getPairId, Collectors.counting()));
        assertEquals(Map.of(0, 2L, 1, 2L, 2, 2L), byId);
        assertTrue(memory.getCards().stream().noneMatch(MemoryCard::isFaceShown), "face down");
        verify(stats).incrementNumberOfGoalsToReach(3);
        assertEquals(1, texts().filter(text -> text.equals("MemoryPairs 0 / 3")).count());
    }

    @Test
    void shouldShowTheCardsFaceUpInTheVisibleVersion() throws Exception {
        Memory memory = launchedGame("MemoryContentNumbers", MemoryGameVariantGenerator.VISIBLE, "2x2");

        assertTrue(memory.getCards().stream().allMatch(MemoryCard::isFaceShown));
    }

    /**
     * Two cards of a pair: framed in green, counted, then removed; no card can be chosen meanwhile.
     */
    @Test
    void shouldRemoveAPairFound() throws Exception {
        Memory memory = launchedGame("MemoryContentImages", MemoryGameVariantGenerator.HIDDEN, "2x2");
        List<MemoryCard> pair = memory.getCards().stream().filter(card -> card.getPairId() == 0).toList();
        MemoryCard other = memory.getCards().stream().filter(card -> card.getPairId() == 1).findFirst().orElseThrow();

        WaitForAsyncUtils.asyncFx(() -> {
            memory.cardFixated(pair.get(0));
            memory.cardFixated(pair.get(1));
        }).get();
        assertTrue(memory.isComparing());
        assertFalse(memory.canChoose(other));
        assertEquals(1, memory.getPairsFound());
        verify(stats).incrementNumberOfGoalsReached();

        WaitForAsyncUtils.sleep((long) Memory.RIGHT_PAIR_SHOWN.toMillis() + 300, java.util.concurrent.TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(pair.get(0).isFound() && pair.get(1).isFound());
        assertTrue(memory.canChoose(other));
    }

    /**
     * Two cards of different pairs: shown 1.5 s (maintainer's choice), then turned back; nothing counted.
     */
    @Test
    void shouldTurnBackAWrongPair() throws Exception {
        Memory memory = launchedGame("MemoryContentImages", MemoryGameVariantGenerator.HIDDEN, "2x2");
        MemoryCard first = memory.getCards().stream().filter(card -> card.getPairId() == 0).findFirst().orElseThrow();
        MemoryCard second = memory.getCards().stream().filter(card -> card.getPairId() == 1).findFirst().orElseThrow();

        WaitForAsyncUtils.asyncFx(() -> {
            memory.cardFixated(first);
            memory.cardFixated(second);
        }).get();
        assertTrue(first.isSelected() && second.isSelected());

        WaitForAsyncUtils.sleep((long) Memory.WRONG_PAIR_SHOWN.toMillis() - 500, java.util.concurrent.TimeUnit.MILLISECONDS);
        assertTrue(memory.isComparing(), "still shown");
        WaitForAsyncUtils.sleep(900, java.util.concurrent.TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(first.isSelected() || second.isSelected());
        assertFalse(memory.isComparing());
        assertEquals(0, memory.getPairsFound());
        verify(stats, never()).incrementNumberOfGoalsReached();
    }

    /**
     * The dynamic difficulty looks at the 3 last rounds (it used to look 3 times at the last one): up when all of them
     * took at most 2 tries per pair, down when all of them took at least 2.5, else unchanged.
     */
    @Test
    void shouldChangeTheLevelWithTheThreeLastRounds() {
        Memory memory = new Memory(gameContext, stats, new GroupedGameVariant("MemoryContentImages",
            MemoryGameVariantGenerator.HIDDEN, MemoryGameVariantGenerator.DYNAMIC, null), 1);
        assertEquals(2, memory.getNbLines());
        assertEquals(2, memory.getNbColumns());

        memory.getRoundTries().addAll(List.of(4, 3, 4));
        memory.updateLevel();
        assertEquals(3, memory.getLevel());

        memory.setLevel(4);
        memory.adaptLevel();
        memory.getRoundTries().addAll(List.of(10, 4, 10));
        memory.updateLevel();
        assertEquals(4, memory.getLevel(), "one good round of the three: unchanged");

        memory.getRoundTries().addAll(List.of(10, 11, 12));
        memory.updateLevel();
        assertEquals(3, memory.getLevel());
    }

    /**
     * "My pictures" without pictures (the menu asks for them first): a message instead of the cards.
     */
    @Test
    void shouldSayWhenThereIsNoPictureOfTheUser() throws Exception {
        File folder = UserImagesVariant.directory(MemoryContent.USER_IMAGES_FOLDER);
        File[] files = folder.listFiles();
        assumeEmpty(files);

        Memory memory = launchedGame("MemoryContentUserImages", MemoryGameVariantGenerator.HIDDEN, "2x2");

        assertTrue(memory.getCards().isEmpty());
        assertTrue(texts().anyMatch(text -> text.equals("NoUserImages")));
    }

    private static void assumeEmpty(File[] files) {
        org.junit.jupiter.api.Assumptions.assumeTrue(files == null || files.length == 0,
            "the hermetic test folder has no picture of the user for Memory");
    }

    private java.util.stream.Stream<String> texts() {
        return gameArea.getChildren().stream().filter(Group.class::isInstance)
            .flatMap(layer -> ((Group) layer).getChildren().stream()).filter(Group.class::isInstance)
            .flatMap(group -> ((Group) group).getChildren().stream()).filter(Text.class::isInstance)
            .map(node -> ((Text) node).getText());
    }
}
