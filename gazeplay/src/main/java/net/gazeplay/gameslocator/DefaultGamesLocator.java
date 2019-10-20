package net.gazeplay.gameslocator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

    private static List<GameSpec> gameList = null;

    @Override
    public List<GameSpec> listGames() {

        if (gameList != null) {

            log.debug("Game List already created.");
            return gameList;
        } else {

            log.debug("Game List has to be created.");
        }

        LinkedList<GameSpec> gameList = new LinkedList<>();

        gameList.add(new GameSpec(
                new GameSummary("Creampie", "data/Thumbnails/creamPie.png", GameCategories.Category.SELECTION),
                new CreampieGameLauncher()));

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
                        }, new NinjaGameLauncher()));

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
                }, new BlockGameLauncher()));

        gameList.add(new GameSpec(new GameSummary("ScratchCard", "data/Thumbnails/scratchcard.png",
                GameCategories.Category.ACTION_REACTION), new ScratchCardGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("ColoredBubbles", "data/Thumbnails/bubblecolor.png", GameCategories.Category.SELECTION),
                new ColoredBubblesGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("PortraitBubbles", "data/Thumbnails/bubble.png", GameCategories.Category.SELECTION),
                new PortraitBubblesGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Biboule", "data/Thumbnails/biboules.png", GameCategories.Category.SELECTION,
                        "https://opengameart.org/sites/default/files/TalkingCuteChiptune_0.mp3"),
                new BibouleGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Robots", "data/Thumbnails/robots.png", GameCategories.Category.SELECTION,
                        "https://opengameart.org/sites/default/files/DST-TowerDefenseTheme_1.mp3"),
                new RobotsGameLauncher()));

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
                }, new BibouleJumpGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("SpaceGame", "data/Thumbnails/space.png", GameCategories.Category.SELECTION),
                new SpaceGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("WhacAmole", "data/Thumbnails/mole.png", GameCategories.Category.ACTION_REACTION),
                new WhacAMoleGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Divisor", "data/Thumbnails/divisor.png", GameCategories.Category.SELECTION),
                new DivisorGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Lapins", "data/Thumbnails/rabbits.png", GameCategories.Category.SELECTION),
                new RabbitsGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("FrogsRace", "data/Thumbnails/frogsrace.png", GameCategories.Category.SELECTION),
                new FrogsRaceGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Piano", "data/Thumbnails/pianosight.png", GameCategories.Category.ACTION_REACTION),
                new PianoGameLauncher()));

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
                }, new MagicCardsGameLauncher()));

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
                        }, new DiceGameLauncher()));

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
                }, new OpenMemoryGameLauncher()));

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
                }, new OpenMemoryLettersGameLauncher()));

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
                }, new OpenMemoryNumbersGameLauncher()));

        gameList.add(
                new GameSpec(new GameSummary("Pet", "data/Thumbnails/pet.png", GameCategories.Category.ACTION_REACTION),
                        new PetGameLauncher()));

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
                }, new GooseGameLauncher()));

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
                }, new HorsesGameLauncher()));

        gameList.add(new GameSpec(new GameSummary("Horses Simplified", "data/Thumbnails/horsesSimplified.png",
                GameCategories.Category.ACTION_REACTION), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.IntGameVariant(2, "2 players"),
                                new GameSpec.IntGameVariant(3, "3 players"), new GameSpec.IntGameVariant(4, "4 players")

                ));
                    }
                }, new HorsesSimplifiedGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Farm", "data/Thumbnails/farm.png", GameCategories.Category.ACTION_REACTION),
                new FarmGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Jungle", "data/Thumbnails/jungle.png", GameCategories.Category.ACTION_REACTION),
                new JungleGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Savanna", "data/Thumbnails/savana.png", GameCategories.Category.ACTION_REACTION),
                new SavannaGameLauncher()));

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
                }, new WhereIsTheAnimalGameLauncher()));

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
                }, new WhereIsTheColorGameLauncher()));

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
                }, new WhereIsTheLetterGameLauncher()));

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
                }, new WhereIsTheNumberGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("flags", "data/Thumbnails/flags.png", GameCategories.Category.MEMORIZATION),
                new FlagsGameLauncher()));

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
                }, new LettersGameLauncher()));

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
                }, new WhereIsItGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Order", "data/Thumbnails/ordre.png", GameCategories.Category.MEMORIZATION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(

                                new GameSpec.TargetsGameVariant(3), new GameSpec.TargetsGameVariant(5),
                                new GameSpec.TargetsGameVariant(7)));
                    }
                }, new OrderGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("CupsBalls", "data/Thumbnails/passpass.png", GameCategories.Category.MEMORIZATION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(
                                Lists.newArrayList(new GameSpec.CupsGameVariant(3), new GameSpec.CupsGameVariant(5)));
                    }
                }, new CupsBallsGameLauncher()));

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
                }, new MemoryGameLauncher()));

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
                }, new MemoryLettersGameLauncher()));

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
                }, new MemoryNumbersGameLauncher()));

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
                }, new FindOddGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("RushHour", "data/Thumbnails/rushHour.png", GameCategories.Category.ACTION_REACTION),
                new RushHourGameLauncher()));

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

                new LabyrinthGameLauncher()));

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
                }, new CakesGameLauncher()));

        gameList.add(new GameSpec(new GameSummary("SpotDifference", "data/Thumbnails/spotthedifference.png",
                GameCategories.Category.ACTION_REACTION), new SpotDifferencesGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("puzzle", "data/Thumbnails/slidingpuzzle.png", GameCategories.Category.ACTION_REACTION),
                new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(1, "Numbers"),
                                new GameSpec.IntGameVariant(2, "Mona Lisa"), new GameSpec.IntGameVariant(3, "Fish"),
                                new GameSpec.IntGameVariant(4, "Biboule")));
                    }
                }, new PuzzleGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Potions", "data/Thumbnails/potions.jpg", GameCategories.Category.SELECTION),
                new PotionsGameLauncher()));

        gameList.add(new GameSpec(new GameSummary("Math101", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDescAdd"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 8"),
                                new GameSpec.IntGameVariant(1, "0 to 12"), new GameSpec.IntGameVariant(2, "0 to 20")));
                    }
                }, new Math101GameLauncher()));

        gameList.add(new GameSpec(new GameSummary("Math102", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDescSub"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 8"),
                                new GameSpec.IntGameVariant(1, "0 to 12"), new GameSpec.IntGameVariant(2, "0 to 20")));
                    }
                }, new Math102GameLauncher()));

        gameList.add(new GameSpec(new GameSummary("Math103", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDescMult"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 3"),
                                new GameSpec.IntGameVariant(1, "0 to 5"), new GameSpec.IntGameVariant(2, "0 to 7"),
                                new GameSpec.IntGameVariant(3, "0 to 9"), new GameSpec.IntGameVariant(4, "0 to 11"),
                                new GameSpec.IntGameVariant(5, "0 to 12")));
                    }
                }, new Math103GameLauncher()));

        gameList.add(new GameSpec(new GameSummary("Math104", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDescDiv"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 10"),
                                new GameSpec.IntGameVariant(1, "0 to 15"), new GameSpec.IntGameVariant(2, "0 to 20"),
                                new GameSpec.IntGameVariant(3, "0 to 30")));
                    }
                }, new Math104GameLauncher()));

        gameList.add(new GameSpec(new GameSummary("Math201", "data/Thumbnails/math101.png",
                GameCategories.Category.LOGIC, null, "MathDesc"), new GameSpec.GameVariantGenerator() {
                    @Override
                    public Set<GameSpec.GameVariant> getVariants() {
                        return Sets.newLinkedHashSet(Lists.newArrayList(new GameSpec.IntGameVariant(0, "0 to 5"),
                                new GameSpec.IntGameVariant(1, "0 to 10"), new GameSpec.IntGameVariant(2, "0 to 15"),
                                new GameSpec.IntGameVariant(3, "0 to 20")));
                    }
                }, new Math201GameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Colorsss", "data/Thumbnails/colors.png", GameCategories.Category.ACTION_REACTION, null,
                        "ColorDesc"),

                new ColorsGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Scribble", "data/Thumbnails/gribouille.png", GameCategories.Category.ACTION_REACTION),
                new ScribbleGameLauncher()));

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
                }, new VideoPlayerGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("MediaPlayer", "data/Thumbnails/gazeMedia.png",
                        GameCategories.Category.ACTION_REACTION),
                new MediaPlayerGameLauncher()));

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
                }, new VideoGridGameLauncher()));

        gameList.add(new GameSpec(
                new GameSummary("Room", "data/Thumbnails/home.png", GameCategories.Category.ACTION_REACTION),
                new RoomGameLauncher()));

        log.info("Games found : {}", gameList.size());

        return gameList;
    }

    // TODO complete fonction
    public List<GameSpec> listGames(GameCategories.Category category) {

        return listGames();
    }

}
