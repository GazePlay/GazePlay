package net.gazeplay;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.blocs.Blocs;
import net.gazeplay.games.blocs.BlocsGamesStats;
import net.gazeplay.games.bubbles.Bubble;
import net.gazeplay.games.bubbles.BubbleType;
import net.gazeplay.games.bubbles.BubblesGamesStats;
import net.gazeplay.games.creampie.CreamPie;
import net.gazeplay.games.creampie.CreampieStats;
import net.gazeplay.games.magiccards.MagicCards;
import net.gazeplay.games.magiccards.MagicCardsGamesStats;
import net.gazeplay.games.ninja.Ninja;
import net.gazeplay.games.ninja.NinjaStats;
import net.gazeplay.games.scratchcard.ScratchcardGamesStats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItStats;
import net.gazeplay.games.cups.CupsAndBalls;
import net.gazeplay.games.cups.CupsAndBallsStats;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

    @Override
    public List<GameSpec> listGames() {

        List<GameSpec> result = new ArrayList<>();

        result.add(new GameSpec("Creampie", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new CreampieStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new CreamPie(gameContext, stats);
            }
        }));

        result.add(new GameSpec("Ninja", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new NinjaStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new Ninja(gameContext, stats);
            }
        }));

        result.add(new GameSpec("Cups and Balls", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new CupsAndBallsStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new CupsAndBalls(gameContext, stats, 3, 3);
            }
        }));

        result.add(new GameSpec("MagicCards", "(2x2)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new MagicCardsGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new MagicCards(gameContext, 2, 2, stats);
            }
        }));

        result.add(new GameSpec("MagicCards", "(2x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new MagicCardsGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new MagicCards(gameContext, 2, 3, stats);
            }
        }));

        result.add(new GameSpec("MagicCards", "(3x2)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new MagicCardsGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new MagicCards(gameContext, 3, 2, stats);
            }
        }));

        result.add(new GameSpec("MagicCards", "(3x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new MagicCardsGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new MagicCards(gameContext, 3, 3, stats);
            }
        }));

        result.add(new GameSpec("Blocks", "(2x2)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new BlocsGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new Blocs(gameContext, 2, 2, true, 1, false, stats);
            }
        }));

        result.add(new GameSpec("Blocks", "(2x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new BlocsGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new Blocs(gameContext, 2, 3, true, 1, false, stats);
            }
        }));

        result.add(new GameSpec("Blocks", "(3x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new BlocsGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new Blocs(gameContext, 3, 3, true, 1, false, stats);
            }
        }));

        result.add(new GameSpec("ScratchCard", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new ScratchcardGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new Blocs(gameContext, 100, 100, false, 0.6f, true, stats);
            }
        }));

        result.add(new GameSpec("ColoredBubbles", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new BubblesGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new Bubble(gameContext, BubbleType.COLOR, stats, true);
            }
        }));

        result.add(new GameSpec("PortraitBubbles", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new BubblesGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new Bubble(gameContext, BubbleType.PORTRAIT, stats, false);
            }
        }));

        result.add(new GameSpec("WhereIsTheAnimal", "(2x2)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.ANIMALNAME.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 2, 2, false, gameContext, stats);
            }

        }));

        result.add(new GameSpec("WhereIsTheAnimal", "(2x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.ANIMALNAME.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 2, 3, false, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsTheAnimal", "(3x2)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.ANIMALNAME.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 3, 2, true, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsTheAnimal", "(3x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.ANIMALNAME.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 3, 3, false, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsTheColor", "(2x2)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.COLORNAME.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 2, 2, false, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsTheColor", "(2x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.COLORNAME.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 2, 3, false, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsTheColor", "(3x2)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.COLORNAME.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 3, 2, false, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsTheColor", "(3x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.COLORNAME.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 3, 3, false, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsIt", "(2x2)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.CUSTOMIZED.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 2, 2, false, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsIt", "(2x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.CUSTOMIZED.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 2, 3, false, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsIt", "(3x2)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.CUSTOMIZED.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 3, 2, false, gameContext, stats);
            }
        }));

        result.add(new GameSpec("WhereIsIt", "(3x3)", new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.CUSTOMIZED.getGameName());
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 3, 3, false, gameContext, stats);
            }
        }));

        log.info("Games found : {}", result.size());

        return result;
    }

}
