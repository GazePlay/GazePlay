package net.gazeplay.games.memory;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.components.GameStyle;

/**
 * A card of Memory (reviewed on 30/10/2026): a rounded card of the colors of the menus showing a picture, a letter or a
 * digit, face down (a back of the main color with a "?") or face up. During a fixation, a ring of the main color goes
 * round the card; at its end, the card is chosen ({@link Memory#cardFixated}). It turns over with an animation, is
 * framed in green with its pair, or in the color "danger" for a wrong pair.
 */
public class MemoryCard extends Group {

    private static final Duration TURN = Duration.millis(160);

    @Getter
    private final int pairId;

    private final Memory game;

    private final IGameContext gameContext;

    /**
     * The face (picture, letter or digit on a white card) and the back (face down cards only).
     */
    private final Group face;

    private final Group back;

    private final Rectangle ring;

    private final double ringPerimeter;

    private final DoubleProperty fixationProgress = new SimpleDoubleProperty(0);

    private Timeline fixation;

    /**
     * Chosen (turned over, or framed for the face up cards) and waiting for its pair.
     */
    @Getter
    private boolean selected;

    /**
     * Found with its pair, and removed.
     */
    @Getter
    private boolean found;

    /**
     * @param image the picture of the face, or null for a text
     * @param text  the letter or the digit of the face, when there is no picture
     */
    MemoryCard(final double x, final double y, final double width, final double height, final int pairId,
               final Image image, final String text, final boolean faceUp, final Memory game,
               final IGameContext gameContext) {
        this.pairId = pairId;
        this.game = game;
        this.gameContext = gameContext;
        setLayoutX(x);
        setLayoutY(y);

        final Rectangle surface = GameStyle.targetCard(0, 0, width, height);
        final Node content;
        if (image != null) {
            final ImageView view = new ImageView(image);
            view.setPreserveRatio(true);
            view.setFitWidth(width * 0.84);
            view.setFitHeight(height * 0.84);
            final double shownWidth = Math.min(width * 0.84, height * 0.84 * image.getWidth() / image.getHeight());
            final double shownHeight = Math.min(height * 0.84, width * 0.84 * image.getHeight() / image.getWidth());
            view.setLayoutX((width - shownWidth) / 2);
            view.setLayoutY((height - shownHeight) / 2);
            content = view;
        } else {
            final double size = Math.min(width, height) * 0.62;
            final Text glyph = new Text(text);
            glyph.setFont(GameStyle.titleFont(size));
            glyph.setStyle(GameStyle.titleFontStyle(size) + " -fx-fill: -gp-text;");
            glyph.setX((width - glyph.getLayoutBounds().getWidth()) / 2 - glyph.getLayoutBounds().getMinX());
            glyph.setY((height - glyph.getLayoutBounds().getHeight()) / 2 - glyph.getLayoutBounds().getMinY());
            content = glyph;
        }
        face = new Group(surface, content);

        back = createBack(width, height);
        face.setVisible(faceUp);
        back.setVisible(!faceUp);

        // The ring of the fixation, then the green or "danger" frame
        final double radius = GameStyle.cardRadius(width, height);
        ring = new Rectangle(0, 0, width, height);
        ring.setArcWidth(2 * radius);
        ring.setArcHeight(2 * radius);
        ring.setFill(null);
        ring.setStrokeType(StrokeType.INSIDE);
        ring.setStrokeLineCap(StrokeLineCap.BUTT);
        ring.setStrokeWidth(Math.max(5, Math.min(width, height) * 0.045));
        ring.setVisible(false);
        ring.setMouseTransparent(true);
        ringPerimeter = 2 * (width + height) - (8 - 2 * Math.PI) * radius;
        fixationProgress.addListener((observable, oldValue, newValue) ->
            ring.getStrokeDashArray().setAll(newValue.doubleValue() * ringPerimeter, ringPerimeter));

        getChildren().addAll(face, back, ring);

        addEventFilter(MouseEvent.MOUSE_ENTERED, this::onEnter);
        addEventFilter(GazeEvent.GAZE_ENTERED, this::onEnter);
        addEventFilter(MouseEvent.MOUSE_EXITED, e -> stopFixation());
        addEventFilter(GazeEvent.GAZE_EXITED, e -> stopFixation());
        gameContext.getGazeDeviceManager().addEventFilter(this);
    }

