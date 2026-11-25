package net.gazeplay.ui.scenes.whereisit;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.gamevariants.CustomWhereIsItVariant;
import net.gazeplay.commons.gamevariants.LivesVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.whereisit.CustomGame;
import net.gazeplay.commons.whereisit.CustomGames;
import net.gazeplay.commons.whereisit.CustomScreen;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The windows of "Where is it?" (maintainer's request of 25/11/2026, point 86): the games of the user, and making one.
 */
@ExtendWith(ApplicationExtension.class)
class WhereIsItDialogsTest {

    @TempDir
    Path temp;

    private final GazePlay gazePlay = mock(GazePlay.class);

    private final GameMenuController gameMenuController = mock(GameMenuController.class);

    private final GameSpec gameSpec = mock(GameSpec.class);

    private final Translator translator = mock(Translator.class);

    private final BorderPane menuRoot = new BorderPane();

    private Stage stage;

    private File picture;

    private File text;

    @Start
    void start(final Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 1000, 700));
        stage.show();
    }

    @BeforeEach
    void setUp() throws IOException {
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(gazePlay.getTranslator()).thenReturn(translator);
        when(gazePlay.getPrimaryStage()).thenReturn(stage);
        final Path file = temp.resolve("ours.jpg");
        try (InputStream image = getClass().getResourceAsStream("/data/common/default/images/bear.jpg")) {
            Files.copy(Objects.requireNonNull(image), file);
        }
        picture = file.toFile();
        text = Files.writeString(temp.resolve("texte.txt"), "x").toFile();
        deleteGames();
    }

    @AfterEach
    void deleteGames() throws IOException {
        for (final CustomGame game : CustomGames.list()) {
            CustomGames.delete(game);
        }
    }

    private static <T> T fx(final java.util.concurrent.Callable<T> action) throws Exception {
        return WaitForAsyncUtils.asyncFx(action).get();
    }

    private WhereIsItGamesDialog openGames() throws Exception {
        return fx(() -> {
            menuRoot.setEffect(new BoxBlur());
            menuRoot.setDisable(true);
            final WhereIsItGamesDialog dialog = new WhereIsItGamesDialog(gazePlay, gameMenuController, gameSpec, menuRoot);
            dialog.show();
            return dialog;
        });
    }

    private CustomGame saved(final String name) throws IOException {
        final CustomGame game = new CustomGame();
        game.setName(name);
        final CustomScreen screen = new CustomScreen();
        screen.setSize(1, 2);
        screen.setPicture(0, picture.toPath());
        screen.setPicture(1, picture.toPath());
        screen.setRightAnswer(1);
        game.getScreens().add(screen);
        CustomGames.save(game);
        return game;
    }

    @Test
    void shouldOnlyOfferToMakeAGameAtFirst() throws Exception {
        final WhereIsItGamesDialog dialog = openGames();
        assertTrue(dialog.getCards().getChildren().isEmpty());
        assertTrue(dialog.getCreateButton().isVisible());
        assertFalse(dialog.getNextButton().isVisible());
        assertTrue(dialog.getPlayButton().isDisable());
        fx(() -> {
            dialog.close();
            return null;
        });
        // The menu comes back
        assertNull(menuRoot.getEffect());
        assertFalse(menuRoot.isDisable());
    }

    @Test
    void shouldPlayTheGameChosen() throws Exception {
        saved("Abeilles");
        final CustomGame second = saved("Bateaux");
        final CustomGame incomplete = new CustomGame();
        incomplete.setName("Chats");
        incomplete.getScreens().add(new CustomScreen());
        CustomGames.save(incomplete);

        final WhereIsItGamesDialog dialog = openGames();
        assertEquals(3, dialog.getCards().getChildren().size());
        assertEquals("abeilles", dialog.getChosenFolder());
        fx(() -> {
            ((Button) ((VBox) dialog.getCards().getChildren().get(2)).getChildren().getFirst()).fire();
            return null;
        });
        assertTrue(dialog.getPlayButton().isDisable(), "no screen to play in this game");
        fx(() -> {
            ((Button) ((VBox) dialog.getCards().getChildren().get(1)).getChildren().getFirst()).fire();
            dialog.getPlayButton().fire();
            return null;
        });
        verify(gameMenuController).chooseAndStartNewGameProcess(gazePlay, gameSpec,
            new CustomWhereIsItVariant(second.getFolder(), LivesVariant.WITHOUT));
        assertFalse(dialog.isShowing());
    }

    /**
     * Maintainer's request of 25/11/2026: a slider showing at least 2 games, the card making a new one always seen.
     */
    @Test
    void shouldSlideAmongTheGames() throws Exception {
        for (final String name : List.of("A", "B", "C", "D", "E", "F")) {
            saved(name);
        }
        final WhereIsItGamesDialog dialog = openGames();
        assertTrue(dialog.getNextButton().isVisible());
        assertTrue(dialog.getPreviousButton().isDisable(), "at the first game");
        fx(() -> {
            dialog.getNextButton().fire();
            assertEquals(1, dialog.getFirstVisible());
            assertFalse(dialog.getPreviousButton().isDisable());
            for (int i = 0; i < 10; i++) {
                dialog.slide(1);
            }
            return null;
        });
        assertTrue(dialog.getNextButton().isDisable(), "at the last game");
        final int last = dialog.getFirstVisible();
        assertTrue(last >= 1 && last <= 6 - WhereIsItGamesDialog.MIN_VISIBLE, "first seen " + last);
        assertTrue(dialog.getCreateButton().isVisible());
        fx(() -> {
            dialog.slide(-100);
            return null;
        });
        assertEquals(0, dialog.getFirstVisible());
    }

    @Test
    void shouldMakeAGame() throws Exception {
        final CustomGame game = new CustomGame();
        final WhereIsItEditorDialog editor = fx(() -> {
            final WhereIsItEditorDialog made = new WhereIsItEditorDialog(stage, translator, game);
            made.show();
            return made;
        });
        assertFalse(editor.isChanged());
        assertEquals("OwnGameNew", editor.getNameField().getText());
        assertEquals("OwnGameNotPlayed", editor.getProblemLabel().getText());
        fx(() -> {
            editor.getNameField().setText("Mes ours");
            editor.chooseSize(1, 2);
            assertEquals(2, editor.getGrid().getChildren().size());
            assertFalse(editor.putPictures(0, List.of(text)), "not an image");
            // Several at once: the first place, then the next empty ones
            assertTrue(editor.putPictures(0, List.of(picture, text, picture)));
            assertEquals(CustomScreen.Problem.ANSWER, game.getScreens().getFirst().problem());
            editor.chooseAnswer(1);
            editor.getSentenceField().setText("Où est l'ours ?");
            return null;
        });
        assertTrue(editor.isChanged());
        assertEquals("", editor.getProblemLabel().getText());
        fx(() -> {
            editor.addScreen(new CustomScreen());
            assertEquals(1, editor.getChosenScreen());
            assertEquals("", editor.getSentenceField().getText());
            assertEquals("OwnGameNotPlayed", editor.getProblemLabel().getText());
            editor.showScreen(0);
            assertEquals("Où est l'ours ?", editor.getSentenceField().getText());
            editor.addScreen(game.getScreens().getFirst().copy());
            assertEquals(3, editor.getScreenButtons().getChildren().size());
            editor.removeScreen();
            assertEquals(1, editor.getChosenScreen());
            editor.getSaveButton().fire();
            return null;
        });
        assertFalse(editor.isShowing());
        final CustomGame read = CustomGames.list().getFirst();
        assertEquals("Mes ours", read.getName());
        assertEquals("mes-ours", read.getFolder());
        assertEquals(2, read.getScreens().size());
        assertEquals(1, read.playableScreens().size());
        assertEquals("Où est l'ours ?", read.playableScreens().getFirst().getSentence());
        assertEquals(1, read.playableScreens().getFirst().getRightAnswer());
    }

    /**
     * A game just opened is not changed by the pictures shown in it: closing it asks nothing.
     */
    @Test
    void shouldChangeAGameAndKeepItsFolder() throws Exception {
        final CustomGame game = CustomGames.load(saved("Avant").getFolder());
        fx(() -> {
            final WhereIsItEditorDialog editor = new WhereIsItEditorDialog(stage, translator, game);
            editor.show();
            assertFalse(editor.isChanged());
            editor.removeScreen();
            assertEquals(1, game.getScreens().size(), "an empty screen instead of the last one");
            editor.getNameField().setText("   ");
            assertTrue(editor.save());
            editor.close();
            return null;
        });
        assertEquals("OwnGameNew", CustomGames.load("avant").getName());
        assertTrue(CustomGames.load("avant").playableScreens().isEmpty());
    }
}
