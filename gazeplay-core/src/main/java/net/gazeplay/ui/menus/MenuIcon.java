package net.gazeplay.ui.menus;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The icons of the menus: Material Symbols Rounded (Apache License 2.0, see data/icons/MaterialSymbols-LICENSE.txt),
 * drawn as vectors, so sharp at any size and colored by the stylesheet (style class {@code gp-icon-shape}). The games
 * keep their PNG images.
 * <p>
 * Each icon is the unchanged "24px" SVG file of Material Symbols in data/icons ({@code _fill1}: the filled variant), a
 * single path in a 960 x 960 box ({@code viewBox="0 -960 960 960"}), read once.
 */
public enum MenuIcon {

    POWER("power_settings_new"),
    /**
     * A game controller: the mode of the games.
     */
    GAMES("sports_esports_fill1"),
    /**
     * A clipboard with a check mark: the mode of the evaluations.
     */
    EVALUATIONS("assignment_turned_in_fill1"),
    SWAP("swap_horiz"),
    EDIT("edit"),
    DELETE("delete"),
    /**
     * A person: the avatar of a profile without picture.
     */
    PERSON("person_fill1"),
    /**
     * A person with a gear: the default profile, whose settings are those of GazePlay.
     */
    MANAGE_ACCOUNTS("manage_accounts_fill1"),
    ADD("add"),
    CLOSE("close"),
    SEARCH("search"),
    SETTINGS("settings"),
    FULLSCREEN("fullscreen"),
    FULLSCREEN_EXIT("fullscreen_exit"),
    REPLAY("replay"),
    /**
     * Logout: back to the profiles.
     */
    LOGOUT("logout"),
    PLAY("play_arrow_fill1"),
    /**
     * An eye: GazePlay Eval Test, a check of the calibration.
     */
    TEST("visibility"),
    UPLOAD_FILE("upload_file"),
    FOLDER("folder_open"),
    FAVORITE("favorite"),
    FAVORITE_FILLED("favorite_fill1"),
    ARROW_UP("keyboard_arrow_up"),
    ARROW_DOWN("keyboard_arrow_down"),
    INFO("info"),
    /**
     * Categories of the games (GameCategories.Category).
     */
    CATEGORY_ACTION_REACTION("bolt_fill1"),
    CATEGORY_SELECTION("ads_click"),
    CATEGORY_MEMORIZATION("psychology_fill1"),
    CATEGORY_LOGIC_MATHS("calculate_fill1"),
    CATEGORY_LITERACY("menu_book_fill1"),
    CATEGORY_MULTIMEDIA("perm_media_fill1");

    private static final double VIEW_BOX = 960;

    private static final Pattern PATH = Pattern.compile(" d=\"([^\"]+)\"");

    private final String file;

    private String path;

    MenuIcon(final String file) {
        this.file = file;
    }

    /**
     * @return the path of the icon, read from its SVG file at the first call
     */
    String getPath() {
        if (path == null) {
            final String resource = "data/icons/" + file + ".svg";
            try (InputStream svg = MenuIcon.class.getClassLoader().getResourceAsStream(resource)) {
                if (svg == null) {
                    throw new IllegalStateException("Missing icon " + resource);
                }
                final Matcher matcher = PATH.matcher(new String(svg.readAllBytes(), StandardCharsets.UTF_8));
                if (!matcher.find()) {
                    throw new IllegalStateException("No path in " + resource);
                }
                path = matcher.group(1);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return path;
    }

    /**
     * @return the icon, centered in a square of the given size (style classes {@code gp-icon} and, on its shape,
     * {@code gp-icon-shape})
     */
    public StackPane create(final double size) {
        final SVGPath shape = new SVGPath();
        shape.setContent(getPath());
        shape.getStyleClass().add("gp-icon-shape");
        final double scale = size / VIEW_BOX;
        shape.getTransforms().add(new Scale(scale, scale));
        final StackPane icon = new StackPane(new Group(shape));
        icon.getStyleClass().add("gp-icon");
        icon.setMinSize(size, size);
        icon.setPrefSize(size, size);
        icon.setMaxSize(size, size);
        return icon;
    }
}
