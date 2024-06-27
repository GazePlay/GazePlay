package net.gazeplay.games.cups2;

import javafx.scene.paint.Color;
import javafx.util.Callback;
import lombok.Getter;
import net.gazeplay.games.cups2.action.Action;
import net.gazeplay.games.cups2.strategy.*;

import java.util.*;

public class Config {
    // Fixed config
    public static final boolean DEBUG = true;

    public final static double ROUND_DELAY = 1000;
    public static final double INTER_ROUND_DELAY = 200;

    public final static StrategyBuilder.Type STRATEGY_TYPE = StrategyBuilder.Type.ADAPTIVE;

    public final static String CUP_IMAGE_PATH = "data/cups2/images/cup.png";
    public final static double CUP_MARGIN = 70;
    public static final double CUP_WIDTH = 170;

    public final static Color BALL_COLOR = Color.BLUEVIOLET;
    public static final double BALL_RADIUS = 20;

    public static final double ACTION_REVEAL_TIME = 1500;  // Except for REVEAL, all action times are on a per-index basis
    public static final double ACTION_EXCHANGE_TIME = 1300;
    public static final double ACTION_FAKE_EXCHANGE_TIME = 1300;
    public static final double ACTION_CYCLE_TIME = 1000;
    public static final double ACTION_FAKE_CYCLE_TIME = 1000;
    public static final double ACTION_TRICK_TIME = 1500;
    public static final double ACTION_FAKE_TRICK_TIME = 1500;

    // The difficulty is not really used anymore,
    // but initially it was used by PlayerModel and Adaptive Strategy to determine the whole difficulty of a round
    public static final double ACTION_REVEAL_DIFFICULTY = 0;
    public static final double ACTION_EXCHANGE_DIFFICULTY = 1;
    public static final double ACTION_FAKE_EXCHANGE_DIFFICULTY = 2;
    public static final double ACTION_CYCLE_DIFFICULTY = 1;
    public static final double ACTION_FAKE_CYCLE_DIFFICULTY = 1.5;
    public static final double ACTION_TRICK_DIFFICULTY = 3;
    public static final double ACTION_FAKE_TRICK_DIFFICULTY = 2;

    public static final int MIN_NB_CUPS = 3;
    public static final int MAX_NB_CUPS = 7;
    public static final double MIN_SPEED_FACTOR = 0.5;
    public static final double MAX_SPEED_FACTOR = 6;
    public static final int MIN_ACTIONS_PER_ROUND = 12;
    public static final int MAX_ACTIONS_PER_ROUND = 18;

    public static final List<Action.Type> ADAPTIVE_IMMUTABLE_POOL = List.of(
        Action.Type.EXCHANGE,
        Action.Type.CYCLE
    );
    public static final double ADAPTIVE_INTRODUCE_FEATURE_THRESHOLD = 0.8;
    public static final double ADAPTIVE_REMOVE_FAKENESS_THRESHOLD = 0.1;
    public static final double ADAPTIVE_INTRODUCE_FAKENESS_THRESHOLD = 0.8;

    public static final int PLAYER_BALL_TRACKING_COOLDOWN = 2;  // 2 actions before considering the player has lost or found again the ball


    // Game state (dynamic)
    @Getter
    private static int nbCups = 0;  // Will get initialized by the game anyway
    private final static List<Callback<Void, Void>> nbCupsWatchers = new ArrayList<>();
    public static void setNbCups(int newValue) {
        nbCups = Math.min(Math.max(MIN_NB_CUPS, newValue), MAX_NB_CUPS);
        for (Callback<Void, Void> watcher : List.copyOf(nbCupsWatchers))
            watcher.call(null);
    }
    public static void nbCupsSubscribe(Callback<Void, Void> watcher) {
        nbCupsWatchers.add(watcher);
    }
    public static void nbCupsUnsubscribe(Callback<Void, Void> watcher) {
        nbCupsWatchers.remove(watcher);
    }

    @Getter
    private static double speedFactor = 1;
    public static void setSpeedFactor(double newValue) {
        speedFactor = Math.min(Math.max(MIN_SPEED_FACTOR, newValue), MAX_SPEED_FACTOR);
    }
}
