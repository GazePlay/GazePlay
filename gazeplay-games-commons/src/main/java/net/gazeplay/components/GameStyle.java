package net.gazeplay.components;

import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The look of the menus (REFONTE_GRAPHIQUE.md) for the games reviewed in REVUE_DES_JEUX.md: the title font (Fredoka,
 * shipped in data/fonts), rounded labels and the vector icons of data/icons. The colors are the tokens of base-light.css
 * and base-dark.css (-gp-surface, -gp-text, -gp-accent...), defined on the root of every scene, games included.
 */
@Slf4j
public final class GameStyle {

    /**
     * Family of the title font, as in menus.css.
     */
    public static final String TITLE_FONT_FAMILY = "Fredoka SemiBold";

    /**
     * Green of a right answer, readable on the light and dark backgrounds.
     */
    public static final String SUCCESS_COLOR = "#2FAE66";

    private static final String TITLE_FONT_FILE = "data/fonts/Fredoka-SemiBold.ttf";

    private static final double ICON_VIEW_BOX = 960;

    private static final Pattern SVG_PATH = Pattern.compile(" d=\"([^\"]+)\"");

    private static boolean fontLoaded;

    private GameStyle() {
    }

    /**
     * @return the title font at this size; the font of the system if Fredoka cannot be loaded. Writings that Fredoka
     * does not cover (e.g. Arabic, Chinese) fall back to a font of the system, as in the menus.
     */
    public static Font titleFont(final double size) {
        if (!fontLoaded) {
            fontLoaded = true;
            final var url = GameStyle.class.getClassLoader().getResource(TITLE_FONT_FILE);
            if (url == null || Font.loadFont(url.toExternalForm(), 12) == null) {
                log.warn("Font not loaded: {}", TITLE_FONT_FILE);
            }
        }
        return Font.font(TITLE_FONT_FAMILY, size);
    }

    /**
     * @return the inline style giving the title font at this size: a style wins over the font of the stylesheets of
     * the scene, which a font set by code does not
     */
    public static String titleFontStyle(final double size) {
        titleFont(size);
        return "-fx-font-family: \"" + TITLE_FONT_FAMILY + "\"; -fx-font-size: " + size + "px;";
    }

    /**
     * @return a text in the title font on a rounded label (colors of the menus), its top left corner at (0, 0)
     */
    public static Group pill(final String content, final double fontSize) {
        final Text text = new Text(content);
        text.setFont(titleFont(fontSize));
        text.setStyle(titleFontStyle(fontSize) + " -fx-fill: -gp-text;");
        final double width = text.getLayoutBounds().getWidth();
        final double height = text.getLayoutBounds().getHeight();
        final double paddingX = fontSize * 0.8;
        final double paddingY = fontSize * 0.3;

        final Rectangle background = new Rectangle(width + 2 * paddingX, height + 2 * paddingY);
        background.setArcWidth(background.getHeight());
        background.setArcHeight(background.getHeight());
        background.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        background.setStrokeWidth(2);

        text.setX(paddingX - text.getLayoutBounds().getMinX());
        text.setY(paddingY - text.getLayoutBounds().getMinY());
        return new Group(background, text);
    }

    /**
     * @param name the name of an icon of data/icons (a Material Symbols file, without ".svg")
     * @return the icon, filling a square of this size from (0, 0); an empty shape if the icon is missing
     */
    public static SVGPath icon(final String name, final double size) {
        final SVGPath shape = new SVGPath();
        final String resource = "data/icons/" + name + ".svg";
        try (InputStream svg = GameStyle.class.getClassLoader().getResourceAsStream(resource)) {
            if (svg == null) {
                log.warn("Missing icon {}", resource);
                return shape;
            }
            final Matcher matcher = SVG_PATH.matcher(new String(svg.readAllBytes(), StandardCharsets.UTF_8));
            if (matcher.find()) {
                shape.setContent(matcher.group(1));
            }
        } catch (final IOException e) {
            log.warn("Cannot read the icon {}", resource, e);
            return shape;
        }
        // The paths are drawn in the box (0, -960) - (960, 0)
        final double scale = size / ICON_VIEW_BOX;
        shape.getTransforms().addAll(new Scale(scale, scale, 0, 0), new Translate(0, ICON_VIEW_BOX));
        return shape;
    }
}
