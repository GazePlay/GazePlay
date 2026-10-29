package net.gazeplay.games.whereisit;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

/**
 * A picture of a round of "Where is it?", on a rounded card; a fixation on it chooses it, the right one ending the
 * round. During the fixation, a ring of the main color goes round the card; a right choice is framed in green, a wrong
 * one is greyed and crossed.
 */
@Slf4j
@ToString
@Getter
class PictureCard extends Group {

    /**
     * Scale of the card while it is looked at.
     */
    private static final double LOOKED_AT_SCALE = 1.03;

    private final IGameContext gameContext;
    private final boolean winner;

    private final double initialWidth;
    private final double initialHeight;

    private final double initialPositionX;
    private final double initialPositionY;

    private final Stats stats;
    private final String imagePath;

    private final Rectangle background;
    private final ImageView imageRectangle;
    private final Rectangle ring;
    private final Group errorMark;

    private final DoubleProperty fixationProgress = new SimpleDoubleProperty(0);
    private final double ringPerimeter;
    private Timeline fixationTimeline;
    private ScaleTransition lookedAtTransition;

    private boolean selected;

    private final PictureCard.CustomInputEventHandler customInputEventHandler;

    private final WhereIsIt gameInstance;

    PictureCard(double posX, double posY, double width, double height, @NonNull IGameContext gameContext,
                boolean winner, @NonNull String imagePath, @NonNull Stats stats, WhereIsIt gameInstance) {

        log.info("imagePath = {}", imagePath);

        this.initialPositionX = posX;
        this.initialPositionY = posY;
        this.initialWidth = width;
        this.initialHeight = height;
        this.selected = false;
        this.winner = winner;
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameInstance = gameInstance;
        this.imagePath = imagePath;

        final double radius = Math.min(width, height) * 0.08;
        this.background = new Rectangle(posX, posY, width, height);
        background.setArcWidth(2 * radius);
        background.setArcHeight(2 * radius);
        background.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        background.setStrokeType(StrokeType.INSIDE);

        final boolean stretched = gameInstance.getGameType().toString().contains("COLORS");
        this.imageRectangle = createImageView(posX, posY, width, height, radius, imagePath, stretched);

        final double ringWidth = Math.max(5, Math.min(width, height) * 0.03);
        this.ring = new Rectangle(posX, posY, width, height);
        ring.setArcWidth(2 * radius);
        ring.setArcHeight(2 * radius);
        ring.setFill(null);
        ring.setStrokeType(StrokeType.INSIDE);
        ring.setStrokeLineCap(StrokeLineCap.BUTT);
        ring.setStrokeWidth(ringWidth);
        ring.setStyle("-fx-stroke: -gp-accent;");
        ring.setVisible(false);
        ring.setMouseTransparent(true);
        this.ringPerimeter = 2 * (width + height) - (8 - 2 * Math.PI) * radius;
        fixationProgress.addListener((observable, oldValue, newValue) ->
            ring.getStrokeDashArray().setAll(newValue.doubleValue() * ringPerimeter, ringPerimeter));

        this.errorMark = createErrorMark(posX + width / 2, posY + height / 2, Math.min(width, height) * 0.18);

        this.getChildren().addAll(background, imageRectangle);
        if (stretched) {
            // A color as light as the card (white, silver...) keeps its outline
            final Rectangle clip = (Rectangle) imageRectangle.getClip();
            final Rectangle outline = new Rectangle(clip.getX(), clip.getY(), clip.getWidth(), clip.getHeight());
            outline.setArcWidth(clip.getArcWidth());
            outline.setArcHeight(clip.getArcHeight());
            outline.setFill(null);
            outline.setStrokeType(StrokeType.INSIDE);
            outline.setStyle("-fx-stroke: -gp-text-muted; -fx-stroke-width: 2;");
            outline.setMouseTransparent(true);
            this.getChildren().add(outline);
        }
        this.getChildren().addAll(ring, errorMark);

        customInputEventHandler = new PictureCard.CustomInputEventHandler();

        // The whole card is the target of the gaze, not only the picture
        gameContext.getGazeDeviceManager().addEventFilter(background);

        this.addEventFilter(MouseEvent.ANY, customInputEventHandler);

        this.addEventFilter(GazeEvent.ANY, customInputEventHandler);
    }

