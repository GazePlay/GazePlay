package net.gazeplay.ui.scenes.configuration;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Layout of the settings page (REFONTE_GRAPHIQUE.md 4.5): the sections on the left, each with its icon; on the right,
 * the settings of the chosen section, in cards, one row per setting (its name on the left, its control on the right).
 * The content of the sections is declared by {@link ConfigurationContext}; this class only lays it out.
 */
public final class SettingsPage {

    /**
     * Key of the properties of a row giving the translation key of its setting (to find a setting in the tests).
     */
    static final String SETTING_KEY = "gp-setting";

    /**
     * Below this width of the page, the sections show their icon only.
     */
    private static final double COMPACT_WIDTH = 1000;

    /**
     * A setting: the translation key of its name, and its control (bound to the setting by its builder).
     */
    record Setting(String key, Node control) {
    }

    /**
     * A group of settings, in its own card, with a title (null for none), e.g. the heatmaps in the statistics.
     */
    record Group(String titleKey, List<Setting> settings) {
    }

    /**
     * A section of the page: its title, its icon and its groups.
     */
    record Section(String titleKey, MenuIcon icon, List<Group> groups) {

        /**
         * @return the control of a setting of the section, by the translation key of its name
         */
        Optional<Node> find(final String key) {
            return groups.stream().flatMap(group -> group.settings().stream())
                .filter(setting -> setting.key().equals(key))
                .map(Setting::control)
                .findFirst();
        }
    }

    /**
     * Builds the settings of a section, group by group.
     */
    static final class SectionBuilder {

        private final String titleKey;
        private final MenuIcon icon;
        private final List<Group> groups = new ArrayList<>();
        private List<Setting> current;

        SectionBuilder(final String titleKey, final MenuIcon icon) {
            this.titleKey = titleKey;
            this.icon = icon;
            group(null);
        }

        /**
         * Starts a new group (a new card), with a title, or none if null.
         */
        SectionBuilder group(final String groupTitleKey) {
            current = new ArrayList<>();
            groups.add(new Group(groupTitleKey, current));
            return this;
        }

        SectionBuilder add(final String key, final Node control) {
            current.add(new Setting(key, control));
            return this;
        }

        Section build() {
            return new Section(titleKey, icon, groups.stream().filter(group -> !group.settings().isEmpty()).toList());
        }
    }

    private SettingsPage() {
    }

    /**
     * @return the two columns: the sections on the left (the first one chosen), the chosen one on the right
     */
    static HBox create(final Translator translator, final List<Section> sections) {
        final ScrollPane content = new ScrollPane();
        content.setFitToWidth(true);
        content.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        content.getStyleClass().add("gp-settings-content");
        HBox.setHgrow(content, Priority.ALWAYS);

        final VBox navigation = new VBox(6);
        navigation.getStyleClass().add("gp-settings-nav");
        final ToggleGroup toggles = new ToggleGroup();
        for (final Section section : sections) {
            final ToggleButton button = new ToggleButton(null, section.icon().create(24));
            button.textProperty().bind(new I18NLabel(translator, section.titleKey()).textProperty());
            final Tooltip tooltip = MenuStyle.tooltip("");
            tooltip.textProperty().bind(button.textProperty());
            button.setTooltip(tooltip);
            button.getStyleClass().add("gp-settings-nav-item");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setAlignment(Pos.CENTER_LEFT);
            button.setToggleGroup(toggles);
            final Node page = sectionContent(translator, section);
            button.setOnAction(e -> {
                // Always one section shown: a click on the chosen one keeps it chosen
                button.setSelected(true);
                content.setContent(page);
                content.setVvalue(0);
            });
            navigation.getChildren().add(button);
        }
        if (!navigation.getChildren().isEmpty()) {
            ((ToggleButton) navigation.getChildren().getFirst()).fire();
        }
        final HBox page = new HBox(navigation, content);
        // On a narrow window, the sections show their icon only (their name in the tooltip): room for the settings
        page.widthProperty().addListener((observable, oldValue, newValue) -> {
            final boolean compact = newValue.doubleValue() < COMPACT_WIDTH;
            for (final Node item : navigation.getChildren()) {
                ((ToggleButton) item).setContentDisplay(compact ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT);
            }
            if (compact && !navigation.getStyleClass().contains("gp-settings-nav-compact")) {
                navigation.getStyleClass().add("gp-settings-nav-compact");
            } else if (!compact) {
                navigation.getStyleClass().remove("gp-settings-nav-compact");
            }
        });
        return page;
    }

    /**
     * @return the settings of a section: its title, then one card per group
     */
    static VBox sectionContent(final Translator translator, final Section section) {
        final Label title = new I18NLabel(translator, section.titleKey());
        title.getStyleClass().add("gp-settings-title");
        final VBox page = new VBox(16, title);
        page.getStyleClass().add("gp-settings-section");
        for (final Group group : section.groups()) {
            final VBox card = new VBox();
            card.getStyleClass().add("gp-settings-card");
            if (group.titleKey() != null) {
                final Label groupTitle = new I18NLabel(translator, group.titleKey());
                groupTitle.getStyleClass().add("gp-settings-group-title");
                card.getChildren().add(groupTitle);
            }
            for (final Setting setting : group.settings()) {
                card.getChildren().add(row(translator, setting));
            }
            // No line under the last row of a card
            card.getChildren().getLast().getStyleClass().add("gp-setting-row-last");
            page.getChildren().add(card);
        }
        return page;
    }

    /**
     * @return a row: the name of the setting on the left, its control on the right
     */
    static HBox row(final Translator translator, final Setting setting) {
        final Label name = new I18NLabel(translator, setting.key());
        name.getStyleClass().add("gp-setting-name");
        name.setWrapText(true);
        name.setMinWidth(160);
        HBox.setHgrow(name, Priority.ALWAYS);
        name.setMaxWidth(Double.MAX_VALUE);
        final HBox row = new HBox(24, name, setting.control());
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("gp-setting-row");
        row.getProperties().put(SETTING_KEY, setting.key());
        return row;
    }
}
