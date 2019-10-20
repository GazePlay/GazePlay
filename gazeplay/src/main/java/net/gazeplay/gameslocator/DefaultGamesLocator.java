package net.gazeplay.gameslocator;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameCategories;
import net.gazeplay.GameSpec;
import net.gazeplay.GameSummary;
import net.gazeplay.GamesLocator;
import net.gazeplay.games.biboulejump.BibouleJumpGameLauncher;
import net.gazeplay.games.biboulejump.BibouleJumpGameVariantGenerator;
import net.gazeplay.games.blocs.BlocsGameLauncher;
import net.gazeplay.games.blocs.BlocsGameVariantGenerator;
import net.gazeplay.games.bubbles.ColoredBubblesGameLauncher;
import net.gazeplay.games.bubbles.PortraitBubblesGameLauncher;
import net.gazeplay.games.cakes.CakesGameLauncher;
import net.gazeplay.games.cakes.CakesGameVariantGenerator;
import net.gazeplay.games.colors.ColorsGameLauncher;
import net.gazeplay.games.creampie.CreampieGameLauncher;
import net.gazeplay.games.cups.CupsBallsGameLauncher;
import net.gazeplay.games.cups.CupsBallsGameVariantGenerator;
import net.gazeplay.games.dice.DiceGameLauncher;
import net.gazeplay.games.dice.DiceGameVariantGenerator;
import net.gazeplay.games.divisor.DivisorGameLauncher;
import net.gazeplay.games.divisor.RabbitsGameLauncher;
import net.gazeplay.games.draw.ScribbleGameLauncher;
import net.gazeplay.games.drawonvideo.VideoPlayerGameLauncher;
import net.gazeplay.games.drawonvideo.VideoPlayerGameVariantGenerator;
import net.gazeplay.games.goosegame.GooseGameLauncher;
import net.gazeplay.games.goosegame.GooseGameVariantGenerator;
import net.gazeplay.games.horses.HorsesGameLauncher;
import net.gazeplay.games.horses.HorsesGameVariantGenerator;
import net.gazeplay.games.horses.HorsesSimplifiedGameLauncher;
import net.gazeplay.games.horses.HorsesSimplifiedGameVariantGenerator;
import net.gazeplay.games.labyrinth.LabyrinthGameLauncher;
import net.gazeplay.games.labyrinth.LabyrinthGameVariantGenerator;
import net.gazeplay.games.literacy.LettersGameLauncher;
import net.gazeplay.games.literacy.LettersGameVariantGenerator;
import net.gazeplay.games.magicPotions.PotionsGameLauncher;
import net.gazeplay.games.magiccards.MagicCardsGameLauncher;
import net.gazeplay.games.magiccards.MagicCardsGameVariantGenerator;
import net.gazeplay.games.math101.*;
import net.gazeplay.games.mediaPlayer.MediaPlayerGameLauncher;
import net.gazeplay.games.memory.*;
import net.gazeplay.games.moles.WhacAMoleGameLauncher;
import net.gazeplay.games.ninja.NinjaGameLauncher;
import net.gazeplay.games.ninja.NinjaGameVariantGenerator;
import net.gazeplay.games.order.OrderGameLauncher;
import net.gazeplay.games.order.OrdersGameVariantGenerator;
import net.gazeplay.games.pet.PetGameLauncher;
import net.gazeplay.games.pianosight.PianoGameLauncher;
import net.gazeplay.games.race.FrogsRaceGameLauncher;
import net.gazeplay.games.room.RoomGameLauncher;
import net.gazeplay.games.rushHour.RushHourGameLauncher;
import net.gazeplay.games.scratchcard.ScratchCardGameLauncher;
import net.gazeplay.games.shooter.RobotsGameLauncher;
import net.gazeplay.games.shooter.ShooterGameLauncher;
import net.gazeplay.games.slidingpuzzle.PuzzleGameLauncher;
import net.gazeplay.games.slidingpuzzle.PuzzleGameVariantGenerator;
import net.gazeplay.games.soundsoflife.FarmGameLauncher;
import net.gazeplay.games.soundsoflife.JungleGameLauncher;
import net.gazeplay.games.soundsoflife.SavannaGameLauncher;
import net.gazeplay.games.space.SpaceGameLauncher;
import net.gazeplay.games.spotthedifferences.SpotDifferencesGameLauncher;
import net.gazeplay.games.videogrid.VideoGridGameLauncher;
import net.gazeplay.games.videogrid.VideoGridGameVariantGenerator;
import net.gazeplay.games.whereisit.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

	private final List<GameSpecSource> sources;

	public DefaultGamesLocator() {
		sources = new ArrayList<>();

		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Creampie", "data/Thumbnails/creamPie.png", GameCategories.Category.SELECTION),
						new CreampieGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("Ninja", "data/Thumbnails/ninja.png", GameCategories.Category.SELECTION),
						new NinjaGameVariantGenerator(), new NinjaGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Blocks", "data/Thumbnails/block.png", GameCategories.Category.ACTION_REACTION),
						new BlocsGameVariantGenerator(), new BlocsGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("ScratchCard", "data/Thumbnails/scratchcard.png",
						GameCategories.Category.ACTION_REACTION), new ScratchCardGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("ColoredBubbles", "data/Thumbnails/bubblecolor.png", GameCategories.Category.SELECTION),
						new ColoredBubblesGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("PortraitBubbles", "data/Thumbnails/bubble.png", GameCategories.Category.SELECTION),
						new PortraitBubblesGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Biboule", "data/Thumbnails/biboules.png", GameCategories.Category.SELECTION,
								"https://opengameart.org/sites/default/files/TalkingCuteChiptune_0.mp3"),
						new ShooterGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Robots", "data/Thumbnails/robots.png", GameCategories.Category.SELECTION,
								"https://opengameart.org/sites/default/files/DST-TowerDefenseTheme_1.mp3"),
						new RobotsGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("BibJump", "data/Thumbnails/biboulejump.png", GameCategories.Category.SELECTION),
						new BibouleJumpGameVariantGenerator(), new BibouleJumpGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("SpaceGame", "data/Thumbnails/space.png", GameCategories.Category.SELECTION),
						new SpaceGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("WhacAmole", "data/Thumbnails/mole.png", GameCategories.Category.ACTION_REACTION),
						new WhacAMoleGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Divisor", "data/Thumbnails/divisor.png", GameCategories.Category.SELECTION),
						new DivisorGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Lapins", "data/Thumbnails/rabbits.png", GameCategories.Category.SELECTION),
						new RabbitsGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("FrogsRace", "data/Thumbnails/frogsrace.png", GameCategories.Category.SELECTION),
						new FrogsRaceGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Piano", "data/Thumbnails/pianosight.png", GameCategories.Category.ACTION_REACTION),
						new PianoGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("MagicCards", "data/Thumbnails/magicCard.png", GameCategories.Category.ACTION_REACTION),
						new MagicCardsGameVariantGenerator(), new MagicCardsGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("Dice", "data/Thumbnails/dice.png", GameCategories.Category.MEMORIZATION),
						new DiceGameVariantGenerator(), new DiceGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("OpenMemory", "data/Thumbnails/openMemory.png",
						GameCategories.Category.ACTION_REACTION), new OpenMemoryGameVariantGenerator(), new OpenMemoryGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("OpenMemoryLetters", "data/Thumbnails/openMemoryLetters.png",
						GameCategories.Category.ACTION_REACTION), new OpenMemoryLettersGameVariantGenerator(), new OpenMemoryLettersGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("OpenMemoryNumbers", "data/Thumbnails/openMemoryNumbers.png",
						GameCategories.Category.ACTION_REACTION), new OpenMemoryNumbersGameVariantGenerator(), new OpenMemoryNumbersGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("Pet", "data/Thumbnails/pet.png", GameCategories.Category.ACTION_REACTION),
						new PetGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("GooseGame", "data/Thumbnails/goosegame.png", GameCategories.Category.ACTION_REACTION),
						new GooseGameVariantGenerator(), new GooseGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Horses", "data/Thumbnails/horses.png", GameCategories.Category.ACTION_REACTION),
						new HorsesGameVariantGenerator(), new HorsesGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("Horses Simplified", "data/Thumbnails/horsesSimplified.png",
						GameCategories.Category.ACTION_REACTION), new HorsesSimplifiedGameVariantGenerator(), new HorsesSimplifiedGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Farm", "data/Thumbnails/farm.png", GameCategories.Category.ACTION_REACTION),
						new FarmGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Jungle", "data/Thumbnails/jungle.png", GameCategories.Category.ACTION_REACTION),
						new JungleGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Savanna", "data/Thumbnails/savana.png", GameCategories.Category.ACTION_REACTION),
						new SavannaGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("WhereIsTheAnimal", "data/Thumbnails/whereisanimal.png",
						GameCategories.Category.MEMORIZATION), new WhereIsTheAnimalGameVariantGenerator(), new WhereIsTheAnimalGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("WhereIsTheColor", "data/Thumbnails/whereiscolor.png",
						GameCategories.Category.MEMORIZATION), new WhereIsTheColorGameVariantGenerator(), new WhereIsTheColorGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("WhereIsTheLetter", "data/Thumbnails/Where-is-the-Letter.png",
						GameCategories.Category.MEMORIZATION), new WhereIsTheLetterGameVariantGenerator(), new WhereIsTheLetterGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("WhereIsTheNumber", "data/Thumbnails/Where-is-the-Number.png",
						GameCategories.Category.MEMORIZATION), new WhereIsTheNumberGameVariantGenerator(), new WhereIsTheNumberGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("flags", "data/Thumbnails/flags.png", GameCategories.Category.MEMORIZATION),
						new FlagsGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("letters", "data/Thumbnails/letters.png", GameCategories.Category.ACTION_REACTION),
						new LettersGameVariantGenerator(), new LettersGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("WhereIsIt", "data/Thumbnails/whereisit.png", GameCategories.Category.MEMORIZATION),
						new WhereIsItGameVariantGenerator(), new WhereIsItGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Order", "data/Thumbnails/ordre.png", GameCategories.Category.MEMORIZATION),
						new OrdersGameVariantGenerator(), new OrderGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("CupsBalls", "data/Thumbnails/passpass.png", GameCategories.Category.MEMORIZATION),
						new CupsBallsGameVariantGenerator(), new CupsBallsGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Memory", "data/Thumbnails/memory.png", GameCategories.Category.MEMORIZATION),
						new MemoryGameVariantGenerator(), new MemoryGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("MemoryLetters", "data/Thumbnails/memory-letter.png",
						GameCategories.Category.MEMORIZATION), new MemoryLettersGameVariantGenerator(), new MemoryLettersGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("MemoryNumbers", "data/Thumbnails/memory-numbers.png",
						GameCategories.Category.MEMORIZATION), new MemoryNumbersGameVariantGenerator(), new MemoryNumbersGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("findodd", "data/Thumbnails/findtheodd.jpg", GameCategories.Category.MEMORIZATION),
						new FindOddGameVariantGenerator(), new FindOddGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("RushHour", "data/Thumbnails/rushHour.png", GameCategories.Category.ACTION_REACTION),
						new RushHourGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Labyrinth", "data/Thumbnails/labyrinth.png", GameCategories.Category.ACTION_REACTION),
						new LabyrinthGameVariantGenerator(), new LabyrinthGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Cakes", "data/Thumbnails/cakes.png", GameCategories.Category.MEMORIZATION),
						new CakesGameVariantGenerator(), new CakesGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("SpotDifference", "data/Thumbnails/spotthedifference.png",
						GameCategories.Category.ACTION_REACTION), new SpotDifferencesGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("puzzle", "data/Thumbnails/slidingpuzzle.png", GameCategories.Category.ACTION_REACTION),
						new PuzzleGameVariantGenerator(), new PuzzleGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Potions", "data/Thumbnails/potions.jpg", GameCategories.Category.SELECTION),
						new PotionsGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("Math101", "data/Thumbnails/math101.png",
						GameCategories.Category.LOGIC, null, "MathDescAdd"), new Math101GameVariantGenerator(), new Math101GameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("Math102", "data/Thumbnails/math101.png",
						GameCategories.Category.LOGIC, null, "MathDescSub"), new Math102GameVariantGenerator(), new Math102GameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("Math103", "data/Thumbnails/math101.png",
						GameCategories.Category.LOGIC, null, "MathDescMult"), new Math103GameVariantGenerator(), new Math103GameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("Math104", "data/Thumbnails/math101.png",
						GameCategories.Category.LOGIC, null, "MathDescDiv"), new Math104GameVariantGenerator(), new Math104GameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(new GameSummary("Math201", "data/Thumbnails/math101.png",
						GameCategories.Category.LOGIC, null, "MathDesc"), new Math201GameVariantGenerator(), new Math201GameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Colorsss", "data/Thumbnails/colors.png", GameCategories.Category.ACTION_REACTION, null,
								"ColorDesc"), new ColorsGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Scribble", "data/Thumbnails/gribouille.png", GameCategories.Category.ACTION_REACTION),
						new ScribbleGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("VideoPlayer", "data/Thumbnails/youtube.png", GameCategories.Category.ACTION_REACTION),
						new VideoPlayerGameVariantGenerator(), new VideoPlayerGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("MediaPlayer", "data/Thumbnails/gazeMedia.png", GameCategories.Category.ACTION_REACTION),
						new MediaPlayerGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("VideoGrid", "data/Thumbnails/videogrid.png", GameCategories.Category.SELECTION),
						new VideoGridGameVariantGenerator(), new VideoGridGameLauncher());
			}
		});
		sources.add(new GameSpecSource() {
			@Override
			public GameSpec getGameSpec() {
				return new GameSpec(
						new GameSummary("Room", "data/Thumbnails/home.png", GameCategories.Category.ACTION_REACTION),
						new RoomGameLauncher());
			}
		});
	}

	@Override
	public List<GameSpec> listGames() {
		LinkedList<GameSpec> gameList = new LinkedList<>();
		for (GameSpecSource source : sources) {
			gameList.add(source.getGameSpec());
		}
		log.info("Games found : {}", gameList.size());
		return gameList;
	}

}
