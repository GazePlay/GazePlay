package net.gazeplay.games.pet;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.FixationRing;
import net.gazeplay.components.GameStyle;
import net.gazeplay.components.GazeRoundButton;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Game "Pet" (reviewed on 30/10/2026): the player takes care of a Biboule. Three gauges (hunger, cleanliness, play)
 * empty in {@link #EMPTYING}; under a quarter, the gauge blinks, the Biboule is sad and a bubble shows what it wants.
 * The round buttons on the left open a room, where an object follows the gaze (or the mouse): the hand in the living
 * room (stroking makes it happy), the spoon in the kitchen (look at the bowl to fill it, then bring it to the mouth),
 * the shower in the bathroom (water on the Biboule washes it), the ball in the garden (the Biboule flies; touch it with
 * the ball to play). Each activity done fills its gauge by a quarter. The game does not end.
 * <p>
 * Tester's feedback of 24/11/2026, point 80 (maintainer's choices of 27/11/2026): sounds support what works (a chirp
 * when the Biboule is stroked, a crunch at each bite, the water of the shower, a "boing" when a toy touches it, three
 * bell notes when a gauge gains a quarter); four dishes on the table of the kitchen (the bowl, an apple, a banana, a
 * cake: the gaze on one takes it, then on the mouth the Biboule eats it, all filling the gauge alike); three toys in the
 * garden (the ball, juggling balls, a hoop), one taken by a fixation on it, each with its own game.
 */
@Slf4j
public class PetHouse implements GameLifeCycle {

    /**
     * How long a full gauge takes to empty.
     */
    static final Duration EMPTYING = Duration.minutes(2);

    /**
     * Under this level, a need is low.
     */
    static final double LOW = 0.25;

    /**
     * How much an activity fills its gauge.
     */
    static final double GAIN = 0.25;

    /**
     * How long the water must fall on the Biboule to wash it.
     */
    static final Duration WASHING = Duration.seconds(3);

    private static final String FOLDER = "data/pet/images/";
    static final String SOUNDS = "data/pet/sounds/";
    /**
     * The ticks between two sounds of the water of the shower.
     */
    private static final int SPLASH_TICKS = 6;

    /**
     * The dishes of the kitchen (point 80): the picture on the table, and what the gaze carries to the mouth.
     */
    @RequiredArgsConstructor
    enum Food {
        PUREE("bowl.png", "spoon-full.png"),
        APPLE("apple.png", "apple.png"),
        BANANA("banana.png", "banana.png"),
        CAKE("cake.png", "cake.png");

        final String dish;
        final String held;
    }

    /**
     * The toys of the garden (point 80).
     */
    @RequiredArgsConstructor
    enum Toy {
        BALL("ball.png"),
        JUGGLING("juggling.png"),
        HOOP("hoop.png");

        final String picture;
    }
    private static final Duration TICK = Duration.millis(100);

    /**
     * The needs of the pet: name, icon, color of the gauge.
     */
    @RequiredArgsConstructor
    enum Need {
        HUNGER("PetHunger", "restaurant", "#F08A24"),
        CLEANLINESS("PetCleanliness", "shower_fill1", "#2F8FDD"),
        PLAY("PetPlay", "sports_soccer_fill1", GameStyle.SUCCESS_COLOR);

        final String label;
        final String icon;
        final String color;
    }

    /**
     * The rooms: name, icon of the button, picture, object following the gaze and where the gaze is on it (fraction of
     * its size), need filled there.
     */
    @RequiredArgsConstructor
    enum Room {
        HOME("PetHome", "home_fill1", "room.png", "hand.png", 0.5, 0.5, null),
        KITCHEN("PetKitchen", "restaurant", "kitchen.png", "spoon.png", 0.5, 0.135, Need.HUNGER),
        BATHROOM("PetBathroom", "shower_fill1", "bathroom.png", "shower.png", 0.45, 0.45, Need.CLEANLINESS),
        GARDEN("PetGarden", "sports_soccer_fill1", "garden.png", "ball.png", 0.5, 0.5, Need.PLAY);

