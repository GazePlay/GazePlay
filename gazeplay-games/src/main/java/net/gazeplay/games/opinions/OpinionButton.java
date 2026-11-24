package net.gazeplay.games.opinions;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.RingThickness;

/**
 * A button of an answer of the game "Opinions" (2/11/2026): a card with a big icon of the color of the answer and its
 * word under it. A fixation (the fixation length of the settings) gives the answer, a ring of the main color going round
 * the card meanwhile. Its top left corner is at (0, 0). Since 23/11/2026 (point 22, maintainer's decision), the next
 * image waits for the delay of the settings, as everywhere; the card no longer had to be left and looked at again.
 */
class OpinionButton extends Group {

    private final IGameContext gameContext;

    @Getter
    private final OpinionChoice choice;

    private final Rectangle background;

    private final Rectangle ring;

    private final double ringPerimeter;

    private final DoubleProperty progress = new SimpleDoubleProperty(0);

    private final Runnable action;

    private Timeline fixation;

    @Getter
    private boolean enabled = true;

    OpinionButton(final IGameContext gameContext, final OpinionChoice choice, final double width, final double height,
                  final Runnable action) {
        this.gameContext = gameContext;
        this.choice = choice;
        this.action = action;

        final double radius = GameStyle.cardRadius(width, height);
        background = new Rectangle(width, height);
        background.setArcWidth(2 * radius);
        background.setArcHeight(2 * radius);
        background.setStrokeType(StrokeType.INSIDE);
        setChosen(false);

        final double iconSize = Math.min(width, height) * 0.55;
        final SVGPath icon = GameStyle.icon(choice.getIcon(), iconSize);
        icon.setStyle("-fx-fill: " + choice.getColor() + ";");
        final Group iconGroup = new Group(icon);
        iconGroup.setLayoutX((width - iconSize) / 2);
        iconGroup.setLayoutY(height * 0.12);
        iconGroup.setMouseTransparent(true);

        final double fontSize = Math.min(width * 0.13, height * 0.1);
        final Text label = new Text(gameContext.getTranslator().translate(choice.getLabel()));
        label.setFont(GameStyle.titleFont(fontSize));
        label.setStyle(GameStyle.titleFontStyle(fontSize) + " -fx-fill: " + GameStyle.CARD_TEXT + ";");
        double size = fontSize;
        if (label.getLayoutBounds().getWidth() > width * 0.9) {
            size = fontSize * width * 0.9 / label.getLayoutBounds().getWidth();
            label.setFont(GameStyle.titleFont(size));
            label.setStyle(GameStyle.titleFontStyle(size) + " -fx-fill: " + GameStyle.CARD_TEXT + ";");
        }
        label.setX((width - label.getLayoutBounds().getWidth()) / 2 - label.getLayoutBounds().getMinX());
        label.setY(height * 0.12 + iconSize + (height * 0.88 - iconSize - label.getLayoutBounds().getHeight()) / 2
            - label.getLayoutBounds().getMinY());
        label.setMouseTransparent(true);

        ring = new Rectangle(width, height);
        ring.setArcWidth(2 * radius);
        ring.setArcHeight(2 * radius);
        ring.setFill(null);
        ring.setStrokeType(StrokeType.INSIDE);
        ring.setStrokeLineCap(StrokeLineCap.BUTT);
        RingThickness.apply(ring, Math.max(5, Math.min(width, height) * 0.05));
        ring.setStyle("-fx-stroke: -gp-ring;");
        ring.setMouseTransparent(true);
        ring.setVisible(false);
        ringPerimeter = GameStyle.clockwiseRing(ring);
        progress.addListener((observable, oldValue, newValue) ->
            GameStyle.fillRing(ring, ringPerimeter, newValue.doubleValue()));

        getChildren().addAll(background, iconGroup, label, ring);

        gameContext.getGazeDeviceManager().addEventFilter(background);
        addEventFilter(MouseEvent.ANY, this::handle);
        addEventFilter(GazeEvent.ANY, this::handle);
    }

    private void handle(final Event e) {
        if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
            if (enabled && !gameContext.isSelectionBlocked()) {
                startFixation();
            }
        } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
            stopFixation();
        }
    }

    private void startFixation() {
        stopFixation();
        ring.setVisible(true);
        fixation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(progress, 0)),
            new KeyFrame(Duration.millis(gameContext.getConfiguration().getFixationLength()), new KeyValue(progress, 1)));
        fixation.setOnFinished(event -> select());
        fixation.playFromStart();
    }

    private void stopFixation() {
        if (fixation != null) {
            fixation.stop();
            fixation = null;
        }
        progress.set(0);
        ring.setVisible(false);
    }

    /**
     * Gives the answer, as at the end of a fixation.
     */
    void select() {
        if (enabled) {
            stopFixation();
            action.run();
        }
    }

    /**
     * Shows the card as the answer just given (framed in the color of the answer) or not. The card is white in both
     * themes, its word in {@link GameStyle#CARD_TEXT}, as every card to choose (tester's feedback of 20/11/2026,
     * point 29).
     */
    void setChosen(final boolean chosen) {
        background.setStyle("-fx-fill: " + GameStyle.PICTURE_BACKGROUND + "; -fx-stroke: " + (chosen ? choice.getColor() : "-gp-border")
            + "; -fx-stroke-width: " + (chosen ? 10 : 2) + ";");
    }

    void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            stopFixation();
        }
    }

    /**
     * Stops following the gaze.
     */
    void dispose() {
        stopFixation();
        gameContext.getGazeDeviceManager().removeEventFilter(background);
    }
}
