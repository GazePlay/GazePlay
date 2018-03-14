package net.gazeplay.games.pianosight;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.utils.stats.Stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.games.shooter.Point;
import net.gazeplay.games.shooter.Shooter;
import net.gazeplay.games.shooter.Target;

/**
 * Created by schwab on 28/08/2016.
 */
@Slf4j
public class Piano extends Parent implements GameLifeCycle {

    private static final int maxRadius = 70;
    private static final int minRadius = 30;

    private static final int maxTimeLength = 7;
    private static final int minTimeLength = 4;

    // private final EventHandler<Event> enterEvent;

    private static double centerX;
    private static double centerY;

    private Tile A;
    private Tile B;
    private Tile C;
    private Tile D;
    private Tile E;
    private Tile F;
    private Tile G;

    private final Stats stats;

    private final GameContext gameContext;

    private final Instru instru;

    private Parser parser;

    // done
    public Piano(GameContext gameContext, Stats stats) {
        this.gameContext = gameContext;
        this.stats = stats;
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        centerX = dimension2D.getWidth() / 2;
        centerY = dimension2D.getHeight() / 2;

        A = new Tile();
        B = new Tile();
        C = new Tile();
        D = new Tile();
        E = new Tile();
        F = new Tile();
        G = new Tile();

        instru = new Instru();
        /*Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.setFill(new ImagePattern(new Image("data/biboule/images/Background.jpg")));
        gameContext.getChildren().add(imageRectangle);*/
        gameContext.getChildren().add(this);
    }

    @Override
    public void launch() {
        this.gameContext.resetBordersToFront();

        CreateArcs();
        try {
            parser = new Parser();
            Path filePath = Paths.get("gazeplay-data\\src\\main\\resources\\data\\pianosight\\songs\\AuClairDeLaLune.txt");
            filePath = filePath.toAbsolutePath();
            FileReader input = new FileReader(filePath.toFile());
            parser.bufRead = new BufferedReader(input);
            parser.myLine = null;
            parser.myLine = parser.bufRead.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {

    }

    public int getNote(char c) {
        int note;
        if (c == 'G') {
            note = 55;
        } else if (c == 'A') {
            note = 57;
        } else if (c == 'B') {
            note = 59;
        } else if (c == 'C') {
            note = 60;
        } else if (c == 'D') {
            note = 62;
        } else if (c == 'E') {
            note = 64;
        } else {
            note = 65;
        }
        return note;
    }

    public void CreateArc(int index, double angle) {
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        double size = dimension2D.getHeight() / 3;
        Arc a3 = new Arc(centerX, centerY, size, size, index * (360 / 7), angle);
        a3.setType(ArcType.ROUND);
        a3.setStroke(Color.BLACK);
        a3.setFill(Color.AQUA);
        a3.setStrokeWidth(10);
        int note = 55;
        a3.setVisible(true);

        
        EventHandler<Event> tileEventEnter = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                char c = 'A';
                try {
                    c = parser.nextChar();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                instru.note_on(getNote(c));
            }
        };
        EventHandler<Event> tileEventExit = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
                instru.note_off(note);
            }
        };
        

        a3.addEventHandler(GazeEvent.ANY, tileEventEnter);
        //this.addEventFilter(GazeEvent.GAZE_EXITED, tileEventExit);
        a3.addEventHandler(MouseEvent.MOUSE_ENTERED, tileEventEnter);
        a3.addEventHandler(MouseEvent.MOUSE_EXITED, tileEventExit);
        this.getChildren().add(a3);
    }

    public void CreateArcs() {
        for (int i = 0; i < 7; i++) {
            double angle = 360 / 7;
            if (i == 6) {
                angle = 360 - 6 * angle;
            }
            CreateArc(i, angle);

        }

    }

}
