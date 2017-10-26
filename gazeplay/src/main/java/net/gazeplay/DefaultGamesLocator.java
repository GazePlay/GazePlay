package net.gazeplay;

import creampie.CreamPie;
import lombok.extern.slf4j.Slf4j;
import magiccards.Card;
import net.gazeplay.games.blocs.Blocs;
import net.gazeplay.games.blocs.BlocsGamesStats;
import net.gazeplay.games.bubbles.Bubble;
import ninja.Ninja;
import utils.games.stats.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {


    @Override
    public List<GameSpec> listGames() {

        List<GameSpec> result = new ArrayList<>();

        result.add(new GameSpec("\tCreampie", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new CreampieStats(scene);
            CreamPie.launch(root, scene, (CreampieStats) stats);
            return stats;
        }));

        result.add(new GameSpec("\tNinja Portraits", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new NinjaStats(scene);
            Ninja.launch(root, scene, (ShootGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("Magic Cards\t\t(2x2)", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new MagicCardsGamesStats(scene);
            Card.addCards(root, scene, cbxGames, 2, 2, (HiddenItemsGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("Magic Cards\t\t(2x3)", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new MagicCardsGamesStats(scene);
            Card.addCards(root, scene, cbxGames, 2, 3, (HiddenItemsGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("Magic Cards\t\t(3x2)", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new MagicCardsGamesStats(scene);
            Card.addCards(root, scene, cbxGames, 3, 2, (HiddenItemsGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("Magic Cards\t\t(3x3)", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new MagicCardsGamesStats(scene);
            Card.addCards(root, scene, cbxGames, 3, 3, (HiddenItemsGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("blocks\t\t\t(2x2)", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new BlocsGamesStats(scene);
            Blocs.makeBlocks(scene, root, cbxGames, 2, 2, true, 1, false, (HiddenItemsGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("blocks\t\t\t(2x3)", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new BlocsGamesStats(scene);
            Blocs.makeBlocks(scene, root, cbxGames, 2, 3, true, 1, false, (HiddenItemsGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("blocks\t\t\t(3x3)", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new BlocsGamesStats(scene);
            Blocs.makeBlocks(scene, root, cbxGames, 3, 3, true, 1, false, (HiddenItemsGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("\tCarte à gratter", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new ScratchcardGamesStats(scene);
            Blocs.makeBlocks(scene, root, cbxGames, 100, 100, false, 0.6f, true, (HiddenItemsGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("\tColored Bubbles", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new BubblesGamesStats(scene);
            Bubble bubble = new Bubble(scene, root, Bubble.COLOR, (BubblesGamesStats) stats);
            return stats;
        }));

        result.add(new GameSpec("\tPortrait Bubbles", (gameSpec, scene, root, cbxGames) -> {
            Stats stats = new BubblesGamesStats(scene);
            Bubble bubble = new Bubble(scene, root, Bubble.PORTRAIT, (BubblesGamesStats) stats);
            return stats;
        }));


        log.info("Games found : " + result.size());

        return result;
    }

}
