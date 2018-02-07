package net.gazeplay.games.magiccards;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import net.gazeplay.GazePlay;
import net.gazeplay.GraphicalContext;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameConfigurationScreen;
import net.gazeplay.HomeMenuScreen;
import net.gazeplay.commons.gaze.devicemanager.GazeEvent;
import net.gazeplay.commons.utils.GamePane;

/**
 * This class is used to display a configuration screen for MagicCard so the user can choose this game particular
 * configuration.
 * 
 * @author Thomas MEDARD
 */
@Slf4j
public final class MagicCardConfigScreen implements GameConfigurationScreen {

    /**
     * The game itself
     */
    private final MagicCards game;

    /**
     * The root of the configuration screen. Don't forget to remove it before launching the game.
     */
    private BorderPane configRoot;

    /**
     * The real root. WARNING : do not alter this other than for things from this configuration screen.
     */
    private final Pane root;

    /**
     * The number of game mods.
     */
    public static final int NB_MODS = 4;

    /**
     * The constructor.
     * 
     * @param graphicContext
     *            The root to use to display this configuration screen.
     * @param game
     *            The magic card game to configure.
     */
    public MagicCardConfigScreen(GraphicalContext<Pane> graphicContext, MagicCards game) {

        this.game = game;

        // The real root
        this.root = graphicContext.getRoot();
    }

    /**
     * @see GameConfigurationScreen#displayConfigurationScreen()
     */
    @Override
    public void displayConfigurationScreen() {

        // The configuration screen root
        this.configRoot = new BorderPane();

        Stage primaryStage = GazePlay.getInstance().getPrimaryStage();
        root.getChildren().add(this.configRoot);

        Pane gameMods = this.createGameModePane();
        this.configRoot.setCenter(gameMods);

        // Center the config root
        // TODO : Compute the width, or update position after display
        double x = (primaryStage.getWidth() / 2) - this.configRoot.getWidth();
        double y = (primaryStage.getHeight() / 2) - this.configRoot.getHeight();

        log.info("Config root : {} {}", this.configRoot.getWidth(), this.configRoot.getHeight());
        this.configRoot.relocate(x, y);
    }

    /**
     * Create the Pane that will contains all game mods panes. If one of the game pane is selected, then the game will
     * launch with the corresponding configuration. TODO : make something so we can automate the creation of all game
     * mods (game mode list ?).
     * 
     * @return The Pan that contains all game mods panes.
     */
    public Pane createGameModePane() {

        GridPane gamesGrid = new GridPane();
        gamesGrid.setHgap(10);
        gamesGrid.setVgap(5);

        int nbCol = HomeMenuScreen.NB_GAME_PER_ROW;

        // Not used for now
        // int nbRow = HomeMenuScreen.gcd(MagicCardConfigScreen.NB_MODS, nbCol);

        int i = 0;

        // All game mods
        // 2 x 2
        Pane gamePane = this.createGameModePane("2 x 2", 2, 2);
        gamesGrid.add(gamePane, i % nbCol, i / nbCol);
        GridPane.setHgrow(gamePane, Priority.ALWAYS);
        ++i;

        // 2 x 3
        gamePane = this.createGameModePane("2 x 3", 2, 3);
        gamesGrid.add(gamePane, i % nbCol, i / nbCol);
        GridPane.setHgrow(gamePane, Priority.ALWAYS);
        ++i;

        // 3 x 2
        gamePane = this.createGameModePane("3 x 2", 3, 2);
        gamesGrid.add(gamePane, i % nbCol, i / nbCol);
        GridPane.setHgrow(gamePane, Priority.ALWAYS);
        ++i;

        // 3 x 3
        gamePane = this.createGameModePane("3 x 3", 3, 3);
        gamesGrid.add(gamePane, i % nbCol, i / nbCol);
        GridPane.setHgrow(gamePane, Priority.ALWAYS);
        ++i;

        return gamesGrid;
    }

    /**
     * Create one game mode Pane. When selected, this pane will launch the game with the corresponding configuration.
     * 
     * @param gameLabel
     *            The label corresponding to the game mode.
     * @param cardRow
     *            The number of cards row.
     * @param cardCol
     *            The number of cards columns.
     * @return The game mode Pane.
     */
    private Pane createGameModePane(String gameLabel, int cardRow, int cardCol) {
        // The main gamePane
        BorderPane gamePane = new GamePane(gameLabel);

        // Add a listener to launch the game when clicked or gazed at.
        // TODO : see if this is suffecient for eye tracking use. Maybe we will need
        // some timed choice.
        EventHandler<Event> enterEvent = (Event e) -> {
            if (e.getEventType() == MouseEvent.MOUSE_CLICKED || e.getEventType() == GazeEvent.GAZE_ENTERED) {

                // Remove the configuration screen.
                this.root.getChildren().remove(this.configRoot);
                // Launch the game with the corresponding configuration
                this.game.beginPlay(cardRow, cardCol);
            }
        };

        gamePane.addEventFilter(MouseEvent.ANY, enterEvent);
        gamePane.addEventFilter(GazeEvent.ANY, enterEvent);

        return gamePane;
    }
}
