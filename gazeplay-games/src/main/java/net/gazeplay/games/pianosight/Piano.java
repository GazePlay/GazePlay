package net.gazeplay.games.pianosight;

import javafx.animation.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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

    private int firstNote;

    private Circle circ;
    private Circle circleTemp;
    private final List<Tile> tilesTab;
    private final Jukebox jukebox;

    private final Stats stats;

    private final IGameContext gameContext;

    private final Instru instru;

    private MidiReader midiReader;

    private final List<ImageView> fragments;

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

    private void loadMusic(final boolean b) throws IOException {
        if (b) {
            final String fileName = jukebox.getS();
            if (fileName == null) {
                return;
            }
            log.info("you loaded the song : " + fileName);
            final File f = new File(fileName);
            try (InputStream inputStream = new FileInputStream(f)) {
                loadMusicStream(inputStream);
            }
        } else {
            final String fileName = "RIVER.mid";
            log.info("you loaded the song : " + fileName);
            try (InputStream inputStream = Utils.getInputStream("data/pianosight/songs/" + fileName)) {
                loadMusicStream(inputStream);
            }
        }
    }

    private void loadMusicStream(final InputStream inputStream) {
        midiReader = new MidiReader(inputStream);
        firstNote = midiReader.nextNote();
        for (final Tile tile : tilesTab) {
            tile.arc.setFill(tile.color1);
        }

        if (firstNote != -1) {
            tilesTab.get(firstNote).arc.setFill(Color.YELLOW);
        }
    }

    @Override
    public void launch() {
        this.gameContext.resetBordersToFront();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        circ = new Circle(centerX, centerY, dimension2D.getHeight() / 4);
        circ.setFill(Color.BLACK);
        this.getChildren().add(circ);

        circleTemp = new Circle(centerX, centerY, dimension2D.getHeight() / 5);
        circleTemp.setFill(Color.BLACK);
        circleTemp.setStroke(Color.BLACK);
        circleTemp.setOpacity(0);
        this.getChildren().add(circleTemp);

        createArcs();

        this.getChildren().remove(circ);

        final EventHandler<Event> circleEvent = e -> {
            if (circleTemp.getFill() == Color.YELLOW) {
                if (firstNote != -1) {
                    final int precNote = firstNote;
                    final int precKey = midiReader.getKey();

                    final int index = midiReader.nextNote();
                    if (index > -1) {
                        firstNote = NOTE_NAMES[index];
                    } else {
                        firstNote = index;
                    }

                    instru.noteOn(precKey);
                    stats.incNbGoals();
                    stats.notifyNewRoundReady();

                    if (firstNote != -1) {
                        tilesTab.get(precNote).arc.setFill(tilesTab.get(precNote).color1);
                        circleTemp.setFill(Color.BLACK);
                        circleTemp.setOpacity(0);
                        if (firstNote != -1) {
                            tilesTab.get(firstNote).arc.setFill(Color.YELLOW);
                        } else {
                            tilesTab.get(firstNote).arc.setFill(tilesTab.get(precNote).color1);
                        }

                    } else {
                        tilesTab.get(precNote).arc.setFill(tilesTab.get(precNote).color1);
                        circleTemp.setFill(Color.BLACK);
                        circleTemp.setOpacity(0);
                    }

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
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        this.getChildren().add(b);

        try {

            loadMusic(false);

        } catch (final IOException e) {
            e.printStackTrace();
        }
        stats.notifyNewRoundReady();
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

            if (((Tile) e.getTarget()).note == firstNote) {

                final int precNote = firstNote;
                final int precKey = midiReader.getKey();

                final int index1 = midiReader.nextNote();
                if (index1 > -1) {
                    firstNote = NOTE_NAMES[index1];
                } else {
                    firstNote = index1;
                }

                if (precNote != -1 && tilesTab.get(precNote).arc.getFill() == Color.YELLOW) {
                    instru.noteOn(precKey);
                    stats.incNbGoals();
                    stats.notifyNewRoundReady();
                    double x;
                    double y;

                    if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
                        final MouseEvent me = (MouseEvent) e;
                        x = me.getX();
                        y = me.getY();
                    } else if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                        final GazeEvent ge = (GazeEvent) e;
                        x = ge.getX();
                        y = ge.getY();
                    } else {
                        x = centerX + size * Math.cos(Math.toRadians(-theta));
                        y = centerY + size * Math.sin(Math.toRadians(-theta));
                        explose(x, y);
                        final double theta1 = (((index1 + 1) * 360d) / 7d - origin);
                        x = centerX + size * Math.cos(Math.toRadians(-theta1));
                        y = centerY + size * Math.sin(Math.toRadians(-theta1));
                    }
                    explose(x, y);
                    if (firstNote != -1) {
                        if (tilesTab.get(firstNote).arc.getFill() == Color.YELLOW) {
                            tilesTab.get(precNote).arc.setFill(color2);
                            circleTemp.setFill(Color.YELLOW);
                            circleTemp.setOpacity(1);
                        } else {
                            tilesTab.get(precNote).arc.setFill(color2);
                            tilesTab.get(firstNote).arc.setFill(Color.YELLOW);
                        }

                    } else {
                        tilesTab.get(precNote).arc.setFill(color2);
                    }
                }

            } else {
                tilesTab.get(((Tile) e.getTarget()).note).arc.setFill(color2);

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
