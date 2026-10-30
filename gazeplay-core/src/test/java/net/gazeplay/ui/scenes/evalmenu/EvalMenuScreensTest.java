package net.gazeplay.ui.scenes.evalmenu;

import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.gazeplay.AppMode;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.gameslocator.GamesLocator;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;
import net.gazeplay.ui.scenes.modeselect.ModeSelectionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The page of the modes, the menu of the evaluations and its window choosing the evaluation to launch.
 */
@ExtendWith(ApplicationExtension.class)
class EvalMenuScreensTest {

    private final GazePlay gazePlay = mock(GazePlay.class);

    private final GameMenuController gameMenuController = mock(GameMenuController.class);

    private final GamesLocator gamesLocator = mock(GamesLocator.class);

    private final IGameVariant first = new GazeplayEvalGameVariant("Évaluation 1");

    private final IGameVariant second = new GazeplayEvalGameVariant("Évaluation 2");

    private GameSpec evalSpec;

    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 800, 600));
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }

    @BeforeEach
    void setUp() {
        Translator translator = mock(Translator.class);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(gazePlay.getTranslator()).thenReturn(translator);
        when(gazePlay.getPrimaryStage()).thenReturn(stage);
        when(gazePlay.getCurrentScreenDimensionSupplier()).thenReturn(() -> new Dimension2D(800, 600));
        when(gazePlay.getFullScreenProperty()).thenReturn(stage.fullScreenProperty());

        IGameVariantGenerator generator = mock(IGameVariantGenerator.class);
        Set<IGameVariant> variants = new LinkedHashSet<>(List.of(first, second));
        when(generator.getVariants()).thenReturn(variants);
        evalSpec = new GameSpec(GameSummary.builder().nameCode("GazePlayEval").gameThumbnail("whereIsIt")
            .evaluation(true).build(), generator, mock(net.gazeplay.IGameLauncher.class));
    }

    @Test
    void shouldChooseTheModes() throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            ModeSelectionContext context = new ModeSelectionContext(gazePlay);
            context.getGamesButton().fire();
            context.getEvaluationsButton().fire();
        }).get();

        verify(gazePlay).chooseMode(AppMode.GAMES);
        verify(gazePlay).chooseMode(AppMode.EVALUATIONS);
    }

    @Test
    void shouldLaunchTheEvaluationChosenInTheWindow() throws Exception {
        BorderPane menuRoot = new BorderPane();
        EvalChooserDialog dialog = WaitForAsyncUtils.asyncFx(() -> {
            menuRoot.setDisable(true);
            EvalChooserDialog chooser = new EvalChooserDialog(gazePlay, gameMenuController, evalSpec, menuRoot);
            chooser.show();
            return chooser;
        }).get();

        List<Button> buttons = dialog.getEvaluationButtons();
        assertEquals(List.of("Évaluation 1", "Évaluation 2"), buttons.stream().map(Button::getText).toList());

        WaitForAsyncUtils.asyncFx(() -> buttons.get(1).fire()).get();

        verify(gameMenuController).chooseAndStartNewGameProcess(gazePlay, evalSpec, second);
        assertFalse(dialog.isShowing());
        assertFalse(menuRoot.isDisable());
    }

    @Test
    void shouldCancelTheWindow() throws Exception {
        BorderPane menuRoot = new BorderPane();
        EvalChooserDialog dialog = WaitForAsyncUtils.asyncFx(() -> {
            menuRoot.setDisable(true);
            EvalChooserDialog chooser = new EvalChooserDialog(gazePlay, gameMenuController, evalSpec, menuRoot);
            chooser.show();
            return chooser;
        }).get();

        WaitForAsyncUtils.asyncFx(() -> dialog.getCancelButton().fire()).get();

        assertFalse(dialog.isShowing());
        assertFalse(menuRoot.isDisable());
        verifyNoInteractions(gameMenuController);
    }

    @Test
    void shouldOpenTheWindowFromTheLaunchButtonOfTheMenu() throws Exception {
        when(gamesLocator.listGames(any())).thenReturn(List.of(evalSpec));

        EvalMenuScreen menu = WaitForAsyncUtils.asyncFx(
            () -> new EvalMenuScreen(gazePlay, gameMenuController, gamesLocator)).get();

        assertFalse(menu.getLaunchButton().isDisable());

        WaitForAsyncUtils.asyncFx(() -> menu.getLaunchButton().fire()).get();

        assertTrue(menu.getRoot().isDisable(), "the menu waits for the window");
        WaitForAsyncUtils.asyncFx(() -> {
            for (javafx.stage.Window window : List.copyOf(javafx.stage.Window.getWindows())) {
                if (window instanceof EvalChooserDialog chooser) {
                    chooser.close();
                }
            }
        }).get();
        assertFalse(menu.getRoot().isDisable());
    }

    /**
     * The help button opens the help over the menu, in the language of the settings (maintainer's request of
     * 29/10/2026); the menu comes back when it is closed.
     */
    @Test
    void shouldOpenTheHelpFromTheMenu() throws Exception {
        when(gamesLocator.listGames(any())).thenReturn(List.of(evalSpec));
        when(gazePlay.getTranslator().currentLocale()).thenReturn(Locale.of("fra", "FR"));

        EvalMenuScreen menu = WaitForAsyncUtils.asyncFx(
            () -> new EvalMenuScreen(gazePlay, gameMenuController, gamesLocator)).get();
        EvalHelpDialog help = WaitForAsyncUtils.asyncFx(() -> {
            menu.getHelpButton().fire();
            return javafx.stage.Window.getWindows().stream().filter(EvalHelpDialog.class::isInstance)
                .map(EvalHelpDialog.class::cast).findFirst().orElseThrow();
        }).get();

        assertTrue(menu.getRoot().isDisable(), "the menu waits for the help");
        assertEquals("EvalHelp", help.getTitle());
        Label firstHeading = assertInstanceOf(Label.class, help.getTextBox().getChildren().getFirst());
        assertEquals("Préparer une évaluation", firstHeading.getText());

        WaitForAsyncUtils.asyncFx(() -> help.getCloseButton().fire()).get();
        assertFalse(help.isShowing());
        assertFalse(menu.getRoot().isDisable());
    }

    /**
     * The website card opens the GazePlay Eval website, at its fixed address (maintainer's request of 30/10/2026).
     */
    @Test
    void shouldOpenTheWebsiteFromTheMenu() throws Exception {
        when(gamesLocator.listGames(any())).thenReturn(List.of(evalSpec));
        List<String> opened = new java.util.ArrayList<>();

        EvalMenuScreen menu = WaitForAsyncUtils.asyncFx(
            () -> new EvalMenuScreen(gazePlay, gameMenuController, gamesLocator)).get();
        menu.webPageOpener = url -> opened.add(url);
        WaitForAsyncUtils.asyncFx(() -> menu.getWebsiteButton().fire()).get();

        assertEquals(List.of("https://lig-gazeplay.imag.fr/#/home"), opened);
        assertEquals("EvalWebsite", menu.getWebsiteButton().getText());
        assertTrue(javafx.stage.Window.getWindows().stream().noneMatch(window -> window != stage
            && window.isShowing()), "no message when the browser opens");
    }

    /**
     * Without browser, a message gives the address of the website, to be copied.
     */
    @Test
    void shouldGiveTheAddressOfTheWebsiteWhenTheBrowserCannotBeOpened() throws Exception {
        when(gamesLocator.listGames(any())).thenReturn(List.of(evalSpec));

        EvalMenuScreen menu = WaitForAsyncUtils.asyncFx(
            () -> new EvalMenuScreen(gazePlay, gameMenuController, gamesLocator)).get();
        menu.webPageOpener = url -> false;
        // The message is shown and waited for: the button is pressed later on the JavaFX thread
        javafx.application.Platform.runLater(() -> menu.getWebsiteButton().fire());
        WaitForAsyncUtils.waitFor(5, java.util.concurrent.TimeUnit.SECONDS, () -> javafx.stage.Window.getWindows()
            .stream().anyMatch(net.gazeplay.ui.menus.MenuMessageDialog.class::isInstance));

        net.gazeplay.ui.menus.MenuMessageDialog message = WaitForAsyncUtils.asyncFx(() -> javafx.stage.Window
            .getWindows().stream().filter(net.gazeplay.ui.menus.MenuMessageDialog.class::isInstance)
            .map(net.gazeplay.ui.menus.MenuMessageDialog.class::cast).findFirst().orElseThrow()).get();
        assertTrue(message.getMessageLabel().getText().endsWith("https://lig-gazeplay.imag.fr/#/home"),
            message.getMessageLabel().getText());
        WaitForAsyncUtils.asyncFx(() -> message.getOkButton().fire()).get();
        assertFalse(message.isShowing());
    }

    /**
     * The help exists in French and in English, with the same sections; the other languages get the English one.
     */
    @Test
    void shouldHaveTheHelpInFrenchAndInEnglish() {
        List<String> french = EvalHelpDialog.read("fra");
        List<String> english = EvalHelpDialog.read("eng");

        assertEquals("fra", EvalHelpDialog.language(Locale.of("fr")));
        assertEquals("fra", EvalHelpDialog.language(Locale.of("fra", "FR")));
        assertEquals("eng", EvalHelpDialog.language(null));
        assertEquals(english, EvalHelpDialog.read("deu"));
        assertEquals(6, french.stream().filter(line -> line.startsWith("# ")).count());
        assertEquals(french.stream().map(EvalMenuScreensTest::kind).toList(),
            english.stream().map(EvalMenuScreensTest::kind).toList(), "same headings, items and paragraphs");
    }

    private static char kind(String line) {
        return line.isBlank() ? ' ' : line.startsWith("# ") ? '#' : line.startsWith("- ") ? '-' : 'p';
    }

    /**
     * Headings, items of a list (a bullet and the text) and paragraphs.
     */
    @Test
    void shouldBuildTheHeadingsTheItemsAndTheParagraphs() throws Exception {
        List<javafx.scene.Node> nodes = WaitForAsyncUtils.asyncFx(() -> EvalHelpDialog.build(
            List.of("# Titre", "Un paragraphe, avec une virgule.", "", "- Un point"))).get();

        assertEquals(3, nodes.size());
        assertTrue(nodes.get(0).getStyleClass().contains("gp-help-heading"));
        assertEquals("Un paragraphe, avec une virgule.", ((Label) nodes.get(1)).getText());
        HBox item = assertInstanceOf(HBox.class, nodes.get(2));
        assertEquals("•", ((Label) item.getChildren().get(0)).getText());
        assertEquals("Un point", ((Label) item.getChildren().get(1)).getText());
    }

    @Test
    void shouldNotLaunchWithoutGazePlayEvalInTheCatalog() throws Exception {
        when(gamesLocator.listGames(any())).thenReturn(List.of());

        EvalMenuScreen menu = WaitForAsyncUtils.asyncFx(
            () -> new EvalMenuScreen(gazePlay, gameMenuController, gamesLocator)).get();

        assertTrue(menu.getLaunchButton().isDisable());
    }
}
