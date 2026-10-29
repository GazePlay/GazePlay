package net.gazeplay.ui.scenes.configuration;

import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.ui.Translator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The window of the images of the user of a game: adding and removing images, and starting the game only once there
 * is an image.
 */
@ExtendWith(ApplicationExtension.class)
class UserImagesDialogTest {

    private static final String FOLDER = "UserImagesDialogTest";

    private Stage stage;

    private final Translator translator = mock(Translator.class);

    @Start
    void start(Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(new Pane(), 800, 600));
        stage.show();
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void cleanUp() throws IOException {
        for (final File file : UserImagesVariant.images(FOLDER)) {
            Files.delete(file.toPath());
        }
    }

    @Test
    void shouldStartTheGameOnlyWithImages(@TempDir File source) throws Exception {
        final File image = new File(source, "drawing.png");
        ImageIO.write(new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB), "png", image);
        final File text = new File(source, "notes.txt");
        Files.writeString(text.toPath(), "not an image");
        final BorderPane menuRoot = new BorderPane();
        final AtomicBoolean started = new AtomicBoolean();

        final UserImagesDialog dialog = WaitForAsyncUtils.asyncFx(() -> {
            menuRoot.setEffect(new BoxBlur());
            menuRoot.setDisable(true);
            final UserImagesDialog d = new UserImagesDialog(stage, menuRoot, translator, FOLDER, "Coloring",
                () -> started.set(true));
            d.show();
            return d;
        }).get();
        assertEquals("NoUserImages", dialog.getMessageLabel().getText());
        assertTrue(dialog.getDoneButton().isDisable());

        WaitForAsyncUtils.asyncFx(() -> dialog.copy(List.of(image, text))).get();
        assertEquals(List.of("drawing.png"), UserImagesVariant.images(FOLDER).stream().map(File::getName).toList());
        assertEquals(1, dialog.getThumbnails().getChildren().size());
        assertFalse(dialog.getDoneButton().isDisable());

        WaitForAsyncUtils.asyncFx(() -> dialog.getDoneButton().fire()).get();
        assertTrue(started.get());
        assertFalse(dialog.isShowing());
        assertNull(menuRoot.getEffect());
        assertFalse(menuRoot.isDisable());
    }

    @Test
    void shouldHaveNoDoneButtonInTheSettings() throws Exception {
        final UserImagesDialog dialog = WaitForAsyncUtils.asyncFx(
            () -> new UserImagesDialog(stage, null, translator, FOLDER, "Coloring", null)).get();
        assertNull(dialog.getDoneButton());
    }
}
