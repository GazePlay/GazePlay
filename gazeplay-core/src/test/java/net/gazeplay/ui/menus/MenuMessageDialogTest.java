package net.gazeplay.ui.menus;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The messages and questions of the menus in their look, instead of the alerts of the system.
 */
@ExtendWith(ApplicationExtension.class)
class MenuMessageDialogTest {

    private Stage stage;

    @Start
    void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 800, 600));
        stage.show();
    }

    @Test
    void shouldAcceptAQuestionWithItsButton() throws Exception {
        final MenuMessageDialog dialog = WaitForAsyncUtils.asyncFx(() -> {
            final MenuMessageDialog d = new MenuMessageDialog(stage, "Import", "Replace it?", false, "Replace",
                "Cancel", "Close");
            d.show();
            return d;
        }).get();
        assertEquals("Replace it?", dialog.getMessageLabel().getText());
        assertNotNull(dialog.getCancelButton());

        WaitForAsyncUtils.asyncFx(() -> dialog.getOkButton().fire()).get();
        assertTrue(dialog.isAccepted());
        assertFalse(dialog.isShowing());
    }

    @Test
    void shouldRefuseAQuestionWithCancel() throws Exception {
        final MenuMessageDialog dialog = WaitForAsyncUtils.asyncFx(() -> {
            final MenuMessageDialog d = new MenuMessageDialog(stage, "Import", "Replace it?", false, "Replace",
                "Cancel", "Close");
            d.show();
            return d;
        }).get();
        WaitForAsyncUtils.asyncFx(() -> dialog.getCancelButton().fire()).get();
        assertFalse(dialog.isAccepted());
        assertFalse(dialog.isShowing());
    }

    @Test
    void shouldShowAnErrorWithOneButton() throws Exception {
        final MenuMessageDialog dialog = WaitForAsyncUtils.asyncFx(
            () -> new MenuMessageDialog(stage, "Import", "Failed", true, "OK", null, "Close")).get();
        assertNull(dialog.getCancelButton());
        assertTrue(dialog.getMessageLabel().getStyle().contains("-gp-danger"));
    }
}