        final String label;
        final String icon;
        final String picture;
        final String object;
        final double hotX;
        final double hotY;
        final Need need;
    }

    @Getter
    private final IGameContext gameContext;

    @Getter
    private final Stats stats;

    private final ReplayablePseudoRandom random;

    /**
     * The level of each need, from 0 (empty) to 1 (full).
     */
    @Getter
    private final Map<Need, Double> levels = new EnumMap<>(Need.class);

    private final Map<Need, Rectangle> gaugeFills = new EnumMap<>(Need.class);
    private final Map<Need, FadeTransition> gaugeBlinks = new EnumMap<>(Need.class);
    private final Map<Need, Double> barWidths = new EnumMap<>(Need.class);
    private final Map<Room, GazeRoundButton> buttons = new EnumMap<>(Room.class);
    private final List<Animation> animations = new ArrayList<>();

    private Group layer;
    private Rectangle roomView;

    @Getter
    private Mypet pet;

    private ImageView cursor;
    @Getter
    private ImageView bowl;
    private final Map<Food, ImageView> dishes = new EnumMap<>(Food.class);
    @Getter
    private Food heldFood;
    private final Map<Toy, Group> toyCards = new EnumMap<>(Toy.class);
    private final Map<Toy, Circle> toyDiscs = new EnumMap<>(Toy.class);
    @Getter
    private Toy toy = Toy.BALL;
    private Toy lookedToy;
    private double toyProgress;
    private FixationRing toyRing;
    private int splashTicks;
    private Group bubble;
    private SVGPath bubbleIcon;
    private Timeline ticker;
    private Timeline flight;
    private double gaugeWidth;
    private double objectSize;

    @Getter
    private Room room;

    /**
     * Where the gaze (or the mouse) is on the room, in the coordinates of the game; null when it is not on the room.
     */
    private Point2D pointer;

    private boolean stroking;
    private double washed;
    private boolean washing;
    private boolean playing;

    PetHouse(final IGameContext gameContext, final Stats stats) {
        this(gameContext, stats, new ReplayablePseudoRandom());
        this.stats.setGameSeed(random.getSeed());
    }

    PetHouse(final IGameContext gameContext, final Stats stats, final double gameSeed) {
        this(gameContext, stats, new ReplayablePseudoRandom(gameSeed));
    }

