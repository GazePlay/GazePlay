package net.gazeplay.gameslocator;

import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameSpec;
import net.gazeplay.GamesLocator;
import net.gazeplay.games.biboulejump.BibouleJumpGameSpecSource;
import net.gazeplay.games.blocs.BlocsGameSpecSource;
import net.gazeplay.games.bubbles.ColoredBubblesGameSpecSource;
import net.gazeplay.games.bubbles.PortraitsBubblesGameSpecSource;
import net.gazeplay.games.cakes.CakesGameSpecSource;
import net.gazeplay.games.colors.ColorsGameSpecSource;
import net.gazeplay.games.creampie.CreampieGameSpecSource;
import net.gazeplay.games.cups.CupsBallsGameSpecSource;
import net.gazeplay.games.dice.DiceGameSpecSource;
import net.gazeplay.games.divisor.DivisorGameSpecSource;
import net.gazeplay.games.divisor.RabbitsGameSpecSource;
import net.gazeplay.games.draw.ScribbleGameSpecSource;
import net.gazeplay.games.drawonvideo.VideoPlayerGameSpecSource;
import net.gazeplay.games.goosegame.GooseGameSpecSource;
import net.gazeplay.games.horses.HorsesGameSpecSource;
import net.gazeplay.games.horses.HorsesSimplifiedGameSpecSource;
import net.gazeplay.games.labyrinth.LabyrinthGameSpecSource;
import net.gazeplay.games.literacy.LettersGameSpecSource;
import net.gazeplay.games.magicPotions.PotionsGameSpecSource;
import net.gazeplay.games.magiccards.MagicCardsGameSpecSource;
import net.gazeplay.games.math101.*;
import net.gazeplay.games.mediaPlayer.MediaPlayerGameSpecSource;
import net.gazeplay.games.memory.*;
import net.gazeplay.games.moles.WhacAMoleGameSpecSource;
import net.gazeplay.games.ninja.NinjaGameSpecSource;
import net.gazeplay.games.order.OrderGameSpecSource;
import net.gazeplay.games.pet.PetGameSpecSource;
import net.gazeplay.games.pianosight.PianoGameSpecSource;
import net.gazeplay.games.race.FrogRaceGameSpecSource;
import net.gazeplay.games.room.RoomGameSpecSource;
import net.gazeplay.games.rushHour.RushHourGameSpecSource;
import net.gazeplay.games.scratchcard.ScratchCardGameSpecSource;
import net.gazeplay.games.shooter.BibouleGameSpecSource;
import net.gazeplay.games.shooter.RobotsGameSpecSource;
import net.gazeplay.games.slidingpuzzle.PuzzleGameSpecSource;
import net.gazeplay.games.soundsoflife.FarmGameSpecSource;
import net.gazeplay.games.soundsoflife.JungleGameSpecSource;
import net.gazeplay.games.soundsoflife.SavanaGameSpecSource;
import net.gazeplay.games.space.SpaceGameSpecSource;
import net.gazeplay.games.spotthedifferences.SpotDifferencesGameSpecSource;
import net.gazeplay.games.videogrid.VideoGridGameSpecSource;
import net.gazeplay.games.whereisit.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class DefaultGamesLocator implements GamesLocator {

	private final List<GameSpecSource> sources;

	public DefaultGamesLocator() {
		sources = new ArrayList<>();

		sources.add(new CreampieGameSpecSource());
		sources.add(new NinjaGameSpecSource());
		sources.add(new BlocsGameSpecSource());
		sources.add(new ScratchCardGameSpecSource());
		sources.add(new ColoredBubblesGameSpecSource());
		sources.add(new PortraitsBubblesGameSpecSource());
		sources.add(new BibouleGameSpecSource());
		sources.add(new RobotsGameSpecSource());
		sources.add(new BibouleJumpGameSpecSource());
		sources.add(new SpaceGameSpecSource());
		sources.add(new WhacAMoleGameSpecSource());
		sources.add(new DivisorGameSpecSource());
		sources.add(new RabbitsGameSpecSource());
		sources.add(new FrogRaceGameSpecSource());
		sources.add(new PianoGameSpecSource());
		sources.add(new MagicCardsGameSpecSource());
		sources.add(new DiceGameSpecSource());
		sources.add(new OpenMemoryGameSpecSource());
		sources.add(new OpenMemoryLettersGameSpecSource());
		sources.add(new OpenMemoryNumbersGameSpecSource());
		sources.add(new PetGameSpecSource());
		sources.add(new GooseGameSpecSource());
		sources.add(new HorsesGameSpecSource());
		sources.add(new HorsesSimplifiedGameSpecSource());
		sources.add(new FarmGameSpecSource());
		sources.add(new JungleGameSpecSource());
		sources.add(new SavanaGameSpecSource());
		sources.add(new WhereIsTheAnimalGameSpecSource());
		sources.add(new WhereIsTheColorGameSpecSource());
		sources.add(new WhereIsTheLetterGameSpecSource());
		sources.add(new WhereIsTheNumberGameSpecSource());
		sources.add(new FlagsGameSpecSource());
		sources.add(new LettersGameSpecSource());
		sources.add(new WhereIsItGameSpecSource());
		sources.add(new OrderGameSpecSource());
		sources.add(new CupsBallsGameSpecSource());
		sources.add(new MemoryGameSpecSource());
		sources.add(new MemoryLettersGameSpecSource());
		sources.add(new MemoryNumbersGameSpecSource());
		sources.add(new FindOddGameSpecSource());
		sources.add(new RushHourGameSpecSource());
		sources.add(new LabyrinthGameSpecSource());
		sources.add(new CakesGameSpecSource());
		sources.add(new SpotDifferencesGameSpecSource());
		sources.add(new PuzzleGameSpecSource());
		sources.add(new PotionsGameSpecSource());
		sources.add(new Math101GameSpecSource());
		sources.add(new Math102GameSpecSource());
		sources.add(new Math103GameSpecSource());
		sources.add(new Math104GameSpecSource());
		sources.add(new Math201GameSpecSource());
		sources.add(new ColorsGameSpecSource());
		sources.add(new ScribbleGameSpecSource());
		sources.add(new VideoPlayerGameSpecSource());
		sources.add(new MediaPlayerGameSpecSource());
		sources.add(new VideoGridGameSpecSource());
		sources.add(new RoomGameSpecSource());
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
