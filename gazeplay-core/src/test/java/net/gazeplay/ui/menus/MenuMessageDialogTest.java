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

    /**
     * Found by the maintainer on 18/11/2026 with the question of the default values of the settings: a message of one
     * long line had the room of one line, and had to be scrolled. Its wrapped lines are counted: it is seen whole.
     */
    @Test
    void shouldShowAShortMessageOfLongLinesWithoutScrolling() throws Exception {
        final String message = "Les volumes et les réglages des jeux (durée de fixation - vitesse et taille des éléments"
            + " - épaisseur et couleur de l'anneau) reprendront leur valeur par défaut. Les autres réglages sont gardés.";
        final double[] heights = WaitForAsyncUtils.asyncFx(() -> {
            final MenuMessageDialog d = new MenuMessageDialog(stage, "Valeurs par défaut", message, false, "Remettre",
                "Annuler", "Fermer");
            d.show();
            javafx.scene.Node node = d.getMessageLabel();
            while (!(node instanceof javafx.scene.control.ScrollPane)) {
                node = node.getParent();
            }
            final javafx.scene.control.ScrollPane scroller = (javafx.scene.control.ScrollPane) node;
            final double viewport = scroller.getViewportBounds().getHeight();
            final double text = d.getMessageLabel().getHeight();
            d.close();
            return new double[]{viewport, text};
        }).get();
        assertTrue(heights[1] <= heights[0] + 0.5, "text " + heights[1] + ", room " + heights[0]);
        assertTrue(MenuMessageDialog.viewportHeight(message) > MenuMessageDialog.viewportHeight("Replace it?"));
    }

    @Test
    void shouldShowAnErrorWithOneButton() throws Exception {
        final MenuMessageDialog dialog = WaitForAsyncUtils.asyncFx(
            () -> new MenuMessageDialog(stage, "Import", "Failed", true, "OK", null, "Close")).get();
        assertNull(dialog.getCancelButton());
        assertTrue(dialog.getMessageLabel().getStyle().contains("-gp-danger"));
    }
}
