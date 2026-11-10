package net.gazeplay.components;

import javafx.scene.Group;
import javafx.scene.paint.Color;
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
     * Family of the font of the texts, as in menus.css (Andika, made for beginning readers).
     */
    public static final String TEXT_FONT_FAMILY = "Andika";

    private static final String TEXT_FONT_FILE = "data/fonts/Andika-Regular.ttf";

    private static boolean textFontLoaded;

    /**
     * @return the inline style giving the font of the texts at this size; the font of the system if Andika cannot be
     * loaded
     */
    public static String textFontStyle(final double size) {
        if (!textFontLoaded) {
            textFontLoaded = true;
            final var url = GameStyle.class.getClassLoader().getResource(TEXT_FONT_FILE);
            if (url == null || Font.loadFont(url.toExternalForm(), 12) == null) {
                log.warn("Font not loaded: {}", TEXT_FONT_FILE);
            }
        }
        return "-fx-font-family: \"" + TEXT_FONT_FAMILY + "\"; -fx-font-size: " + size + "px;";
    }

    /**
     * @return a text of several lines on a rounded card (colors of the menus), centered, wrapped at the given width;
     * its top left corner at (0, 0)
     */
    public static Group card(final String content, final double fontSize, final double maxWidth) {
        final Text text = new Text(content);
        text.setStyle(textFontStyle(fontSize) + " -fx-fill: -gp-text;");
        text.setFont(Font.font(TEXT_FONT_FAMILY, fontSize));
        text.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        final double padding = fontSize * 0.9;
        text.setWrappingWidth(Math.max(fontSize, maxWidth - 2 * padding));
        if (text.getLayoutBounds().getWidth() < text.getWrappingWidth()) {
            // A short text: the card fits it
            text.setWrappingWidth(text.getLayoutBounds().getWidth() + 1);
        }
        final double width = text.getLayoutBounds().getWidth() + 2 * padding;
        final double height = text.getLayoutBounds().getHeight() + 2 * padding;
        final Rectangle background = targetCard(0, 0, width, height);
        text.setX(padding - text.getLayoutBounds().getMinX());
        text.setY(padding - text.getLayoutBounds().getMinY());
        final Group card = new Group(background, text);
        card.setMouseTransparent(true);
        return card;
    }

    /**
     * @return a light rounded card (colors of the menus) on a rectangle, e.g. behind a picture to select; its corners
     * are those of the fixation ring of the games ({@link #cardRadius})
     */
    public static Rectangle targetCard(final double x, final double y, final double width, final double height) {
        final Rectangle card = new Rectangle(x, y, width, height);
        card.setArcWidth(2 * cardRadius(width, height));
        card.setArcHeight(card.getArcWidth());
        card.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        card.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
        card.setMouseTransparent(true);
        return card;
    }

    /**
     * @return the radius of the corners of a card of this size
     */
    public static double cardRadius(final double width, final double height) {
        return Math.min(Math.min(width, height) * 0.08, 40);
    }

    /**
     * Prepares the ring of a fixation around a card, filled by {@link #fillRing}: it starts at the top center and goes
     * clockwise, like the round rings of the games. The outline of a rounded rectangle starts at the top left and goes
     * down (maintainer's remark of 4/11/2026, measured on a rendering): the ring is mirrored and its start shifted.
     *
     * @return the length of the outline of the ring
     */
    public static double clockwiseRing(final Rectangle ring) {
        final double radius = ring.getArcWidth() / 2;
        ring.setScaleX(-1);
        ring.setStrokeDashOffset(ring.getWidth() / 2 - radius + Math.PI * radius / 2);
        return 2 * (ring.getWidth() + ring.getHeight()) - (8 - 2 * Math.PI) * radius;
    }

    /**
     * Fills a ring prepared by {@link #clockwiseRing}, from 0 to 1.
     */
    public static void fillRing(final Rectangle ring, final double perimeter, final double progress) {
        final double filled = Math.max(0.01, progress * perimeter);
        ring.getStrokeDashArray().setAll(filled, Math.max(0, perimeter - filled));
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
     * The color of a life left, and of a life lost on a light or on a dark background.
     */
    public static final Color LIFE = Color.rgb(235, 70, 95);
    public static final Color LIFE_LOST = Color.rgb(120, 120, 130, 0.8);
    public static final Color LIFE_LOST_ON_DARK = Color.rgb(200, 200, 215, 0.8);

    /**
     * Up to this number of lives, they are shown as hearts; above, as a number and a bar.
     */
    public static final int MAX_HEARTS = 5;

    /**
     * The lives of a game, the same in every reviewed game (maintainer's request of 10/11/2026), from (0, 0): up to
     * {@link #MAX_HEARTS} lives, a red heart for each life left and an empty heart for each life lost (rather than
     * "3 / 3"); above, a red heart, the lives left on the lives at the start ("10 / 15") and a green bar shorter at
     * each life lost.
     *
     * @param lives    the lives left
     * @param total    the lives at the start
     * @param size     the size of a heart (height * 0.055 in the games), smaller if the row would be wider than maxWidth
     * @param maxWidth the widest the row may be
     * @param onDark   whether the background is dark (the empty hearts lighter, the number white)
     */
    public static Group lives(final int lives, final int total, final double size, final double maxWidth,
                              final boolean onDark) {
        if (total > MAX_HEARTS) {
            return livesBar(lives, total, Math.min(size, maxWidth / 6), onDark);
        }
        final double heart = Math.min(size, maxWidth / Math.max(1, total * 1.1));
        final Group row = new Group();
        for (int i = 0; i < total; i++) {
            final SVGPath icon = icon(i < lives ? "favorite_fill1" : "favorite", heart);
            icon.setFill(i < lives ? LIFE : onDark ? LIFE_LOST_ON_DARK : LIFE_LOST);
            icon.setStroke(Color.WHITE);
            // In the units of the icon (960 for its height)
            icon.setStrokeWidth(18);
            icon.setLayoutX(i * heart * 1.1);
            row.getChildren().add(icon);
        }
        row.setMouseTransparent(true);
        return row;
    }

    /**
     * More than {@link #MAX_HEARTS} lives: a red heart, "lives / total", and a green bar shorter at each life lost.
     */
    private static Group livesBar(final int lives, final int total, final double size, final boolean onDark) {
        final SVGPath heart = icon("favorite_fill1", size);
        heart.setFill(LIFE);
        heart.setStroke(Color.WHITE);
        heart.setStrokeWidth(18);
        final Text count = new Text(Math.max(0, lives) + " / " + total);
        count.setFont(titleFont(size * 0.7));
        count.setStyle(titleFontStyle(size * 0.7));
        count.setFill(onDark ? Color.WHITE : Color.rgb(43, 33, 24));
        if (onDark) {
            count.setEffect(new javafx.scene.effect.DropShadow(4, Color.rgb(0, 0, 0, 0.6)));
        }
        count.setTextOrigin(javafx.geometry.VPos.CENTER);
        count.setX(size * 1.2);
        count.setY(size / 2);
        final double barX = count.getX() + count.getLayoutBounds().getWidth() + size * 0.3;
        final double barWidth = size * 3;
        final double barHeight = size * 0.42;
        final Rectangle back = new Rectangle(barX, (size - barHeight) / 2, barWidth, barHeight);
        back.setArcWidth(barHeight);
        back.setArcHeight(barHeight);
        back.setFill(Color.rgb(40, 44, 52, onDark ? 0.55 : 0.18));
        back.setStroke(Color.WHITE);
        back.setStrokeWidth(Math.max(1, size * 0.04));
        final double left = Math.max(0, Math.min(1, lives / (double) total));
        final Rectangle bar = new Rectangle(barX, (size - barHeight) / 2, barWidth * left, barHeight);
        bar.setArcWidth(barHeight);
        bar.setArcHeight(barHeight);
        bar.setFill(Color.web(SUCCESS_COLOR));
        final Group group = new Group(heart, count, back, bar);
        group.setMouseTransparent(true);
        return group;
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
