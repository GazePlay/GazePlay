package net.gazeplay.games.cups2;

import javafx.scene.paint.Color;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;
import net.gazeplay.games.cups2.strategy.*;

import java.util.HashSet;
import java.util.Set;

public class Config {
    // Fixed config
    public final static double ROUND_DELAY = 1000;

    public final static StrategyBuilder.Type STRATEGY_TYPE = StrategyBuilder.Type.SIMPLE;

    public final static String CUP_IMAGE_PATH = "data/cups/images/cup.png";
    public final static double CUP_MARGIN = 20;
    public static final double CUP_WIDTH = 200;

    public final static Color BALL_COLOR = Color.BLUEVIOLET;
    public static final double BALL_RADIUS = 20;

    public static final double ACTION_REVEAL_TIME = 1500;
    public static final double ACTION_EXCHANGE_TIME = 2000;
    public static final double ACTION_FAKE_EXCHANGE_TIME = 2000;
    public static final double ACTION_CYCLE_TIME = 2000;
    public static final double ACTION_FAKE_CYCLE_TIME = 2000;
    public static final double ACTION_TRICK_TIME = 2000;
    public static final double ACTION_FAKE_TRICK_TIME = 2000;


    // Game state (dynamic)
    @Getter
    private static int nbCups = 0;  // Will get initialized by the game anyway
    private final static Set<Callback<Void, Void>> nbCupsWatchers = new HashSet<>();
    public static void setNbCups(int newValue) {
        nbCups = newValue;
        for (Callback<Void, Void> watcher : nbCupsWatchers)
            watcher.call(null);
    }
    public static void nbCupsSubscribe(Callback<Void, Void> watcher) {
        nbCupsWatchers.add(watcher);
    }
    public static void nbCupsUnsubscribe(Callback<Void, Void> watcher) {
        nbCupsWatchers.remove(watcher);
    }

    @Getter
    @Setter
    private static double speedFactor = 1;
}
