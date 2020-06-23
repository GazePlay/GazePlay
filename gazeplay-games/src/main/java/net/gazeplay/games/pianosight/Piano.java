package net.gazeplay.games.pianosight;

import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import net.gazeplay.commons.utils.stats.Stats;

import javax.sound.midi.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class CustomPair {
    public CustomPair(int key, int value){
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        if(key == -1) {
            return "all channels" + " (" + value + ")";
        }
        return "channel " + key + " (" + value + ")";
    }

    public int key;
    public int value;
}

@Slf4j
public class Piano extends Parent implements GameLifeCycle {

    private static final int[] NOTE_NAMES = {0, 7, 1, 9, 2, 3, 10, 4, 11, 5, 13, 6};

    private final double centerX;
    private final double centerY;

    private Circle circ;
    private Circle circleTemp;
    private final List<Tile> tilesTab;
    private final Jukebox jukebox;

    private final Stats stats;

    private final IGameContext gameContext;
    private ChoiceBox<CustomPair> choiceBox;

    private final Instru instru;

    private final List<ImageView> fragments;

   // long lastTickPosition = 0;
    long lastNote = -1;
    MidiSequencerPlayer player;
    private Sequence sequence;
    ObjectProperty<Note> noteProperty;

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
            player.stop();
            try (InputStream inputStream = new FileInputStream(f)) {
                sequence = MidiSystem.getSequence(inputStream);
                updateChoiceBox();
                player.sequencer.setSequence(sequence);
            }
        } else {
            final String fileName = "RIVER.mid";
            log.info("you loaded the song : " + fileName);
            try (InputStream inputStream = /*Utils.getInputStream*/new FileInputStream("C:/Users/Sebastien/Downloads/alanwalker.mid")){//;"data/pianosight/songs/" + fileName)) {
                sequence = MidiSystem.getSequence(inputStream);
                updateChoiceBox();
            }
        }
    }

    public void updateChoiceBox(){
        int[] count = new int[16];
        int sum = 0;
        for(int i = 0; i< sequence.getTracks().length; i++){
            for(int j = 0; j < sequence.getTracks()[i].size(); j++){
                if (sequence.getTracks()[i].get(j).getMessage() instanceof ShortMessage) {
                    count[((ShortMessage)sequence.getTracks()[i].get(j).getMessage()).getChannel()]++;
                    sum++;
                }
            }
        }


        ObservableList<CustomPair> integers = FXCollections.observableArrayList();
        CustomPair defautPair = new CustomPair(-1,  sum );
        integers.add(defautPair);
        for(int i = 0; i<16; i++){
            if(count[i]!=0) {
                log.info("VOICI LE COUNT POUR LA TRACK {} : {}",i,count[i]);
                integers.add(new CustomPair(i,  count[i] ));
            }
        }

        choiceBox.setItems(integers);
        choiceBox.setValue(defautPair);
    }

    @Override
    public void launch() {
        this.gameContext.resetBordersToFront();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        circ = new Circle(centerX, centerY, dimension2D.getHeight() / 4);
        circ.setFill(Color.RED);
        this.getChildren().add(circ);

        circleTemp = new Circle(centerX, centerY, dimension2D.getHeight() / 5);
        circleTemp.setStroke(Color.BLACK);
        circleTemp.setStrokeWidth(10);
        circleTemp.setOpacity(0);
        this.getChildren().add(circleTemp);

        createArcs();

        noteProperty = new SimpleObjectProperty<Note>();
        noteProperty.setValue(new Note(-1, -1, -1));
        noteProperty.addListener((o, n, old) -> {
            if (!n.equals(new Note(-1, -1, -1)) ){
                int firstNote = (int) (n.key % 12);
                boolean isCircle =  tilesTab.get(firstNote).arc.getFill() == Color.YELLOW;

                for (final Tile tile : tilesTab) {
                    tile.arc.setFill(tile.color1);
                }
                if(firstNote == lastNote) {
                    circleTemp.setFill(Color.YELLOW);
                    circleTemp.setOpacity(1);
                    lastNote = -1;
                } else {
                    log.info("The note was {} and the name is {}",(int) firstNote,NOTE_NAMES[(int) (firstNote)]);
                    tilesTab.get(NOTE_NAMES[(int) (firstNote)]).arc.setFill(Color.YELLOW);
                    lastNote = firstNote;
                }
                player.stop();

            }
        });

        circleTemp.setFill(Color.YELLOW);
        circleTemp.setOpacity(1);

        this.getChildren().remove(circ);

        final EventHandler<Event> circleEvent = e -> {
            Color color2 = Color.BLACK;
            if (circleTemp.getFill() == Color.YELLOW) {
                player.start();
                double x = 0;
                double y = 0;
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    final MouseEvent me = (MouseEvent) e;
                    x = me.getX();
                    y = me.getY();
                } else if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    final GazeEvent ge = (GazeEvent) e;
                    x = ge.getX();
                    y = ge.getY();
                }
                explose(x, y);
                circleTemp.setFill(color2);
                circleTemp.setOpacity(0);
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


        choiceBox = new ChoiceBox<CustomPair>();
        choiceBox.setLayoutY(dimension2D.getHeight() / 7);
        choiceBox.setPrefWidth(dimension2D.getWidth() / 7);
        choiceBox.setPrefHeight(dimension2D.getHeight() / 7);

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

        player = new MidiSequencerPlayer(sequence, noteProperty);

        ChangeListener<CustomPair> changeListener = (observable, oldValue, newValue) -> {
            if (newValue == null){
                player.setChanel(-1);
            } else {
                player.setChanel(newValue.key);
            }
        };
        choiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);

        this.getChildren().add(choiceBox);

    }

    @Override
    public void dispose() {

    }

    private void createArc(final int index, final double angle, final Color color1, final Color color2, final double l, final double origin) {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double size = dimension2D.getHeight() / l;
        final double theta = ((index * 360d) / 7d - origin);
        final Tile createdTile = new Tile(centerX, centerY, size, size, theta, angle, circ);
        createdTile.color1 = color1;
        createdTile.arc.setFill(color1);
        createdTile.arc.setStrokeWidth(10);
        createdTile.setVisible(true);

        final EventHandler<Event> tileEventEnter = e -> {
            if (tilesTab.get(((Tile) e.getTarget()).note).arc.getFill() == color1) {
                tilesTab.get(((Tile) e.getTarget()).note).arc.setFill(color2);
            } else
            if (tilesTab.get(((Tile) e.getTarget()).note).arc.getFill() == Color.YELLOW) {
                    tilesTab.get(((Tile) e.getTarget()).note).arc.setFill(color1);
                player.start();

                double x = 0;
                double y = 0;
                if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    final MouseEvent me = (MouseEvent) e;
                    x = me.getX();
                    y = me.getY();
                } else if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                    final GazeEvent ge = (GazeEvent) e;
                    x = ge.getX();
                    y = ge.getY();
                }
                explose(x, y);

            }
        };

        final EventHandler<Event> tileEventExited = e -> {
            log.info("index ={}", index);
            if (tilesTab.get(((Tile) e.getTarget()).note).arc.getFill() == color2) {
                tilesTab.get(((Tile) e.getTarget()).note).arc.setFill(color1);
            }
        };

        if ((origin != 0) && (index == 8 || index == 12)) {
            createdTile.setOpacity(0);
            createdTile.setDisable(true);
        }

        createdTile.tileEventEnter = tileEventEnter;
        createdTile.tileEventExited = tileEventExited;

        createdTile.addEventFilter(MouseEvent.MOUSE_ENTERED, createdTile.tileEventEnter);
        createdTile.addEventFilter(MouseEvent.MOUSE_EXITED, createdTile.tileEventExited);
        createdTile.addEventFilter(GazeEvent.GAZE_ENTERED, createdTile.tileEventEnter);
        createdTile.addEventFilter(GazeEvent.GAZE_EXITED, createdTile.tileEventExited);
        createdTile.note = index;

        gameContext.getGazeDeviceManager().addEventFilter(createdTile);

        tilesTab.add(index, createdTile);
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
