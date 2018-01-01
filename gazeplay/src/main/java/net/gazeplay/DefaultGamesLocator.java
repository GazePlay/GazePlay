package net.gazeplay;

import lombok.extern.slf4j.Slf4j;
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
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItStats;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

    @Override
    public List<GameSpec> listGames() {

        List<GameSpec> result = new ArrayList<>();

        result.add(new GameSpec("Creampie", (gameSpec, gameContext) -> {
            CreampieStats stats = new CreampieStats(gameContext.getScene());
            CreamPie game = new CreamPie(gameContext, stats);
            game.launch();
            return stats;
        }));

        result.add(new GameSpec("Ninja", (gameSpec, gameContext) -> {
            NinjaStats stats = new NinjaStats(gameContext.getScene());
            Ninja game = new Ninja(gameContext, stats);
            game.launch();
            return stats;
        }));

        result.add(new GameSpec("MagicCards", "(2x2)", (gameSpec, gameContext) -> {
            MagicCardsGamesStats stats = new MagicCardsGamesStats(gameContext.getScene());
            Card.addCards(gameContext, 2, 2, stats);
            return stats;
        }));

        result.add(new GameSpec("MagicCards", "(2x3)", (gameSpec, gameContext) -> {
            MagicCardsGamesStats stats = new MagicCardsGamesStats(gameContext.getScene());
            Card.addCards(gameContext, 2, 3, stats);
            return stats;
        }));

        result.add(new GameSpec("MagicCards", "(3x2)", (gameSpec, gameContext) -> {
            MagicCardsGamesStats stats = new MagicCardsGamesStats(gameContext.getScene());
            Card.addCards(gameContext, 3, 2, stats);
            return stats;
        }));

        result.add(new GameSpec("MagicCards", "(3x3)", (gameSpec, gameContext) -> {
            MagicCardsGamesStats stats = new MagicCardsGamesStats(gameContext.getScene());
            Card.addCards(gameContext, 3, 3, stats);
            return stats;
        }));

        result.add(new GameSpec("Blocks", "(2x2)", (gameSpec, gameContext) -> {
            BlocsGamesStats stats = new BlocsGamesStats(gameContext.getScene());
            Blocs game = new Blocs(gameContext, 2, 2, true, 1, false, stats);
            game.launch();
            return stats;
        }));

        result.add(new GameSpec("Blocks", "(2x3)", (gameSpec, gameContext) -> {
            BlocsGamesStats stats = new BlocsGamesStats(gameContext.getScene());
            Blocs game = new Blocs(gameContext, 2, 3, true, 1, false, stats);
            game.launch();
            return stats;
        }));

        result.add(new GameSpec("Blocks", "(3x3)", (gameSpec, gameContext) -> {
            BlocsGamesStats stats = new BlocsGamesStats(gameContext.getScene());
            Blocs game = new Blocs(gameContext, 3, 3, true, 1, false, stats);
            game.launch();
            return stats;
        }));

        result.add(new GameSpec("ScratchCard", (gameSpec, gameContext) -> {
            ScratchcardGamesStats stats = new ScratchcardGamesStats(gameContext.getScene());
            Blocs game = new Blocs(gameContext, 100, 100, false, 0.6f, true, stats);
            game.launch();
            return stats;
        }));

        result.add(new GameSpec("ColoredBubbles", (gameSpec, gameContext) -> {
            BubblesGamesStats stats = new BubblesGamesStats(gameContext.getScene());
            Bubble bubble = new Bubble(gameContext, Bubble.COLOR, stats, true);
            bubble.launch();
            return stats;
        }));

        result.add(new GameSpec("PortraitBubbles", (gameSpec, gameContext) -> {
            BubblesGamesStats stats = new BubblesGamesStats(gameContext.getScene());
            Bubble bubble = new Bubble(gameContext, Bubble.PORTRAIT, stats, false);
            bubble.launch();
            return stats;
        }));

        result.add(new GameSpec("WhereIsTheAnimal", "(2x2)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 2, 2, false, gameContext,
                    stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsTheAnimal", "(2x3)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 2, 3, false, gameContext,
                    stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsTheAnimal", "(3x2)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 3, 2, true, gameContext, stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsTheAnimal", "(3x3)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 3, 3, false, gameContext,
                    stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsTheColor", "(2x2)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 2, 2, false, gameContext, stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsTheColor", "(2x3)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 2, 3, false, gameContext, stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsTheColor", "(3x2)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 3, 2, false, gameContext, stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsTheColor", "(3x3)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 3, 3, false, gameContext, stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsIt", "(2x2)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 2, 2, false, gameContext,
                    stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsIt", "(2x3)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 2, 3, false, gameContext,
                    stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsIt", "(3x2)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 3, 2, false, gameContext,
                    stats);
            whereIsIt.buildGame();
            return stats;
        }));

        result.add(new GameSpec("WhereIsIt", "(3x3)", (gameSpec, gameContext) -> {
            WhereIsItStats stats = new WhereIsItStats(gameContext.getScene());
            WhereIsIt whereIsIt = new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 3, 3, false, gameContext, stats);
            whereIsIt.buildGame();
            return stats;
        }));

        log.info("Games found : {}", result.size());

        return result;
    }

}
