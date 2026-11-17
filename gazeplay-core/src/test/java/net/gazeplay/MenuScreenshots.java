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
import net.gazeplay.ui.menus.MenuMessageDialog;
import net.gazeplay.ui.scenes.configuration.UserImagesDialog;
import net.gazeplay.ui.scenes.errorhandlingui.GameWhereIsItErrorPathDialog;
import net.gazeplay.ui.scenes.gamemenu.GameButtonPane;
import net.gazeplay.ui.scenes.gamemenu.GameHelp;

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
        // menu.color: another main color than the default one, for every profile
        final String color = System.getProperty("menu.color", "");
        final String colorSetting = color.isEmpty() ? "" : "CSS_FILE=builtin:" + color + "\n";
        // Two favorite games, so that the menu shows its favorites row
        Files.writeString(new File(gazePlayFolder, "GazePlay.properties").toPath(),
            "EYE_TRACKER=mouse_control\nLANGUAGE=fra\nCOUNTRY=FR\nDARK_THEME_ENABLED=" + dark + "\n"
                + "FAVORITE_GAMES=Ninja,Simon\n" + colorSetting,
            StandardCharsets.UTF_8);
        // Two more profiles, without and with a picture, besides the default one and "Add"
        final String settings = "EYE_TRACKER=mouse_control\nLANGUAGE=fra\nCOUNTRY=FR\nDARK_THEME_ENABLED=" + dark + "\n"
            + colorSetting;
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
            CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), scene);
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
                // The help of the evaluations (the last small card)
                captureDialog(gazePlay, output, "3g-evaluations-aide", () -> {
                    final List<Node> cards = List.copyOf(gazePlay.getPrimaryScene().getRoot()
                        .lookupAll(".gp-eval-card"));
                    ((Button) cards.get(cards.size() - 1)).fire();
                });
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
                // The window of the variants of Horses, with the choice of the board under the variants
                captureDialog(gazePlay, output, "3a-variantes-petits-chevaux", () -> {
                    final Node card = gazePlay.getPrimaryScene().getRoot().lookupAll(".gp-game-card").stream()
                        .filter(node -> "Horses".equals(((GameButtonPane) node).getGameSpec().getGameSummary()
                            .getNameCode()))
                        .findFirst().orElseThrow();
                    card.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false,
                        false, false));
                });
                // The window of the variants of Where Is The Animal: the themes under the sizes, then "Start"
                captureDialog(gazePlay, output, "3a-variantes-ou-est-l-animal", () -> {
                    final Node card = gazePlay.getPrimaryScene().getRoot().lookupAll(".gp-game-card").stream()
                        .filter(node -> "WhereIsTheAnimal".equals(((GameButtonPane) node).getGameSpec()
                            .getGameSummary().getNameCode()))
                        .findFirst().orElseThrow();
                    card.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false,
                        false, false));
                });
                // The window of the variants of Memory: what the cards show, then hidden or visible, at the bottom
                captureDialog(gazePlay, output, "3a-variantes-memory", () -> {
                    final Node card = gazePlay.getPrimaryScene().getRoot().lookupAll(".gp-game-card").stream()
                        .filter(node -> "Memory".equals(((GameButtonPane) node).getGameSpec().getGameSummary()
                            .getNameCode()))
                        .findFirst().orElseThrow();
                    card.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false,
                        false, false));
                });
                // The help of a game (the "i" of the window of its variants), and the window of a game without variants
                captureDialog(gazePlay, output, "3h-aide-petits-chevaux", () -> GameHelp.dialog(
                    gazePlay.getPrimaryStage(), gazePlay.getTranslator(), "Horses",
                    gazePlay.getTranslator().translate("Horses"), null).show());
                captureDialog(gazePlay, output, "3i-aide-sans-variante", () -> GameHelp.dialog(
                    gazePlay.getPrimaryStage(), gazePlay.getTranslator(), "FrogsRace",
                    gazePlay.getTranslator().translate("FrogsRace"), () -> { }).show());
                // The window asking for the folder of "Where is it?" (no folder in the settings of the captures)
                captureDialog(gazePlay, output, "3d-ou-est-ce-dossier", () -> {
                    final GameWhereIsItErrorPathDialog dialog = new GameWhereIsItErrorPathDialog(gazePlay, null,
                        gazePlay.getPrimaryStage(), null, gazePlay.getPrimaryScene().getRoot(),
                        "WhereIsItDirNotConfig", null);
                    dialog.setTitle(gazePlay.getTranslator().translate("WhereIsIt"));
                    dialog.show();
                });
                // The window asking for the images of a variant played with images of the user (none in the captures)
                captureDialog(gazePlay, output, "3e-images-utilisateur", () -> {
                    final UserImagesDialog dialog = new UserImagesDialog(gazePlay.getPrimaryStage(),
                        gazePlay.getPrimaryScene().getRoot(), gazePlay.getTranslator(), "Coloring",
                        gazePlay.getTranslator().translate("Coloring"), () -> { });
                    dialog.show();
                });
                // A message of the menus (e.g. an evaluation imported)
                captureDialog(gazePlay, output, "3f-message", () -> new MenuMessageDialog(gazePlay.getPrimaryStage(),
                    gazePlay.getTranslator().translate("EvalImportButton"),
                    gazePlay.getTranslator().translate("EvalImportSuccess").replace("{0}", "Tests sons et vidéos"),
                    false, gazePlay.getTranslator().translate("OkButton"), null,
                    gazePlay.getTranslator().translate("Close")).show());
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
        captureStatistics(gazePlay, output);
        System.exit(0);
    }

    /**
     * The statistics screen (refonte of 16/11/2026) of an example game of "Where is the animal?": simple mode, then
     * the advanced mode with the durations, the positions and the chi-squared test. Mouse and gaze, so that the choice
     * of the source of the heat map shows.
     */
    private static void captureStatistics(final GazePlay gazePlay, final File output) throws Exception {
        final File heatmap = File.createTempFile("heatmap", ".png");
        heatmap.deleteOnExit();
        ImageIO.write(exampleHeatmap(), "png", heatmap);
        final net.gazeplay.stats.SelectionGamesStats stats = org.mockito.Mockito.mock(
            net.gazeplay.stats.SelectionGamesStats.class);
        org.mockito.Mockito.when(stats.getCurrentGameNameCode()).thenReturn("WhereIsTheAnimal");
        org.mockito.Mockito.when(stats.getCurrentGameVariant())
            .thenReturn("GroupedGameVariant:Farm:WhereIsItEnd10:2x3:-");
        org.mockito.Mockito.when(stats.computeTotalElapsedDuration()).thenReturn(222_000L);
        org.mockito.Mockito.when(stats.getNbGoalsReached()).thenReturn(10);
        org.mockito.Mockito.when(stats.getNbGoalsToReach()).thenReturn(10);
        org.mockito.Mockito.when(stats.getNbMistakes()).thenReturn(2);
        org.mockito.Mockito.when(stats.getRoundsTotalAdditiveDuration()).thenReturn(184_000L);
        org.mockito.Mockito.when(stats.computeRoundsDurationAverageDuration()).thenReturn(18_400L);
        org.mockito.Mockito.when(stats.computeRoundsDurationMedianDuration()).thenReturn(16_500L);
        org.mockito.Mockito.when(stats.computeRoundsDurationStandardDeviation()).thenReturn(5_700.0);
        org.mockito.Mockito.when(stats.getOriginalDurationsBetweenGoals()).thenReturn(List.of(22_000L, 15_000L,
            19_000L, 31_000L, 14_000L, 17_000L, 12_000L, 25_000L, 16_000L, 13_000L));
        org.mockito.Mockito.when(stats.getSavedStatsInfo()).thenReturn(new net.gazeplay.commons.utils.stats
            .SavedStatsInfo(null, heatmap, heatmap, heatmap, null, null, null));
        final java.util.LinkedList<net.gazeplay.commons.utils.FixationPoint> fixations = new java.util.LinkedList<>();
        for (int i = 0; i < 60; i++) {
            // In memory, x holds the vertical position and y the horizontal one (FixationPoint)
            fixations.add(new net.gazeplay.commons.utils.FixationPoint(1_000L + i * 3_500L, 400,
                250 + (int) (180 * Math.sin(i / 3.0)), 800 + (int) (500 * Math.cos(i / 5.0))));
        }
        org.mockito.Mockito.when(stats.getFixationSequence()).thenReturn(new java.util.ArrayList<>(List.of(
            new java.util.LinkedList<>(), fixations)));
        final net.gazeplay.commons.utils.stats.ChiReport chi = new net.gazeplay.commons.utils.stats.ChiReport();
        chi.addChiObs(4.5);
        chi.addChiLevel(1);
        org.mockito.Mockito.when(stats.getChiReport()).thenReturn(chi);

        onFx(() -> {
            ActiveConfigurationContext.switchToDefaultUser();
            ActiveConfigurationContext.getInstance().setEyeTracker("tobii_and_mouse");
            gazePlay.onDisplayStats(net.gazeplay.ui.scenes.stats.StatsContextFactory.newInstance(gazePlay, stats));
            return null;
        });
        capture(gazePlay, output, "5-statistiques-simple");
        clickToggle(gazePlay, gazePlay.getTranslator().translate("StatsAdvanced"));
        capture(gazePlay, output, "5-statistiques-avance");
        clickToggle(gazePlay, gazePlay.getTranslator().translate("StatsTabPositions"));
        capture(gazePlay, output, "5-statistiques-avance-positions");
        clickToggle(gazePlay, gazePlay.getTranslator().translate("StatsTabChi2"));
        capture(gazePlay, output, "5-statistiques-avance-khi2");
    }

    private static void clickToggle(final GazePlay gazePlay, final String text) throws Exception {
        onFx(() -> {
            gazePlay.getPrimaryScene().getRoot().lookupAll(".toggle-button").stream()
                .map(ToggleButton.class::cast).filter(button -> text.equals(button.getText()))
                .findFirst().orElseThrow().fire();
            return null;
        });
    }

    /**
     * @return an example of the image of the results: a grid of 6 cards and the band of the question, with warm spots
     */
    private static java.awt.image.BufferedImage exampleHeatmap() {
        final int width = 1600;
        final int height = 900;
        final java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height,
            java.awt.image.BufferedImage.TYPE_INT_RGB);
        final java.awt.Graphics2D g = image.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new java.awt.Color(0xEA, 0xF1, 0xE6));
        g.fillRect(0, 0, width, height);
        for (int line = 0; line < 2; line++) {
            for (int column = 0; column < 3; column++) {
                g.setColor(java.awt.Color.WHITE);
                g.fillRoundRect(40 + column * 520, 40 + line * 340, 480, 300, 40, 40);
            }
        }
        g.setColor(new java.awt.Color(0xF6, 0xEF, 0xE4));
        g.fillRect(0, 760, width, 140);
        final int[][] spots = {{800, 190, 200}, {280, 530, 130}, {1320, 180, 100}, {820, 830, 110}};
        for (final int[] spot : spots) {
            final java.awt.RadialGradientPaint paint = new java.awt.RadialGradientPaint(spot[0], spot[1], spot[2],
                new float[]{0f, 0.4f, 1f}, new java.awt.Color[]{new java.awt.Color(214, 40, 40, 210),
                new java.awt.Color(247, 127, 0, 140), new java.awt.Color(252, 191, 73, 0)});
            g.setPaint(paint);
            g.fillOval(spot[0] - spot[2], spot[1] - spot[2], spot[2] * 2, spot[2] * 2);
        }
        g.dispose();
        return image;
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