    /**
     * @return the back of a card face down: the main color, a lighter frame and a "?"
     */
    private static Group createBack(final double width, final double height) {
        final double radius = GameStyle.cardRadius(width, height);
        final Rectangle card = new Rectangle(width, height);
        card.setArcWidth(2 * radius);
        card.setArcHeight(2 * radius);
        card.setStyle("-fx-fill: -gp-accent; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        final double margin = Math.min(width, height) * 0.08;
        final Rectangle frame = new Rectangle(margin, margin, width - 2 * margin, height - 2 * margin);
        frame.setArcWidth(2 * radius * 0.7);
        frame.setArcHeight(2 * radius * 0.7);
        frame.setFill(null);
        frame.setStyle("-fx-stroke: rgba(255, 255, 255, 0.55); -fx-stroke-width: " + Math.max(2, margin * 0.35) + ";");
        final double size = Math.min(width, height) * 0.5;
        final Text mark = new Text("?");
        mark.setFont(GameStyle.titleFont(size));
        mark.setStyle(GameStyle.titleFontStyle(size) + " -fx-fill: -gp-on-accent;");
        mark.setX((width - mark.getLayoutBounds().getWidth()) / 2 - mark.getLayoutBounds().getMinX());
        mark.setY((height - mark.getLayoutBounds().getHeight()) / 2 - mark.getLayoutBounds().getMinY());
        return new Group(card, frame, mark);
    }

    private void onEnter(final Event event) {
        if (!game.canChoose(this)) {
            return;
        }
        stopFixation();
        ring.setStyle("-fx-stroke: -gp-accent;");
        ring.setVisible(true);
        fixationProgress.set(0);
        fixation = new Timeline(new KeyFrame(Duration.millis(gameContext.getConfiguration().getFixationLength()),
            new KeyValue(fixationProgress, 1)));
        fixation.setOnFinished(e -> {
            ring.setVisible(false);
            game.cardFixated(this);
        });
        fixation.playFromStart();
    }

    private void stopFixation() {
        if (fixation != null) {
            fixation.stop();
            fixation = null;
        }
        if (!selected) {
            ring.setVisible(false);
        }
    }

    /**
     * Chosen: a card face down turns over; a card face up is framed in the main color.
     */
    void select() {
        selected = true;
        if (back.isVisible()) {
            turn(true);
        } else {
            frame("-gp-accent");
        }
    }

    /**
     * Framed in green: found with its pair.
     */
    void showRight() {
        frame(GameStyle.SUCCESS_COLOR);
    }

    /**
     * Framed in the color "danger": not the pair of the other card.
     */
    void showWrong() {
        frame("-gp-danger");
    }

    /**
     * No longer chosen: a card face down turns back.
     */
    void reset(final boolean faceDown) {
        selected = false;
        ring.setVisible(false);
        if (faceDown) {
            turn(false);
        }
    }

    /**
     * Found with its pair: it fades out, and can no longer be chosen.
     */
    void remove() {
        found = true;
        selected = false;
        setMouseTransparent(true);
        gameContext.getGazeDeviceManager().removeEventFilter(this);
        final FadeTransition fade = new FadeTransition(Duration.millis(350), this);
        fade.setToValue(0);
        fade.play();
    }

    /**
     * No longer listens to the gaze (end of the round).
     */
    void release() {
        stopFixation();
        gameContext.getGazeDeviceManager().removeEventFilter(this);
    }

    /**
     * @return whether the face is shown
     */
    boolean isFaceShown() {
        return face.isVisible();
    }

    private void frame(final String color) {
        fixationProgress.set(1);
        ring.getStrokeDashArray().clear();
        ring.setStyle("-fx-stroke: " + color + ";");
        ring.setVisible(true);
    }

    /**
     * Turns the card over (to its face, or to its back): it gets narrower, changes side, then widens.
     */
    private void turn(final boolean toFace) {
        final ScaleTransition close = new ScaleTransition(TURN, this);
        close.setToX(0);
        close.setOnFinished(e -> {
            face.setVisible(toFace);
            back.setVisible(!toFace);
            final ScaleTransition open = new ScaleTransition(TURN, this);
            open.setToX(1);
            open.play();
        });
        close.play();
    }
}
