package net.gazeplay.games.simon;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.utils.stats.Stats;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Game "Simon": the computer plays a sequence of colored notes, and the player repeats it by fixations; a note is added
 * at each success. Variants: easy, normal and hard (longer sequences), "Simon copy you" (the computer repeats the
 * sequence of the player), and multiplayer.
 */
public class Simon  extends Parent implements GameLifeCycle {


    private IGameContext gameContext;
    private Stats stats;
    private SimonGameVariant gameVariant;
    protected ArrayList<String> computerSequence;
    protected ArrayList<String> playerSequence;
    protected ArrayList<String> musicNotes;
    private final ArrayList<String> couleurs;

    public Simon(final IGameContext gameContext, final Stats stats, SimonGameVariant gameVariant){
        this.gameContext = gameContext;
        this.stats = stats;
        this.gameVariant = gameVariant;
        this.computerSequence = new ArrayList<>();
        this.playerSequence = new ArrayList<>();
        this.couleurs = new ArrayList<>();
        this.musicNotes = new ArrayList<>();
    }

    @Override
    public void launch() {
        this.stats.reset();
        this.computerSequence.clear();
        this.playerSequence.clear();
        this.couleurs.clear();
        this.musicNotes.clear();
        final Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        gameContext.setLimiterAvailable();
        Rectangle background = new Rectangle(0,0,dimension2D.getWidth(),dimension2D.getHeight());
        background.setFill(Color.WHITE);

        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.getChildren().add(background);
        initMusicNotes();
        startGame();
        stats.notifyNewRoundReady();
        gameContext.firstStart();
    }

    /**
     * Gives the four buttons four notes drawn at random.
     */
    private void initMusicNotes(){
        ArrayList<String> temp = new ArrayList<>();
        temp.add("do");
        temp.add("fa");
        temp.add("la");
        temp.add("mi");
        temp.add("re");
        temp.add("si");
        temp.add("sol");
        Collections.shuffle(temp);

        for (int i = 0; i < 4 ; i++){
            this.musicNotes.add(temp.get(i));
        }

    }

    /**
     * Plays a note, at the volume of the effects.
     */
    protected void playSound(String soundPath){
        Configuration config = gameContext.getConfiguration();
        if (config.isSoundEnabled()){
            gameContext.getSoundManager().add("data/simonGame/songs/"+soundPath+".wav");
        }
    }


    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }
    /**
     * Starts the game: the device and the first note of the computer.
     */
    public void startGame(){

        couleurs.add("vert");
        couleurs.add("rouge");
        couleurs.add("jaune");
        couleurs.add("bleu");
        Borne borne = new Borne(gameContext,this);
        borne.nbNoteMax = 33;
        if (gameVariant.equals(SimonGameVariant.EASY_CLASSIC)){
            borne.nbNoteMax = 10;
        }else if (gameVariant.equals(SimonGameVariant.NORMAL_CLASSIC)){
            borne.nbNoteMax = 22;
        }
        if (!gameVariant.equals(SimonGameVariant.MODE2)){
            addNoteToComputerSequence();
        }else{
            borne.simonCopy = true;
        }

        if (gameVariant.equals(SimonGameVariant.MODE3)){
            borne.multiplayer = true;
        }
        borne.generateBorne();

    }

    /**
     * Adds a color drawn at random to the sequence of the computer.
     */
    protected void addNoteToComputerSequence(){
        Collections.shuffle(couleurs);
        computerSequence.add(couleurs.get(0));
    }


    /**
     * Ends the game and shows the statistics.
     */
    public void endGame(){
        dispose();
        gameContext.showRoundStats(stats,this);
    }
}
