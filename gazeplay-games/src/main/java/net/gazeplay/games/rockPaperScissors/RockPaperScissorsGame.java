package net.gazeplay.games.rockPaperScissors;

import javafx.animation.AnimationTimer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import lombok.Getter;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * "Pierre papier ciseaux" (Rock Paper Scissors; reviewed on 10/11/2026, maintainer's rules). Three cards at the bottom,
 * a rock, a paper and scissors (emoji of OpenMoji) with their names; a fixation on a card plays its sign.
 * <ul>
 * <li>Against the computer: after the choice, "Rock... paper... scissors!" while both fists swing, then both signs are
 * shown; the winning hand grows, a message tells who won the round. The first to win 2, 3 or 5 rounds wins ("Bravo" for
 * the player; the statistics at once if the computer wins), or never.</li>
 * <li>Learning the rule: the sign of the computer is shown; choose the one that beats it. A wrong choice tells why (the
 * paper wraps the rock...) and its card is greyed; try again. The game ends after 5 or 10 right answers ("Bravo"), or
 * never.</li>
 * </ul>
 * Everything but the hands is drawn by the game. One loop, in real seconds: the fixations last the length of the
 * settings (read at each step), nothing can be chosen before the delay of the settings nor while the round is shown,
 * and nothing moves while the menu of the game is open. (Before the review: the computer could not win a match,
 * "Bravo" after each round of the sign shown, a goal counted for each round even lost, cartoon gloves of unknown
 * source.)
 */
public class RockPaperScissorsGame implements GameLifeCycle {

    /**
     * In seconds (at the speed 1 of the settings): each word of "rock, paper, scissors!", the round shown, the end
     * shown, the explanation of a wrong choice.
     */
    static final double WORD = 0.5;
    static final double REVEAL = 1.8;
    static final double SHOW = 2.5;
    static final double EXPLAIN = 2.5;

    private static final String SOUNDS = "data/rockPaperScissors/sounds/";
    private static final Color BACKGROUND = Color.rgb(232, 236, 244);

    enum Phase { WAIT, PLAY, COUNT, REVEAL, SHOW, OVER }

    private final IGameContext gameContext;
    private final Configuration configuration;
    private final Stats stats;
    @Getter
    private final RockPaperScissorsMode mode;
    @Getter
    private final RockPaperScissorsEnd end;
    private final ReplayablePseudoRandom random;

    @Getter
    private Phase phase = Phase.OVER;
    @Getter
    private int playerWins;
    @Getter
    private int computerWins;
    @Getter
    private int rightAnswers;
    @Getter
    private HandSign playerSign;
    @Getter
    private HandSign computerSign;
    private boolean firstTry;
    private final List<HandSign> greyed = new ArrayList<>();
    private boolean won;
    private double elapsed;
    private double explaining;
    @Getter
    private double waiting;

    private double width;
    private double height;
    private double cardWidth;
    private double cardHeight;
    private double handSize;

    private Group layer;
    private final Map<HandSign, Group> cards = new EnumMap<>(HandSign.class);
    private final Map<HandSign, Rectangle> cardBacks = new EnumMap<>(HandSign.class);
    private final Map<HandSign, Image> images = new EnumMap<>(HandSign.class);
    private Group playerSlot;
    private Group computerSlot;
    private Group message;
    private FixationRing ring;
    private Group hud;
    private final List<Node> gazeNodes = new ArrayList<>();

    private HandSign looked;
    @Getter
    private double progress;

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

    /**
     * @param gameSeed the seed of the game to replay, or a negative value for a new game
     */
    RockPaperScissorsGame(final IGameContext gameContext, final Stats stats, final RockPaperScissorsMode mode,
                          final RockPaperScissorsEnd end, final double gameSeed) {
        this.gameContext = gameContext;
        this.configuration = gameContext.getConfiguration();
        this.stats = stats;
        this.mode = mode;
        this.end = end.isFor(mode) ? end : (mode == RockPaperScissorsMode.LEARN ? RockPaperScissorsEnd.ANSWERS_5
            : RockPaperScissorsEnd.WINS_3);
        if (gameSeed < 0) {
            random = new ReplayablePseudoRandom();
            stats.setGameSeed(random.getSeed());
        } else {
            random = new ReplayablePseudoRandom(gameSeed);
        }
    }

    @Override
    public void launch() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        width = dimension.getWidth();
        height = dimension.getHeight();
        handSize = Math.min(height * 0.26, width * 0.18);
        cardHeight = Math.min(height * 0.36, width * 0.28);
        cardWidth = cardHeight * 0.85;

        final Rectangle background = new Rectangle(0, 0, width, height);
        background.setFill(BACKGROUND);
        background.setMouseTransparent(true);
        for (final HandSign sign : HandSign.values()) {
            images.put(sign, sign.getImage());
        }
        final Group cardsLayer = new Group();
        final double gap = cardWidth * 0.3;
        final double left = (width - 3 * cardWidth - 2 * gap) / 2;
        final double cardY = height - cardHeight - height * 0.05;
        for (final HandSign sign : HandSign.values()) {
            final Group card = card(sign);
            card.setLayoutX(left + sign.ordinal() * (cardWidth + gap));
            card.setLayoutY(cardY);
            cards.put(sign, card);
            cardsLayer.getChildren().add(card);
            listen(card, () -> lookAt(sign), () -> lookAway(sign));
        }
        final double slotY = height * 0.13 + handSize * 0.75;
        playerSlot = new Group();
        playerSlot.setLayoutX(width * 0.3);
        playerSlot.setLayoutY(slotY);
        computerSlot = new Group();
        computerSlot.setLayoutX(width * 0.7);
        computerSlot.setLayoutY(slotY);
        final Text playerName = label(gameContext.getTranslator().translate("RpsYou"), height * 0.035);
        playerName.setLayoutX(width * 0.3 - playerName.getLayoutBounds().getWidth() / 2);
        // Above a hand grown to 1.25 (half of it: 0.63)
        playerName.setLayoutY(slotY - handSize * 0.8);
        final Text computerName = label(gameContext.getTranslator().translate("RpsComputer"), height * 0.035);
        computerName.setLayoutX(width * 0.7 - computerName.getLayoutBounds().getWidth() / 2);
        computerName.setLayoutY(slotY - handSize * 0.8);
        message = new Group();
        message.setMouseTransparent(true);
        ring = new FixationRing();
        hud = new Group();
        hud.setMouseTransparent(true);
        layer = new Group(background, playerName, computerName, playerSlot, computerSlot, cardsLayer, message, ring,
            hud);
        gameContext.getChildren().add(layer);

        gameContext.getGazeDeviceManager().addStats(stats);
        playerWins = 0;
        computerWins = 0;
        rightAnswers = 0;
        elapsed = 0;
        waiting = configuration.getGameStartDelay() / 1000.0;
        phase = Phase.WAIT;
        showSlot(playerSlot, null, 1);
        showSlot(computerSlot, null, 1);
        paintCards();
        showHud();
        if (!manual) {
            loop.start();
        }
    }

    private Text label(final String content, final double size) {
        final Text text = new Text(content);
        text.setFont(GameStyle.titleFont(size));
        text.setStyle(GameStyle.titleFontStyle(size) + " -fx-fill: -gp-text;");
        text.setTextOrigin(VPos.CENTER);
        text.setMouseTransparent(true);
        return text;
    }

    /**
     * @return a card of a sign: a rounded white card, the hand and its name
     */
    private Group card(final HandSign sign) {
        final Rectangle back = new Rectangle(cardWidth, cardHeight);
        back.setArcWidth(cardWidth * 0.18);
        back.setArcHeight(cardWidth * 0.18);
        back.setStrokeWidth(Math.max(2, cardWidth * 0.02));
        cardBacks.put(sign, back);
        final ImageView hand = new ImageView(images.get(sign));
        hand.setFitWidth(cardWidth * 0.72);
        hand.setFitHeight(cardWidth * 0.72);
        hand.setLayoutX(cardWidth * 0.14);
        hand.setLayoutY(cardHeight * 0.08);
        hand.setMouseTransparent(true);
        final Text name = label(gameContext.getTranslator().translate(sign.getKey()), cardHeight * 0.11);
        name.setLayoutX((cardWidth - name.getLayoutBounds().getWidth()) / 2);
        name.setLayoutY(cardHeight * 0.86);
        return new Group(back, hand, name);
    }

    private void paintCards() {
        for (final HandSign sign : HandSign.values()) {
            final boolean off = greyed.contains(sign) || phase != Phase.PLAY;
            final boolean lit = sign == looked && !off;
            cardBacks.get(sign).setFill(lit ? Color.rgb(255, 238, 170) : Color.WHITE);
            cardBacks.get(sign).setStroke(Color.rgb(196, 204, 220));
            cards.get(sign).setOpacity(greyed.contains(sign) ? 0.35 : 1);
        }
    }

    /**
     * Shows a sign in a slot (null: a question mark), at a scale, turned by an angle.
     */
    private void showSlot(final Group slot, final HandSign sign, final double scale) {
        showSlot(slot, sign, scale, 0);
    }

    private void showSlot(final Group slot, final HandSign sign, final double scale, final double angle) {
        final Node content;
        if (sign == null) {
            final Circle disc = new Circle(handSize * 0.42, Color.rgb(206, 212, 226));
            final Text mark = label("?", handSize * 0.5);
            mark.setLayoutX(-mark.getLayoutBounds().getWidth() / 2);
            mark.setLayoutY(handSize * 0.02);
            content = new Group(disc, mark);
        } else {
            final ImageView hand = new ImageView(images.get(sign));
            hand.setFitWidth(handSize);
            hand.setFitHeight(handSize);
            hand.setLayoutX(-handSize / 2);
            hand.setLayoutY(-handSize / 2);
            content = hand;
        }
        content.setScaleX(scale);
        content.setScaleY(scale);
        content.setRotate(angle);
        slot.getChildren().setAll(content);
    }

    private void say(final String text, final double size) {
        final Group pill = GameStyle.pill(text, size);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        // Under the hands (the explanations of the rule are long), above the cards
        pill.setLayoutY(playerSlot.getLayoutY() + handSize * 0.8 - pill.getBoundsInLocal().getHeight() / 2);
        message.getChildren().setAll(pill);
    }

    /**
     * Makes a node receive the gaze and the mouse.
     */
    private void listen(final Node node, final Runnable enter, final Runnable exit) {
        final EventHandler<Event> handler = event -> {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED || event.getEventType() == GazeEvent.GAZE_ENTERED) {
                enter.run();
            } else if (event.getEventType() == MouseEvent.MOUSE_EXITED
                || event.getEventType() == GazeEvent.GAZE_EXITED) {
                exit.run();
            }
        };
        node.addEventFilter(MouseEvent.ANY, handler);
        node.addEventFilter(GazeEvent.ANY, handler);
        gameContext.getGazeDeviceManager().addEventFilter(node);
        gazeNodes.add(node);
    }

    /**
     * A new round: against the computer, both signs hidden; learning, the sign of the computer shown.
     */
    private void newRound() {
        greyed.clear();
        playerSign = null;
        firstTry = true;
        explaining = 0;
        stats.notifyNewRoundReady();
        stats.incrementNumberOfGoalsToReach();
        showSlot(playerSlot, null, 1);
        if (mode == RockPaperScissorsMode.LEARN) {
            computerSign = HandSign.values()[random.nextInt(3)];
            showSlot(computerSlot, computerSign, 1);
            say(gameContext.getTranslator().translate("RpsQuestion"), height * 0.04);
        } else {
            computerSign = null;
            showSlot(computerSlot, null, 1);
            message.getChildren().clear();
        }
        phase = Phase.PLAY;
        elapsed = 0;
        showLooked();
        paintCards();
        showHud();
    }

    /**
     * One step of the game, of a duration in seconds.
     */
    void step(final double seconds) {
        if (phase == Phase.OVER || seconds <= 0) {
            return;
        }
        final double scaled = seconds * Math.max(0.1, configuration.getAnimationSpeedRatio());
        switch (phase) {
            case WAIT -> {
                elapsed += seconds;
                if (elapsed >= waiting) {
                    newRound();
                }
            }
            case PLAY -> {
                fixate(seconds);
                if (explaining > 0) {
                    explaining -= seconds;
                    if (explaining <= 0 && phase == Phase.PLAY) {
                        say(gameContext.getTranslator().translate("RpsQuestion"), height * 0.04);
                    }
                }
            }
            case COUNT -> {
                final int before = (int) (elapsed / WORD);
                elapsed += scaled;
                final int word = (int) (elapsed / WORD);
                // Both fists swing, up and down
                final double angle = Math.sin(elapsed / WORD * Math.PI * 2) * 18;
                showSlot(playerSlot, HandSign.ROCK, 1, angle);
                showSlot(computerSlot, HandSign.ROCK, 1, -angle);
                if (word != before && word < 3) {
                    say(gameContext.getTranslator().translate("RpsCount" + (word + 1)), height * 0.05);
                    gameContext.getSoundManager().add(SOUNDS + "tick.wav");
                }
                if (elapsed >= 3 * WORD) {
                    reveal();
                }
            }
            case REVEAL -> {
                elapsed += scaled;
                if (elapsed >= REVEAL) {
                    afterRound();
                }
            }
            case SHOW -> {
                elapsed += seconds;
                if (elapsed >= SHOW) {
                    finish();
                }
            }
            default -> {
            }
        }
    }

    private void fixate(final double seconds) {
        if (looked == null || greyed.contains(looked)) {
            return;
        }
        if (!ring.isVisible()) {
            showLooked();
        }
        progress += seconds * 1000 / Math.max(1, configuration.getFixationLength());
        ring.setProgress(progress);
        if (progress >= 1) {
            final HandSign sign = looked;
            progress = 0;
            ring.stop();
            choose(sign);
        }
    }

    void lookAt(final HandSign sign) {
        if (looked == sign) {
            return;
        }
        looked = sign;
        progress = 0;
        ring.stop();
        showLooked();
        paintCards();
    }

    void lookAway(final HandSign sign) {
        if (looked == sign) {
            looked = null;
            progress = 0;
            ring.stop();
            paintCards();
        }
    }

    private void showLooked() {
        if (looked != null && phase == Phase.PLAY && !greyed.contains(looked)) {
            final Group card = cards.get(looked);
            ring.setSize(cardWidth * 0.62);
            ring.show(card.getLayoutX() + cardWidth / 2, card.getLayoutY() + cardHeight * 0.44);
            ring.setProgress(progress);
        } else {
            ring.stop();
        }
    }

    /**
     * A sign chosen by the player.
     */
    void choose(final HandSign sign) {
        if (phase != Phase.PLAY || greyed.contains(sign)) {
            return;
        }
        playerSign = sign;
        if (mode == RockPaperScissorsMode.VERSUS) {
            phase = Phase.COUNT;
            elapsed = 0;
            ring.stop();
            say(gameContext.getTranslator().translate("RpsCount1"), height * 0.05);
            gameContext.getSoundManager().add(SOUNDS + "tick.wav");
            paintCards();
            return;
        }
        // Learning the rule
        showSlot(playerSlot, sign, 1);
        final int result = sign.fight(computerSign);
        if (result > 0) {
            rightAnswers++;
            if (firstTry) {
                stats.incrementNumberOfGoalsReached();
            }
            gameContext.getSoundManager().add(SOUNDS + "win.wav");
            showSlot(playerSlot, sign, 1.25);
            showSlot(computerSlot, computerSign, 0.8);
            say(gameContext.getTranslator().translate(sign.ruleKey()), height * 0.04);
            phase = Phase.REVEAL;
            elapsed = 0;
            ring.stop();
        } else {
            firstTry = false;
            greyed.add(sign);
            gameContext.getSoundManager().add(SOUNDS + "lose.wav");
            say(gameContext.getTranslator().translate(result == 0 ? "RpsSameSign" : computerSign.ruleKey()),
                height * 0.04);
            explaining = EXPLAIN;
            looked = null;
            ring.stop();
        }
        paintCards();
        showHud();
    }

    /**
     * Against the computer: both signs shown, the winning hand grows.
     */
    private void reveal() {
        computerSign = HandSign.values()[random.nextInt(3)];
        final int result = playerSign.fight(computerSign);
        showSlot(playerSlot, playerSign, result > 0 ? 1.25 : result < 0 ? 0.8 : 1);
        showSlot(computerSlot, computerSign, result < 0 ? 1.25 : result > 0 ? 0.8 : 1);
        if (result > 0) {
            playerWins++;
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add(SOUNDS + "win.wav");
            say(gameContext.getTranslator().translate("RpsWin"), height * 0.05);
        } else if (result < 0) {
            computerWins++;
            gameContext.getSoundManager().add(SOUNDS + "lose.wav");
            say(gameContext.getTranslator().translate("RpsLose"), height * 0.05);
        } else {
            gameContext.getSoundManager().add(SOUNDS + "draw.wav");
            say(gameContext.getTranslator().translate("RpsDraw"), height * 0.05);
        }
        phase = Phase.REVEAL;
        elapsed = 0;
        showHud();
    }

    private void afterRound() {
        if (mode == RockPaperScissorsMode.VERSUS && end.getCount() > 0) {
            if (playerWins >= end.getCount()) {
                won = true;
                endGame("RpsYouWinMatch");
                return;
            }
            if (computerWins >= end.getCount()) {
                won = false;
                endGame("RpsComputerWinsMatch");
                return;
            }
        }
        if (mode == RockPaperScissorsMode.LEARN && end.getCount() > 0 && rightAnswers >= end.getCount()) {
            won = true;
            finish();
            return;
        }
        newRound();
    }

    private void endGame(final String key) {
        say(gameContext.getTranslator().translate(key), height * 0.05);
        phase = Phase.SHOW;
        elapsed = 0;
        paintCards();
    }

    private void finish() {
        phase = Phase.OVER;
        loop.stop();
        stats.takeScreenShot();
        if (won) {
            gameContext.playWinTransition(500, event -> gameContext.showRoundStats(stats, this));
        } else {
            gameContext.showRoundStats(stats, this);
        }
    }

    boolean isWon() {
        return won;
    }

    boolean isGreyed(final HandSign sign) {
        return greyed.contains(sign);
    }

    private void showHud() {
        hud.getChildren().clear();
        final double fontSize = height * 0.035;
        final String text;
        if (mode == RockPaperScissorsMode.VERSUS) {
            text = gameContext.getTranslator().translate("RpsYou") + " " + playerWins + " – " + computerWins + " "
                + gameContext.getTranslator().translate("RpsComputer");
        } else {
            text = gameContext.getTranslator().translate("RpsAnswers") + " " + rightAnswers
                + (end.getCount() > 0 ? " / " + end.getCount() : "");
        }
        final Group pill = GameStyle.pill(text, fontSize);
        pill.setLayoutX((width - pill.getBoundsInLocal().getWidth()) / 2);
        pill.setLayoutY(height * 0.02);
        hud.getChildren().add(pill);
    }

    /**
     * For the tests: the loop does not run, {@link #step(double)} is called instead.
     */
    void setManual() {
        manual = true;
        loop.stop();
    }

    /**
     * @return the card of a sign on the screen (left, top, right, bottom)
     */
    double[] cardBounds(final HandSign sign) {
        final Group card = cards.get(sign);
        return new double[]{card.getLayoutX(), card.getLayoutY(), card.getLayoutX() + cardWidth,
            card.getLayoutY() + cardHeight};
    }

    @Override
    public void dispose() {
        phase = Phase.OVER;
        loop.stop();
        if (ring != null) {
            ring.stop();
        }
        for (final Node node : new ArrayList<>(gazeNodes)) {
            gameContext.getGazeDeviceManager().removeEventFilter(node);
        }
        gazeNodes.clear();
        if (layer != null) {
            gameContext.getChildren().remove(layer);
            layer = null;
        }
    }
}
