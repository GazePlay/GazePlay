package net.gazeplay;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.scenes.gamemenu.GameButtonPane;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
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
        // Two favorite games, so that the menu shows its favorites row
        Files.writeString(new File(gazePlayFolder, "GazePlay.properties").toPath(),
            "EYE_TRACKER=mouse_control\nLANGUAGE=fra\nCOUNTRY=FR\nDARK_THEME_ENABLED=" + dark + "\n"
                + "FAVORITE_GAMES=Ninja,Simon\n",
            StandardCharsets.UTF_8);
        // Two more profiles, without and with a picture, besides the default one and "Add"
        final String settings = "EYE_TRACKER=mouse_control\nLANGUAGE=fra\nCOUNTRY=FR\nDARK_THEME_ENABLED=" + dark + "\n";
        createProfile("Camille", settings);
        // A name without accent: the settings are written here in UTF-8, GazePlay writes them escaped
        final File picture = new File(createProfile("Leo", settings), "bear.jpg");
        try (InputStream image = MenuScreenshots.class.getResourceAsStream("/data/common/default/images/bear.jpg")) {
            Files.copy(Objects.requireNonNull(image), picture.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        // Forward slashes: a backslash is an escape character in a .properties file
        Files.writeString(new File(picture.getParentFile(), "GazePlay.properties").toPath(),
            settings + "USER_PICTURE=" + picture.getAbsolutePath().replace('\\', '/') + "\n", StandardCharsets.UTF_8);

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
            if (mode == AppMode.GAMES) {
                // The windows of the profiles: creating one (Enter on the last card), deleting one (first delete button)
                captureDialog(gazePlay, output, "2a-profil-nouveau", () -> {
                    final List<Node> cards = List.copyOf(gazePlay.getPrimaryScene().getRoot()
                        .lookupAll(".gp-profile-card"));
                    final Node addCard = cards.get(cards.size() - 1);
                    addCard.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false,
                        false, false));
                });
                captureDialog(gazePlay, output, "2b-profil-supprimer", () ->
                    ((Button) gazePlay.getPrimaryScene().getRoot().lookup(".gp-danger")).fire());
            }
            onFx(() -> {
                ActiveConfigurationContext.switchToDefaultUser();
                gazePlay.onReturnToMenu();
                return null;
            });
            capture(gazePlay, output, "3-menu-" + suffix);
            if (mode == AppMode.EVALUATIONS) {
                // The window choosing an evaluation (none imported here)
                captureDialog(gazePlay, output, "3a-evaluations-choix", () ->
                    ((Button) gazePlay.getPrimaryScene().getRoot().lookup(".gp-eval-launch")).fire());
            }
            if (mode == AppMode.GAMES) {
                // Only the favorite games ("Favorites" switch)
                onFx(() -> {
                    ((ToggleButton) gazePlay.getPrimaryScene().getRoot().lookup(".gp-chip-favorites")).setSelected(true);
                    return null;
                });
                capture(gazePlay, output, "3c-menu-jeux-favoris");
                onFx(() -> {
                    ((ToggleButton) gazePlay.getPrimaryScene().getRoot().lookup(".gp-chip-favorites")).setSelected(false);
                    return null;
                });
                // The window of the variants of a game with groups of variants (Enter on its card)
                captureDialog(gazePlay, output, "3a-variantes-ninja", () -> {
                    final Node card = gazePlay.getPrimaryScene().getRoot().lookupAll(".gp-game-card").stream()
                        .filter(node -> "Ninja".equals(((GameButtonPane) node).getGameSpec().getGameSummary()
                            .getNameCode()))
                        .findFirst().orElseThrow();
                    card.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false,
                        false, false));
                });
                // The other orientation of the cards: the menu is built again when the mode is chosen again
                onFx(() -> {
                    gazePlay.chooseMode(mode);
                    ActiveConfigurationContext.switchToDefaultUser();
                    ActiveConfigurationContext.getInstance().setMenuButtonsOrientation("VERTICAL");
                    gazePlay.onReturnToMenu();
                    ActiveConfigurationContext.getInstance().setMenuButtonsOrientation("HORIZONTAL");
                    return null;
                });
                capture(gazePlay, output, "3b-menu-jeux-vertical");
            }
            onFx(() -> {
                gazePlay.onDisplayConfigurationManagement();
                return null;
            });
            capture(gazePlay, output, "4-parametres-" + suffix);
            // The other sections of the settings, one capture each
            final int sections = onFx(() -> gazePlay.getPrimaryScene().getRoot()
                .lookupAll(".gp-settings-nav-item").size());
            for (int i = 1; i < sections; i++) {
                final int index = i;
                onFx(() -> {
                    ((ToggleButton) List.copyOf(gazePlay.getPrimaryScene().getRoot()
                        .lookupAll(".gp-settings-nav-item")).get(index)).fire();
                    return null;
                });
                capture(gazePlay, output, "4-parametres-" + suffix + "-" + (i + 1));
            }
        }
        System.exit(0);
    }

    /**
     * @return the folder of a new profile, with the given settings
     */
    private static File createProfile(final String name, final String settings) throws IOException {
        final File profile = GazePlayDirectories.getUserProfileDirectory(name);
        if (!profile.isDirectory() && !profile.mkdirs()) {
            throw new IOException("Cannot create " + profile);
        }
        Files.writeString(new File(profile, "GazePlay.properties").toPath(), settings, StandardCharsets.UTF_8);
        return profile;
    }

    /**
     * Opens a window with an action of the page, captures it, then closes it (the page loses its blur effect).
     */
    private static void captureDialog(final GazePlay gazePlay, final File output, final String name,
                                      final Runnable open) throws Exception {
        onFx(() -> {
            open.run();
            return null;
        });
        Thread.sleep(SETTLE_MILLIS);
        final WritableImage image = onFx(() -> {
            final Stage dialog = (Stage) Window.getWindows().stream()
                .filter(window -> window != gazePlay.getPrimaryStage() && window.isShowing())
                .findFirst().orElseThrow();
            final WritableImage snapshot = dialog.getScene().snapshot(null);
            dialog.close();
            gazePlay.getPrimaryScene().getRoot().setEffect(null);
            gazePlay.getPrimaryScene().getRoot().setDisable(false);
            return snapshot;
        });
        final File file = new File(output, name + ".png");
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        System.out.println("Capture : " + file);
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
