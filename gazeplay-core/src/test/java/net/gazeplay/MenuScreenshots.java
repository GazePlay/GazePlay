package net.gazeplay;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.components.CssUtil;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Captures of the menus (mode selection, profiles, menu of the games, menu of the evaluations, settings), without any
 * window (Monocle), to compare their look before and after a change (REFONTE_GRAPHIQUE.md). Run by the
 * {@code menuScreenshots} Gradle task, one JVM per theme and width; not a test.
 * <p>
 * Arguments: the output folder, then {@code light} or {@code dark}. The size of the screen is the one of Monocle
 * ({@code headless.geometry}); user.home and gazeplay.folder are given by the task.
 */
public final class MenuScreenshots {

    private static final long SETTLE_MILLIS = 2500;

    private static final long LOGO_ANIMATION_MILLIS = 6000;

    private MenuScreenshots() {
    }

    public static void main(final String[] args) throws Exception {
        final File output = new File(args[0]);
        final boolean dark = "dark".equals(args[1]);
        if (!output.isDirectory() && !output.mkdirs()) {
            throw new IOException("Cannot create " + output);
        }

        final File gazePlayFolder = GazePlayDirectories.getGazePlayFolder();
        if (!gazePlayFolder.isDirectory() && !gazePlayFolder.mkdirs()) {
            throw new IOException("Cannot create " + gazePlayFolder);
        }
        Files.writeString(new File(gazePlayFolder, "GazePlay.properties").toPath(),
            "EYE_TRACKER=mouse_control\nLANGUAGE=fra\nCOUNTRY=FR\nDARK_THEME_ENABLED=" + dark + "\n",
            StandardCharsets.UTF_8);
        // A second profile, so that the profile page shows a card besides the default one and "Add"
        final File profile = GazePlayDirectories.getUserProfileDirectory("Camille");
        if (!profile.isDirectory() && !profile.mkdirs()) {
            throw new IOException("Cannot create " + profile);
        }
        Files.writeString(new File(profile, "GazePlay.properties").toPath(),
            "EYE_TRACKER=mouse_control\nLANGUAGE=fra\nCOUNTRY=FR\nDARK_THEME_ENABLED=" + dark + "\n",
            StandardCharsets.UTF_8);

        final CompletableFuture<Void> started = new CompletableFuture<>();
        Platform.startup(() -> started.complete(null));
        started.get(30, TimeUnit.SECONDS);

        final CompositionRoot root = onFx(CompositionRoot::new);
        final GazePlay gazePlay = root.getGazePlay();
        onFx(() -> {
            final Stage stage = new Stage();
            final Rectangle2D bounds = Screen.getPrimary().getBounds();
            final Scene scene = new Scene(new Pane(), bounds.getWidth(), bounds.getHeight(), Color.BLACK);
            CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), scene,
                gazePlay.getCurrentScreenDimensionSupplier());
            stage.setScene(scene);
            gazePlay.setPrimaryScene(scene);
            gazePlay.setPrimaryStage(stage);
            gazePlay.goToModePage();
            return null;
        });
        // The logo of the mode page is animated: its letters arrive one after the other
        Thread.sleep(LOGO_ANIMATION_MILLIS);
        capture(gazePlay, output, "1-mode");

        for (final AppMode mode : AppMode.values()) {
            final String suffix = mode == AppMode.GAMES ? "jeux" : "evaluations";
            onFx(() -> {
                gazePlay.chooseMode(mode);
                return null;
            });
            capture(gazePlay, output, "2-profils-" + suffix);
            onFx(() -> {
                ActiveConfigurationContext.switchToDefaultUser();
                gazePlay.onReturnToMenu();
                return null;
            });
            capture(gazePlay, output, "3-menu-" + suffix);
            onFx(() -> {
                gazePlay.onDisplayConfigurationManagement();
                return null;
            });
            capture(gazePlay, output, "4-parametres-" + suffix);
        }
        System.exit(0);
    }

    private static void capture(final GazePlay gazePlay, final File output, final String name)
        throws Exception {
        Thread.sleep(SETTLE_MILLIS);
        final WritableImage image = onFx(() -> gazePlay.getPrimaryScene().snapshot(null));
        final File file = new File(output, name + ".png");
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        System.out.println("Capture : " + file);
    }

    private static <T> T onFx(final Callable<T> action) throws Exception {
        final CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(action.call());
            } catch (final Throwable e) {
                result.completeExceptionally(e);
            }
        });
        return result.get(60, TimeUnit.SECONDS);
    }
}
