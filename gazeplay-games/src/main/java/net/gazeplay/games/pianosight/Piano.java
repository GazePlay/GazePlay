package net.gazeplay.games.pianosight;

import javafx.animation.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;

import javax.sound.midi.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class CustomPair {
    public CustomPair(int key, int value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        if (key == -1) {
            return "select all";
        } else if (key == -2) {
            return "autoplay";
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

    private final List<ImageView> fragments;
    private float bpm = 120;

    long lastNote = -1;
    MidiSequencerPlayer player;
    private Sequence sequence;
    ObjectProperty<Note> noteProperty;

    GridPane choiceBoxes;
    BorderPane topBar = new BorderPane();
    Slider slider;

    ChangeListener<Number> sliderListener = (obj, oldval, newval) -> {
        if (slider.isHover()) {
            player.pianoReceiver.isSliderInUse = true;
            log.info("****************1");
            player.sequencer.setTickPosition(newval.longValue());
            log.info("****************11");
            player.pianoReceiver.previousTick = newval.longValue() - 1;
            log.info("****************111");
            player.pianoReceiver.currentTickProperty.setValue(newval.longValue());
            log.info("****************1111");
            player.pianoReceiver.isSliderInUse = false;
        }
    };

    public Piano(final IGameContext gameContext, final Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2.2;
        this.fragments = buildFragments();
        this.getChildren().addAll(fragments);
        tilesTab = new ArrayList<>();
        Instru instru = new Instru();
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

        final ReplayablePseudoRandom random = new ReplayablePseudoRandom();

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

    private void loadMusic(final boolean b) throws IOException, InvalidMidiDataException {

        if (b) {
            final String fileName = jukebox.getS();
            if (fileName == null) {
                return;
            }
            log.info("you loaded the song : " + fileName);
            final File f = new File(fileName);
            player.stop();
            try (InputStream inputStream = new FileInputStream(f)) {
                slider.valueProperty().removeListener(sliderListener);
                sequence = MidiSystem.getSequence(inputStream);
                bpm = 120;
                updateChoiceBox();
                player.pianoReceiver.initPianorReceiverParameters();
                ((CheckBox) choiceBoxes.getChildren().get(0)).setSelected(true);
                player.pianoReceiver.isChangingSequence = true;
                player.sequencer.setSequence(sequence);
                player.pianoReceiver.isChangingSequence = false;
                player.setTempo(bpm);
                player.start();
                resetSlider(slider, player.sequencer.getTickLength());
            }
        } else {
            final String fileName = "RIVER.mid";
            log.info("you loaded the song : " + fileName);
            try (InputStream inputStream = Utils.getInputStream("data/pianosight/songs/" +fileName)) {
                //try (InputStream inputStream = Utils.getInputStream("data/pianosight/songs/" +fileName)) {
                sequence = MidiSystem.getSequence(inputStream);
                updateChoiceBox();
            }
        }
    }

    public void updateSelectedChoiceBox() {
        int i = 2;
        boolean allSelected = true;
        boolean noneSelected = true;
        while (i < choiceBoxes.getChildren().size()) {
            allSelected = allSelected && ((CheckBox) choiceBoxes.getChildren().get(i)).isSelected();
            noneSelected = noneSelected && !((CheckBox) choiceBoxes.getChildren().get(i)).isSelected();
            i++;
        }
        ((CheckBox) choiceBoxes.getChildren().get(0)).setSelected(allSelected);
        ((CheckBox) choiceBoxes.getChildren().get(1)).setSelected(noneSelected);
    }

    public void updateChoiceBox() {
        choiceBoxes.getChildren().clear();

        int[] count = new int[16];
        int sum = 0;
        for (int i = 0; i < sequence.getTracks().length; i++) {
            for (int j = 0; j < sequence.getTracks()[i].size(); j++) {
                if (sequence.getTracks()[i].get(j).getMessage() instanceof ShortMessage) {
                    count[((ShortMessage) sequence.getTracks()[i].get(j).getMessage()).getChannel()]++;
                    sum++;
                } else if (sequence.getTracks()[i].get(j).getMessage() instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) sequence.getTracks()[i].get(j).getMessage();
                    if (mm.getType() == 0x51) {
                        byte[] data = mm.getData();
                        int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
                        bpm = 60000000 / tempo;
                    }
                }
            }
        }

        CustomPair defautPair = new CustomPair(-1, sum);
        CustomPair autoPair = new CustomPair(-2, sum);
        CheckBox selectAllButton = new CheckBox(defautPair.toString());
        CheckBox autoPlayButton = new CheckBox(autoPair.toString());

        choiceBoxes.addRow(0);
        choiceBoxes.add(selectAllButton, 0, 0);
        selectAllButton.selectedProperty().addListener((observable, oldvalue, newvalue) -> {
            if (newvalue) {
                autoPlayButton.setSelected(false);
                for (int i = 2; i < choiceBoxes.getChildren().size(); i++) {
                    ((CheckBox) choiceBoxes.getChildren().get(i)).setSelected(true);
                }
            }
        });

        choiceBoxes.add(autoPlayButton, 1, 0);
        autoPlayButton.selectedProperty().addListener((observable, oldvalue, newvalue) -> {
            if (newvalue) {
                selectAllButton.setSelected(false);
                for (int i = 2; i < choiceBoxes.getChildren().size(); i++) {
                    ((CheckBox) choiceBoxes.getChildren().get(i)).setSelected(false);
                }
            }
        });

        for (int i = 0; i < 16; i++) {
            if (count[i] != 0) {
                CustomPair cp = new CustomPair(i, count[i]);
                CheckBox button = new CheckBox(cp.toString());
                int channelIndex = i;
                button.selectedProperty().addListener((observable, oldvalue, newvalue) -> {
                    player.setChanel(channelIndex, newvalue);
                    updateSelectedChoiceBox();
                });
                choiceBoxes.addRow(choiceBoxes.getChildren().size() - 1, button);
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
        circleTemp.setStroke(Color.BLACK);
        circleTemp.setStrokeWidth(10);
        circleTemp.setOpacity(0);
        this.getChildren().add(circleTemp);

        createArcs();

        noteProperty = new SimpleObjectProperty<Note>();
        noteProperty.setValue(new Note(-1, -1, -1));
        noteProperty.addListener((o, oldVal, newVal) -> {
            if (!newVal.equals(new Note(-1, -1, -1))) {
                player.stop();
                int firstNote = newVal.key % 12;

                for (final Tile tile : tilesTab) {
                    tile.arc.setFill(tile.mainColor);
                }
                if (firstNote == lastNote) {
                    circleTemp.setFill(Color.YELLOW);
                    circleTemp.setOpacity(1);
                    lastNote = -1;
                } else {
                    log.info("The note was {} and the name is {}", firstNote, NOTE_NAMES[firstNote]);
                    tilesTab.get(NOTE_NAMES[firstNote]).arc.setFill(Color.YELLOW);
                    lastNote = firstNote;
                }

            }
        });

        circleTemp.setFill(Color.YELLOW);
        circleTemp.setOpacity(1);

        this.getChildren().remove(circ);

        final EventHandler<Event> circleEvent = e -> {
            Color color2 = Color.BLACK;
            if (circleTemp.getFill() == Color.YELLOW) {
                triggerEvent(e);
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
        final Button openButton = new Button("Open", iv);
        openButton.setOpacity(1);

        choiceBoxes = new GridPane();

        BorderPane choiceBorderPane = new BorderPane();

        Button menuButton = new Button("Menu");
        menuButton.prefWidthProperty().bind(choiceBorderPane.widthProperty());
        menuButton.setPrefHeight(50);

        topBar.setTop(menuButton);
        topBar.setCenter(openButton);
        choiceBorderPane.setTop(new Text("Select channel(s) to play"));
        choiceBorderPane.setCenter(choiceBoxes);
        choiceBorderPane.getStyleClass().add("button");
        topBar.setBottom(choiceBorderPane);
        playTimeSlider(circleTemp);

        final Timeline buttonOpacityTimeline = new Timeline();
        buttonOpacityTimeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(1), new KeyValue(topBar.getTop().opacityProperty(), 0.1, Interpolator.LINEAR)));
        buttonOpacityTimeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(1), new KeyValue(topBar.getCenter().opacityProperty(), 0, Interpolator.LINEAR)));
        buttonOpacityTimeline.getKeyFrames().add(
            new KeyFrame(Duration.seconds(1), new KeyValue(topBar.getBottom().opacityProperty(), 0, Interpolator.LINEAR)));

        buttonOpacityTimeline.setDelay(Duration.seconds(2));
        buttonOpacityTimeline.play();

        topBar.setOnMouseEntered(evt -> {
            topBar.getTop().setOpacity(1);
            topBar.getCenter().setOpacity(1);
            topBar.getBottom().setOpacity(1);
        });

        topBar.setOnMouseExited(evt -> {
            topBar.getTop().setOpacity(0.1);
            topBar.getCenter().setOpacity(0);
            topBar.getBottom().setOpacity(0);
        });

        openButton.prefWidthProperty().bind(choiceBorderPane.widthProperty());
        openButton.setPrefHeight(20);
        iv.setPreserveRatio(true);
        iv.setFitHeight(openButton.getPrefHeight());
        openButton.setOnMousePressed(evt -> {
            try {
                loadMusic(true);
            } catch (final IOException | InvalidMidiDataException e) {
                e.printStackTrace();
            }
        });

        this.getChildren().add(topBar);

        try {
            loadMusic(false);
        } catch (final IOException | InvalidMidiDataException e) {
            e.printStackTrace();
        }
        stats.notifyNewRoundReady();
        circleTemp.setFill(Color.YELLOW);
        circ.setFill(Color.YELLOW);
        circleTemp.toFront();
        circleTemp.setOpacity(1);

        player = new MidiSequencerPlayer(sequence, noteProperty);
        player.setTempo(bpm);
        player.pianoReceiver.initPianorReceiverParameters();
        ((CheckBox) choiceBoxes.getChildren().get(0)).setSelected(true);
        resetSlider(slider, player.sequencer.getTickLength());
    }

    private void triggerEvent(Event e) {
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

    @Override
    public void dispose() {
        if (player != null) {
            player.stop();
            player.pianoReceiver = null;
            for (Transmitter t : player.sequencer.getTransmitters()) {
                t.getReceiver().close();
                t.close();
            }
            player.sequencer.stop();
        }
    }

    private void createArc(final int index, final double angle, final Color color1, final Color color2, final double l, final double origin) {
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double size = dimension2D.getHeight() / l;
        final double theta = ((index * 360d) / 7d - origin);
        final Tile createdTile = new Tile(centerX, centerY, size, size, theta, angle, circ);
        createdTile.mainColor = color1;
        createdTile.arc.setFill(color1);
        createdTile.arc.setStrokeWidth(10);
        createdTile.setVisible(true);

        final EventHandler<Event> tileEventEnter = e -> {
            if (tilesTab.get(((Tile) e.getTarget()).note).arc.getFill() == color1) {
                tilesTab.get(((Tile) e.getTarget()).note).arc.setFill(color2);
            } else if (tilesTab.get(((Tile) e.getTarget()).note).arc.getFill() == Color.YELLOW) {
                tilesTab.get(((Tile) e.getTarget()).note).arc.setFill(color1);
                triggerEvent(e);

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

    public void playTimeSlider(Circle circleTemp) {
        slider = new Slider();

        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(1);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1d);

        double width = circleTemp.getRadius();
        slider.setPrefWidth(width);
        slider.setLayoutX(centerX - width / 2);

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        slider.setLayoutY(dimension2D.getHeight() - 50);
        this.getChildren().add(slider);
    }

    public void resetSlider(Slider slider, long max) {
        slider.setMin(0);
        slider.setMax(max);
        slider.setValue(0);
        player.pianoReceiver.currentTickProperty.addListener((obj, oldval, newval) -> {
            slider.setValue(newval);
        });

        slider.valueProperty().addListener(sliderListener);
    }

}
