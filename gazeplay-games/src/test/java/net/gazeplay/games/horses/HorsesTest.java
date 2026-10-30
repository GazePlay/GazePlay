package net.gazeplay.games.horses;

import javafx.geometry.Dimension2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.gamevariants.EnumGameVariant;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.Position;
import net.gazeplay.components.ProgressButton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * The petits chevaux (reviewed on 30/10/2026): the variants against the computer, the choice of the teams, and the
 * rules of the moves (no move ending on a pawn of the same team, a pawn of another team sent back to its stable).
 */
@ExtendWith(ApplicationExtension.class)
class HorsesTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    IGameContext gameContext;

    @Mock
    Translator translator;

    @Mock
    Stats stats;

    private final Pane gameArea = new Pane();

    private Horses launched;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(gameContext.getChildren()).thenReturn(gameArea.getChildren());
        when(gameContext.getGamePanelDimensionProvider().getDimension2D()).thenReturn(new Dimension2D(1366, 768));
        when(gameContext.getTranslator()).thenReturn(translator);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void stopTheGame() throws Exception {
        if (launched != null) {
            WaitForAsyncUtils.asyncFx(launched::dispose).get();
        }
    }

    private Horses launchedGame(HorsesVariant variant) throws Exception {
        launched = WaitForAsyncUtils.asyncFx(() -> {
            Horses horses = new Horses(gameContext, stats, variant, 1234);
            horses.launch();
            return horses;
        }).get();
        return launched;
    }

    /**
     * The same six ways of playing on each board (the simplified board used to be a game of its own).
     */
    @Test
    void shouldOfferToPlayAgainstTheComputerOrTogether() {
        assertEquals(12, new HorsesGameVariantGenerator().getVariants().size());
        assertEquals(6, java.util.Arrays.stream(HorsesVariant.values()).filter(HorsesVariant::isSimplified).count());
        assertEquals(HorsesVariant.VS_COMPUTER_2.getLabel(), HorsesVariant.SIMPLIFIED_VS_COMPUTER_2.getLabel());
        assertEquals(1, HorsesVariant.VS_COMPUTER_3.getHumans());
        assertEquals(4, HorsesVariant.VS_COMPUTER_3.getTeams());
        assertEquals(HorsesVariant.PLAYERS_2, HorsesGameLauncher.variantOf(null));
        assertEquals(HorsesVariant.VS_COMPUTER_1,
            HorsesGameLauncher.variantOf(new EnumGameVariant<>(HorsesVariant.VS_COMPUTER_1)));
    }

    /**
     * Against the computer, the player chooses a team; the computer takes another one, and the player starts.
     */
    @Test
    void shouldGiveTheOtherTeamToTheComputer() throws Exception {
        Horses horses = launchedGame(HorsesVariant.SIMPLIFIED_VS_COMPUTER_1);
        assertEquals("HorsesChooseTeam", horses.shownMessage());

        WaitForAsyncUtils.asyncFx(() -> horses.selectTeam(Horses.TEAMS.RED)).get();

        assertEquals(List.of(Horses.TEAMS.RED, Horses.TEAMS.BLUE), horses.getChosenTeams());
        assertEquals(java.util.Set.of(Horses.TEAMS.BLUE), horses.getComputerTeams());
        assertEquals(1, horses.getPawns().get(Horses.TEAMS.RED).size(), "1 pawn in Horses Simplified");
        assertEquals("HorsesRoll", horses.shownMessage(), "the player rolls the die first");
        verify(stats).incrementNumberOfGoalsToReach();
    }

    @Test
    void shouldAskForTheOtherTeamsOfThePlayers() throws Exception {
        Horses horses = launchedGame(HorsesVariant.PLAYERS_3);
        assertEquals("HorsesChooseTeams", horses.shownMessage());

        WaitForAsyncUtils.asyncFx(() -> horses.selectTeam(Horses.TEAMS.GREEN)).get();

        assertEquals("HorsesTeamsLeft 2", horses.shownMessage());
        assertEquals(4, horses.getPawns().get(Horses.TEAMS.GREEN).size(), "4 pawns in Horses");
        assertTrue(horses.getComputerTeams().isEmpty());
    }

    /**
     * A track of 5 squares, and pawns put on it.
     */
    private final Square[] track = new Square[5];

    private Pawn pawnOn(Horses.TEAMS team, Square square) throws Exception {
        return WaitForAsyncUtils.asyncFx(() -> {
            ImageView display = new ImageView();
            Pawn pawn = new Pawn(team, display, new ProgressButton(), new Circle(), new Position(0, 0), track[0],
                gameContext);
            pawn.setCurrentSquare(square);
            if (square != null) {
                square.stationnedPawn = pawn;
            }
            return pawn;
        }).get();
    }

    private void buildTrack() {
        for (int i = 0; i < track.length; i++) {
            track[i] = new Square(new Position(i, 0), null);
            if (i > 0) {
                track[i - 1].setNextSquare(track[i]);
                track[i].setPreviousSquare(track[i - 1]);
            }
        }
    }

    /**
     * A move ending on a pawn of the same team is not offered (it used to make the pawn step back and the next team
     * miss its turn); a move ending on a pawn of another team sends it back to its stable.
     */
    @Test
    void shouldNotEndAMoveOnAPawnOfTheSameTeam() throws Exception {
        buildTrack();
        Pawn mover = pawnOn(Horses.TEAMS.BLUE, track[0]);
        pawnOn(Horses.TEAMS.BLUE, track[2]);
        Pawn opponent = pawnOn(Horses.TEAMS.RED, track[4]);

        assertEquals(track[2], mover.destination(2));
        assertFalse(mover.canMove(2), "the move would end on the pawn of the same team");
        assertTrue(mover.canMove(1));
        assertEquals(track[1], mover.destination(1));
        assertFalse(mover.wouldCapture(1));
        assertTrue(opponent.isOnTrack());
    }

    @Test
    void shouldSendAPawnOfAnotherTeamBackToItsStable() throws Exception {
        buildTrack();
        Pawn mover = pawnOn(Horses.TEAMS.BLUE, track[0]);
        pawnOn(Horses.TEAMS.RED, track[1]);
        Pawn sameTeam = pawnOn(Horses.TEAMS.BLUE, track[3]);

        assertTrue(mover.wouldCapture(1));
        assertTrue(mover.canMove(1));
        assertFalse(sameTeam.wouldCapture(1));
    }

    /**
     * Moving over a square with a pawn makes the pawn go back (the rule of the game: no jumping over a pawn): the move
     * is computed as the real one.
     */
    @Test
    void shouldComputeTheDestinationAsTheRealMove() throws Exception {
        buildTrack();
        Pawn mover = pawnOn(Horses.TEAMS.BLUE, track[1]);
        pawnOn(Horses.TEAMS.RED, track[2]);

        // A 1 lands on the red pawn (captured); with more, the red pawn blocks the way and the pawn comes back
        assertEquals(track[2], mover.destination(1));
        assertEquals(track[1], mover.destination(2));
        assertEquals(track[0], mover.destination(3));
    }

    /**
     * The computer sends a pawn of another team back to its stable when it can; else it moves the pawn which went the
     * furthest.
     */
    @Test
    void shouldMakeTheComputerCaptureFirst() throws Exception {
        buildTrack();
        Horses horses = new Horses(gameContext, stats, HorsesVariant.VS_COMPUTER_1, 1);
        Pawn quiet = pawnOn(Horses.TEAMS.BLUE, track[0]);
        Pawn capturing = pawnOn(Horses.TEAMS.BLUE, track[2]);
        pawnOn(Horses.TEAMS.RED, track[3]);

        // With a 1, the second pawn lands on the red pawn
        assertSame(capturing, horses.computerChoice(List.of(quiet, capturing), 1));
        // With a 2, no capture (it would be blocked): the first pawn of the list, both having moved as much
        assertSame(quiet, horses.computerChoice(List.of(quiet, capturing), 2));
    }

    /**
     * On the final path, a pawn goes up one square with the exact number of the square.
     */
    @Test
    void shouldClimbTheFinalPathWithTheExactNumber() throws Exception {
        buildTrack();
        FinalPathSquare second = new FinalPathSquare(new Position(0, 0), null, 2);
        Square third = new Square(new Position(1, 0), null);
        second.setNextSquare(third);
        third.setPreviousSquare(second);
        Pawn pawn = pawnOn(Horses.TEAMS.GREEN, second);

        assertTrue(pawn.isOnFinalPath());
        assertTrue(pawn.canMove(2));
        assertEquals(third, pawn.destination(2));
        assertFalse(pawn.canMove(3));
    }

    /**
     * A 6 takes a pawn out of its stable, also onto a pawn of another team (sent back), not onto a pawn of its team.
     */
    @Test
    void shouldLeaveTheStableOnASixUnlessAPawnOfTheTeamIsOnTheStart() throws Exception {
        buildTrack();
        Pawn inStable = pawnOn(Horses.TEAMS.YELLOW, null);

        assertTrue(inStable.canSpawn(6));
        assertFalse(inStable.canSpawn(5));
        pawnOn(Horses.TEAMS.RED, track[0]);
        assertTrue(inStable.canSpawn(6), "a pawn of another team on the start square is sent back");
        assertTrue(inStable.wouldCapture(6));
        track[0].stationnedPawn = null;
        pawnOn(Horses.TEAMS.YELLOW, track[0]);
        assertFalse(inStable.canSpawn(6));
    }
}