    private PetHouse(final IGameContext gameContext, final Stats stats, final ReplayablePseudoRandom random) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.random = random;
    }

    @Override
    public void launch() {
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double width = dimension.getWidth();
        final double height = dimension.getHeight();
        for (final Need need : Need.values()) {
            levels.put(need, 1.0);
        }

        final Rectangle background = new Rectangle(width, height);
        background.setStyle("-fx-fill: -gp-bg;");
        background.setMouseTransparent(true);
        layer = new Group(background);
        gameContext.getChildren().add(layer);

        // The buttons of the rooms, in a column on the left
        final double margin = height * 0.03;
        final double buttonSize = height * 0.14;
        final double buttonGap = (height - 4 * buttonSize) / 5;
        for (final Room each : Room.values()) {
            final GazeRoundButton button = new GazeRoundButton(gameContext, buttonSize, each.icon,
                gameContext.getTranslator().translate(each.label), () -> open(each));
            button.setLayoutX(margin);
            button.setLayoutY(buttonGap + each.ordinal() * (buttonSize + buttonGap));
            buttons.put(each, button);
            layer.getChildren().add(button);
        }

        // The room, as big as the space left allows (4:3), under the gauges
        final double gaugeBand = height * 0.12;
        final double left = 2 * margin + buttonSize + margin;
        final double availableWidth = width - left - margin;
        final double availableHeight = height - gaugeBand - 2 * margin;
        final double roomWidth = Math.min(availableWidth, availableHeight * 4 / 3);
        final double roomHeight = roomWidth * 3 / 4;
        final double roomX = left + (availableWidth - roomWidth) / 2;
        final double roomY = margin + gaugeBand;
        roomView = new Rectangle(roomX, roomY, roomWidth, roomHeight);
        roomView.setArcWidth(2 * GameStyle.cardRadius(roomWidth, roomHeight));
        roomView.setArcHeight(roomView.getArcWidth());
        roomView.setStroke(Color.web("#00000033"));
        roomView.setStrokeWidth(3);
        layer.getChildren().add(roomView);
        listenToThePointer();

        createGauges(roomX, margin, roomWidth, gaugeBand);

        pet = new Mypet(roomWidth * 0.24, random);
        layer.getChildren().add(pet);
        createBubble();

        objectSize = roomWidth * 0.16;
        cursor = new ImageView();
        cursor.setFitWidth(objectSize);
        cursor.setFitHeight(objectSize);
        cursor.setMouseTransparent(true);
        cursor.setVisible(false);

        // The dishes on the table, side by side from the left (point 80)
        double dishX = roomX + roomWidth * 0.05;
        for (final Food food : Food.values()) {
            final ImageView dish = new ImageView(new Image(FOLDER + food.dish));
            final double size = objectSize * (food == Food.PUREE ? 1.2 : 0.95);
            dish.setFitWidth(size);
            dish.setFitHeight(size);
            dish.setPreserveRatio(true);
            dish.setLayoutX(dishX);
            dish.setLayoutY(roomY + roomHeight - size - roomHeight * 0.04);
            dish.setMouseTransparent(true);
            dishes.put(food, dish);
            layer.getChildren().add(dish);
            dishX += size + objectSize * 0.3;
        }
        bowl = dishes.get(Food.PUREE);

        // The toys of the garden, on white discs at the bottom right, apart (point 80)
        final double discRadius = objectSize * 0.45;
        for (final Toy each : Toy.values()) {
            final double cx = roomX + roomWidth * (0.56 + 0.16 * each.ordinal());
            final double cy = roomY + roomHeight - discRadius * 1.25;
            final Circle disc = new Circle(cx, cy, discRadius, Color.rgb(255, 255, 255, 0.9));
            disc.setStroke(Color.web("#00000055"));
            disc.setStrokeWidth(Math.max(2, discRadius * 0.06));
            final ImageView picture = new ImageView(new Image(FOLDER + each.picture));
            picture.setFitWidth(discRadius * 1.3);
            picture.setFitHeight(discRadius * 1.3);
            picture.setLayoutX(cx - discRadius * 0.65);
            picture.setLayoutY(cy - discRadius * 0.65);
            final Group card = new Group(disc, picture);
            card.setMouseTransparent(true);
            toyCards.put(each, card);
            toyDiscs.put(each, disc);
            layer.getChildren().add(card);
        }
        toyRing = new FixationRing();
        toyRing.setSize(discRadius * 2.5);
        layer.getChildren().addAll(toyRing, cursor);

        ticker = new Timeline(new KeyFrame(TICK, event -> tick()));
        ticker.setCycleCount(Animation.INDEFINITE);
        ticker.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        ticker.play();

        open(Room.HOME);

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Gauges and bubble

    private void createGauges(final double x, final double y, final double width, final double band) {
        final double gap = width * 0.04;
        gaugeWidth = (width - 2 * gap) / 3;
        final double iconSize = band * 0.42;
        final double fontSize = band * 0.24;
        final double barHeight = band * 0.24;
        for (final Need need : Need.values()) {
            final double gaugeX = x + need.ordinal() * (gaugeWidth + gap);
            final SVGPath icon = GameStyle.icon(need.icon, iconSize);
            icon.setStyle("-fx-fill: " + need.color + ";");
            final Group iconGroup = new Group(icon);
            iconGroup.setLayoutX(gaugeX);
            iconGroup.setLayoutY(y + (band - iconSize) / 2);

            final double barX = gaugeX + iconSize * 1.25;
            final double barWidth = gaugeWidth - iconSize * 1.25;
            final Text name = new Text(gameContext.getTranslator().translate(need.label));
            name.setStyle(GameStyle.titleFontStyle(fontSize) + " -fx-fill: -gp-text;");
            name.setX(barX);
            name.setY(y + band * 0.42);

            final Rectangle bar = new Rectangle(barX, y + band * 0.55, barWidth, barHeight);
            bar.setArcWidth(barHeight);
            bar.setArcHeight(barHeight);
            bar.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
            final Rectangle fill = new Rectangle(barX, y + band * 0.55, barWidth, barHeight);
            fill.setArcWidth(barHeight);
            fill.setArcHeight(barHeight);
            fill.setFill(Color.web(need.color));
            gaugeFills.put(need, fill);
            barWidths.put(need, barWidth);

            final FadeTransition blink = new FadeTransition(Duration.millis(500), fill);
            blink.setFromValue(1);
            blink.setToValue(0.3);
            blink.setAutoReverse(true);
            blink.setCycleCount(Animation.INDEFINITE);
            gaugeBlinks.put(need, blink);

            final Group gauge = new Group(iconGroup, name, bar, fill);
            gauge.setMouseTransparent(true);
            layer.getChildren().add(gauge);
        }
    }

    private void createBubble() {
        final double size = pet.getPetWidth() * 0.55;
        final Ellipse cloud = new Ellipse(size / 2, size / 2, size / 2, size * 0.42);
        final Circle small = new Circle(-size * 0.02, size * 0.95, size * 0.08);
        final Circle smaller = new Circle(-size * 0.14, size * 1.08, size * 0.05);
        for (final javafx.scene.shape.Shape shape : List.of(cloud, small, smaller)) {
            shape.setStyle("-fx-fill: -gp-surface; -fx-stroke: -gp-border; -fx-stroke-width: 2;");
        }
        bubbleIcon = new SVGPath();
        final Group iconGroup = new Group(bubbleIcon);
        iconGroup.setLayoutX(size * 0.25);
        iconGroup.setLayoutY(size * 0.25);
        bubble = new Group(cloud, small, smaller, iconGroup);
        bubble.setMouseTransparent(true);
        bubble.setVisible(false);
        layer.getChildren().add(bubble);
    }

    /**
     * Every tick: the gauges empty, the low needs are shown, the water washes, the bubble follows the pet.
     */
    void tick() {
        final double loss = TICK.toMillis() / EMPTYING.toMillis();
        for (final Need need : Need.values()) {
            final double before = levels.get(need);
            final double after = Math.max(0, before - loss);
            levels.put(need, after);
            if (Math.ceil(after * 4) < Math.ceil(before * 4)) {
                // A quarter lost: one more activity to do
                stats.incrementNumberOfGoalsToReach();
            }
        }
        updateGauges();
        if (room == Room.BATHROOM) {
            wash();
        }
        if (room == Room.GARDEN) {
            chooseToy();
        }
        placeBubble();
    }

    private void updateGauges() {
        Need lowest = null;
        for (final Need need : Need.values()) {
            final double level = levels.get(need);
            final Rectangle fill = gaugeFills.get(need);
            fill.setWidth(level * barWidths.get(need));
            final FadeTransition blink = gaugeBlinks.get(need);
            if (level < LOW) {
                if (blink.getStatus() != Animation.Status.RUNNING) {
                    blink.play();
                }
                if (lowest == null || level < levels.get(lowest)) {
                    lowest = need;
                }
            } else if (blink.getStatus() == Animation.Status.RUNNING) {
                blink.stop();
                fill.setOpacity(1);
            }
        }
        pet.setSad(lowest != null);
        bubble.setVisible(lowest != null);
        if (lowest != null) {
            final SVGPath icon = GameStyle.icon(lowest.icon, pet.getPetWidth() * 0.55 * 0.5);
            bubbleIcon.setContent(icon.getContent());
            bubbleIcon.getTransforms().setAll(icon.getTransforms());
            bubbleIcon.setStyle("-fx-fill: " + lowest.color + ";");
        }
    }

    /**
     * @return whether the bubble of a low need is shown
     */
    boolean isBubbleShown() {
        return bubble.isVisible();
    }

    private void placeBubble() {
        bubble.setLayoutX(pet.getLayoutX() + pet.getPetWidth() * 0.95);
        bubble.setLayoutY(pet.getLayoutY() - pet.getPetWidth() * 0.35);
    }

    /**
     * Fills a gauge by a quarter: an activity done.
     */
    void refill(final Need need) {
        final double level = levels.get(need);
        if (level < 1) {
            levels.put(need, Math.min(1, level + GAIN));
            stats.incrementNumberOfGoalsReached();
            gameContext.getSoundManager().add(SOUNDS + "gain.wav");
        }
        updateGauges();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Rooms

    /**
     * Opens a room: its picture, its object following the gaze, its activity.
     */
    void open(final Room opened) {
        stopActivity();
        room = opened;
        buttons.forEach((each, button) -> button.setActive(each == opened));
        roomView.setFill(new ImagePattern(new Image(FOLDER + opened.picture)));
        cursor.setImage(new Image(FOLDER + (opened == Room.GARDEN ? toy.picture : opened.object)));
        dishes.values().forEach(dish -> dish.setVisible(opened == Room.KITCHEN));
        toyCards.values().forEach(card -> card.setVisible(opened == Room.GARDEN));
        showChosenToy();
        placePetAtTheCenter();
        if (opened == Room.GARDEN) {
            fly();
        }
        if (pointer != null) {
            pointerAt(pointer);
        }
    }

    private void stopActivity() {
        if (flight != null) {
            flight.stop();
            flight = null;
        }
        animations.forEach(Animation::stop);
        animations.clear();
        layer.getChildren().removeIf(node -> "drop".equals(node.getId()) || "thrown".equals(node.getId()));
        stroking = false;
        heldFood = null;
        lookedToy = null;
        toyProgress = 0;
        if (toyRing != null) {
            toyRing.stop();
        }
        washed = 0;
        washing = false;
        playing = false;
        if (pet != null) {
            pet.calm();
        }
        if (cursor != null) {
            cursor.setVisible(pointer != null);
        }
    }

    private void placePetAtTheCenter() {
        pet.setLayoutX(roomView.getX() + (roomView.getWidth() - pet.getPetWidth()) / 2);
        pet.setLayoutY(roomView.getY() + (roomView.getHeight() - pet.getPetHeight()) / 2);
        placeBubble();
    }

    /**
     * In the garden, the Biboule flies from place to place.
     */
    private void fly() {
        flight = new Timeline();
        flight.setOnFinished(event -> fly());
        final double x = roomView.getX() + random.nextDouble() * (roomView.getWidth() - pet.getPetWidth());
        final double y = roomView.getY() + random.nextDouble() * (roomView.getHeight() - pet.getPetHeight());
        flight.getKeyFrames().add(new KeyFrame(Duration.millis(1500), new KeyValue(pet.layoutXProperty(), x),
            new KeyValue(pet.layoutYProperty(), y)));
        flight.rateProperty().bind(gameContext.getAnimationSpeedRatioSource().getSpeedRatioProperty());
        flight.play();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // The gaze on the room

    private void listenToThePointer() {
        gameContext.getGazeDeviceManager().addEventFilter(roomView);
        roomView.addEventFilter(MouseEvent.ANY, this::onPointer);
        roomView.addEventFilter(GazeEvent.ANY, this::onPointer);
    }

    private void onPointer(final Event e) {
        if (e.getEventType() == MouseEvent.MOUSE_EXITED || e.getEventType() == GazeEvent.GAZE_EXITED) {
            pointerLeft();
        } else if (e instanceof MouseEvent mouse && (e.getEventType() == MouseEvent.MOUSE_MOVED
            || e.getEventType() == MouseEvent.MOUSE_ENTERED || e.getEventType() == MouseEvent.MOUSE_DRAGGED)) {
            pointerAt(layer.sceneToLocal(mouse.getSceneX(), mouse.getSceneY()));
        } else if (e instanceof GazeEvent gaze && (e.getEventType() == GazeEvent.GAZE_MOVED
            || e.getEventType() == GazeEvent.GAZE_ENTERED) && e.getSource() instanceof Node node) {
            // The position of a gaze event is in the coordinates of the node
            pointerAt(layer.sceneToLocal(node.localToScene(gaze.getX(), gaze.getY())));
        }
    }

    private void pointerLeft() {
        pointer = null;
        cursor.setVisible(false);
        if (stroking) {
            stroking = false;
            pet.calm();
        }
    }

    /**
     * The gaze (or the mouse) is at this place of the room: the object follows it, and does what it does there.
     */
    void pointerAt(final Point2D position) {
        pointer = position;
        cursor.setVisible(!playing);
        cursor.setLayoutX(position.getX() - room.hotX * objectSize);
        cursor.setLayoutY(position.getY() - room.hotY * objectSize);
        final boolean onPet = pet.getBoundsInParent().contains(position);
        switch (room) {
            case HOME -> {
                if (onPet && !stroking) {
                    stroking = true;
                    pet.setHappy();
                    gameContext.getSoundManager().add(SOUNDS + "stroke.wav");
                } else if (!onPet && stroking) {
                    stroking = false;
                    pet.calm();
                }
            }
            case KITCHEN -> {
                final Food onDish = dishAt(position);
                if (onDish != null && onDish != heldFood) {
                    // A dish taken: the spoon full of purée, or the fruit or the cake itself
                    heldFood = onDish;
                    cursor.setImage(new Image(FOLDER + onDish.held));
                } else if (heldFood != null && nearTheMouth(position)) {
                    heldFood = null;
                    cursor.setImage(new Image(FOLDER + "spoon.png"));
                    pet.eat();
                    gameContext.getSoundManager().add(SOUNDS + "munch.wav");
                    refill(Need.HUNGER);
                }
            }
            case GARDEN -> {
                final Toy onToy = toyAt(position);
                if (onToy != lookedToy) {
                    lookedToy = onToy;
                    toyProgress = 0;
                    toyRing.stop();
                }
                if (onToy == null && onPet && !playing) {
                    play(position);
                }
            }
            default -> {
                // The bathroom washes with time (see wash)
            }
        }
    }

    /**
     * @return the dish of the table at this place, or null
     */
    private Food dishAt(final Point2D position) {
        for (final Map.Entry<Food, ImageView> dish : dishes.entrySet()) {
            if (dish.getValue().getBoundsInParent().contains(position)) {
                return dish.getKey();
            }
        }
        return null;
    }

    /**
     * @return the toy of the garden at this place, or null
     */
    private Toy toyAt(final Point2D position) {
        for (final Map.Entry<Toy, Circle> disc : toyDiscs.entrySet()) {
            if (disc.getValue().contains(position)) {
                return disc.getKey();
            }
        }
        return null;
    }

    /**
     * At every tick in the garden: the fixation on a toy not in hand fills its ring; full, the toy is taken.
     */
    private void chooseToy() {
        if (lookedToy == null || lookedToy == toy || playing) {
            toyRing.stop();
            return;
        }
        final Circle disc = toyDiscs.get(lookedToy);
        if (!toyRing.isVisible()) {
            toyRing.show(disc.getCenterX(), disc.getCenterY());
        }
        toyProgress += TICK.toMillis() / Math.max(1, gameContext.getConfiguration().getFixationLength());
        toyRing.setProgress(toyProgress);
        if (toyProgress >= 1) {
            chooseToy(lookedToy);
        }
    }

    /**
     * Takes a toy in hand.
     */
    void chooseToy(final Toy chosen) {
        toy = chosen;
        toyProgress = 0;
        toyRing.stop();
        cursor.setImage(new Image(FOLDER + chosen.picture));
        showChosenToy();
    }

    /**
     * The disc of the toy in hand stands out.
     */
    private void showChosenToy() {
        toyDiscs.forEach((each, disc) -> {
            disc.setStyle(each == toy ? "-fx-stroke: -gp-accent;" : "");
            disc.setStrokeWidth(disc.getRadius() * (each == toy ? 0.12 : 0.06));
        });
    }

    /**
     * @return the center of the disc of a toy, for the tests
     */
    Point2D toyCenter(final Toy each) {
        final Circle disc = toyDiscs.get(each);
        return new Point2D(disc.getCenterX(), disc.getCenterY());
    }

    /**
     * @return the dish of a food, for the tests
     */
    ImageView dish(final Food food) {
        return dishes.get(food);
    }

    /**
     * @return whether a dish is in hand (the spoon full, or a fruit or a cake)
     */
    boolean isSpoonFull() {
        return heldFood != null;
    }

    /**
     * @return whether this place is on the mouth of the pet, or close to it (the gaze is not precise)
     */
    private boolean nearTheMouth(final Point2D position) {
        final var mouth = pet.localToParent(pet.getMouth().getBoundsInParent());
        final double margin = mouth.getWidth() * 0.5;
        return position.getX() > mouth.getMinX() - margin && position.getX() < mouth.getMaxX() + margin
            && position.getY() > mouth.getMinY() - margin && position.getY() < mouth.getMaxY() + margin;
    }

    /**
     * In the bathroom, at every tick: drops fall from the shower; above the Biboule, they wash it.
     */
    private void wash() {
        if (pointer == null) {
            if (washing) {
                washing = false;
                pet.calm();
            }
            return;
        }
        final var petBounds = pet.getBoundsInParent();
        final boolean above = pointer.getX() > petBounds.getMinX() && pointer.getX() < petBounds.getMaxX()
            && pointer.getY() < petBounds.getMaxY();
        final double fall = above ? Math.max(objectSize * 0.3, petBounds.getMinY() + petBounds.getHeight() * 0.3
            - pointer.getY()) : roomView.getY() + roomView.getHeight() - pointer.getY();
        final Circle drop = new Circle(pointer.getX() - objectSize * 0.15 + random.nextDouble() * objectSize * 0.3,
            pointer.getY() + objectSize * 0.3, objectSize * 0.04, Color.web("#7CC8F2"));
        drop.setId("drop");
        drop.setMouseTransparent(true);
        layer.getChildren().add(layer.getChildren().indexOf(cursor), drop);
        final TranslateTransition down = new TranslateTransition(Duration.millis(500), drop);
        down.setByY(Math.max(0, fall - objectSize * 0.3));
        down.setOnFinished(event -> layer.getChildren().remove(drop));
        down.play();
        if (above) {
            if (!washing) {
                washing = true;
                pet.setHappy();
                splashTicks = 0;
            }
            if (splashTicks++ % SPLASH_TICKS == 0) {
                gameContext.getSoundManager().add(SOUNDS + "splash.wav");
            }
            washed += TICK.toMillis();
            if (washed >= WASHING.toMillis()) {
                washed = 0;
                refill(Need.CLEANLINESS);
            }
        } else if (washing) {
            washing = false;
            pet.calm();
        }
    }

    /**
     * In the garden: the toy touches the Biboule and plays its game (the ball bounces up and comes back, the juggling
     * balls turn above its head, the hoop comes down around it, spinning); then the gauge is filled.
     */
    private void play(final Point2D position) {
        playing = true;
        cursor.setVisible(false);
        pet.setHappy();
        gameContext.getSoundManager().add(SOUNDS + "boing.wav");
        final ImageView ball = new ImageView(cursor.getImage());
        ball.setId("thrown");
        ball.setMouseTransparent(true);
        layer.getChildren().add(ball);
        final var petBounds = pet.getBoundsInParent();
        final Animation bounce;
        switch (toy) {
            case JUGGLING -> {
                // Above its head, turning as balls juggled
                final double size = objectSize * 1.1;
                ball.setFitWidth(size);
                ball.setFitHeight(size);
                ball.setLayoutX(petBounds.getCenterX() - size / 2);
                ball.setLayoutY(petBounds.getMinY() - size * 0.9);
                final RotateTransition turn = new RotateTransition(Duration.millis(800), ball);
                turn.setByAngle(720);
                final TranslateTransition hop = new TranslateTransition(Duration.millis(200), ball);
                hop.setByY(-size * 0.25);
                hop.setAutoReverse(true);
                hop.setCycleCount(4);
                bounce = new ParallelTransition(turn, hop);
            }
            case HOOP -> {
                // From above its head down around it, spinning
                final double size = petBounds.getWidth() * 1.3;
                ball.setFitWidth(size);
                ball.setFitHeight(size);
                ball.setLayoutX(petBounds.getCenterX() - size / 2);
                ball.setLayoutY(petBounds.getCenterY() - size / 2);
                ball.setTranslateY(-petBounds.getHeight() * 0.8);
                final TranslateTransition down = new TranslateTransition(Duration.millis(800), ball);
                down.setToY(0);
                final ScaleTransition spin = new ScaleTransition(Duration.millis(200), ball);
                spin.setFromX(1);
                spin.setToX(-1);
                spin.setAutoReverse(true);
                spin.setCycleCount(4);
                bounce = new ParallelTransition(down, spin);
            }
            default -> {
                ball.setFitWidth(objectSize);
                ball.setFitHeight(objectSize);
                ball.setLayoutX(position.getX() - objectSize / 2);
                ball.setLayoutY(position.getY() - objectSize / 2);
                final TranslateTransition up = new TranslateTransition(Duration.millis(400), ball);
                up.setByY(-roomView.getHeight() * 0.25);
                final TranslateTransition back = new TranslateTransition(Duration.millis(400), ball);
                back.setByY(roomView.getHeight() * 0.25);
                bounce = new SequentialTransition(up, back);
            }
        }
        bounce.setOnFinished(event -> {
            layer.getChildren().remove(ball);
            playing = false;
            pet.calm();
            cursor.setVisible(pointer != null);
            refill(Need.PLAY);
        });
        animations.add(bounce);
        bounce.play();
    }

    @Override
    public void dispose() {
        if (ticker != null) {
            ticker.stop();
        }
        stopActivity();
        if (toyRing != null) {
            toyRing.stop();
        }
        gaugeBlinks.values().forEach(Animation::stop);
        buttons.values().forEach(GazeRoundButton::dispose);
        if (roomView != null) {
            gameContext.getGazeDeviceManager().removeEventFilter(roomView);
        }
        if (pet != null) {
            pet.stop();
        }
    }

    /**
     * @return the settings of the menu (Esc) acting on this game (17/11/2026): the others are greyed
     */
    @Override
    public java.util.Set<net.gazeplay.MenuSetting> menuSettings() {
        return net.gazeplay.MenuSetting.fixationWithOwnRing(net.gazeplay.MenuSetting.ANIMATION_SPEED, net.gazeplay.MenuSetting.GAME_BUTTONS);
    }
}
