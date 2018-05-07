package net.gazeplay.games.moles;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.stats.Stats;
import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
@Slf4j
public class Moles extends Parent implements GameLifeCycle {

    public final int nbHoles = 9;
    
    public final int timeGame = 60000; // Game duration = 1 minute
            
    private ProgressIndicator progressIndicator;

    @Data
    @AllArgsConstructor
    public class RoundDetails {
        public final List<MolesChar> molesList;
    }

    private final GameContext gameContext;

    private final Stats stats;

    public Rectangle terrain;

    public int nbMolesWacked;
    
    public int nbMolesOut;

    private Label lab; 
    
    public RoundDetails currentRoundDetails;

    public Moles(GameContext gameContext, Stats stats) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);

        Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.setFill(new ImagePattern(new Image("data/wackmole/images/terrainTaupes.jpg")));
        gameContext.getChildren().add(imageRectangle);
        gameContext.getChildren().add(this);

    }

    @Override
    public void launch() {
    	
    	/* Affichage du score */
        lab = new Label();
        String s = "Score:" + nbMolesWacked;
        lab.setText(s);
        lab.setTextFill(Color.WHITE);
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        lab.setFont(Font.font(dimension2D.getHeight()/10)); 
        lab.setLineSpacing(10);
        lab.setLayoutX(0.4 * dimension2D.getWidth());
        lab.setLayoutY(0.1 * dimension2D.getHeight());
        this.getChildren().add(lab);
    	
        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        List<MolesChar> molesList = initMoles(config);

        currentRoundDetails = new RoundDetails(molesList);

        gameContext.getChildren().addAll(molesList);

        stats.notifyNewRoundReady();

        play(dimension2D);
        
    }
    
    /* Moles get out randomly */
    private void play(Dimension2D gameDim2D ) {

        progressIndicator = createProgressIndicator(gameDim2D);
        this.getChildren().add(this.progressIndicator);
        
        nbMolesOut = 0;
        nbMolesWacked = 0;
        Random r = new Random();

        Timer minuteur = new Timer();
        TimerTask tache = new TimerTask() {
        	public void run() {

                if(nbMolesOut < 2) {
                	chooseMoleToOut();
                }
                else if((r.nextInt()%7 == 0)&&(nbMolesOut <= 4)) {
            		chooseMoleToOut();
            	}
                else if((r.nextInt()%16 == 0)&&(nbMolesOut <= 6)) {
            		chooseMoleToOut();
            	}

        	}
        };
        minuteur.schedule(tache,0,500);
    }

    
    
   private ProgressIndicator createProgressIndicator(javafx.geometry.Dimension2D gameDim2D) {
        ProgressIndicator indicator = new ProgressIndicator(0);
        indicator.setTranslateX(gameDim2D.getWidth() - gameDim2D.getWidth()*0.1);
        indicator.setTranslateY(gameDim2D.getHeight()*0.1);
        indicator.setMinWidth(computeMoleWidth(gameDim2D) * 0.9);
        indicator.setMinHeight(computeMoleWidth(gameDim2D) * 0.9);
        indicator.setOpacity(1);
        return indicator;
    }
    
    @Override
    public void dispose() {
        if (currentRoundDetails != null) {
            if (currentRoundDetails.molesList != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.molesList);
            }
            currentRoundDetails = null;
        }
    }

    /* Select a mole not out for the moment and call "getOut()" */
    public void chooseMoleToOut() {
        if (this.currentRoundDetails == null) {
            return;
        }
        Random r = new Random();
        int indice;
        do { 
            indice = r.nextInt(nbHoles);
        } while (!currentRoundDetails.molesList.get(indice).canGoOut);
        MolesChar m = currentRoundDetails.molesList.get(indice);
        m.getOut();
    }

    private List<MolesChar> initMoles(Configuration config) {
        javafx.geometry.Dimension2D gameDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        ArrayList<MolesChar> result = new ArrayList<>();

        double moleHeight = computeMoleHeight(gameDimension2D);
        double moleWidth = computeMoleWidth(gameDimension2D);
        double height = gameDimension2D.getHeight();
        double width = gameDimension2D.getWidth();
        double distTrans = computeDistTransMole(gameDimension2D);

        /* Creation and placement of moles in the field */
        result.add(new MolesChar(0.0865 * width, 0.325 * height 
        		, moleWidth, moleHeight,distTrans, gameContext, stats, this));
        result.add(new MolesChar(0.39 * width, 0.383 * height , moleWidth, moleHeight,distTrans, gameContext, stats, this));
        result.add(new MolesChar(0.733 * width, 0.308 * height, moleWidth, moleHeight,distTrans, gameContext, stats, this));
        result.add(new MolesChar(0.2 * width, 0.551 * height, moleWidth, moleHeight,distTrans, gameContext, stats, this));
        result.add(new MolesChar(0.573 * width, 0.545 * height, moleWidth, moleHeight,distTrans, gameContext, stats, this));
        result.add(new MolesChar(0.855 * width, 0.535 * height, moleWidth, moleHeight,distTrans, gameContext, stats, this));
        result.add(new MolesChar(0.0577 * width, 0.749 * height, moleWidth, moleHeight,distTrans, gameContext, stats, this));
        result.add(new MolesChar(0.405 * width, 0.755 * height, moleWidth, moleHeight,distTrans, gameContext, stats, this));
        result.add(new MolesChar(0.717 * width, 0.745 * height, moleWidth, moleHeight,distTrans, gameContext, stats, this));

        return result;

    }

    private static double computeDistTransMole(Dimension2D gameDimension2D) {
        return gameDimension2D.getHeight() * 0.16;
    }
    
    private static double computeMoleHeight(Dimension2D gameDimension2D) {
        return gameDimension2D.getHeight() * 0.14;
    }

    private static double computeMoleWidth(Dimension2D gameDimension2D) {
        return gameDimension2D.getWidth() * 0.13;
    }
    
    public void OneMoleWacked() {
    	nbMolesWacked ++;
    	String s = "Score:" + nbMolesWacked;
    	lab.setText(s);    	
    }

}