    /**
     * Chooses the picture, at the end of the fixation.
     */
    private void onFixationCompleted() {
        log.debug("FINISHED");

        selected = true;
        gameContext.getGazeDeviceManager().removeEventFilter(background);
        setLookedAt(false);

        if (winner) {
            onCorrectCardSelected();
        } else {
            onWrongCardSelected();
        }
    }

    /**
     * Counts a goal (and a right decision for the levels), frames the picture in green, removes the other pictures and
     * ends the round: the picture grows to the size of the screen, then the win animation.
     */
    private void onCorrectCardSelected() {
        log.debug("WINNER");

        if (!gameInstance.getFirstWrong()) {
            gameInstance.updateRight();
        }

        gameInstance.firstRightCardSelected();

        stats.incrementNumberOfGoalsReached();

        customInputEventHandler.ignoreAnyInput = true;

        ring.getStrokeDashArray().clear();
        ring.setStyle("-fx-stroke: " + GameStyle.SUCCESS_COLOR + ";");
        ring.setVisible(true);

        gameInstance.removeAllIncorrectPictureCards();

        this.toFront();

        final Dimension2D gamePanelDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("gamePanelDimension2D = {}", gamePanelDimension2D);

        // The whole card, centered, as large as the game area allows
        final double ratio = Math.min(gamePanelDimension2D.getWidth() * 0.92 / initialWidth,
            gamePanelDimension2D.getHeight() * 0.92 / initialHeight);
        final ScaleTransition scaleToFullScreenTransition = new ScaleTransition(new Duration(900), this);
        scaleToFullScreenTransition.setToX(ratio);
        scaleToFullScreenTransition.setToY(ratio);

        final TranslateTransition translateToCenterTransition = new TranslateTransition(new Duration(900), this);
        translateToCenterTransition.setToX(gamePanelDimension2D.getWidth() / 2 - (initialPositionX + initialWidth / 2));
        translateToCenterTransition.setToY(gamePanelDimension2D.getHeight() / 2 - (initialPositionY + initialHeight / 2));

        final FadeTransition ringFadeOut = new FadeTransition(new Duration(900), ring);
        ringFadeOut.setToValue(0);

        final ParallelTransition fullAnimation = new ParallelTransition(translateToCenterTransition,
            scaleToFullScreenTransition, ringFadeOut);
        fullAnimation.setInterpolator(Interpolator.EASE_BOTH);
        // The green frame is seen before the picture grows
        fullAnimation.setDelay(new Duration(400));

        gameContext.updateScore(stats, gameInstance);

        fullAnimation.setOnFinished(actionEvent -> gameContext.playWinTransition(500, actionEvent1 -> {
            gameInstance.dispose();
            gameContext.clear();
            gameInstance.launch();
        }));

        fullAnimation.play();
    }

    /**
     * Shows that the picture is not the right one (greyed and crossed), and counts a wrong decision for the levels.
     */
    private void onWrongCardSelected() {
        gameInstance.updateWrong();
        gameInstance.firstWrongCardSelected();

        customInputEventHandler.ignoreAnyInput = true;
        ring.setVisible(false);

        final FadeTransition imageFadeOutTransition = new FadeTransition(new Duration(600), imageRectangle);
        imageFadeOutTransition.setFromValue(1);
        // Not zero, so that the picture can still be seen after it is marked as a wrong choice
        imageFadeOutTransition.setToValue(0.25);

        errorMark.toFront();
        errorMark.setVisible(true);
        final ScaleTransition errorPopTransition = new ScaleTransition(new Duration(350), errorMark);
        errorPopTransition.setFromX(0);
        errorPopTransition.setFromY(0);
        errorPopTransition.setToX(1);
        errorPopTransition.setToY(1);
        errorPopTransition.setInterpolator(Interpolator.EASE_OUT);

        final ParallelTransition fullAnimation = new ParallelTransition(imageFadeOutTransition, errorPopTransition);

        fullAnimation.setOnFinished(actionEvent -> {
            if (gameContext.getConfiguration().isQuestionReaskedOnFail()) {
                gameInstance.playQuestionSound();
            }
            customInputEventHandler.ignoreAnyInput = false;
        });

        fullAnimation.play();
    }

