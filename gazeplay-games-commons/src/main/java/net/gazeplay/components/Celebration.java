package net.gazeplay.components;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A reward over a game, for a game won (tester's feedback of 24/11/2026, point 69, first for the Morpion): confetti
 * falling over the whole screen, and two Biboules jumping for joy on the sides of a zone left free in the middle (the
 * grid of the game). Moved by the loop of the game ({@link #step}), so that it stops while the menu of the game is open
 * and with the game; its own draws, so that the draws of the game, and thus its replays, do not change.
 */
public class Celebration extends Group {

    /**
     * The seconds it lasts, the number of confetti, and the seconds of a jump.
     */
    public static final double LENGTH = 2.5;
    static final int CONFETTI = 80;
    static final double JUMP = 0.5;

    private static final Color[] COLORS = {Color.rgb(230, 60, 70), Color.rgb(250, 190, 40), Color.rgb(60, 170, 90),
        Color.rgb(52, 112, 222), Color.rgb(170, 90, 210), Color.rgb(255, 130, 170)};

    private final double width;
    private final double height;
    private final Random random = new Random(69);
    private final List<Piece> pieces = new ArrayList<>();
    private final List<ImageView> biboules = new ArrayList<>();
    private double bibouleBottom;
    private double jumpHeight;
    @Getter
    private double time;
    @Getter
    private boolean running;

    /**
     * @param width  the width of the screen
     * @param height the height of the screen
     */
    public Celebration(final double width, final double height) {
        this.width = width;
        this.height = height;
        // The confetti waiting above the screen are not drawn outside it
        setClip(new Rectangle(0, 0, width, height));
        setMouseTransparent(true);
        setVisible(false);
    }

    /**
     * Starts the reward: confetti from above the screen, and a Biboule on each side of a free zone in the middle.
     *
     * @param biboule   the picture of the Biboules
     * @param freeLeft  the left of the zone left free (the grid), its right at the same distance from the right edge
     * @param freeTop   the top of the free zone
     * @param freeBottom the bottom of the free zone, where the Biboules stand
     */
    public void start(final Image biboule, final double freeLeft, final double freeTop, final double freeBottom) {
        getChildren().clear();
        pieces.clear();
        biboules.clear();
        final double side = Math.max(0, freeLeft);
        final double size = Math.min(side * 0.8, (freeBottom - freeTop) * 0.45);
        bibouleBottom = freeBottom;
        jumpHeight = size * 0.45;
        if (size > 20) {
            for (int i = 0; i < 2; i++) {
                final ImageView view = new ImageView(biboule);
                view.setPreserveRatio(true);
                view.setFitWidth(size);
                final double shown = size * biboule.getHeight() / Math.max(1, biboule.getWidth());
                view.setX(i == 0 ? (side - size) / 2 : width - side + (side - size) / 2);
                view.setY(freeBottom - shown);
                // The Biboule on the right looks to the left
                view.setScaleX(i == 0 ? 1 : -1);
                biboules.add(view);
                getChildren().add(view);
            }
        }
        final double scale = Math.min(width, height);
        for (int i = 0; i < CONFETTI; i++) {
            final double w = scale * (0.008 + 0.01 * random.nextDouble());
            final Rectangle paper = new Rectangle(-w / 2, -w * 0.3, w, w * 0.6);
            paper.setFill(COLORS[i % COLORS.length]);
            final Piece piece = new Piece(paper, width * random.nextDouble(), -height * (0.05 + 0.6 * random.nextDouble()),
                height * (0.35 + 0.3 * random.nextDouble()), scale * (0.01 + 0.03 * random.nextDouble()),
                random.nextDouble() * Math.PI * 2, 90 + 360 * random.nextDouble());
            pieces.add(piece);
            getChildren().add(paper);
        }
        time = 0;
        running = true;
        setVisible(true);
        setOpacity(1);
        step(0);
    }

    /**
     * Moves the confetti and makes the Biboules jump, for a duration in seconds; the reward ends after {@link #LENGTH}
     * seconds.
     */
    public void step(final double seconds) {
        if (!running) {
            return;
        }
        time += seconds;
        for (final Piece piece : pieces) {
            final double y = piece.startY + piece.speed * time;
            piece.paper.setLayoutX(piece.x + Math.sin(piece.phase + time * 4) * piece.sway);
            piece.paper.setLayoutY(y);
            piece.paper.setRotate(piece.spin * time);
        }
        for (int i = 0; i < biboules.size(); i++) {
            // The two Biboules jump in turn
            final double t = ((time / JUMP) + i * 0.5) % 1;
            biboules.get(i).setTranslateY(-jumpHeight * 4 * t * (1 - t));
        }
        // Fades out at the end
        setOpacity(Math.max(0, Math.min(1, (LENGTH - time) / 0.4)));
        if (time >= LENGTH) {
            stop();
        }
    }

    /**
     * Ends the reward at once.
     */
    public void stop() {
        running = false;
        setVisible(false);
        getChildren().clear();
        pieces.clear();
        biboules.clear();
    }

    /**
     * @return the Biboules shown, for the tests
     */
    public int bibouleCount() {
        return biboules.size();
    }

    /**
     * @return the confetti shown, for the tests
     */
    public int confettiCount() {
        return pieces.size();
    }

    /**
     * @return the base of the Biboules, for the tests
     */
    double getBibouleBottom() {
        return bibouleBottom;
    }

    /**
     * A piece of confetti: where it starts, how fast it falls (per second), how far and from where it sways, how fast
     * it turns (degrees per second).
     */
    private static final class Piece {
        private final Rectangle paper;
        private final double x;
        private final double startY;
        private final double speed;
        private final double sway;
        private final double phase;
        private final double spin;

        Piece(final Rectangle paper, final double x, final double startY, final double speed, final double sway,
              final double phase, final double spin) {
            this.paper = paper;
            this.x = x;
            this.startY = startY;
            this.speed = speed;
            this.sway = sway;
            this.phase = phase;
            this.spin = spin;
        }
    }
}
