package net.gazeplay;

import gaze.configuration.Configuration;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.games.whereisit.WhereIsItStats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.blocs.Blocs;
import net.gazeplay.games.blocs.BlocsGamesStats;
import net.gazeplay.games.bubbles.Bubble;
import net.gazeplay.games.bubbles.BubblesGamesStats;
import net.gazeplay.games.creampie.CreamPie;
import net.gazeplay.games.creampie.CreampieStats;
import net.gazeplay.games.magiccards.Card;
import net.gazeplay.games.magiccards.MagicCardsGamesStats;
import net.gazeplay.games.ninja.Ninja;
import net.gazeplay.games.ninja.NinjaStats;
import net.gazeplay.games.scratchcard.ScratchcardGamesStats;
import net.gazeplay.utils.multilinguism.Multilinguism;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

    @Override
    public List<GameSpec> listGames() {

        Multilinguism multilinguism = Multilinguism.getMultilinguism();
        String language = (new Configuration()).language;

        List<GameSpec> result = new ArrayList<>();

        result.add(new GameSpec(multilinguism.getTrad("Creampie", language), (gameSpec, scene, root, cbxGames) -> {
            CreampieStats stats = new CreampieStats(scene);
            CreamPie.launch(root, scene, stats);
            return stats;
        }));

        result.add(new GameSpec(multilinguism.getTrad("Ninja", language), (gameSpec, scene, root, cbxGames) -> {
            NinjaStats stats = new NinjaStats(scene);
            Ninja.launch(root, scene, stats);
            return stats;
        }));

        result.add(new GameSpec(multilinguism.getTrad("MagicCards", language) + " (2x2)",
                (gameSpec, scene, root, cbxGames) -> {
                    MagicCardsGamesStats stats = new MagicCardsGamesStats(scene);
                    Card.addCards(root, scene, cbxGames, 2, 2, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("MagicCards", language) + " (2x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    MagicCardsGamesStats stats = new MagicCardsGamesStats(scene);
                    Card.addCards(root, scene, cbxGames, 2, 3, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("MagicCards", language) + " (3x2)",
                (gameSpec, scene, root, cbxGames) -> {
                    MagicCardsGamesStats stats = new MagicCardsGamesStats(scene);
                    Card.addCards(root, scene, cbxGames, 3, 2, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("MagicCards", language) + " (3x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    MagicCardsGamesStats stats = new MagicCardsGamesStats(scene);
                    Card.addCards(root, scene, cbxGames, 3, 3, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("Blocks", language) + " (2x2)",
                (gameSpec, scene, root, cbxGames) -> {
                    BlocsGamesStats stats = new BlocsGamesStats(scene);
                    Blocs.makeBlocks(scene, root, cbxGames, 2, 2, true, 1, false, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("Blocks", language) + " (2x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    BlocsGamesStats stats = new BlocsGamesStats(scene);
                    Blocs.makeBlocks(scene, root, cbxGames, 2, 3, true, 1, false, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("Blocks", language) + " (3x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    BlocsGamesStats stats = new BlocsGamesStats(scene);
                    Blocs.makeBlocks(scene, root, cbxGames, 3, 3, true, 1, false, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("ScratchCard", language), (gameSpec, scene, root, cbxGames) -> {
            ScratchcardGamesStats stats = new ScratchcardGamesStats(scene);
            Blocs.makeBlocks(scene, root, cbxGames, 100, 100, false, 0.6f, true, stats);
            return stats;
        }));

        result.add(
                new GameSpec(multilinguism.getTrad("ColoredBubbles", language), (gameSpec, scene, root, cbxGames) -> {
                    BubblesGamesStats stats = new BubblesGamesStats(scene);
                    Bubble bubble = new Bubble(scene, root, Bubble.COLOR, stats, true);
                    return stats;
                }));

        result.add(
                new GameSpec(multilinguism.getTrad("PortraitBubbles", language), (gameSpec, scene, root, cbxGames) -> {
                    BubblesGamesStats stats = new BubblesGamesStats(scene);
                    Bubble bubble = new Bubble(scene, root, Bubble.PORTRAIT, stats, false);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsTheAnimal", language) + " (2x2)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.ANIMALNAME, 2, 2, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsTheAnimal", language) + " (2x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.ANIMALNAME, 2, 3, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsTheAnimal", language) + " (3x2)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.ANIMALNAME, 3, 2, true, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsTheAnimal", language) + " (3x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.ANIMALNAME, 3, 3, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsTheColor", language) + " (2x2)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.COLORNAME, 2, 2, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsTheColor", language) + " (2x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.COLORNAME, 2, 3, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsTheColor", language) + " (3x2)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.COLORNAME, 3, 2, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsTheColor", language) + " (3x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.COLORNAME, 3, 3, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsIt", language) + " (2x2)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.CUSTOMIZED, 2, 2, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsIt", language) + " (2x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.CUSTOMIZED, 2, 3, false, root, scene, cbxGames, stats);
                    return stats;
                }));


        result.add(new GameSpec(multilinguism.getTrad("WhereIsIt", language) + " (3x2)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.CUSTOMIZED, 3, 2, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        result.add(new GameSpec(multilinguism.getTrad("WhereIsIt", language) + " (3x3)",
                (gameSpec, scene, root, cbxGames) -> {
                    WhereIsItStats stats = new WhereIsItStats(scene);
                    WhereIsIt.buildGame(WhereIsIt.COLORNAME, 3, 3, false, root, scene, cbxGames, stats);
                    return stats;
                }));

        log.info("Games found : " + result.size());

        return result;
    }

}
