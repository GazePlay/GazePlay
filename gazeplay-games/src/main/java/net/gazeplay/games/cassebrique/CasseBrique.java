package net.gazeplay.games.cassebrique;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.UserImagesVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * "Casse-brique" (brick breaker, reviewed on 3/11/2026, maintainer's rules): the paddle goes along the bottom where the
 * gaze or the mouse is (as the setting of the inputs allows) and sends the ball back towards a wall of bricks (the
 * drawing of the variant). A brick hit breaks (with a little flash), or cracks first in the hard level; all the bricks
 * broken: "Bravo" then the statistics. Three lives: a fallen ball costs one, the ball comes back on the paddle; the
 * three lost: "Lost!" then the statistics. Behind the bricks, a soft background drawn by the game, or an image of the
 * user found little by little.
 * <p>
 * One loop moves everything in real time, at the animation speed of the settings; nothing moves while the menu of the
 * gear is open. (Before the review: a loop of 15 ms that never stopped, even after the win, which it started again
 * and again; a ball going 8 pixels at a time on every screen; no lives.)
 */
public class CasseBrique implements GameLifeCycle {

    static final int LIVES = 3;
    static final double FIRST_WAIT = 3;
    static final double WAIT_AFTER_FALL = 1.5;
    /**
     * The opacity of the veil over the image of the user at the start; none left when all the bricks are broken.
     */
    static final double VEIL_OPACITY = 0.92;

    /**
     * The soft colors of the bricks, 1 to 7 of the drawings (violet, blue, cyan, green, yellow, orange, red).
     */
    static final Color[] COLORS = {
        Color.rgb(175, 140, 225), Color.rgb(115, 165, 240), Color.rgb(110, 205, 225), Color.rgb(135, 205, 125),
        Color.rgb(250, 210, 95), Color.rgb(250, 165, 95), Color.rgb(240, 115, 125)};

    enum Phase { PLAY, OVER }

    private final IGameContext gameContext;
    private final Stats stats;
    @Getter
    private final CasseBriqueMap map;
    @Getter
    private final CasseBriqueLevel level;
    @Getter
    private final CasseBriqueBackground background;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int lives;
    @Getter
    private int broken;
    @Getter
    private int toBreak;

    private double width;
    private double height;
    private double top;

    private Group layer;
    private Group bricksLayer;
    private Group hud;
    private Rectangle area;
    /**
     * The light veil over the image of the user (variant "My images"), null without it.
     */
    @Getter
    private Rectangle veil;
    private Rectangle paddle;
    private Circle ball;

    @Getter
    private double paddleX;
    private double paddleWidth;
    private double paddleY;
    @Getter
    private double ballX;
    @Getter
    private double ballY;
    private double ballRadius;
    private double velocityX;
    private double velocityY;
    /**
     * Seconds before the ball leaves the paddle, 0 when it flies.
     */
    @Getter
    private double waiting;
    private double aimX;

    @Getter
    private final List<Brick> bricks = new ArrayList<>();
    private final EventHandler<Event> aimHandler = this::aim;
    private PauseTransition lostPause;

    private final AnimationTimer loop = new AnimationTimer() {
        private long last = -1;

        @Override
        public void handle(final long now) {
            final double seconds = last < 0 ? 0 : Math.min(0.05, (now - last) / 1e9);
            last = now;
            if (!Stats.isConfigMenuOpen()) {
                step(seconds);
            }
        }

        @Override
        public void stop() {
            super.stop();
            last = -1;
        }
    };
    private boolean manual;

    CasseBrique(final IGameContext gameContext, final Stats stats, final CasseBriqueMap map,
                final CasseBriqueLevel level, final CasseBriqueBackground background, final double gameSeed) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.map = map;
        this.level = level;
        this.background = background;
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
    }

    @Override
    public void launch() {
        gameContext.setLimiterAvailable();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();
        top = height * 0.1;

        layer = new Group();
        area = new Rectangle(0, 0, width, height);
        area.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(205, 195, 245)), new Stop(1, Color.rgb(195, 230, 250))));
        layer.getChildren().add(area);
        if (background == CasseBriqueBackground.MY_IMAGES) {
            final ImageView image = userImage();
            if (image != null) {
                layer.getChildren().add(image);
                // A light veil hides the image, also around the bricks; it fades as the bricks break (maintainer's
                // request of 3/11/2026: with a sparse drawing the image was seen almost whole from the start)
                veil = new Rectangle(0, 0, width, height);
                veil.setFill(Color.rgb(245, 242, 255));
                veil.setOpacity(VEIL_OPACITY);
                veil.setMouseTransparent(true);
                layer.getChildren().add(veil);
            }
        } else {
            // A few soft round shapes on the gradient
            for (int i = 0; i < 9; i++) {
                final Circle spot = new Circle(width * (0.05 + 0.11 * i), height * (0.25 + 0.5 * random.nextDouble()),
                    height * (0.06 + 0.08 * random.nextDouble()), Color.rgb(255, 255, 255, 0.18));
                spot.setMouseTransparent(true);
                layer.getChildren().add(spot);
            }
        }
        bricksLayer = new Group();
        bricksLayer.setMouseTransparent(true);
        hud = new Group();
        hud.setMouseTransparent(true);

        paddleWidth = width * level.getPaddleWidth();
        paddleY = height * 0.92;
        paddleX = width / 2;
        aimX = paddleX;
        paddle = new Rectangle(paddleWidth, height * 0.03);
        paddle.setArcWidth(height * 0.03);
        paddle.setArcHeight(height * 0.03);
        paddle.setStyle("-fx-fill: -gp-accent; -fx-stroke: white; -fx-stroke-width: 3;");
        paddle.setMouseTransparent(true);
        ballRadius = height * 0.018;
        ball = new Circle(ballRadius, Color.WHITE);
        ball.setStroke(Color.rgb(60, 55, 90));
        ball.setStrokeWidth(Math.max(2, ballRadius * 0.25));
        ball.setMouseTransparent(true);

        layer.getChildren().addAll(bricksLayer, paddle, ball, hud);
        gameContext.getChildren().add(layer);

        buildBricks();

        // The paddle goes where the gaze or the mouse is (as the setting of the inputs allows: with the gaze only, the
        // game area stops the mouse; it used to listen to the whole scene)
        gameContext.getGazeDeviceManager().addEventFilter(area);
        area.addEventFilter(GazeEvent.ANY, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_MOVED, aimHandler);
        layer.addEventFilter(MouseEvent.MOUSE_DRAGGED, aimHandler);

        lives = LIVES;
        waiting = FIRST_WAIT;
        gameContext.getGazeDeviceManager().addStats(stats);
        stats.notifyNewRoundReady();
        gameContext.firstStart();
        phase = Phase.PLAY;
        placePaddle();
        placeBallOnPaddle();
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    /**
     * @return an image of the user, covering the screen behind the bricks; null if there is none
     */
    private ImageView userImage() {
        final List<File> images = UserImagesVariant.images(CasseBriqueBackground.USER_IMAGES_FOLDER);
        if (images.isEmpty()) {
            return null;
        }
        final Image image = new Image(images.get(random.nextInt(images.size())).toURI().toString());
        final ImageView view = new ImageView(image);
        // Covers the screen, keeping its proportions (cut at the edges if needed)
        final double scale = Math.max(width / Math.max(1, image.getWidth()), height / Math.max(1, image.getHeight()));
        view.setFitWidth(image.getWidth() * scale);
        view.setFitHeight(image.getHeight() * scale);
        view.setLayoutX((width - view.getFitWidth()) / 2);
        view.setLayoutY((height - view.getFitHeight()) / 2);
        view.setMouseTransparent(true);
        return view;
    }

    /**
     * The bricks of the drawing, in the top 55 % of the screen under the lives and the score; in the hard level one
     * brick in three must be hit twice.
     */
    private void buildBricks() {
        final String[] rows = map.getRows();
        int columns = 0;
        for (final String row : rows) {
            columns = Math.max(columns, row.length());
        }
        final double cellWidth = width / columns;
        final double cellHeight = Math.min(height * 0.065, (height * 0.55) / rows.length);
        final double gap = Math.max(2, cellHeight * 0.08);
        for (int line = 0; line < rows.length; line++) {
            for (int column = 0; column < rows[line].length(); column++) {
                final char cell = rows[line].charAt(column);
                if (cell < '1' || cell > '7') {
                    continue;
                }
                final boolean strong = level.isStrongBricks() && (line + column) % 3 == 0;
                final Brick brick = new Brick(column * cellWidth + gap / 2, top + line * cellHeight + gap / 2,
                    cellWidth - gap, cellHeight - gap, COLORS[cell - '1'], strong ? 2 : 1);
                bricks.add(brick);
                bricksLayer.getChildren().add(brick.node);
            }
        }
        toBreak = bricks.size();
        for (int i = 0; i < toBreak; i++) {
            stats.incrementNumberOfGoalsToReach();
        }
    }

    private void aim(final Event event) {
        if (event instanceof GazeEvent gaze && (gaze.getEventType() == GazeEvent.GAZE_MOVED
            || gaze.getEventType() == GazeEvent.GAZE_ENTERED)) {
            aimAt(gaze.getX());
        } else if (event instanceof MouseEvent mouse) {
            aimAt(mouse.getX());
        }
    }

    /**
     * The place looked at (or of the mouse), where the paddle goes.
     */
    void aimAt(final double x) {
        aimX = x;
    }

    private void placePaddle() {
        paddleX = Math.max(paddleWidth / 2, Math.min(width - paddleWidth / 2, aimX));
        paddle.setX(paddleX - paddleWidth / 2);
        paddle.setY(paddleY - paddle.getHeight() / 2);
    }

    private void placeBallOnPaddle() {
        ballX = paddleX;
        ballY = paddleY - paddle.getHeight() / 2 - ballRadius;
        placeBall();
    }

    private void placeBall() {
        ball.setCenterX(ballX);
        ball.setCenterY(ballY);
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase != Phase.PLAY || seconds <= 0) {
            return;
        }
        final double ratio = Math.max(0.1, gameContext.getConfiguration().getAnimationSpeedRatio());
        final double dt = seconds * ratio;
        placePaddle();
        if (waiting > 0) {
            waiting -= dt;
            placeBallOnPaddle();
            if (waiting <= 0) {
                launchBall();
            }
            return;
        }
        // Small steps, so that the ball never goes through a brick
        final double distance = Math.hypot(velocityX, velocityY) * dt;
        final int steps = Math.max(1, (int) Math.ceil(distance / (ballRadius * 0.5)));
        for (int i = 0; i < steps && phase == Phase.PLAY && waiting <= 0; i++) {
            move(dt / steps);
        }
        placeBall();
    }

    /**
     * The ball leaves the paddle, upwards, a little to the side.
     */
    private void launchBall() {
        waiting = 0;
        final double angle = Math.toRadians(-20 + 40 * random.nextDouble());
        final double speed = level.getBallSpeed() * height;
        velocityX = speed * Math.sin(angle);
        velocityY = -speed * Math.cos(angle);
    }

    private void move(final double dt) {
        ballX += velocityX * dt;
        ballY += velocityY * dt;
        // The edges
        if (ballX - ballRadius < 0 && velocityX < 0 || ballX + ballRadius > width && velocityX > 0) {
            velocityX = -velocityX;
        }
        // The top of the screen (maintainer's remark of 3/11/2026: it used to bounce under the lives and the score,
        // which are drawn over the ball)
        if (ballY - ballRadius < 0 && velocityY < 0) {
            velocityY = -velocityY;
        }
        if (ballY - ballRadius > height) {
            fall();
            return;
        }
        // The paddle: the farther from its middle, the more to the side the ball goes back
        final double halfHeight = paddle.getHeight() / 2;
        if (velocityY > 0 && ballY + ballRadius >= paddleY - halfHeight && ballY < paddleY + halfHeight
            && ballX >= paddleX - paddleWidth / 2 - ballRadius && ballX <= paddleX + paddleWidth / 2 + ballRadius) {
            final double place = Math.max(-1, Math.min(1, (ballX - paddleX) / (paddleWidth / 2)));
            final double angle = Math.toRadians(60 * place);
            final double speed = Math.hypot(velocityX, velocityY);
            velocityX = speed * Math.sin(angle);
            velocityY = -speed * Math.cos(angle);
            ballY = paddleY - halfHeight - ballRadius;
        }
        // The bricks: the first one touched, the ball bounces on its nearest side
        for (final Brick brick : bricks) {
            if (brick.touches(ballX, ballY, ballRadius)) {
                bounceOn(brick);
                hit(brick);
                break;
            }
        }
    }

    private void bounceOn(final Brick brick) {
        final double overlapLeft = ballX + ballRadius - brick.x;
        final double overlapRight = brick.x + brick.width - (ballX - ballRadius);
        final double overlapTop = ballY + ballRadius - brick.y;
        final double overlapBottom = brick.y + brick.height - (ballY - ballRadius);
        final double horizontal = Math.min(overlapLeft, overlapRight);
        final double vertical = Math.min(overlapTop, overlapBottom);
        if (horizontal < vertical) {
            velocityX = overlapLeft < overlapRight ? -Math.abs(velocityX) : Math.abs(velocityX);
        } else {
            velocityY = overlapTop < overlapBottom ? -Math.abs(velocityY) : Math.abs(velocityY);
        }
    }

    /**
     * A brick hit: it cracks (a hard one, the first time) or breaks with a flash; all broken, "Bravo".
     */
    void hit(final Brick brick) {
        if (phase != Phase.PLAY || !bricks.contains(brick)) {
            return;
        }
        brick.hits--;
        if (brick.hits > 0) {
            brick.crack();
            return;
        }
        bricks.remove(brick);
        brick.burst();
        broken++;
        if (veil != null) {
            veil.setOpacity(VEIL_OPACITY * (1 - broken / (double) Math.max(1, toBreak)));
        }
        stats.incrementNumberOfGoalsReached();
        gameContext.updateScore(stats, this);
        showHud();
        if (bricks.isEmpty()) {
            win();
        }
    }

    /**
     * The ball fallen: one life less; the ball comes back on the paddle, or "Lost!" after the third.
     */
    void fall() {
        if (phase != Phase.PLAY) {
            return;
        }
        lives--;
        showHud();
        if (lives <= 0) {
            lose();
            return;
        }
        waiting = WAIT_AFTER_FALL;
        velocityX = 0;
        velocityY = 0;
        placeBallOnPaddle();
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final double margin = height * 0.025;
        final double heart = height * 0.055;
        for (int i = 0; i < LIVES; i++) {
            final SVGPath icon = GameStyle.icon(i < lives ? "favorite_fill1" : "favorite", heart);
            icon.setFill(i < lives ? Color.rgb(235, 70, 95) : Color.rgb(120, 120, 130, 0.8));
            icon.setStroke(Color.WHITE);
            icon.setStrokeWidth(18);
            icon.setLayoutX(margin + i * heart * 1.1);
            icon.setLayoutY(margin * 0.6);
            hud.getChildren().add(icon);
        }
        final Group pill = GameStyle.pill(gameContext.getTranslator().translate("CasseBriqueScore") + " " + broken
            + " / " + toBreak, fontSize);
        pill.setLayoutX(width - pill.getBoundsInLocal().getWidth() - margin);
        pill.setLayoutY(margin * 0.6);
        hud.getChildren().add(pill);
    }

    private void win() {
        if (phase != Phase.PLAY) {
            return;
        }
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        gameContext.updateScore(stats, this);
        gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
    }

    private void lose() {
        if (phase != Phase.PLAY) {
            return;
        }
        phase = Phase.OVER;
        loop.stop();
        ball.setVisible(false);
        stats.takeScreenShot();
        gameContext.updateScore(stats, this);
        final Group card = GameStyle.card(gameContext.getTranslator().translate("CasseBriqueLost"), height * 0.06,
            width * 0.6);
        card.setLayoutX(width / 2 - card.getBoundsInLocal().getWidth() / 2);
        card.setLayoutY(height / 2 - card.getBoundsInLocal().getHeight() / 2);
        hud.getChildren().add(card);
        lostPause = new PauseTransition(Duration.seconds(2));
        lostPause.setOnFinished(event -> gameContext.showRoundStats(stats, this));
        lostPause.play();
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    /**
     * For the tests: the ball at a place, going a way (heights of the screen per second).
     */
    void throwBall(final double x, final double y, final double vx, final double vy) {
        waiting = 0;
        ballX = x;
        ballY = y;
        velocityX = vx * height;
        velocityY = vy * height;
        placeBall();
    }

    double getHeight() {
        return height;
    }

    double getWidth() {
        return width;
    }

    double getPaddleWidth() {
        return paddleWidth;
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (lostPause != null) {
            lostPause.stop();
        }
        if (area != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(area);
        }
        bricks.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }

    /**
     * A brick: a rounded block of a soft color, with a light top; a hard one is darker and cracks at its first hit.
     */
    final class Brick {
        @Getter
        private final double x;
        @Getter
        private final double y;
        @Getter
        private final double width;
        private final double height;
        @Getter
        private int hits;
        private final Group node;
        private final Rectangle body;
        private final Rectangle crack;

        Brick(final double x, final double y, final double width, final double height, final Color color,
              final int hits) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.hits = hits;
            final double arc = Math.min(width, height) * 0.45;
            body = new Rectangle(x, y, width, height);
            body.setArcWidth(arc);
            body.setArcHeight(arc);
            body.setFill(hits > 1 ? color.darker() : color);
            body.setStroke(Color.rgb(255, 255, 255, 0.85));
            body.setStrokeWidth(Math.max(1.5, height * 0.06));
            final Rectangle light = new Rectangle(x + width * 0.12, y + height * 0.14, width * 0.76, height * 0.22);
            light.setArcWidth(height * 0.22);
            light.setArcHeight(height * 0.22);
            light.setFill(Color.rgb(255, 255, 255, 0.35));
            crack = new Rectangle(x + width * 0.48, y + height * 0.12, Math.max(2, width * 0.04), height * 0.76);
            crack.setFill(Color.rgb(255, 255, 255, 0.85));
            crack.setRotate(18);
            crack.setVisible(false);
            node = new Group(body, light, crack);
        }

        boolean touches(final double cx, final double cy, final double radius) {
            final double nearestX = Math.max(x, Math.min(cx, x + width));
            final double nearestY = Math.max(y, Math.min(cy, y + height));
            return Math.hypot(cx - nearestX, cy - nearestY) <= radius;
        }

        void crack() {
            crack.setVisible(true);
            body.setFill(((Color) body.getFill()).brighter());
        }

        void burst() {
            final ScaleTransition pop = new ScaleTransition(Duration.millis(220), node);
            pop.setToX(1.25);
            pop.setToY(1.6);
            final FadeTransition fade = new FadeTransition(Duration.millis(260), node);
            fade.setToValue(0);
            fade.setOnFinished(event -> bricksLayer.getChildren().remove(node));
            pop.play();
            fade.play();
        }
    }
}
