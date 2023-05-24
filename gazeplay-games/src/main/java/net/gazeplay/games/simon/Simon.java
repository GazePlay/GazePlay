package net.gazeplay.games.simon;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;
import java.util.Collections;

public class Simon  extends Parent implements GameLifeCycle {


    private IGameContext gameContext;
    private Stats stats;
    private SimonGameVariant gameVariant;
    protected ArrayList<String> computerSequence;
    protected ArrayList<String> playerSequence;
    private final ArrayList<String> notes;

    public Simon(final IGameContext gameContext, final Stats stats, SimonGameVariant gameVariant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.computerSequence = new ArrayList<>();
        this.playerSequence = new ArrayList<>();
        this.notes = new ArrayList<>();
        notes.add("vert");
        notes.add("rouge");
        notes.add("jaune");
        notes.add("bleu");
    }

    public void startGame(){

        Borne borne = new Borne(gameContext,this);
        if (gameVariant.equals(SimonGameVariant.EASY_CLASSIC)  || gameVariant.equals(SimonGameVariant.EASY_MULTIPLAYER)){
            borne.secondsReset = 10;
        }else if (gameVariant.equals(SimonGameVariant.NORMAL_CLASSIC) || gameVariant.equals(SimonGameVariant.NORMAL_MULTIPLAYER)){
            borne.secondsReset = 8;
        }else if (gameVariant.equals(SimonGameVariant.HARD_CLASSIC)  || gameVariant.equals(SimonGameVariant.HARD_MULTIPLAYER)){
            borne.secondsReset = 6;
        }
        if (!gameVariant.equals(SimonGameVariant.MODE2)){
            addNoteToComputerSequence();
        }else{
            borne.simonCopy = true;
        }

        if (gameVariant.equals(SimonGameVariant.EASY_MULTIPLAYER) || gameVariant.equals(SimonGameVariant.NORMAL_MULTIPLAYER) || gameVariant.equals(SimonGameVariant.HARD_MULTIPLAYER)){
            borne.multiplayer = true;
        }


    }

    protected void addNoteToComputerSequence(){

        Collections.shuffle(notes);
        computerSequence.add(notes.get(0));
    }

    @Override
    public void launch() {
        this.stats.reset();

        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.setLimiterAvailable();
        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        background.setFill(Color.WHITE);

        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.getChildren().add(background);

        startGame();

        stats.notifyNewRoundReady();
        gameContext.firstStart();
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    public void endGame(){
        dispose();
        gameContext.showRoundStats(stats,this);
    }
}
