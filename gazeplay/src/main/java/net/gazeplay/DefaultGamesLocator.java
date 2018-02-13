package net.gazeplay;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.biboules.Biboule;
import net.gazeplay.games.biboules.BibouleGamesStats;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

    public static final String DEFAULT_AIMING_GAME_THUMBNAIL = "data/common/images/target.png";

    public static final String DEFAULT_SEARCHING_GAME_THUMBNAIL = "data/common/images/searching-magnifying-glass.png";

    @Override
    public List<GameSpec> listGames() {

        List<GameSpec> result = new ArrayList<>();

        result.add(new GameSpec(new GameSummary("Creampie", DEFAULT_AIMING_GAME_THUMBNAIL), new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new CreampieStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new CreamPie(gameContext, stats);
            }
        }));

        result.add(new GameSpec(new GameSummary("Ninja", DEFAULT_AIMING_GAME_THUMBNAIL), new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new NinjaStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new Ninja(gameContext, stats);
            }
        }));

        result.add(new GameSpec(new GameSummary("MagicCards", DEFAULT_SEARCHING_GAME_THUMBNAIL), "(2x2)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MagicCardsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new MagicCards(gameContext, 2, 2, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("MagicCards", DEFAULT_SEARCHING_GAME_THUMBNAIL), "(2x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MagicCardsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new MagicCards(gameContext, 2, 3, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("MagicCards", DEFAULT_SEARCHING_GAME_THUMBNAIL), "(3x2)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MagicCardsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new MagicCards(gameContext, 3, 2, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("MagicCards", DEFAULT_SEARCHING_GAME_THUMBNAIL), "(3x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MagicCardsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new MagicCards(gameContext, 3, 3, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("Blocks", DEFAULT_SEARCHING_GAME_THUMBNAIL), "(2x2)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new BlocsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new Blocs(gameContext, 2, 2, true, 1, false, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("Blocks", DEFAULT_SEARCHING_GAME_THUMBNAIL), "(2x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new BlocsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new Blocs(gameContext, 2, 3, true, 1, false, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("Blocks", DEFAULT_SEARCHING_GAME_THUMBNAIL), "(3x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new BlocsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new Blocs(gameContext, 3, 3, true, 1, false, stats);
                    }
                }));

        result.add(
                new GameSpec(new GameSummary("ScratchCard", DEFAULT_SEARCHING_GAME_THUMBNAIL), new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new ScratchcardGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new Blocs(gameContext, 100, 100, false, 0.6f, true, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("ColoredBubbles", DEFAULT_AIMING_GAME_THUMBNAIL), null,
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new BubblesGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new Bubble(gameContext, BubbleType.COLOR, stats, true);
                    }
                }));

        result.add(new GameSpec(new GameSummary("PortraitBubbles", DEFAULT_AIMING_GAME_THUMBNAIL), null,
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new BubblesGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new Bubble(gameContext, BubbleType.PORTRAIT, stats, false);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsTheAnimal", DEFAULT_AIMING_GAME_THUMBNAIL), "(2x2)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.ANIMALNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 2, 2, false, gameContext, stats);
                    }

                }));

        result.add(new GameSpec(new GameSummary("WhereIsTheAnimal", DEFAULT_AIMING_GAME_THUMBNAIL), "(2x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.ANIMALNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 2, 3, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsTheAnimal", DEFAULT_AIMING_GAME_THUMBNAIL), "(3x2)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.ANIMALNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 3, 2, true, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsTheAnimal", DEFAULT_AIMING_GAME_THUMBNAIL), "(3x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.ANIMALNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, 3, 3, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsTheColor", DEFAULT_AIMING_GAME_THUMBNAIL), "(2x2)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.COLORNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 2, 2, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsTheColor", DEFAULT_AIMING_GAME_THUMBNAIL), "(2x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.COLORNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 2, 3, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsTheColor", DEFAULT_AIMING_GAME_THUMBNAIL), "(3x2)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.COLORNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 3, 2, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsTheColor", DEFAULT_AIMING_GAME_THUMBNAIL), "(3x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.COLORNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, 3, 3, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsIt", DEFAULT_AIMING_GAME_THUMBNAIL), "(2x2)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.CUSTOMIZED.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 2, 2, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsIt", DEFAULT_AIMING_GAME_THUMBNAIL), "(2x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.CUSTOMIZED.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 2, 3, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsIt", DEFAULT_AIMING_GAME_THUMBNAIL), "(3x2)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.CUSTOMIZED.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 3, 2, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("WhereIsIt", DEFAULT_AIMING_GAME_THUMBNAIL), "(3x3)",
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.CUSTOMIZED.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, 3, 3, false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("Biboules", DEFAULT_AIMING_GAME_THUMBNAIL), new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new BibouleGamesStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, Stats stats) {
                return new Biboule(gameContext, stats);
            }
        }));

        log.info("Games found : {}", result.size());

        return result;
    }

}
