package net.gazeplay.games.opinions;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Opinions (reviewed on 2/11/2026, rules of the maintainer): the images of the user one by one, in the order of their
 * names; "I like it" or "I don't like it" recorded for each; the answers file written with the statistics; the end
 * after the last image; no image; the variants.
 */
@ExtendWith(ApplicationExtension.class)
class OpinionsGameTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Configuration configuration;

    @Mock
    Translator translator;

    @Mock
    OpinionsGameStats stats;

    private final Pane gameArea = new Pane();

    private final List<File> created = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getConfiguration()).thenReturn(configuration);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(configuration.getFixationLength()).thenReturn(100);
    }

    @AfterEach
    void tearDown() throws Exception {
        for (final File file : created) {
            Files.deleteIfExists(file.toPath());
        }
    }

    /**
     * Puts images in the folder of the user for Opinions (the folder of the tests, hermetic), named so that they come
     * in this order.
     */
    private void givenImages(final String... names) throws Exception {
        final File folder = UserImagesVariant.directory(OpinionsGameVariant.USER_IMAGES_FOLDER);
        final File[] previous = folder.listFiles();
        org.junit.jupiter.api.Assumptions.assumeTrue(previous == null || previous.length == 0,
            "the hermetic test folder has no image of the user for Opinions");
        for (final String name : names) {
            final File file = new File(folder, name);
            ImageIO.write(new BufferedImage(60, 40, BufferedImage.TYPE_INT_RGB), "png", file);
            created.add(file);
        }
    }

    private OpinionsGame launched() throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            gameArea.getChildren().clear();
            final OpinionsGame game = new OpinionsGame(gameContext, stats);
            game.launch();
            return game;
        }).get();
    }

    private void answer(final OpinionsGame game, final OpinionChoice choice) throws Exception {
        WaitForAsyncUtils.asyncFx(() -> game.getButtons().stream().filter(button -> button.getChoice() == choice)
            .findFirst().orElseThrow().select()).get();
        WaitForAsyncUtils.sleep((long) OpinionsGame.ANSWER_SHOWN.toMillis() + 200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * The images one by one, in the order of their names; each answer recorded; after the last one, "Bravo" then the
     * statistics.
     */
    @Test
    void shouldRecordTheOpinionOnEachImageInOrder() throws Exception {
        givenImages("b-chat.png", "a-chien.png", "c-gateau.png");
        final OpinionsGame game = launched();
        assertEquals(List.of("a-chien.png", "b-chat.png", "c-gateau.png"),
            game.getImages().stream().map(File::getName).toList());
        assertEquals(0, game.getIndex());

        answer(game, OpinionChoice.LIKE);
        assertEquals(1, game.getIndex());
        answer(game, OpinionChoice.DISLIKE);
        verify(gameContext, never()).playWinTransition(anyLong(), any());
        answer(game, OpinionChoice.LIKE);

        final InOrder order = inOrder(stats);
        order.verify(stats).addAnswer("a-chien.png", OpinionChoice.LIKE);
        order.verify(stats).addAnswer("b-chat.png", OpinionChoice.DISLIKE);
        order.verify(stats).addAnswer("c-gateau.png", OpinionChoice.LIKE);
        verify(stats, times(3)).incrementNumberOfGoalsToReach();
        verify(stats, times(3)).incrementNumberOfGoalsReached();
        verify(gameContext).playWinTransition(anyLong(), any());
    }

    /**
     * One answer per image: a second fixation while the answer is shown does nothing.
     */
    @Test
    void shouldTakeOneAnswerPerImage() throws Exception {
        givenImages("a.png", "b.png");
        final OpinionsGame game = launched();
        WaitForAsyncUtils.asyncFx(() -> {
            game.answer(OpinionChoice.LIKE);
            game.answer(OpinionChoice.DISLIKE);
        }).get();
        verify(stats, times(1)).addAnswer(anyString(), any());
        assertTrue(game.getButtons().stream().noneMatch(OpinionButton::isEnabled));
    }

    /**
     * The answers file: header, one line per answer, ";" between the columns, a value with ";" quoted.
     */
    @Test
    void shouldWriteTheAnswersFile(@TempDir final Path folder) throws Exception {
        final OpinionsAnswers answers = new OpinionsAnswers();
        answers.add("chien.png", OpinionChoice.LIKE);
        answers.add("frites; ketchup.jpg", OpinionChoice.DISLIKE);
        final File file = folder.resolve("2026-11-02-10-00-00" + OpinionsAnswers.FILE_SUFFIX).toFile();

        answers.write(file);

        final String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertEquals("﻿image;choix\r\nchien.png;aime\r\n\"frites; ketchup.jpg\";n'aime pas\r\n", content);
        assertEquals(1, OpinionsAnswers.FORMAT_VERSION);
    }

    /**
     * The image in the middle, the answers on each side, away from it and from the gear at the bottom left.
     */
    @Test
    void shouldLayTheAnswersOutBesideTheImage() throws Exception {
        givenImages("a.png");
        final OpinionsGame game = launched();
        final Bounds like = game.getButtons().get(0).getBoundsInParent();
        final Bounds dislike = game.getButtons().get(1).getBoundsInParent();
        assertEquals(OpinionChoice.LIKE, game.getButtons().get(0).getChoice());
        assertTrue(like.getMaxX() < 683 && dislike.getMinX() > 683);
        assertTrue(like.getMaxY() < 620, "away from the gear");
        final Group layer = (Group) gameArea.getChildren().get(0);
        final Bounds picture = layer.getChildren().get(1).getBoundsInParent();
        assertFalse(picture.intersects(like) || picture.intersects(dislike), picture + " " + like + " " + dislike);
    }

    /**
     * Without images (the menu asks for them first): a message, nothing to answer.
     */
    @Test
    void shouldSayWhenThereIsNoImage() throws Exception {
        final File[] files = UserImagesVariant.directory(OpinionsGameVariant.USER_IMAGES_FOLDER).listFiles();
        org.junit.jupiter.api.Assumptions.assumeTrue(files == null || files.length == 0);
        final OpinionsGame game = launched();
        assertTrue(game.getButtons().stream().noneMatch(OpinionButton::isEnabled));
        final Group layer = (Group) gameArea.getChildren().get(0);
        assertTrue(layer.getChildren().stream().filter(Group.class::isInstance)
            .flatMap(node -> ((Group) node).getChildren().stream()).filter(Text.class::isInstance)
            .anyMatch(text -> "NoUserImages".equals(((Text) text).getText())));
    }

    /**
     * One variant, the images of the user; the former values still read (saved games), with the same folder.
     */
    @Test
    void shouldOfferOnlyTheImagesOfTheUser() {
        final List<IGameVariant> variants = new ArrayList<>(new OpinionsGameVariantGenerator().getVariants());
        assertEquals(1, variants.size());
        assertEquals("Opinions", UserImagesVariant.folderOf(variants.getFirst()));
        assertEquals("OpinionsMyImages", variants.getFirst().getLabel(translator));
        final IGameVariant former = IGameVariant.toGameVariant(
            new EnumGameVariant<>(OpinionsGameVariant.ONHB).toString());
        assertEquals("Opinions", UserImagesVariant.folderOf(former));
    }
}