    /**
     * @return the picture inside the card, with rounded corners: keeping its proportions and centered, or filling the
     * card ({@code stretched}, for the colors). It is loaded at most at the size of the game area, the largest size at
     * which it is shown (when it fills the screen at the end of the round), not at the size of its file.
     */
    private ImageView createImageView(double posX, double posY, double width, double height, double radius,
                                      @NonNull String imagePath, boolean stretched) {
        final double padding = Math.min(width, height) * 0.04;
        final double innerWidth = width - 2 * padding;
        final double innerHeight = height - 2 * padding;

        final Dimension2D gameArea = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double maxWidth = gameArea == null ? 0 : Math.max(0, gameArea.getWidth());
        final double maxHeight = gameArea == null ? 0 : Math.max(0, gameArea.getHeight());
        final Image image = new Image(imagePath, maxWidth, maxHeight, true, true);

        final ImageView result = new ImageView(image);
        final double w;
        final double h;
        if (stretched || image.getWidth() <= 0 || image.getHeight() <= 0) {
            w = innerWidth;
            h = innerHeight;
            result.setPreserveRatio(false);
        } else {
            final double ratio = Math.min(innerWidth / image.getWidth(), innerHeight / image.getHeight());
            w = image.getWidth() * ratio;
            h = image.getHeight() * ratio;
            result.setPreserveRatio(true);
        }
        result.setFitWidth(w);
        result.setFitHeight(h);
        result.setSmooth(true);
        result.setX(posX + (width - w) / 2);
        result.setY(posY + (height - h) / 2);

        final double imageRadius = Math.max(0, radius - padding);
        final Rectangle clip = new Rectangle(result.getX(), result.getY(), w, h);
        clip.setArcWidth(2 * imageRadius);
        clip.setArcHeight(2 * imageRadius);
        result.setClip(clip);
        return result;
    }

    /**
     * @return the cross shown on a wrong picture: a white cross on a disc of the color "danger" of the theme
     */
    private static Group createErrorMark(double centerX, double centerY, double radius) {
        final Circle disc = new Circle(centerX, centerY, radius);
        disc.setStyle("-fx-fill: -gp-danger;");
        final double arm = radius * 0.45;
        final Line first = new Line(centerX - arm, centerY - arm, centerX + arm, centerY + arm);
        final Line second = new Line(centerX - arm, centerY + arm, centerX + arm, centerY - arm);
        for (final Line line : new Line[]{first, second}) {
            line.setStrokeWidth(radius * 0.22);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.setStyle("-fx-stroke: -gp-on-danger;");
        }
        final Group mark = new Group(disc, first, second);
        mark.setVisible(false);
        mark.setMouseTransparent(true);
        return mark;
    }

    /**
     * Makes the card slightly larger while it is looked at, and back to its size after.
     */
    private void setLookedAt(final boolean lookedAt) {
        if (lookedAtTransition != null) {
            lookedAtTransition.stop();
        }
        lookedAtTransition = new ScaleTransition(new Duration(150), this);
        lookedAtTransition.setToX(lookedAt ? LOOKED_AT_SCALE : 1);
        lookedAtTransition.setToY(lookedAt ? LOOKED_AT_SCALE : 1);
        lookedAtTransition.play();
    }

    /**
     * Starts the fixation when the gaze (or the mouse) enters the card, and stops it when it leaves.
     */
    private class CustomInputEventHandler implements EventHandler<Event> {

        /**
         * this is used to temporarily indicate to ignore input for instance, when an animation is in progress, we
         * do not want the game to continue to process input, as the user input is irrelevant while the animation is
         * in progress
         */
        private boolean ignoreAnyInput = false;

        @Override
        public void handle(Event e) {
            if (ignoreAnyInput) {
                return;
            }

            if (selected) {
                return;
            }

            if (e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == GazeEvent.GAZE_ENTERED) {
                onEntered();
            } else if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
                onExited();
            }

        }

        /**
         * Starts the fixation on the card.
         */
        private void onEntered() {
            log.info("ENTERED {}", imagePath);

            if (fixationTimeline != null) {
                fixationTimeline.stop();
            }
            fixationProgress.set(0);
            ring.setVisible(true);
            fixationTimeline = new Timeline(new KeyFrame(
                new Duration(gameContext.getConfiguration().getFixationLength()),
                new KeyValue(fixationProgress, 1)));
            fixationTimeline.setOnFinished(actionEvent -> onFixationCompleted());
            fixationTimeline.playFromStart();
            setLookedAt(true);
        }

        /**
         * Stops the fixation on the card.
         */
        private void onExited() {
            log.info("EXITED {}", imagePath);

            if (fixationTimeline != null) {
                fixationTimeline.stop();
            }
            ring.setVisible(false);
            fixationProgress.set(0);
            setLookedAt(false);
        }

    }

}
