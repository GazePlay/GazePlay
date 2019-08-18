package net.gazeplay;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.games.biboulejump.BibouleJump;
import net.gazeplay.games.biboulejump.BibouleJumpStats;
import net.gazeplay.games.blocs.Blocs;
import net.gazeplay.games.blocs.BlocsGamesStats;
import net.gazeplay.games.bubbles.Bubble;
import net.gazeplay.games.bubbles.BubbleType;
import net.gazeplay.games.bubbles.BubblesGamesStats;
import net.gazeplay.games.cakes.CakeFactory;
import net.gazeplay.games.cakes.CakeStats;
import net.gazeplay.games.colors.ColorsGame;
import net.gazeplay.games.colors.ColorsGamesStats;
import net.gazeplay.games.creampie.CreamPie;
import net.gazeplay.games.creampie.CreampieStats;
import net.gazeplay.games.cups.CupsAndBalls;
import net.gazeplay.games.cups.utils.CupsAndBallsStats;
import net.gazeplay.games.dice.Dice;
import net.gazeplay.games.divisor.Divisor;
import net.gazeplay.games.divisor.DivisorStats;
import net.gazeplay.games.draw.DrawApplication;
import net.gazeplay.games.drawonvideo.VideoPlayerWithLiveFeedbackApp;
import net.gazeplay.games.goosegame.GooseGame;
import net.gazeplay.games.horses.Horses;
import net.gazeplay.games.labyrinth.Labyrinth;
import net.gazeplay.games.labyrinth.LabyrinthStats;
import net.gazeplay.games.literacy.Letters;
import net.gazeplay.games.literacy.LettersGamesStats;
import net.gazeplay.games.magicPotions.MagicPotions;
import net.gazeplay.games.magicPotions.MagicPotionsStats;
import net.gazeplay.games.magiccards.MagicCards;
import net.gazeplay.games.magiccards.MagicCardsGamesStats;
import net.gazeplay.games.math101.Math101;
import net.gazeplay.games.math101.MathGamesStats;
import net.gazeplay.games.mediaPlayer.GazeMediaPlayer;
import net.gazeplay.games.memory.Memory;
import net.gazeplay.games.moles.MoleStats;
import net.gazeplay.games.moles.Moles;
import net.gazeplay.games.ninja.Ninja;
import net.gazeplay.games.ninja.NinjaStats;
import net.gazeplay.games.order.Order;
import net.gazeplay.games.order.OrderStats;
import net.gazeplay.games.pet.PetHouse;
import net.gazeplay.games.pet.PetStats;
import net.gazeplay.games.pianosight.Piano;
import net.gazeplay.games.race.Race;
import net.gazeplay.games.race.RaceGamesStats;
import net.gazeplay.games.room.Room;
import net.gazeplay.games.room.RoomStats;
import net.gazeplay.games.rushHour.RushHour;
import net.gazeplay.games.scratchcard.ScratchcardGamesStats;
import net.gazeplay.games.shooter.Shooter;
import net.gazeplay.games.shooter.ShooterGamesStats;
import net.gazeplay.games.slidingpuzzle.slidingpuzzle;
import net.gazeplay.games.slidingpuzzle.slidingpuzzlestats;
import net.gazeplay.games.soundsoflife.SoundsOfLife;
import net.gazeplay.games.space.SpaceGame;
import net.gazeplay.games.space.SpaceGameStats;
import net.gazeplay.games.spotthedifferences.SpotTheDifferences;
import net.gazeplay.games.videogrid.VideoGrid;
import net.gazeplay.games.whereisit.WhereIsIt;
import net.gazeplay.games.whereisit.WhereIsItStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

    // public static final Translator translator = this.getTranslator();
    @Override
    public List<GameSpec> listGames() {

        List<GameSpec> gameList = new ArrayList<>();

        gameList.add(new GameSpec(
                new GameSummary("Creampie", "data/Thumbnails/creamPie.png", GameCategories.Category.SELECTION),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new CreampieStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new CreamPie(gameContext, stats);
                    }
                }));

        gameList.add(
                new GameSpec(new GameSummary("Ninja", "data/Thumbnails/ninja.png", GameCategories.Category.SELECTION),
                        new GameSpec.GameVariantGenerator() {
                            @Override
                            public Set<GameSpec.GameVariant> getVariants() {
                                return Sets.newLinkedHashSet(Lists.newArrayList(
                                        new GameSpec.IntGameVariant(1, "Random"),
                                        new GameSpec.IntGameVariant(2, "Vertical"),
                                        new GameSpec.IntGameVariant(3, "Horizontal"),
                                        new GameSpec.IntGameVariant(4, "Diagonal from upper left to lower right"),
                                        new GameSpec.IntGameVariant(5, "Diagonal from upper right to lower left")));
                            }
                        }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                            @Override
                            public Stats createNewStats(Scene scene) {
                                return new NinjaStats(scene);
                            }

                            @Override
                            public GameLifeCycle createNewGame(GameContext gameContext,
                                    GameSpec.IntGameVariant gameVariant, Stats stats) {
                                return new Ninja(gameContext, stats, gameVariant.getNumber());
                            }

                        }));

        gameList.add(new GameSpec(
                new GameSummary("MagicCards", "data/Thumbnails/magicCard.png", GameCategories.Category.ACTION_REACTION),
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

        gameList.add(new GameSpec(
                new GameSummary("Blocks", "data/Thumbnails/block.png", GameCategories.Category.ACTION_REACTION),
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
                        return new Blocs(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), true, 1, false,
                                stats);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("letters", "data/Thumbnails/letters.png", GameCategories.Category.ACTION_REACTION),
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
                        return new LettersGamesStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new Letters(gameContext, gameVariant.getWidth(), gameVariant.getHeight(), stats);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("ScratchCard", "data/Thumbnails/scratchcard.png",
                GameCategories.Category.ACTION_REACTION), new GameSpec.GameLauncher() {
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

        gameList.add(new GameSpec(
                new GameSummary("ColoredBubbles", "data/Thumbnails/bubblecolor.png", GameCategories.Category.SELECTION),
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

        gameList.add(new GameSpec(
                new GameSummary("PortraitBubbles", "data/Thumbnails/bubble.png", GameCategories.Category.SELECTION),
                new GameSpec.GameLauncher() {
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

        gameList.add(new GameSpec(new GameSummary("WhereIsTheAnimal", "data/Thumbnails/whereisanimal.png",
                GameCategories.Category.MEMORIZATION), new GameSpec.GameVariantGenerator() {
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

        gameList.add(new GameSpec(new GameSummary("WhereIsTheColor", "data/Thumbnails/whereiscolor.png",
                GameCategories.Category.MEMORIZATION), new GameSpec.GameVariantGenerator() {
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
        gameList.add(new GameSpec(
                new GameSummary("findodd", "data/Thumbnails/findtheodd.jpg", GameCategories.Category.MEMORIZATION),
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
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.FINDODD.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.FINDODD, gameVariant.getWidth(),
                                gameVariant.getHeight(), false, gameContext, stats);
                    }
                }));
        gameList.add(new GameSpec(new GameSummary("WhereIsTheLetter", "data/Thumbnails/Where-is-the-Letter.png",
                GameCategories.Category.MEMORIZATION), new GameSpec.GameVariantGenerator() {
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
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.LETTERS.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.LETTERS, gameVariant.getWidth(),
                                gameVariant.getHeight(), false, gameContext, stats);
                    }

                }));

        gameList.add(new GameSpec(new GameSummary("WhereIsTheNumber", "data/Thumbnails/Where-is-the-Number.png",
                GameCategories.Category.MEMORIZATION), new GameSpec.GameVariantGenerator() {
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
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.NUMBERS.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.NUMBERS, gameVariant.getWidth(),
                                gameVariant.getHeight(), false, gameContext, stats);
                    }

                }));

        gameList.add(new GameSpec(
                new GameSummary("flags", "data/Thumbnails/flags.png", GameCategories.Category.MEMORIZATION),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new WhereIsItStats(scene, WhereIsIt.WhereIsItGameType.FLAGS.getGameName());
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new WhereIsIt(WhereIsIt.WhereIsItGameType.FLAGS, 2, 2, false, gameContext, stats);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("WhereIsIt", "data/Thumbnails/whereisit.png", GameCategories.Category.MEMORIZATION),
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

        gameList.add(new GameSpec(
                new GameSummary("Biboule", "data/Thumbnails/biboules.png", GameCategories.Category.SELECTION,
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

        gameList.add(new GameSpec(
                new GameSummary("Robots", "data/Thumbnails/robots.png", GameCategories.Category.SELECTION,
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

        gameList.add(new GameSpec(
                new GameSummary("Divisor", "data/Thumbnails/divisor.png", GameCategories.Category.SELECTION),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new DivisorStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Divisor(gameContext, stats, false);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("Lapins", "data/Thumbnails/rabbits.png", GameCategories.Category.SELECTION),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new DivisorStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Divisor(gameContext, stats, true);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("Memory", "data/Thumbnails/memory.png", GameCategories.Category.MEMORIZATION),
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
                        return new Memory(Memory.MemoryGameType.DEFAULT, gameContext, gameVariant.getWidth(),
                                gameVariant.getHeight(), stats, false);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("MemoryLetters", "data/Thumbnails/memory-letter.png",
                GameCategories.Category.MEMORIZATION), new GameSpec.GameVariantGenerator() {
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
                        return new Memory(Memory.MemoryGameType.LETTERS, gameContext, gameVariant.getWidth(),
                                gameVariant.getHeight(), stats, false);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("MemoryNumbers", "data/Thumbnails/memory-numbers.png",
                GameCategories.Category.MEMORIZATION), new GameSpec.GameVariantGenerator() {
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
                        return new Memory(Memory.MemoryGameType.NUMBERS, gameContext, gameVariant.getWidth(),
                                gameVariant.getHeight(), stats, false);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("OpenMemory", "data/Thumbnails/openMemory.png",
                GameCategories.Category.ACTION_REACTION), new GameSpec.GameVariantGenerator() {
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
                        return new Memory(Memory.MemoryGameType.DEFAULT, gameContext, gameVariant.getWidth(),
                                gameVariant.getHeight(), stats, true);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("OpenMemoryLetters", "data/Thumbnails/openMemoryLetters.png",
                GameCategories.Category.ACTION_REACTION), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.DimensionGameVariant(2, 2),

                                new GameSpec.DimensionGameVariant(2, 3),

                                new GameSpec.DimensionGameVariant(3, 2),

                                new GameSpec.DimensionGameVariant(3, 4),

                                new GameSpec.DimensionGameVariant(4, 3),

                                new GameSpec.DimensionGameVariant(4, 4)

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
                        return new Memory(Memory.MemoryGameType.LETTERS, gameContext, gameVariant.getWidth(),
                                gameVariant.getHeight(), stats, true);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("OpenMemoryNumbers", "data/Thumbnails/openMemoryNumbers.png",
                GameCategories.Category.ACTION_REACTION), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.DimensionGameVariant(2, 2),

                                new GameSpec.DimensionGameVariant(2, 3),

                                new GameSpec.DimensionGameVariant(3, 2),

                                new GameSpec.DimensionGameVariant(3, 4),

                                new GameSpec.DimensionGameVariant(4, 3),

                                new GameSpec.DimensionGameVariant(4, 4)

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
                        return new Memory(Memory.MemoryGameType.NUMBERS, gameContext, gameVariant.getWidth(),
                                gameVariant.getHeight(), stats, true);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("VideoPlayer", "data/Thumbnails/youtube.png", GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.StringGameVariant("Big Buck Bunny", "YE7VzlLtp-4"),

                                new GameSpec.StringGameVariant("Caminandes 2: Gran Dillama - Blender Animated Short",
                                        "Z4C82eyhwgU"),

                                new GameSpec.StringGameVariant("Caminandes 3: Llamigos - Funny 3D Animated Short",
                                        "SkVqJ1SGeL0"),

                                new GameSpec.StringGameVariant("1H de Petit Ours Brun", "PUIou9gUVos"),

                                new GameSpec.StringGameVariant("Zou s'amuse", "f9qKQ5snhOI"),

                                new GameSpec.StringGameVariant("Tchoupi et ses amis", "aPX6q1HC4Ho"),

                                // new GameSpec.StringGameVariant("Tchoupi à l'école", "a_KH2U2wqok"),

                                new GameSpec.StringGameVariant("Princesse sofia rencontre Belle", "szptWdF2B5s")

                        // new GameSpec.StringGameVariant("Lulu Vroumette", "2Eg7r6WGWhQ")

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

        gameList.add(new GameSpec(
                new GameSummary("Scribble", "data/Thumbnails/gribouille.png", GameCategories.Category.ACTION_REACTION),
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

        gameList.add(new GameSpec(
                new GameSummary("CupsBalls", "data/Thumbnails/passpass.png", GameCategories.Category.MEMORIZATION),
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

        gameList.add(new GameSpec(
                new GameSummary("Order", "data/Thumbnails/ordre.png", GameCategories.Category.MEMORIZATION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.TargetsGameVariant(3), new GameSpec.TargetsGameVariant(5),
                                new GameSpec.TargetsGameVariant(7)));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.TargetsGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new OrderStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.TargetsGameVariant gameVariant,
                            Stats stats) {
                        return new Order(gameContext, gameVariant.getNoTargets(), stats);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("Room", "data/Thumbnails/home.png", GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameLauncher() {

                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new RoomStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Room(gameContext, stats);
                    }
                }));

        // cups and balls was here

        gameList.add(new GameSpec(
                new GameSummary("Piano", "data/Thumbnails/pianosight.png", GameCategories.Category.ACTION_REACTION),
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
        gameList.add(new GameSpec(
                new GameSummary("WhacAmole", "data/Thumbnails/mole.png", GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MoleStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new Moles(gameContext, stats);
                    }

                }));

        gameList.add(
                new GameSpec(new GameSummary("Pet", "data/Thumbnails/pet.png", GameCategories.Category.ACTION_REACTION),
                        new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                            @Override
                            public Stats createNewStats(Scene scene) {
                                return new PetStats(scene);
                            }

                            @Override
                            public GameLifeCycle createNewGame(GameContext gameContext,
                                    GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                                return new PetHouse(gameContext, stats);
                            }

                        }));

        gameList.add(new GameSpec(
                new GameSummary("MediaPlayer", "data/Thumbnails/gazeMedia.png",
                        GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new PetStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new GazeMediaPlayer(gameContext, stats);
                    }

                }));
        gameList.add(new GameSpec(
                new GameSummary("RushHour", "data/Thumbnails/rushHour.png", GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new PetStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new RushHour(gameContext);
                    }

                }));

        gameList.add(new GameSpec(
                new GameSummary("Colorsss", "data/Thumbnails/colors.png", GameCategories.Category.ACTION_REACTION, null,
                        "ColorDesc"),

                new GameSpec.GameLauncher() {

                    private ColorsGamesStats gameStat;

                    @Override
                    public Stats createNewStats(Scene scene) {

                        gameStat = new ColorsGamesStats(scene);
                        return gameStat;
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new ColorsGame(gameContext, gameStat);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("Cakes", "data/Thumbnails/cakes.png", GameCategories.Category.MEMORIZATION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.IntGameVariant(0, "free"),

                                new GameSpec.IntGameVariant(1, "normal"),

                                new GameSpec.IntGameVariant(2, "extreme")

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new CakeStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new CakeFactory(gameContext, stats, gameVariant.getNumber());
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("Labyrinth", "data/Thumbnails/labyrinth.png", GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.IntGameVariant(0, "Look at the destination box to move"),

                                new GameSpec.IntGameVariant(2, "Look the movement arrows around the mouse to move"),

                                new GameSpec.IntGameVariant(3, "Look the movement arrows around the labyrinth to move"),

                                new GameSpec.IntGameVariant(4,
                                        "Select the mouse then look at the destination box to move")

                ));
                    }
                },

                new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new LabyrinthStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new Labyrinth(gameContext, stats, gameVariant.getNumber());
                    }

                }));

        gameList.add(new GameSpec(
                new GameSummary("FrogsRace", "data/Thumbnails/frogsrace.png", GameCategories.Category.SELECTION),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new RaceGamesStats(scene, "race");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new Race(gameContext, stats, "race");
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("BibJump", "data/Thumbnails/biboulejump.png", GameCategories.Category.SELECTION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.IntGameVariant(0, "With moving platforms"),

                                new GameSpec.IntGameVariant(1, "Without moving platforms")

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new BibouleJumpStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new BibouleJump(gameContext, stats, gameVariant.getNumber());
                    }

                }));

        gameList.add(new GameSpec(new GameSummary("Math101", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDescAdd"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 8"),
                                new GameSpec.IntGameVariant(1, "0 to 12"), new GameSpec.IntGameVariant(2, "0 to 20")));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MathGamesStats(scene);
                    }// Need to make customized stats

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new Math101(Math101.Math101GameType.ADDITION, gameContext, gameVariant.getNumber(),
                                stats);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("Math102", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDescSub"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 8"),
                                new GameSpec.IntGameVariant(1, "0 to 12"), new GameSpec.IntGameVariant(2, "0 to 20")));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MathGamesStats(scene);
                    }// Need to make customized stats

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new Math101(Math101.Math101GameType.SUBTRACTIONPOS, gameContext, gameVariant.getNumber(),
                                stats);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("Math103", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDescMult"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 3"),
                                new GameSpec.IntGameVariant(1, "0 to 5"), new GameSpec.IntGameVariant(2, "0 to 7"),
                                new GameSpec.IntGameVariant(3, "0 to 9"), new GameSpec.IntGameVariant(4, "0 to 11"),
                                new GameSpec.IntGameVariant(5, "0 to 12")));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MathGamesStats(scene);
                    }// Need to make customized stats

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new Math101(Math101.Math101GameType.MULTIPLICATION, gameContext, gameVariant.getNumber(),
                                stats);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("Math104", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDescDiv"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 10"),
                                new GameSpec.IntGameVariant(1, "0 to 15"), new GameSpec.IntGameVariant(2, "0 to 20"),
                                new GameSpec.IntGameVariant(3, "0 to 30")));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MathGamesStats(scene);
                    }// Need to make customized stats

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new Math101(Math101.Math101GameType.DIVISION, gameContext, gameVariant.getNumber(),
                                stats);
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("Math201", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDesc"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 5"),
                                new GameSpec.IntGameVariant(1, "0 to 10"), new GameSpec.IntGameVariant(2, "0 to 15"),
                                new GameSpec.IntGameVariant(3, "0 to 20")));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MathGamesStats(scene);
                    }// Need to make customized stats

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new Math101(Math101.Math101GameType.MATHALL, gameContext, gameVariant.getNumber(),
                                stats);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("puzzle", "data/Thumbnails/slidingpuzzle.png", GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(1, "Numbers"),
                                new GameSpec.IntGameVariant(2, "Mona Lisa"), new GameSpec.IntGameVariant(3, "Fish"),
                                new GameSpec.IntGameVariant(4, "Biboule")));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new slidingpuzzlestats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new slidingpuzzle(stats, gameContext, 3, 3, gameVariant.getNumber());
                    }
                }));

        gameList.add(new GameSpec(new GameSummary("SpotDifference", "data/Thumbnails/spotthedifference.png",
                GameCategories.Category.ACTION_REACTION), new GameSpec.GameLauncher() {

                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new Stats(scene, "spotthedifferences");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new SpotTheDifferences(gameContext, stats);
                    }
                }));

        gameList.add(
                new GameSpec(new GameSummary("Dice", "data/Thumbnails/dice.png", GameCategories.Category.MEMORIZATION),
                        new GameSpec.GameVariantGenerator() {
                            @Override
                            public Set<GameSpec.GameVariant> getVariants() {
                                return Sets.newLinkedHashSet(Lists.newArrayList(

                                        new GameSpec.IntGameVariant(1, "1 die"),

                                        new GameSpec.IntGameVariant(2, "2 dice"),

                                        new GameSpec.IntGameVariant(3, "3 dice"),

                                        new GameSpec.IntGameVariant(4, "4 dice"),

                                        new GameSpec.IntGameVariant(5, "5 dice"),

                                        new GameSpec.IntGameVariant(6, "6 dice")

                        ));
                            }
                        }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                            @Override
                            public Stats createNewStats(Scene scene) {
                                return new Stats(scene, "dice");
                            }

                            @Override
                            public GameLifeCycle createNewGame(GameContext gameContext,
                                    GameSpec.IntGameVariant gameVariant, Stats stats) {
                                return new Dice(gameContext, stats, gameVariant.getNumber());
                            }

                        }));

        gameList.add(new GameSpec(
                new GameSummary("SpaceGame", "data/Thumbnails/space.png", GameCategories.Category.SELECTION),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new SpaceGameStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new SpaceGame(gameContext, stats);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("Potions", "data/Thumbnails/potions.jpg", GameCategories.Category.SELECTION),
                new GameSpec.GameLauncher() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new MagicPotionsStats(scene);
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.GameVariant gameVariant,
                            Stats stats) {
                        return new MagicPotions(gameContext, stats);
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("GooseGame", "data/Thumbnails/goosegame.png", GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.IntGameVariant(2, "2 players"),
                                new GameSpec.IntGameVariant(3, "3 players"),
                                new GameSpec.IntGameVariant(4, "4 players"), new GameSpec.IntGameVariant(5, "5 players")

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new Stats(scene, "goosegame");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new GooseGame(gameContext, stats, gameVariant.getNumber());
                    }

                }));

        gameList.add(new GameSpec(
                new GameSummary("VideoGrid", "data/Thumbnails/videogrid.png", GameCategories.Category.SELECTION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.DimensionGameVariant(2, 1),

                                new GameSpec.DimensionGameVariant(2, 2),

                                new GameSpec.DimensionGameVariant(3, 3),

                                new GameSpec.DimensionGameVariant(4, 4)

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.DimensionGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new Stats(scene, "videogrid");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext,
                            GameSpec.DimensionGameVariant gameVariant, Stats stats) {
                        return new VideoGrid(gameContext, stats, gameVariant.getWidth(), gameVariant.getHeight());
                    }
                }));

        gameList.add(new GameSpec(
                new GameSummary("Horses", "data/Thumbnails/horses.png", GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.IntGameVariant(2, "2 players"),
                                new GameSpec.IntGameVariant(3, "3 players"), new GameSpec.IntGameVariant(4, "4 players")

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new Stats(scene, "horses");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new Horses(gameContext, stats, 0, gameVariant.getNumber());
                    }

                }));

        gameList.add(new GameSpec(new GameSummary("Horses Simplified", "data/Thumbnails/horsesSimplified.png",
                GameCategories.Category.ACTION_REACTION), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.IntGameVariant(2, "2 players"),
                                new GameSpec.IntGameVariant(3, "3 players"), new GameSpec.IntGameVariant(4, "4 players")

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new Stats(scene, "horsesSimplified");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new Horses(gameContext, stats, 1, gameVariant.getNumber());
                    }

                }));

        gameList.add(new GameSpec(new GameSummary("Sounds of Life", "data/Thumbnails/soundsoflife.png",
                GameCategories.Category.ACTION_REACTION), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.IntGameVariant(0, "Farm"), new GameSpec.IntGameVariant(1, "Jungle"),
                                new GameSpec.IntGameVariant(2, "Savanna")

                ));
                    }
                }, new GameSpec.GameLauncher<Stats, GameSpec.IntGameVariant>() {
                    @Override
                    public Stats createNewStats(Scene scene) {
                        return new Stats(scene, "soundsoflife");
                    }

                    @Override
                    public GameLifeCycle createNewGame(GameContext gameContext, GameSpec.IntGameVariant gameVariant,
                            Stats stats) {
                        return new SoundsOfLife(gameContext, stats, gameVariant.getNumber());
                    }

                }));

        log.info("Games found : {}", gameList.size());

        return gameList;
    }
}
