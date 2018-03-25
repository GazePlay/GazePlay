package net.gazeplay;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import net.gazeplay.games.cups.CupsAndBalls;
import net.gazeplay.games.cups.utils.CupsAndBallsStats;
import net.gazeplay.games.draw.DrawApplication;
import net.gazeplay.games.drawonvideo.VideoPlayerWithLiveFeedbackApp;
import net.gazeplay.games.magiccards.MagicCards;
import net.gazeplay.games.magiccards.MagicCardsGamesStats;
import net.gazeplay.games.memory.Memory;
import net.gazeplay.games.ninja.Ninja;
import net.gazeplay.games.ninja.NinjaStats;
import net.gazeplay.games.openmemory.OpenMemory;
import net.gazeplay.games.pianosight.Piano;
import net.gazeplay.games.scratchcard.ScratchcardGamesStats;
import net.gazeplay.games.shooter.Shooter;
import net.gazeplay.games.shooter.ShooterGamesStats;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItStats;
import net.gazeplay.games.divisor.Divisor;
import net.gazeplay.games.divisor.DivisorStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.gazeplay.games.colors.ColorsGame;
import net.gazeplay.games.colors.ColorsGamesStats;
import net.gazeplay.games.room.Room;
import net.gazeplay.games.room.RoomStats;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

    public static final String DEFAULT_AIMING_GAME_THUMBNAIL = "data/common/images/target.png";

    public static final String DEFAULT_SEARCHING_GAME_THUMBNAIL = "data/common/images/searching-magnifying-glass.png";

    @Override
    public List<GameSpec> listGames() {

        List<GameSpec> result = new ArrayList<>();

        result.add(
                new GameSpec(new GameSummary("Creampie", DEFAULT_AIMING_GAME_THUMBNAIL, "data/Thumbnails/CreamPie.jpg"),
                        new GameSpec.GameLauncher() {
                            @Override
                            public Stats createNewStats(Scene scene) {
                                return new CreampieStats(scene);
                            }

                            @Override
                            public GameLifeCycle createNewGame(GameContext gameContext,
                                    GameSpec.GameVariant gameVariant, Stats stats) {
                                return new CreamPie(gameContext, stats);
                            }
                        }));

        result.add(new GameSpec(new GameSummary("Ninja", DEFAULT_AIMING_GAME_THUMBNAIL, "data/Thumbnails/ninja-1.jpg"),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new NinjaStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Ninja(gameContext, stats);
                    }
                }));

        /*
         * result.add(new GameSpec(new GameSummary("Room", DEFAULT_SEARCHING_GAME_THUMBNAIL), new
         * GameSpec.GameLauncher() {
         * 
         * @Override public Stats createNewStats(Scene scene) { return new RoomStats(scene); }
         * 
         * @Override public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant, Stats
         * stats) { return new Room(gameContext, stats); } }));
         */

        result.add(new GameSpec(
                new GameSummary("Cups and Balls", DEFAULT_SEARCHING_GAME_THUMBNAIL, "data/Thumbnails/cups.jpg"),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(
                                Lists.newArrayList(new GameSpec.CupsGameVariant(3), new GameSpec.CupsGameVariant(5)));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.CupsGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new CupsAndBallsStats(scene);
                    }

                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.CupsGameVariant gameVariant,
                            Stats stats) {
                        return new CupsAndBalls(gameContext, stats, gameVariant.getNoCups(), 3);
                    }
                }));

        result.add(new GameSpec(
                new GameSummary("MagicCards", DEFAULT_SEARCHING_GAME_THUMBNAIL, "data/Thumbnails/magic-card-1.jpg"),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.DimensionGameVariant(2, 2),

                                new GameSpec.DimensionGameVariant(2, 3),

                                new GameSpec.DimensionGameVariant(3, 2),

                                new GameSpec.DimensionGameVariant(3, 3)

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MagicCardsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new MagicCards(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats);
                    }
                }));

        result.add(
                new GameSpec(new GameSummary("Blocks", DEFAULT_SEARCHING_GAME_THUMBNAIL, "data/Thumbnails/blocs.jpg"),
                        new GameSpec.GameVariantGenerator() {
                            @Override
                            public Set<GameSpec.GameVariant> getVariants() {
                                return Sets.newLinkedHashSet(Lists.newArrayList(

                                        new GameSpec.DimensionGameVariant(2, 2),

                                        new GameSpec.DimensionGameVariant(2, 3),

                                        new GameSpec.DimensionGameVariant(3, 3)

                        ));
                            }
                        }, new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                            @Override
                            public Stats createNewStats(Scene scene) {
                                return new BlocsGamesStats(scene);
                            }

                            @Override
                            public GameLifeCycle createNewGame(GameContext gameContext,
                                    GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                                return new Blocs(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), true, 1,
                                        false, stats);
                            }
                        }));

        result.add(new GameSpec(
                new GameSummary("ScratchCard", DEFAULT_SEARCHING_GAME_THUMBNAIL, "data/Thumbnails/Scratchcard.jpg"),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new ScratchcardGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Blocs(gameContext, 100, 100, false, 0.6f, true, stats);
                    }
                }));

        result.add(new GameSpec(
                new GameSummary("ColoredBubbles", DEFAULT_AIMING_GAME_THUMBNAIL, "data/Thumbnails/colored-bubbles.jpg"),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new BubblesGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Bubble(gameContext, BubbleType.COLOR, stats, true);
                    }
                }));

        result.add(new GameSpec(new GameSummary("PortraitBubbles", DEFAULT_AIMING_GAME_THUMBNAIL,
                "data/Thumbnails/portrait-bubbles.jpg"), new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new BubblesGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Bubble(gameContext, BubbleType.PORTRAIT, stats, false);
                    }
                }));

        result.add(new GameSpec(
                new GameSummary("WhereIsTheAnimal", DEFAULT_AIMING_GAME_THUMBNAIL, "data/Thumbnails/animals.jpeg"),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.DimensionGameVariant(2, 2),

                                new GameSpec.DimensionGameVariant(2, 3),

                                new GameSpec.DimensionGameVariant(3, 2),

                                new GameSpec.DimensionGameVariant(3, 3)

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.ANIMALNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.ANIMALNAME, gameVariant.getWidth(),
                                gameVariant.getHeight(), false, gameContext, stats);
                    }

                }));

        result.add(new GameSpec(
                new GameSummary("WhereIsTheColor", DEFAULT_AIMING_GAME_THUMBNAIL, "data/Thumbnails/colors.jpeg"),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.DimensionGameVariant(2, 2),

                                new GameSpec.DimensionGameVariant(2, 3),

                                new GameSpec.DimensionGameVariant(3, 2),

                                new GameSpec.DimensionGameVariant(3, 3)

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.COLORNAME.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.COLORNAME, gameVariant.getWidth(),
                                gameVariant.getHeight(), false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(
                new GameSummary("WhereIsIt", DEFAULT_AIMING_GAME_THUMBNAIL, "data/Thumbnails/whereisit.jpeg"),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.DimensionGameVariant(2, 2),

                                new GameSpec.DimensionGameVariant(2, 3),

                                new GameSpec.DimensionGameVariant(3, 2),

                                new GameSpec.DimensionGameVariant(3, 3)

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.CUSTOMIZED.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.CUSTOMIZED, gameVariant.getWidth(),
                                gameVariant.getHeight(), false, gameContext, stats);
                    }
                }));

        result.add(new GameSpec(
                new GameSummary("Biboules", DEFAULT_AIMING_GAME_THUMBNAIL, "data/Thumbnails/biboules.jpeg",
                        "https://opengameart.org/sites/default/files/TalkingCuteChiptune_0.mp3"),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new ShooterGamesStats(scene, "biboule");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Shooter(gameContext, stats, "biboule");
                    }
                }));

        result.add(new GameSpec(
                new GameSummary("Robots", DEFAULT_AIMING_GAME_THUMBNAIL, "data/Thumbnails/robots.jpeg",
                        "https://opengameart.org/sites/default/files/DST-TowerDefenseTheme_1.mp3"),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new ShooterGamesStats(scene, "robot");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Shooter(gameContext, stats, "robot");
                    }
                }));

        result.add(new GameSpec(new GameSummary("Divisor", DEFAULT_AIMING_GAME_THUMBNAIL), new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new DivisorStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant, Stats stats) {
                return new Divisor(gameContext, stats, false);
            }
        }));

        result.add(new GameSpec(new GameSummary("Lapins", DEFAULT_AIMING_GAME_THUMBNAIL), new GameSpec.GameLauncher() {
            @Override
            public Stats createNewStats(Scene scene) {
                return new DivisorStats(scene);
            }

            @Override
            public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant, Stats stats) {
                return new Divisor(gameContext, stats, true);
            }
        }));

        result.add(
                new GameSpec(new GameSummary("Memory", DEFAULT_SEARCHING_GAME_THUMBNAIL, "data/Thumbnails/memory.jpeg"),
                        new GameSpec.GameVariantGenerator() {
                            @Override
                            public Set<GameSpec.GameVariant> getVariants() {
                                return Sets.newLinkedHashSet(Lists.newArrayList(

                                        new GameSpec.DimensionGameVariant(2, 2),

                                        new GameSpec.DimensionGameVariant(2, 3),

                                        new GameSpec.DimensionGameVariant(3, 2),

                                        new GameSpec.DimensionGameVariant(3, 4),

                                        new GameSpec.DimensionGameVariant(4, 3)

                        ));
                            }
                        }, new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                            @Override
                            public Stats createNewStats(Scene scene) {
                                return new MagicCardsGamesStats(scene);
                            }

                            @Override
                            public GameLifeCycle createNewGame(GameContext gameContext,
                                    GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                                return new Memory(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats);
                            }
                        }));
        result.add(new GameSpec(
                new GameSummary("OpenMemory", DEFAULT_SEARCHING_GAME_THUMBNAIL, "data/Thumbnails/memory.jpeg"),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.DimensionGameVariant(2, 2),

                                new GameSpec.DimensionGameVariant(2, 3),

                                new GameSpec.DimensionGameVariant(3, 2),

                                new GameSpec.DimensionGameVariant(3, 4),

                                new GameSpec.DimensionGameVariant(4, 3)

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MagicCardsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new OpenMemory(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("Video Player with Feedback", DEFAULT_SEARCHING_GAME_THUMBNAIL,
                "data/Thumbnails/youtube-logo-128.png"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.StringGameVariant("Big Buck Bunny", "YE7VzlLtp-4"),

                                new GameSpec.StringGameVariant("Caminandes 2: Gran Dillama - Blender Animated Short",
                                        "Z4C82eyhwgU"),

                                new GameSpec.StringGameVariant("Caminandes 3: Llamigos - Funny 3D Animated Short",
                                        "SkVqJ1SGeL0")

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.StringGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new Stats(scene, "Video Player with Feedback");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.StringGameVariant gameVariant,
                            Stats stats) {
                        return new VideoPlayerWithLiveFeedbackApp(gameContext, stats, gameVariant.getValue());
                    }
                }));

        result.add(new GameSpec(
                new GameSummary("Scribble", DEFAULT_SEARCHING_GAME_THUMBNAIL, "data/Thumbnails/scribble.jpeg"),
                new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new Stats(scene, "Scribble");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new DrawApplication(gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("Piano", DEFAULT_SEARCHING_GAME_THUMBNAIL),
                new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new Stats(scene, "Piano");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new Piano(gameContext, stats);
                    }
                }));

        result.add(new GameSpec(new GameSummary("Colors!", DEFAULT_SEARCHING_GAME_THUMBNAIL),

                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new ColorsGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new ColorsGame(gameContext);
                    }
                }));

        log.info("Games found : {}", result.size());

        return result;
    }

}
