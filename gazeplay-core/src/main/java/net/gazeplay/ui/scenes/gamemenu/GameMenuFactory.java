package net.gazeplay.ui.scenes.gamemenu;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.*;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.ThumbnailImage;
import net.gazeplay.commons.utils.games.BackgroundMusicManager;
import net.gazeplay.ui.menus.MenuIcon;
import net.gazeplay.ui.menus.MenuStyle;

import java.util.Locale;

import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

/**
 * Creates the cards of the games of the menu (look of the menus, menus.css): thumbnail with rounded corners, name,
 * description on two lines at most, the icons of the categories, a dot telling whether the game can be replayed, and
 * the heart marking a favorite game (shown on hover, always when the game is a favorite).
 * <p>
 * Vertical orientation: the thumbnail above the texts; horizontal: the thumbnail on the left. The size of the cards
 * follows the width of the screen.
 */
@Slf4j
@Data
public class GameMenuFactory {

    private static final double CARD_PADDING = 12;

    /**
     * Height of the texts of a vertical card: the name on two lines and the icons (with a description, its first
     * line), the same for every card.
     */
    private static final double TEXTS_HEIGHT = 96;

    private final GameMenuController gameMenuController;

    /**
     * @return the card of a game; a press (or Enter, Space) starts the game, a click on the heart adds it to the
     *   favorites or removes it
     */
    public GameButtonPane createGameButton(
        @NonNull final GazePlay gazePlay,
        @NonNull final Region root,
        @NonNull final Configuration config,
        @NonNull final Translator translator,
        @NonNull final GameSpec gameSpec,
        @NonNull final GameButtonOrientation orientation,
        @NonNull final ThumbnailImage thumbnailImage
    ) {
        final GameSummary gameSummary = gameSpec.getGameSummary();
        final String gameName = translator.translate(gameSummary.getNameCode());
        final boolean vertical = orientation == GameButtonOrientation.VERTICAL;

        final Dimension2D screen = gazePlay.getCurrentScreenDimensionSupplier().get();
        final double cardWidth = vertical
            ? clamp(screen.getWidth() / 6.2, 210, 300)
            : clamp(screen.getWidth() / 3.4, 340, 520);
        final double thumbnailWidth = vertical ? cardWidth - 2 * CARD_PADDING : cardWidth * 0.42;
        final double thumbnailHeight = thumbnailWidth * 9 / 16;

        final Node thumbnail = createThumbnail(gameSummary, thumbnailImage, thumbnailWidth, thumbnailHeight);

        final Label title = new Label(gameName);
        title.getStyleClass().add("gp-game-title");
        title.setWrapText(true);

        // Few games have a description (6 of the catalog in October 2026): no room kept without one
        final Label description = new Label(
            gameSummary.getDescription() == null ? "" : translator.translate(gameSummary.getDescription()));
        description.getStyleClass().add("gp-game-description");
        description.setWrapText(true);
        description.setManaged(!description.getText().isBlank());
        description.setVisible(description.isManaged());

        final HBox footer = createFooter(gameSummary, translator);

        final GameButtonPane gameCard = new GameButtonPane(gameSpec);
        gameCard.getStyleClass().addAll("gp-game-card", vertical ? "gp-game-card-vertical" : "gp-game-card-horizontal");
        gameCard.setFocusTraversable(true);
        gameCard.setMinWidth(cardWidth);
        gameCard.setPrefWidth(cardWidth);
        gameCard.setMaxWidth(cardWidth);

        final Node content;
        if (vertical) {
            final Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            final VBox texts = new VBox(6, title, description, spacer, footer);
            // Title and description on two lines at most (menus.css): the same height for every card
            texts.setMinHeight(TEXTS_HEIGHT);
            texts.setPrefHeight(TEXTS_HEIGHT);
            content = new VBox(10, thumbnail, texts);
        } else {
            final Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            final VBox texts = new VBox(6, title, description, spacer, footer);
            HBox.setHgrow(texts, Priority.ALWAYS);
            final HBox row = new HBox(12, thumbnail, texts);
            row.setMinHeight(thumbnailHeight);
            content = row;
        }

        // Top right in both orientations: on a horizontal card, over the texts, not the image (maintainer's feedback
        // of 28/10/2026); the name keeps room for it (gp-game-title, menus.css)
        final StackPane favorite = createFavoriteSwitch(config, gameSummary, translator);
        StackPane.setAlignment(favorite, Pos.TOP_RIGHT);
        final StackPane layers = new StackPane(content, favorite);
        gameCard.setCenter(layers);

        gameCard.addEventHandler(MOUSE_PRESSED, (MouseEvent e) -> {
            if (!favorite.isHover()) {
                startGame(gazePlay, root, gameSpec, gameName);
            }
        });
        gameCard.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                startGame(gazePlay, root, gameSpec, gameName);
                event.consume();
            }
        });

        return gameCard;
    }

    /**
     * Starts a game (or shows its variants), pausing the music of the menu.
     */
    private void startGame(final GazePlay gazePlay, final Region root, final GameSpec gameSpec, final String gameName) {
        gameMenuController.onGameSelection(gazePlay, root, gameSpec, gameName);
        BackgroundMusicManager.getInstance().pause();
    }

    /**
     * @return the thumbnail of a game, filling a box with rounded corners (cut to its ratio), or an empty box
     */
    private static Node createThumbnail(final GameSummary gameSummary, final ThumbnailImage thumbnailImage,
                                       final double width, final double height) {
        final StackPane box = new StackPane();
        box.getStyleClass().add("gp-game-thumbnail");
        box.setMinSize(width, height);
        box.setPrefSize(width, height);
        box.setMaxSize(width, height);
        final Rectangle clip = new Rectangle(width, height);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        box.setClip(clip);

        if (gameSummary.getGameThumbnail() != null) {
            final String path = thumbnailImage.getPathThumbnailImg(gameSummary.getGameThumbnail());
            // Twice the size of the box: sharp on a screen scaled at 200 %
            final Image image = new Image(path, width * 2, height * 2, true, true);
            final ImageView imageView = new ImageView(image);
            if (image.getWidth() > 0 && image.getHeight() > 0) {
                // Cover: the part of the image with the ratio of the box, centered
                final double scale = Math.max(width / image.getWidth(), height / image.getHeight());
                final double viewWidth = width / scale;
                final double viewHeight = height / scale;
                imageView.setViewport(new Rectangle2D((image.getWidth() - viewWidth) / 2,
                    (image.getHeight() - viewHeight) / 2, viewWidth, viewHeight));
            }
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setSmooth(true);
            box.getChildren().add(imageView);
        }
        return box;
    }

    /**
     * @return the icons of the categories of a game, then a dot: green if the game can be replayed, grey otherwise
     */
    private static HBox createFooter(final GameSummary gameSummary, final Translator translator) {
        final HBox footer = new HBox(6);
        footer.setAlignment(Pos.CENTER_LEFT);
        for (final GameCategories.Category category : gameSummary.getCategories()) {
            final StackPane icon = categoryIcon(category).create(20);
            icon.getStyleClass().addAll("gp-game-category", categoryStyleClass(category));
            Tooltip.install(icon, MenuStyle.tooltip(translator.translate(category.getGameCategory())));
            footer.getChildren().add(icon);
        }
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final boolean replayable = ReplayingGameFromJson.replayIsAllowed(gameSummary.getNameCode());
        final Circle replayDot = new Circle(5);
        replayDot.getStyleClass().addAll("gp-replay-dot", replayable ? "gp-replay-yes" : "gp-replay-no");
        Tooltip.install(replayDot, MenuStyle.tooltip(translator.translate(replayable ? "ReplayAvailable" : "ReplayUnavailable")));
        footer.getChildren().addAll(spacer, replayDot);
        return footer;
    }

    /**
     * @return the icon of a category of games
     */
    static MenuIcon categoryIcon(final GameCategories.Category category) {
        return switch (category) {
            case ACTION_REACTION -> MenuIcon.CATEGORY_ACTION_REACTION;
            case SELECTION -> MenuIcon.CATEGORY_SELECTION;
            case MEMORIZATION -> MenuIcon.CATEGORY_MEMORIZATION;
            case LOGIC_MATHS -> MenuIcon.CATEGORY_LOGIC_MATHS;
            case LITERACY -> MenuIcon.CATEGORY_LITERACY;
            case MULTIMEDIA -> MenuIcon.CATEGORY_MULTIMEDIA;
        };
    }

    /**
     * @return the style class giving a category its color (menus.css), e.g. gp-cat-action_reaction
     */
    static String categoryStyleClass(final GameCategories.Category category) {
        return "gp-cat-" + category.name().toLowerCase(Locale.ROOT);
    }

    /**
     * @return the heart of a card: filled when the game is a favorite (FAVORITE_GAMES setting), a click switches it
     */
    private static StackPane createFavoriteSwitch(final Configuration config, final GameSummary gameSummary,
                                                  final Translator translator) {
        final StackPane outline = MenuIcon.FAVORITE.create(24);
        final StackPane filled = MenuIcon.FAVORITE_FILLED.create(24);
        filled.getStyleClass().add("gp-favorite-filled");
        final StackPane favorite = new StackPane(outline, filled);
        favorite.getStyleClass().add("gp-favorite");
        favorite.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        final Tooltip tooltip = MenuStyle.tooltip("");
        Tooltip.install(favorite, tooltip);

        final Runnable update = () -> {
            final boolean on = config.getFavoriteGamesProperty().contains(gameSummary.getNameCode());
            outline.setVisible(!on);
            filled.setVisible(on);
            if (on) {
                if (!favorite.getStyleClass().contains("gp-favorite-on")) {
                    favorite.getStyleClass().add("gp-favorite-on");
                }
            } else {
                favorite.getStyleClass().remove("gp-favorite-on");
            }
            tooltip.setText(translator.translate(on ? "FavoriteRemove" : "FavoriteAdd"));
        };
        update.run();
        // A favorite game has two cards (favorites, all the games): both hearts follow the setting. Weak listener, kept
        // by the heart itself: the cards of a former menu can be collected
        final InvalidationListener favoritesListener = observable -> update.run();
        favorite.getProperties().put(InvalidationListener.class, favoritesListener);
        config.getFavoriteGamesProperty().addListener(new WeakInvalidationListener(favoritesListener));

        favorite.addEventFilter(MOUSE_CLICKED, event -> {
            if (config.getFavoriteGamesProperty().contains(gameSummary.getNameCode())) {
                config.getFavoriteGamesProperty().remove(gameSummary.getNameCode());
            } else {
                config.getFavoriteGamesProperty().add(gameSummary.getNameCode());
            }
        });
        return favorite;
    }

    private static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }
}
