package net.gazeplay.ui.menus;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.CssUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

/**
 * A window of help in the look of the menus ({@link MenuDialog}): a text of headings, paragraphs and lists, read from a
 * file of the resources, then a close button, and a button starting something when there is one (e.g. a game without
 * variants: maintainer's request of 30/10/2026).
 * <p>
 * The texts are in files {@code <prefix><language>.txt} (French and English; English for the other languages), not in
 * multilinguism.csv, whose values can hold neither commas nor line breaks. A line starting with {@code "# "} is a
 * heading, a line starting with {@code "- "} an item of a list, any other line a paragraph; blank lines are ignored.
 */
@Slf4j
public class HelpDialog extends Stage {

    public static final String FALLBACK_LANGUAGE = "eng";

    /**
     * The headings, items and paragraphs of the text, in their order.
     */
    @Getter
    private final VBox textBox;

    @Getter
    private final Button closeButton;

    /**
     * The button starting something (e.g. the game), null if none.
     */
    @Getter
    private final Button launchButton;

    /**
     * @param launchText the text of the button starting something, null for a help only
     * @param launch     what the button starts (the window is closed first), null for a help only
     */
    public HelpDialog(final Stage owner, final Translator translator, final String title, final List<String> lines,
                      final String launchText, final Runnable launch) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);
        MenuDialog.undecorate(this);
        setTitle(title);

        textBox = new VBox(10);
        textBox.setPadding(new Insets(8, 28, 8, 28));
        textBox.getChildren().addAll(build(lines));

        final ScrollPane scroller = new ScrollPane(textBox);
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        closeButton = new Button(translator.translate("Close"));
        closeButton.setOnAction(e -> close());
        final HBox bottom = new HBox(12, closeButton);
        if (launch != null) {
            closeButton.getStyleClass().addAll("gp-button", "gp-button-secondary");
            closeButton.setCancelButton(true);
            launchButton = new Button(launchText);
            launchButton.getStyleClass().addAll("gp-button", "gp-button-primary");
            launchButton.setDefaultButton(true);
            launchButton.setOnAction(e -> {
                close();
                launch.run();
            });
            bottom.getChildren().add(launchButton);
        } else {
            closeButton.getStyleClass().addAll("gp-button", "gp-button-primary");
            closeButton.setDefaultButton(true);
            launchButton = null;
        }
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(12, 28, 24, 28));

        final BorderPane content = new BorderPane();
        content.setCenter(scroller);
        content.setBottom(bottom);

        final Scene scene = MenuDialog.createScene(this, content, translator.translate("Close"), this::close);
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), scene);
        setScene(scene);
        setWidth(Math.max(640, owner.getWidth() * 0.6));
        setHeight(Math.max(480, owner.getHeight() * 0.8));
    }

    /**
     * @return the language of a help: the three letters of the LANGUAGE setting ("fra"), also for a locale of two
     *   letters ("fr"); English when it is unknown
     */
    public static String language(final Locale locale) {
        try {
            return locale == null ? FALLBACK_LANGUAGE : locale.getISO3Language();
        } catch (final MissingResourceException e) {
            return FALLBACK_LANGUAGE;
        }
    }

    /**
     * @param prefix the path of the files of the help in the resources, before the language
     * @return the lines of the help in a language, or in English when there is none in that language; null when there
     *   is none at all
     */
    public static List<String> read(final String prefix, final String language) {
        String text = resource(prefix + language + ".txt");
        if (text == null) {
            text = resource(prefix + FALLBACK_LANGUAGE + ".txt");
        }
        return text == null ? null : text.lines().toList();
    }

    private static String resource(final String path) {
        try (InputStream in = HelpDialog.class.getClassLoader().getResourceAsStream(path)) {
            return in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            log.warn("Cannot read {}", path, e);
            return null;
        }
    }

    /**
     * @return a label per heading and paragraph, a bullet and its text per item
     */
    public static List<Node> build(final List<String> lines) {
        final List<Node> nodes = new ArrayList<>();
        for (final String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            if (line.startsWith("# ")) {
                final Label heading = text(line.substring(2), "gp-help-heading");
                // Room above each section but the first
                if (!nodes.isEmpty()) {
                    VBox.setMargin(heading, new Insets(14, 0, 0, 0));
                }
                nodes.add(heading);
            } else if (line.startsWith("- ")) {
                final Label item = text(line.substring(2), "gp-help-text");
                HBox.setHgrow(item, Priority.ALWAYS);
                final HBox row = new HBox(10, text("•", "gp-help-text"), item);
                row.getStyleClass().add("gp-help-item");
                nodes.add(row);
            } else {
                nodes.add(text(line, "gp-help-text"));
            }
        }
        return nodes;
    }

    private static Label text(final String text, final String styleClass) {
        final Label label = new Label(text);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
