package sample;

import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class LightningSimulator extends Application {
    private static final int FIELD_SIZE = 10;

    private static final Random random = new Random(42);

    @Override
    public void start(final Stage stage) {

        final TilePane field = generateField();

        final Scene scene = new Scene(field);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        field.addEventFilter(LightningEvent.PLASMA_STRIKE,
            event -> log.info("Field filtered strike: " + event.getI() + ", " + event.getJ()));

        field.addEventHandler(LightningEvent.PLASMA_STRIKE,
            event -> log.info("Field handled strike: " + event.getI() + ", " + event.getJ()));

        periodicallyStrikeRandomNodes(field);
    }

    private void periodicallyStrikeRandomNodes(final TilePane field) {
        final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), event -> strikeRandomNode(field)),
            new KeyFrame(Duration.seconds(2)));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void strikeRandomNode(final TilePane field) {
        final LightningReactor struckNode = (LightningReactor) field.getChildren()
            .get(random.nextInt(FIELD_SIZE * FIELD_SIZE));
        final LightningEvent lightningStrike = new LightningEvent(this, struckNode);

        struckNode.fireEvent(lightningStrike);
    }

    private TilePane generateField() {
        final TilePane field = new TilePane();
        field.setPrefColumns(10);
        field.setMinWidth(TilePane.USE_PREF_SIZE);
        field.setMaxWidth(TilePane.USE_PREF_SIZE);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                field.getChildren().add(new LightningReactor(i, j, new StrikeEventHandler()));
            }
        }
        return field;
    }

    private static class LightningReactor extends Rectangle {
        private static final int SIZE = 20;
        private final int i;
        private final int j;

        private final FillTransition fillTransition = new FillTransition(Duration.seconds(4));

        public LightningReactor(final int i, final int j, final EventHandler<? super LightningEvent> lightningEventHandler) {
            super(SIZE, SIZE);

            this.i = i;
            this.j = j;

            final Color baseColor = (i + j) % 2 == 0 ? Color.RED : Color.WHITE;
            setFill(baseColor);

            fillTransition.setFromValue(Color.YELLOW);
            fillTransition.setToValue(baseColor);
            fillTransition.setShape(this);

            addEventHandler(LightningEvent.PLASMA_STRIKE, lightningEventHandler);
        }

        public void strike() {
            fillTransition.playFromStart();
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }
    }

    private static class StrikeEventHandler implements EventHandler<LightningEvent> {
        @Override
        public void handle(final LightningEvent event) {
            final LightningReactor reactor = (LightningReactor) event.getTarget();
            reactor.strike();

            log.info("Reactor received strike: " + reactor.getI() + ", " + reactor.getJ());

            // event.consume(); if event is consumed the handler for the parent node will not be invoked.
        }
    }

    static class LightningEvent extends Event {

        private static final long serialVersionUID = 20121107L;

        private int i, j;

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }

        /**
         * The only valid EventType for the CustomEvent.
         */
        public static final EventType<LightningEvent> PLASMA_STRIKE = new EventType<>(Event.ANY, "PLASMA_STRIKE");

        /**
         * Creates a new {@code LightningEvent} with an event type of {@code PLASMA_STRIKE}. The source and Target of
         * the event is set to {@code NULL_SOURCE_TARGET}.
         */
        public LightningEvent() {
            super(PLASMA_STRIKE);
        }

        /**
         * Construct a new {@code LightningEvent} with the specified event source and Target. If the source or Target is
         * set to {@code null}, it is replaced by the {@code NULL_SOURCE_TARGET} value. All LightningEvents have their
         * type set to {@code PLASMA_STRIKE}.
         *
         * @param source the event source which sent the event
         * @param target the event Target to associate with the event
         */
        public LightningEvent(final Object source, final EventTarget target) {
            super(source, target, PLASMA_STRIKE);

            this.i = ((LightningReactor) target).getI();
            this.j = ((LightningReactor) target).getJ();
        }

        @Override
        public LightningEvent copyFor(final Object newSource, final EventTarget newTarget) {
            return (LightningEvent) super.copyFor(newSource, newTarget);
        }

        @Override
        public EventType<? extends LightningEvent> getEventType() {
            return (EventType<? extends LightningEvent>) super.getEventType();
        }

    }

}
