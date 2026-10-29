package net.gazeplay.ui.scenes.errorhandlingui;

import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.gazeplay.GameSpec;
import net.gazeplay.GazePlay;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gamevariants.DimensionGameVariant;
import net.gazeplay.commons.gamevariants.IGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.ui.scenes.gamemenu.GameMenuController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The window asking for the folder of "Where is it?": the game starts only from a valid folder, and the menu comes back
 * without its blur.
 */
@ExtendWith(ApplicationExtension.class)
class GameWhereIsItErrorPathDialogTest {

    private final GazePlay gazePlay = mock(GazePlay.class);

    private final GameMenuController gameMenuController = mock(GameMenuController.class);

    private final GameSpec gameSpec = mock(GameSpec.class);

    private final IGameVariant variant = new DimensionGameVariant(2, 2);

    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 800, 600));
        stage.show();
    }

    @BeforeEach
    void setUp() {
        Translator translator = mock(Translator.class);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(gazePlay.getTranslator()).thenReturn(translator);
    }

    @Test
    void shouldNotStartTheGameFromAWrongFolder(@TempDir File emptyFolder) throws Exception {
        BorderPane menuRoot = new BorderPane();
        GameWhereIsItErrorPathDialog dialog = openDialog(menuRoot);
        assertEquals("WhereIsItDirNotConfig", dialog.getPromptLabel().getText());
        assertTrue(dialog.getDoneButton().isDisable());

        WaitForAsyncUtils.asyncFx(() -> dialog.chooseFolder(emptyFolder.getAbsolutePath(),
            ActiveConfigurationContext.getInstance(), gazePlay.getTranslator())).get();

        assertEquals("PickedWrongDir", dialog.getPromptLabel().getText());
        assertTrue(dialog.getPromptLabel().getStyle().contains("-gp-danger"));
        assertTrue(dialog.getDoneButton().isDisable());
    }

    @Test
    void shouldStartTheGameAndUnblurTheMenu() throws Exception {
        BorderPane menuRoot = new BorderPane();
        GameWhereIsItErrorPathDialog dialog = openDialog(menuRoot);

        WaitForAsyncUtils.asyncFx(() -> {
            dialog.getDoneButton().setDisable(false);
            dialog.getDoneButton().fire();
        }).get();

        verify(gameMenuController).chooseAndStartNewGameProcess(gazePlay, gameSpec, variant);
        assertFalse(dialog.isShowing());
        assertNull(menuRoot.getEffect());
        assertFalse(menuRoot.isDisable());
    }

    /**
     * @return the window, open over a menu blurred and disabled as the window of the variants leaves it
     */
    private GameWhereIsItErrorPathDialog openDialog(final BorderPane menuRoot) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            menuRoot.setEffect(new BoxBlur());
            menuRoot.setDisable(true);
            GameWhereIsItErrorPathDialog dialog = new GameWhereIsItErrorPathDialog(gazePlay, gameMenuController, stage,
                gameSpec, menuRoot, "WhereIsItDirNotConfig", variant);
            dialog.show();
            return dialog;
        }).get();
    }
}
