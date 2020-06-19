package net.gazeplay.games.pianosight;

import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import javax.sound.midi.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class Piano extends Parent implements GameLifeCycle {

    private static final int[] NOTE_NAMES = {0, 7, 1, 8, 2, 3, 9, 4, 10, 5, 11, 6};

    private final double centerX;
    private final double centerY;

    private Circle circ;
    private Circle circleTemp;
    private final List<Tile> tilesTab;
    private final Jukebox jukebox;

    private final Stats stats;

    private final IGameContext gameContext;

    private final Instru instru;

    private final List<ImageView> fragments;

   // long lastTickPosition = 0;
    long lastKey = 0;
    MidiSequencerPlayer player;
    private Sequence sequence;
    ObjectProperty<Note> ip;

    public Piano(final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2.2;
        this.fragments = buildFragments();
        this.getChildren().addAll(fragments);
        tilesTab = new ArrayList<>();
        instru = new Instru();
        gameContext.getChildren().add(this);
        jukebox = new Jukebox(gameContext);
    }

    private List<ImageView> buildFragments() {
        final int nbFragments = 5;

        final List<ImageView> fragments = new ArrayList<>();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        for (int i = 0; i < nbFragments; i++) {

            final String s = "data/pianosight/images/" + i + ".png";

            final ImageView fragment = new ImageView(new Image(s));
            fragment.setOpacity(0);
            fragment.setPreserveRatio(true);
            fragment.setFitHeight(dimension2D.getHeight() / 10);
            fragment.setVisible(true);
            fragment.setX(-100);
            fragment.setY(-100);
            fragments.add(fragment);
        }
        return fragments;
    }

    private void explose(final double xcenter, final double ycenter) {

        final Timeline timeline1 = new Timeline();
        final Timeline timeline2 = new Timeline();

        final Random random = new Random();

        for (final ImageView fragment : fragments) {

            timeline1.getKeyFrames().add(
                new KeyFrame(new Duration(1), new KeyValue(fragment.xProperty(), xcenter, Interpolator.LINEAR)));
            timeline1.getKeyFrames().add(
                new KeyFrame(new Duration(1), new KeyValue(fragment.yProperty(), ycenter, Interpolator.EASE_OUT)));
            timeline1.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.opacityProperty(), 1)));

            final int worh = random.nextInt(4);

            final Dimension2D screenDimension = gameContext.getCurrentScreenDimensionSupplier().get();

            final double xEndValue;
            final double yEndValue;
            switch (worh) {
                case 0:
                    xEndValue = 0;
                    yEndValue = random.nextDouble() * screenDimension.getHeight();
                    break;
                case 1:
                    xEndValue = random.nextDouble() * screenDimension.getWidth();
                    yEndValue = 0;
                    break;
                case 2:
                    xEndValue = screenDimension.getWidth();
                    yEndValue = random.nextDouble() * screenDimension.getHeight();
                    break;
                case 3:
                    xEndValue = random.nextDouble() * screenDimension.getWidth();
                    yEndValue = screenDimension.getHeight();
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported value : " + worh);
            }

            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fragment.xProperty(), xEndValue, Interpolator.LINEAR)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000),
                new KeyValue(fragment.yProperty(), yEndValue, Interpolator.EASE_OUT)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.opacityProperty(), 0)));
        }

        final SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(timeline1, timeline2);
        sequence.play();

    }

    private void loadMusic(final boolean b) throws IOException, InvalidMidiDataException, MidiUnavailableException {

        if (b) {
            final String fileName = jukebox.getS();
            if (fileName == null) {
                return;
            }
            log.info("you loaded the song : " + fileName);
            final File f = new File(fileName);
            try (InputStream inputStream = new FileInputStream(f)) {
                sequence = MidiSystem.getSequence(inputStream);
            }
        } else {
            final String fileName = "RIVER.mid";
            log.info("you loaded the song : " + fileName);
            try (InputStream inputStream = Utils.getInputStream("data/pianosight/songs/" + fileName)) {
                sequence = MidiSystem.getSequence(inputStream);
            }
        }

    }

    @Override
    public void launch() {
        this.gameContext.resetBordersToFront();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        circ = new Circle(centerX, centerY, dimension2D.getHeight() / 4);
        circ.setFill(Color.RED);
        this.getChildren().add(circ);

        circleTemp = new Circle(centerX, centerY, dimension2D.getHeight() / 5);
        circleTemp.setFill(Color.BLACK);
        circleTemp.setStroke(Color.BLACK);
        circleTemp.setStrokeWidth(10);
        circleTemp.setOpacity(0);
        this.getChildren().add(circleTemp);

        createArcs();

        ip = new SimpleObjectProperty<Note>();
        ip.setValue(new Note(-1, -1, -1));
        ip.addListener((o, n, old) -> {
            if (!n.equals(new Note(-1, -1, -1)) ){
            //&& (n.tick - lastTickPosition > 10)) {
             //   lastTickPosition = n.tick;
                lastKey = n.key;
                int firstNote = (int) (lastKey % 12);
                boolean isCircle =  tilesTab.get(firstNote).arc.getFill() == Color.YELLOW;

                for (final Tile tile : tilesTab) {
                    tile.arc.setFill(tile.color1);
                }
                if(isCircle) {
                    circleTemp.setFill(Color.YELLOW);
                } else {
                    tilesTab.get(NOTE_NAMES[(int) (lastKey%12)]).arc.setFill(Color.YELLOW);
                }
                //sequencer.setTempoInBPM(0);
                //sequencer.close();
                //player.stop();

            }
        });

        circleTemp.setFill(Color.YELLOW);

        this.getChildren().remove(circ);

        final EventHandler<Event> circleEvent = e -> {
            if (circleTemp.getFill() == Color.YELLOW) {
                try {
                    player.playPause();
                } catch (MidiUnavailableException ex) {
                    ex.printStackTrace();
                }
            }
        };

        circleTemp.addEventFilter(MouseEvent.MOUSE_ENTERED, circleEvent);
        circleTemp.addEventFilter(GazeEvent.GAZE_ENTERED, circleEvent);
        gameContext.getGazeDeviceManager().addEventFilter(circleTemp);

        this.getChildren().addAll(this.tilesTab);
        this.getChildren().get(this.getChildren().indexOf(circleTemp)).toFront();
        final ImageView iv = new ImageView(new Image("data/pianosight/images/1.png"));
        final Button b = new Button("Open", iv);

        b.setOpacity(1);

        final Timeline buttonOpacityTimeline = new Timeline();
        buttonOpacityTimeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(1), new KeyValue(b.opacityProperty(), 0.1, Interpolator.LINEAR)));

        buttonOpacityTimeline.setDelay(Duration.seconds(2));
        buttonOpacityTimeline.play();

        b.setOnMouseEntered(evt -> {
            b.setOpacity(1);
        });

        b.setOnMouseExited(evt -> {
            b.setOpacity(0.1);
        });


        b.setPrefWidth(dimension2D.getWidth() / 7);
        b.setPrefHeight(dimension2D.getHeight() / 7);
        iv.setPreserveRatio(true);
        iv.setFitHeight(b.getPrefHeight());
        // TODO add a replay button
        b.setOnMousePressed(evt -> {
            try {
                loadMusic(true);
            } catch (final IOException | MidiUnavailableException | InvalidMidiDataException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        this.getChildren().add(b);

        ObservableList<Integer> integers = FXCollections.observableArrayList();
        for(int i = 0; i<68; i++){
            integers.add(i);
        }
        ChoiceBox<Integer> choiceBox = new ChoiceBox<Integer>(integers);
        choiceBox.setLayoutY(dimension2D.getHeight() / 7);
        choiceBox.setPrefWidth(dimension2D.getWidth() / 7);
        choiceBox.setPrefHeight(dimension2D.getHeight() / 7);
        //
        ChangeListener<Integer> changeListener = (observable, oldValue, newValue) -> {
            instru.setInstrument(newValue);
        };
        choiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
        this.getChildren().add(choiceBox);



        try {
            loadMusic(false);
        } catch (final IOException | InvalidMidiDataException | MidiUnavailableException e) {
            e.printStackTrace();
        }
        stats.notifyNewRoundReady();
        circleTemp.setFill(Color.YELLOW);
        circ.setFill(Color.YELLOW);
        circleTemp.toFront();
        circleTemp.setOpacity(1);

        player = new MidiSequencerPlayer(sequence, ip);
    }

    @Override
    public void dispose() {

    }

    private void createArc(final int index, final double angle, final Color color1, final Color color2, final double l, final double origin) {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double size = dimension2D.getHeight() / l;
        final double theta = ((index * 360d) / 7d - origin);
        final Tile a3 = new Tile(centerX, centerY, size, size, theta, angle, circ);
        a3.color1 = color1;
        //a3.color2 = color2;
        a3.arc.setFill(color1);
        a3.arc.setStrokeWidth(10);
        a3.setVisible(true);

        final EventHandler<Event> tileEventEnter = e -> {
            if (tilesTab.get(((Tile) e.getTarget()).note).arc.getFill() == color1) {
                tilesTab.get(((Tile) e.getTarget()).note).arc.setFill(color2);
            } else
            if (tilesTab.get(((Tile) e.getTarget()).note).arc.getFill() == Color.YELLOW) {
                    tilesTab.get(((Tile) e.getTarget()).note).arc.setFill(color1);
                    player.start();
            }
        };

        final EventHandler<Event> tileEventExited = e -> {
            log.info("index ={}", index);
            if (tilesTab.get(((Tile) e.getTarget()).note).arc.getFill() == color2) {
                tilesTab.get(((Tile) e.getTarget()).note).arc.setFill(color1);
            }

        };

        if ((origin != 0) && (index == 8 || index == 12)) {
            a3.setOpacity(0);
            a3.setDisable(true);
        }

        a3.tileEventEnter = tileEventEnter;
        a3.tileEventExited = tileEventExited;

        a3.addEventFilter(MouseEvent.MOUSE_ENTERED, a3.tileEventEnter);
        a3.addEventFilter(MouseEvent.MOUSE_EXITED, a3.tileEventExited);
        a3.addEventFilter(GazeEvent.GAZE_ENTERED, a3.tileEventEnter);
        a3.addEventFilter(GazeEvent.GAZE_EXITED, a3.tileEventExited);
        a3.note = index;

        gameContext.getGazeDeviceManager().addEventFilter(a3);

        tilesTab.add(index, a3);
    }

    private void createArcs() {
        for (int i = 0; i < 7; i++) {
            final double angle = 360d / 7d;
            createArc(i, angle, Color.GHOSTWHITE, Color.GAINSBORO.darker(), 2.3, -90 + (720d / 7d));
        }

        for (int i = 7; i < 14; i++) {
            final double angle = 360d / 14d;
            createArc(i, angle, Color.BLACK, Color.DIMGREY.darker(), 2.7, -90 + (720d / 7d) + 2 * angle + angle / 2);
        }

    }

}
