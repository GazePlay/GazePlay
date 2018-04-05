package net.gazeplay.games.moles;

import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.ConfigurationBuilder;
import net.gazeplay.commons.utils.games.Utils;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.memory.MemoryCard;
import net.gazeplay.games.memory.Memory.RoundDetails;
import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

@Slf4j
public class Moles extends Parent implements GameLifeCycle {

    private static final float cardRatio = 0.75f;

    private static final int minHeight = 30;

    public final int nbHoles = 9;

    @Data
    @AllArgsConstructor
    public class RoundDetails {
        public final List<MolesChar> molesList;
    }

    private final GameContext gameContext;

    private final Stats stats;

    public Rectangle terrain;

    public int nbMolesWacked;

    public RoundDetails currentRoundDetails;

    public Moles(GameContext gameContext, Stats stats) {
        super();

        this.gameContext = gameContext;
        this.stats = stats;

        /* Creation du terrain avec les trous */

        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        log.info("dimension2D = {}", dimension2D);

        Rectangle imageRectangle = new Rectangle(0, 0, dimension2D.getWidth(), dimension2D.getHeight());
        imageRectangle.setFill(new ImagePattern(new Image("data/wackmole/images/terrainTaupes.jpg")));
        gameContext.getChildren().add(imageRectangle);
        gameContext.getChildren().add(this);

    }

    @Override
    public void launch() {
        Configuration config = ConfigurationBuilder.createFromPropertiesResource().build();

        List<MolesChar> molesList = initMoles(config);

        currentRoundDetails = new RoundDetails(molesList);

        gameContext.getChildren().addAll(molesList);

        stats.start();
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

    public void chooseMoleToOut() {
        if (this.currentRoundDetails == null) {
            return;
        }
        Random r = new Random();
        int indice;
        do { // Select a mole not out for the moment
            indice = r.nextInt(nbHoles);
        } while (currentRoundDetails.molesList.get(indice).out);
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

        /* Creation and placement of moles in the field */
        result.add(new MolesChar(0.0865 * width, 0.325 * height, moleWidth, moleHeight, gameContext, stats, this));
        result.add(new MolesChar(0.39 * width, 0.383 * height, moleWidth, moleHeight, gameContext, stats, this));
        result.add(new MolesChar(0.733 * width, 0.308 * height, moleWidth, moleHeight, gameContext, stats, this));
        result.add(new MolesChar(0.2 * width, 0.551 * height, moleWidth, moleHeight, gameContext, stats, this));
        result.add(new MolesChar(0.573 * width, 0.545 * height, moleWidth, moleHeight, gameContext, stats, this));
        result.add(new MolesChar(0.855 * width, 0.535 * height, moleWidth, moleHeight, gameContext, stats, this));
        result.add(new MolesChar(0.0577 * width, 0.749 * height, moleWidth, moleHeight, gameContext, stats, this));
        result.add(new MolesChar(0.405 * width, 0.755 * height, moleWidth, moleHeight, gameContext, stats, this));
        result.add(new MolesChar(0.717 * width, 0.745 * height, moleWidth, moleHeight, gameContext, stats, this));

        return result;

    }

    private static double computeMoleHeight(Dimension2D gameDimension2D) {
        return gameDimension2D.getHeight() * 0.14;
    }

    private static double computeMoleWidth(Dimension2D gameDimension2D) {
        return gameDimension2D.getWidth() * 0.13;
    }

}
