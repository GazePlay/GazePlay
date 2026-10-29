package net.gazeplay.ui.scenes.configuration;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ActiveConfigurationContext;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.components.CssUtil;
import net.gazeplay.ui.menus.MenuDialog;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Window of the images given by the user for a game (folder images/&lt;game&gt; of the files folder of the profile, see
 * {@link UserImagesVariant}), in the look of the menus: the images, each with a button removing it, and a button adding
 * images (copied into the folder). Opened from the settings, or from the menu when a variant playing with the images of
 * the user is chosen while it has none: then a "Done" button, enabled once there is an image, starts the game.
 */
@Slf4j
public class UserImagesDialog extends Stage {

    private static final double THUMBNAIL_SIZE = 110;

    private final String folder;

    private final Translator translator;

    @Getter
    private final Label messageLabel;

    @Getter
    private final FlowPane thumbnails = new FlowPane(10, 10);

    @Getter
    private final Button addButton;

    /**
     * The button starting the game; null when the window is opened from the settings.
     */
    @Getter
    private final Button doneButton;

    /**
     * @param owner     the window it is opened over
     * @param menuRoot  the page below, blurred and disabled while the window is open (given back when it closes); may
     *                  be null
     * @param folder    the folder of the images of the game (e.g. "Coloring")
     * @param title     the title of the window (the name of the game)
     * @param onDone    what the "Done" button does (start the game); null for no such button (settings)
     */
    public UserImagesDialog(final Stage owner, final Parent menuRoot, final Translator translator, final String folder,
                            final String title, final Runnable onDone) {
        this.folder = folder;
        this.translator = translator;
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        MenuDialog.undecorate(this);
        setTitle(title);
        if (menuRoot != null) {
            setOnHidden(event -> {
                menuRoot.setEffect(null);
                menuRoot.setDisable(false);
            });
        }

        messageLabel = new Label();
        messageLabel.getStyleClass().add("gp-dialog-prompt");
        messageLabel.setWrapText(true);
        // On several lines rather than cut
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);
        messageLabel.setTextAlignment(TextAlignment.CENTER);

        final ScrollPane scroller = new ScrollPane(thumbnails);
        scroller.setFitToWidth(true);
        scroller.setPrefViewportHeight(2 * THUMBNAIL_SIZE + 30);
        thumbnails.setAlignment(Pos.CENTER);
        thumbnails.setPadding(new Insets(8));

        addButton = new Button(translator.translate("AddImages"), MenuIcon.ADD.create(22));
        addButton.getStyleClass().addAll("gp-button", "gp-button-secondary");
        addButton.setOnAction(event -> addImages());

        final HBox buttons = new HBox(12, addButton);
        buttons.setAlignment(Pos.CENTER);
        if (onDone != null) {
            doneButton = new Button(translator.translate("Done"));
            doneButton.getStyleClass().addAll("gp-button", "gp-button-primary");
            doneButton.setOnAction(event -> {
                close();
                onDone.run();
            });
            buttons.getChildren().add(doneButton);
        } else {
            doneButton = null;
        }

        final VBox content = new VBox(16, messageLabel, scroller, buttons);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(8, 28, 28, 28));

        setScene(MenuDialog.createScene(this, content, translator.translate("Close"), this::close));
        CssUtil.setPreferredStylesheets(ActiveConfigurationContext.getInstance(), getScene());
        setWidth(Math.max(640, owner.getWidth() * 0.55));
        setHeight(Math.max(460, owner.getHeight() * 0.6));
        refresh();
    }

    /**
     * Shows the images of the folder again, and the message and the "Done" button for their number.
     */
    void refresh() {
        final List<File> images = UserImagesVariant.images(folder);
        thumbnails.getChildren().clear();
        for (final File file : images) {
            thumbnails.getChildren().add(thumbnail(file));
        }
        messageLabel.setText(translator.translate(images.isEmpty() ? "NoUserImages" : "UserImagesOfTheGame"));
        messageLabel.setStyle(images.isEmpty() && doneButton != null ? "-fx-text-fill: -gp-danger;" : "");
        if (doneButton != null) {
            doneButton.setDisable(images.isEmpty());
        }
    }

    /**
     * @return an image of the folder on a card, with a button removing it from the folder
     */
    private StackPane thumbnail(final File file) {
        final ImageView view = new ImageView(new Image(file.toURI().toString(), THUMBNAIL_SIZE - 10,
            THUMBNAIL_SIZE - 10, true, true, true));
        final Button remove = MenuStyle.iconButton(MenuIcon.DELETE, translator.translate("Delete"), () -> {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (final IOException e) {
                log.warn("Cannot remove {}", file, e);
            }
            refresh();
        });
        StackPane.setAlignment(remove, Pos.TOP_RIGHT);
        final StackPane card = new StackPane(view, remove);
        card.setMinSize(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        card.setMaxSize(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        card.setStyle("-fx-background-color: -gp-surface; -fx-background-radius: 14; -fx-border-color: -gp-border;"
            + " -fx-border-radius: 14; -fx-border-width: 2;");
        return card;
    }

    /**
     * Copies images chosen by the user into the folder (an image of the same name is replaced).
     */
    private void addImages() {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle(translator.translate("AddImages"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(translator.translate("Images"),
            UserImagesVariant.IMAGE_EXTENSIONS.stream().flatMap(e -> List.of("*." + e, "*." + e.toUpperCase()).stream())
                .toList()));
        final List<File> chosen = chooser.showOpenMultipleDialog(this);
        if (chosen != null) {
            copy(chosen);
        }
    }

    /**
     * Copies images into the folder, then shows it again.
     */
    void copy(final List<File> files) {
        final File directory = UserImagesVariant.directory(folder);
        for (final File file : files) {
            if (!UserImagesVariant.isImage(file)) {
                continue;
            }
            try {
                Files.copy(file.toPath(), new File(directory, file.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                log.warn("Cannot copy {} into {}", file, directory, e);
            }
        }
        refresh();
    }
}
