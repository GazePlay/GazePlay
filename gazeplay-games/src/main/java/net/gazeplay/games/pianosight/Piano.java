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
import javafx.stage.Screen;
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

@Slf4j
public class Piano extends Parent implements GameLifeCycle {

    private static final int[] NOTE_NAMES = { 0, 7, 1, 8, 2, 3, 9, 4, 10, 5, 11, 6 };

    private double centerX;
    private double centerY;

    private int firstNote;

    private Circle circ;
    private Circle circleTemp;
    private List<Tile> tilesTab;
    private Jukebox jukebox;

    private final Stats stats;

    private final IGameContext gameContext;

    private final Instru instru;

    private MidiReader midiReader;

    private int nbFragments = 5;

    private final List<ImageView> fragments;

    public Piano(IGameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
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
        List<ImageView> fragments = new ArrayList<>(nbFragments);
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        for (int i = 0; i < nbFragments; i++) {

            String s = "data/pianosight/images/" + i + ".png";

            ImageView fragment = new ImageView(new Image(s));
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

    private void explose(double xcenter, double ycenter) {

        Timeline timeline = new Timeline();
        Timeline timeline2 = new Timeline();

        for (int i = 0; i < nbFragments; i++) {

            ImageView fragment = fragments.get(i);

            timeline.getKeyFrames().add(
                    new KeyFrame(new Duration(1), new KeyValue(fragment.xProperty(), xcenter, Interpolator.LINEAR)));
            timeline.getKeyFrames().add(
                    new KeyFrame(new Duration(1), new KeyValue(fragment.yProperty(), ycenter, Interpolator.EASE_OUT)));
            timeline.getKeyFrames().add(new KeyFrame(new Duration(1), new KeyValue(fragment.opacityProperty(), 1)));

            int worh = (int) (Math.random() * 4);

            double XendValue;
            double YendValue;
            if (worh == 0) {
                XendValue = 0;
                YendValue = Math.random() * Screen.getPrimary().getBounds().getHeight();
            } else if (worh == 1) {
                XendValue = Math.random() * Screen.getPrimary().getBounds().getWidth();
                YendValue = 0;
            } else if (worh == 2) {
                XendValue = Screen.getPrimary().getBounds().getWidth();
                YendValue = Math.random() * Screen.getPrimary().getBounds().getHeight();
            } else {
                XendValue = Math.random() * Screen.getPrimary().getBounds().getWidth();
                YendValue = Screen.getPrimary().getBounds().getHeight();
            }

            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000),
                    new KeyValue(fragment.xProperty(), XendValue, Interpolator.LINEAR)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000),
                    new KeyValue(fragment.yProperty(), YendValue, Interpolator.EASE_OUT)));
            timeline2.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(fragment.opacityProperty(), 0)));
        }

        SequentialTransition sequence = new SequentialTransition();
        sequence.getChildren().addAll(timeline, timeline2);
        sequence.play();

    }

    private void loadMusic(boolean b) throws IOException {
        InputStream inputStream;
        String fileName;
        if (b) {
            fileName = jukebox.getS();
            if (fileName == null) {
                return;
            } else {
                File f = new File(fileName);
                inputStream = new FileInputStream(f);
            }
        } else {
            fileName = "RIVER.mid";
            inputStream = Utils.getInputStream("data/pianosight/songs/" + fileName);
        }
        log.info("you loaded the song : " + fileName);
        midiReader = new MidiReader(inputStream);
        firstNote = midiReader.nextNote();
        for (Tile tile : tilesTab) {
            tile.arc.setFill(tile.color1);
        }

        if (firstNote != -1) {
            tilesTab.get(firstNote).arc.setFill(Color.YELLOW);
        }
    }

    @Override
    public void launch() {
        this.gameContext.resetBordersToFront();

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
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

        EventHandler<Event> circleEvent = e -> {
            if (circleTemp.getFill() == Color.YELLOW) {
                if (firstNote != -1) {
                    int precNote = firstNote;
                    int precKey = midiReader.getKey();

                    int index = midiReader.nextNote();
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
        ImageView iv = new ImageView(new Image("data/pianosight/images/1.png"));
        Button b = new Button("Open", iv);
        b.setPrefWidth(dimension2D.getWidth() / 7);
        b.setPrefHeight(dimension2D.getHeight() / 7);
        iv.setPreserveRatio(true);
        iv.setFitHeight(b.getPrefHeight());
        // TODO add a replay button
        b.setOnMousePressed(evt -> {
            try {
                loadMusic(true);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        this.getChildren().add(b);

        try {

            loadMusic(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
        stats.notifyNewRoundReady();
    }

    @Override
    public void dispose() {

    }

    private void createArc(int index, double angle, Color color1, Color color2, double l, double origin) {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double size = dimension2D.getHeight() / l;
        double theta = ((index * 360d) / 7d - origin);
        Tile a3 = new Tile(centerX, centerY, size, size, theta, angle, circ);
        a3.color1 = color1;
        a3.color2 = color2;
        a3.arc.setFill(color1);
        a3.arc.setStrokeWidth(10);
        a3.setVisible(true);

        EventHandler<Event> tileEventEnter = e -> {

            if (((Tile) e.getTarget()).note == firstNote) {

                int precNote = firstNote;
                int precKey = midiReader.getKey();

                int index1 = midiReader.nextNote();
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
                        MouseEvent me = (MouseEvent) e;
                        x = me.getX();
                        y = me.getY();
                    } else if (e.getEventType() == GazeEvent.GAZE_ENTERED) {
                        GazeEvent ge = (GazeEvent) e;
                        x = ge.getX();
                        y = ge.getY();
                    } else {
                        x = centerX + size * Math.cos(Math.toRadians(-theta));
                        y = centerY + size * Math.sin(Math.toRadians(-theta));
                        explose(x, y);
                        double theta1 = (((index1 + 1) * 360d) / 7d - origin);
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

        EventHandler<Event> tileEventExited = e -> {
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
            double angle = 360d / 7d;
            createArc(i, angle, Color.GHOSTWHITE, Color.GAINSBORO.darker(), 2.3, -90 + (720d / 7d));
        }

        for (int i = 7; i < 14; i++) {
            double angle = 360d / 14d;
            createArc(i, angle, Color.BLACK, Color.DIMGREY.darker(), 2.7, -90 + (720d / 7d) + 2 * angle + angle / 2);
        }

    }

}
