package net.gazeplay.ui.scenes.gamemenu;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GazePlay;
import net.gazeplay.IGameLauncher;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.gamevariants.generators.IGameVariantGenerator;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The window of the variants: the menu, blurred and disabled while it is open, comes back as it was when it closes.
 */
@ExtendWith(ApplicationExtension.class)
class GameVariantDialogTest {

    private final GazePlay gazePlay = mock(GazePlay.class);

    private final GameMenuController gameMenuController = mock(GameMenuController.class);

    private final IGameVariant first = new DimensionGameVariant(1, 2);

    private final IGameVariant second = new DimensionGameVariant(2, 2);

    private GameSpec gameSpec;

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

        IGameVariantGenerator generator = mock(IGameVariantGenerator.class);
        Set<IGameVariant> variants = new LinkedHashSet<>(List.of(first, second));
        when(generator.getVariants()).thenReturn(variants);
        when(generator.getVariantChooseText()).thenReturn("Choose");
        gameSpec = new GameSpec(GameSummary.builder().nameCode("WhereIsTheNumber").gameThumbnail("whereIsTheNumber")
            .build(), generator, mock(IGameLauncher.class));
    }

    /**
     * A variant chosen used to leave the menu blurred: it is kept, and showed blurred when coming back from the game.
     */
    @Test
    void shouldUnblurTheMenuWhenAVariantIsChosen() throws Exception {
        BorderPane menuRoot = new BorderPane();
        GameVariantDialog dialog = openDialog(menuRoot);

        List<Button> buttons = WaitForAsyncUtils.asyncFx(() -> dialog.getScene().getRoot().lookupAll(".gp-variant")
            .stream().map(Button.class::cast).toList()).get();
        assertEquals(2, buttons.size());
        WaitForAsyncUtils.asyncFx(() -> buttons.get(1).fire()).get();

        verify(gameMenuController).chooseAndStartNewGameProcess(gazePlay, gameSpec, second);
        assertFalse(dialog.isShowing());
        assertNull(menuRoot.getEffect());
        assertFalse(menuRoot.isDisable());
    }

    @Test
    void shouldUnblurTheMenuWhenTheWindowIsClosed() throws Exception {
        BorderPane menuRoot = new BorderPane();
        GameVariantDialog dialog = openDialog(menuRoot);

        WaitForAsyncUtils.asyncFx(dialog::close).get();

        assertNull(menuRoot.getEffect());
        assertFalse(menuRoot.isDisable());
        verifyNoInteractions(gameMenuController);
    }

    /**
     * A reviewed game has an "i" at the bottom right of the window of its variants, opening its help; the other games
     * have none (maintainer's request of 30/10/2026).
     */
    @Test
    void shouldOpenTheHelpOfAReviewedGame() throws Exception {
        GameVariantDialog dialog = openDialog(new BorderPane());
        assertNotNull(dialog.getHelpButton(), "Where Is The Number has a help");

        net.gazeplay.ui.menus.HelpDialog help = WaitForAsyncUtils.asyncFx(() -> {
            dialog.getHelpButton().fire();
            return javafx.stage.Window.getWindows().stream()
                .filter(net.gazeplay.ui.menus.HelpDialog.class::isInstance)
                .map(net.gazeplay.ui.menus.HelpDialog.class::cast).findFirst().orElseThrow();
        }).get();
        assertNull(help.getLaunchButton(), "the help only: the variants start the game");
        assertFalse(help.getTextBox().getChildren().isEmpty());
        WaitForAsyncUtils.asyncFx(() -> {
            help.close();
            dialog.close();
        }).get();

        gameSpec = new GameSpec(GameSummary.builder().nameCode("Ninja").gameThumbnail("ninja").build(),
            gameSpec.getGameVariantGenerator(), mock(IGameLauncher.class));
        GameVariantDialog withoutHelp = openDialog(new BorderPane());
        assertNull(withoutHelp.getHelpButton());
        WaitForAsyncUtils.asyncFx(withoutHelp::close).get();
    }

    /**
     * Values named as those of HorsesVariant (gazeplay-games), on the normal board or on the simplified one.
     */
    enum Board { PLAYERS_2, SIMPLIFIED_PLAYERS_2 }

    /**
     * Horses: the variants of the normal board, and under them the switches choosing the simplified board (which used to
     * be a game of its own, maintainer's request of 30/10/2026).
     */
    @Test
    void shouldChooseTheBoardOfHorsesUnderTheVariants() throws Exception {
        IGameVariant normal = new EnumGameVariant<>(Board.PLAYERS_2);
        IGameVariant simplified = new EnumGameVariant<>(Board.SIMPLIFIED_PLAYERS_2);
        IGameVariantGenerator generator = mock(IGameVariantGenerator.class);
        when(generator.getVariants()).thenReturn(new LinkedHashSet<>(List.of(normal, simplified)));
        gameSpec = new GameSpec(GameSummary.builder().nameCode("Horses").gameThumbnail("horses").build(), generator,
            mock(IGameLauncher.class));
        GameVariantDialog dialog = openDialog(new BorderPane());

        List<RadioButton> boards = WaitForAsyncUtils.asyncFx(() -> dialog.getScene().getRoot()
            .lookupAll(".radio-button").stream().map(RadioButton.class::cast).toList()).get();
        assertEquals(List.of("HorsesBoardNormal", "HorsesBoardSimplified"),
            boards.stream().map(RadioButton::getText).toList());
        // Under the variants: in the same column, after the list of the variants
        VBox column = (VBox) boards.get(0).getParent().getParent();
        assertInstanceOf(ScrollPane.class, column.getChildren().get(0));
        assertEquals(1, column.getChildren().indexOf(boards.get(0).getParent()));

        WaitForAsyncUtils.asyncFx(() -> boards.get(1).fire()).get();
        List<Button> shown = WaitForAsyncUtils.asyncFx(() -> ((ScrollPane) column.getChildren().get(0)).getContent()
            .lookupAll(".gp-variant").stream().map(Button.class::cast).toList()).get();
        assertEquals(1, shown.size());
        WaitForAsyncUtils.asyncFx(() -> shown.get(0).fire()).get();

        verify(gameMenuController).chooseAndStartNewGameProcess(gazePlay, gameSpec, simplified);
    }

    /**
     * @return the window, open over a menu blurred and disabled as {@link GameMenuController#onGameSelection} does
     */
    private GameVariantDialog openDialog(final Parent menuRoot) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            menuRoot.setEffect(new BoxBlur());
            menuRoot.setDisable(true);
            GameVariantDialog dialog = new GameVariantDialog(gazePlay, gameMenuController, stage, gameSpec,
                menuRoot, "Choose");
            dialog.show();
            return dialog;
        }).get();
    }
}
