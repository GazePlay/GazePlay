package net.gazeplay.ui.scenes.gamemenu;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GazePlay;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.ui.menus.HelpDialog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The help of the games already reviewed (maintainer's request of 30/10/2026): in French and in English, with the same
 * parts; a game without variants shows it with a button starting the game.
 */
@ExtendWith(ApplicationExtension.class)
class GameHelpTest {

    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 800, 600));
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }

    @ParameterizedTest
    @ValueSource(strings = {"WhereIsTheAnimal", "WhereIsTheSound", "WhereIsTheColor", "WhereIsTheFlag",
        "WhereIsTheLetter", "WhereIsTheNumber", "WhereIsTheShape", "FindTheOddOneOut", "WhereIsIt", "Coloring",
        "FrogsRace", "Horses", "Memory", "SoundsOfLife", "SpotDifference"})
    void shouldExplainEachReviewedGameInFrenchAndInEnglish(String nameCode) {
        assertTrue(GameHelp.exists(nameCode));
        List<String> french = HelpDialog.read(GameHelp.FOLDER + nameCode + "-", "fra");
        List<String> english = HelpDialog.read(GameHelp.FOLDER + nameCode + "-", "eng");

        assertTrue(headings(french).containsAll(List.of("Le principe", "Comment jouer", "Comment gagner",
            "Les variantes")), headings(french).toString());
        assertTrue(headings(english).containsAll(List.of("The principle", "How to play", "How to win",
            "The variants")), headings(english).toString());
        assertEquals(french.stream().map(GameHelpTest::kind).toList(), english.stream().map(GameHelpTest::kind).toList(),
            "same headings, items and paragraphs");
    }

    @Test
    void shouldHaveNoHelpForAGameNotReviewedYet() {
        assertFalse(GameHelp.exists("Ninja"));
    }

    /**
     * A game without variants and with a help: a window with the help and a button starting the game (none of the games
     * reviewed so far has no variants).
     */
    @Test
    void shouldStartAGameWithoutVariantsFromItsHelp() throws Exception {
        GazePlay gazePlay = mock(GazePlay.class);
        Translator translator = mock(Translator.class);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(translator.currentLocale()).thenReturn(Locale.FRANCE);
        when(gazePlay.getTranslator()).thenReturn(translator);
        when(gazePlay.getPrimaryStage()).thenReturn(stage);
        IGameVariantGenerator generator = mock(IGameVariantGenerator.class);
        when(generator.getVariants()).thenReturn(new LinkedHashSet<>());
        GameSpec spec = new GameSpec(GameSummary.builder().nameCode("FrogsRace").gameThumbnail("frogsrace").build(),
            generator, mock(IGameLauncher.class));
        GameMenuController controller = mock(GameMenuController.class, Mockito.CALLS_REAL_METHODS);
        doNothing().when(controller).chooseAndStartNewGameProcess(any(), any(), any());
        BorderPane menuRoot = new BorderPane();

        HelpDialog help = WaitForAsyncUtils.asyncFx(() -> {
            controller.onGameSelection(gazePlay, menuRoot, spec, "La course des grenouilles");
            return Window.getWindows().stream().filter(HelpDialog.class::isInstance).map(HelpDialog.class::cast)
                .findFirst().orElseThrow();
        }).get();

        assertTrue(menuRoot.isDisable(), "the menu waits for the window");
        assertEquals("La course des grenouilles", help.getTitle());
        assertEquals("GameLaunch", help.getLaunchButton().getText());
        WaitForAsyncUtils.asyncFx(() -> help.getLaunchButton().fire()).get();

        verify(controller).chooseAndStartNewGameProcess(gazePlay, spec, null);
        assertFalse(help.isShowing());
        assertFalse(menuRoot.isDisable());
    }

    private static List<String> headings(List<String> lines) {
        return lines.stream().filter(line -> line.startsWith("# ")).map(line -> line.substring(2)).toList();
    }

    private static char kind(String line) {
        return line.isBlank() ? ' ' : line.startsWith("# ") ? '#' : line.startsWith("- ") ? '-' : 'p';
    }
}
