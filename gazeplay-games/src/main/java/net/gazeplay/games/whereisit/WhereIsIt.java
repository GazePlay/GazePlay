package net.gazeplay.games.whereisit;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.gamevariants.difficulty.SourceSet;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.games.ForegroundSoundsUtils;
import net.gazeplay.commons.utils.games.ResourceFileManager;
import net.gazeplay.commons.utils.multilinguism.Multilinguism;
import net.gazeplay.commons.utils.multilinguism.MultilinguismFactory;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.commons.utils.stats.TargetAOI;
import net.gazeplay.commons.whereisit.CustomGame;
import net.gazeplay.commons.whereisit.CustomScreen;
import net.gazeplay.components.GameStyle;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static net.gazeplay.games.whereisit.WhereIsItGameType.*;

/**
 * Games of the "Where is it?" family (animals, colors, flags, letters, numbers, shapes, sounds, the odd one out, or the
 * pictures of the user): a question is asked, written and said, and the player chooses the matching picture among
 * several by a fixation. In the variant with levels, the level (1 to 5) chooses the set of pictures shown with the
 * right one; it goes up or down every 8 goals, depending on whether the answers differ from answers at random
 * (chi-squared test, shown on the statistics screen).
 * <p>
 * The question is first shown in the middle of the screen, then goes to a band at the bottom, with a button to hear
 * it again; the pictures are rounded cards above the band.
 * <p>
 * Created by Didier Schwab on the 18/11/2017
 */
@Slf4j
public class WhereIsIt implements GameLifeCycle {

    /**
     * Languages whose voices say the questions: French and English, the only languages of GazePlay Learning since
     * 18/11/2026 (the Chinese voices, which no language offered could reach any more, are removed).
     */
    private static final Set<String> LANGUAGES_WITH_VOICES = Set.of("fra", "eng");

    /**
     * Longest move of the question from the middle of the screen to the band, in ms.
     */
    private static final double QUESTION_MOVE_MAX_LENGTH = 800;

    private Group questionPill;

    /**
     * The animation of the question being shown, stopped when the game is left meanwhile.
     */
    private Transition questionAnimation;

    private ReplayQuestionButton replayButton;

    @Getter
    private final WhereIsItGameType gameType;

    /**
     * The grid of the screen being played: that of the variant, or of each screen of a game made by the user.
     */
    @Getter
    private int nbLines;
    @Getter
    private int nbColumns;

    /**
     * The game made by the user ("Where is it?", 25/11/2026), null for the other games, or when it no longer exists.
     */
    @Getter
    private CustomGame customGame;

    /**
     * The screens of the game made by the user still to play, before they are shuffled again.
     */
    private final List<CustomScreen> screensToPlay = new ArrayList<>();

    /**
     * The screen being played of the game made by the user.
     */
    @Getter
    private CustomScreen customScreen;

    private int level = 1;
    private int lvlReplays = 1;
    private int rightDecision = 0;
    private int wrongDecision = 0;
    private boolean firstWrong = false;

    /**
     * Name of the folder of the right answer of the previous round: it is not the right answer of the next one.
     */
    private String previousWinnerFolderName;

    /**
     * The right answers still to ask before the list is shuffled again (see {@link #nextWinner}).
     */
    private final List<String> winnersToAsk = new ArrayList<>();

    private final IGameContext gameContext;
    private final Stats stats;
    private RoundDetails currentRoundDetails;
    private final ReplayablePseudoRandom randomGenerator;

    private final ArrayList<TargetAOI> targetAOIList;

    /**
     * The end of the game (16/11/2026): after a number of screens found, or without end.
     */
    @Getter
    private final WhereIsItEnd end;

    /**
     * The screens found since the start of the game.
     */
    @Getter
    private int screensFound;

    /**
     * Whether the last screen has been found (or the game lost): the next {@link #launch()} starts a new game.
     */
    private boolean ended;

    /**
     * The counter of the screens, in the band (see {@link #showCounter()}).
     */
    private Group counterPill;

    /**
     * The lives at the start of a game with the hearts (maintainer's requests of 16/11 and 17/11/2026, with or without
     * end): a wrong picture costs one.
     */
    static final int LIVES = 3;

    /**
     * Whether the user chose the hearts (16/11/2026); without them, the game is never lost.
     */
    private final boolean withLives;

    /**
     * The lives left; none are shown without hearts.
     */
    @Getter
    private int lives = LIVES;

    /**
     * Whether the game is lost: no life left, "Lost!" then the statistics.
     */
    @Getter
    private boolean lost;

    private Group livesGroup;

    private Group replayHint;

    private PauseTransition hintPause;

    private ScaleTransition replayPulse;

    private PauseTransition lostPause;

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns,
                     final IGameContext gameContext, final Stats stats) {
        this(gameType, nbLines, nbColumns, gameContext, stats, WhereIsItEnd.ENDLESS);
    }

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns,
                     final IGameContext gameContext, final Stats stats, final WhereIsItEnd end) {
        this(gameType, nbLines, nbColumns, gameContext, stats, end, true);
    }

    /**
     * @param withLives whether the user chose the hearts (variant, {@code LivesVariant}); they only apply with an end
     */
    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns,
                     final IGameContext gameContext, final Stats stats, final WhereIsItEnd end,
                     final boolean withLives) {
        this.withLives = withLives;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.stats = stats;
        this.end = end;
        this.targetAOIList = new ArrayList<>();
        this.randomGenerator = new ReplayablePseudoRandom();
        this.stats.setGameSeed(randomGenerator.getSeed());
    }

    public WhereIsIt(final WhereIsItGameType gameType, final int nbLines, final int nbColumns,
                     final IGameContext gameContext, final Stats stats, final WhereIsItEnd end,
                     final boolean withLives, double gameSeed) {
        this.withLives = withLives;
        this.gameContext = gameContext;
        this.nbLines = nbLines;
        this.nbColumns = nbColumns;
        this.gameType = gameType;
        this.stats = stats;
        this.end = end;
        this.targetAOIList = new ArrayList<>();
        this.randomGenerator = new ReplayablePseudoRandom(gameSeed);
    }

    /**
     * A game made by the user (maintainer's request of 25/11/2026, point 86): each of its screens once, in an order drawn
     * at random, with its grid, its pictures at their places, its sentence and sound.
     *
     * @param customGame the game, null when it no longer exists: the game says so
     */
    public WhereIsIt(final IGameContext gameContext, final Stats stats, final CustomGame customGame,
                     final boolean withLives) {
        this(CUSTOMIZED, 2, 2, gameContext, stats, WhereIsItEnd.ENDLESS, withLives);
        this.customGame = customGame;
    }

    /**
     * The same, replayed with a saved seed.
     */
    public WhereIsIt(final IGameContext gameContext, final Stats stats, final CustomGame customGame,
                     final boolean withLives, final double gameSeed) {
        this(CUSTOMIZED, 2, 2, gameContext, stats, WhereIsItEnd.ENDLESS, withLives, gameSeed);
        this.customGame = customGame;
    }

    /**
     * @return the screens to find to end the game, 0 without end
     */
    int screensToFind() {
        if (gameType == CUSTOMIZED) {
            return customGame != null ? customGame.playableScreens().size() : 0;
        }
        return end.getScreens();
    }

    /**
     * Takes the next screen of the game made by the user: from its playable screens, shuffled, shuffled again once they
     * have all been played, never the same twice in a row.
     */
    void nextCustomScreen() {
        final List<CustomScreen> playable = customGame == null ? List.of() : customGame.playableScreens();
        if (playable.isEmpty()) {
            customScreen = null;
            return;
        }
        if (screensToPlay.isEmpty()) {
            screensToPlay.addAll(playable);
            for (int i = screensToPlay.size() - 1; i > 0; i--) {
                Collections.swap(screensToPlay, i, randomGenerator.nextInt(i + 1));
            }
            if (screensToPlay.getFirst() == customScreen) {
                Collections.swap(screensToPlay, 0, screensToPlay.size() - 1);
            }
        }
        customScreen = screensToPlay.removeFirst();
        nbLines = customScreen.getLines();
        nbColumns = customScreen.getColumns();
    }

    /**
     * Counts a screen found (its right picture chosen).
     *
     * @return whether it was the last screen of the game
     */
    boolean screenFound() {
        screensFound++;
        ended = screensToFind() > 0 && screensFound >= screensToFind();
        // The counter keeps the number of the screen just found until the next one appears
        return ended;
    }

    /**
     * @return the number of the screen being played (tester's feedback of 20/11/2026, point 14): 1 for the first
     *   screen, never beyond the last screen of the end
     */
    int currentScreen() {
        final int current = screensFound + 1;
        return screensToFind() > 0 ? Math.min(current, screensToFind()) : current;
    }

    /**
     * The screen being played, from its start (tester's feedback of 20/11/2026, point 14; until then the screens
     * found): "Screen 3 / 5" ("Screen 7" without end), in the corner on the left of the band, which the grid leaves free.
     */
    void showCounter() {
        gameContext.getChildren().remove(counterPill);
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final GameSizing sizing = new GameSizingComputer(nbLines, nbColumns, hasLives()).computeGameSizing(dimension);
        final String text = gameContext.getTranslator().translate("WhereIsItScreen") + " " + currentScreen()
            + (screensToFind() > 0 ? " / " + screensToFind() : "");
        final double room = sizing.bandLeft() - 2 * sizing.gap();
        final double font = sizing.bandHeight() * 0.18;
        counterPill = GameStyle.pill(text, font);
        final double pillWidth = counterPill.getLayoutBounds().getWidth();
        if (pillWidth > room) {
            counterPill = GameStyle.pill(text, font * room / pillWidth);
        }
        counterPill.setMouseTransparent(true);
        counterPill.setLayoutX(sizing.gap());
        counterPill.setLayoutY(sizing.bandTop() + (sizing.bandHeight()
            - counterPill.getLayoutBounds().getHeight()) / 2);
        gameContext.getChildren().add(counterPill);
    }

    /**
     * @return the counter of the screens (for the tests)
     */
    Group counter() {
        return counterPill;
    }

    /**
     * @return whether the game has lives: the hearts chosen, with or without end (maintainer's decision of 17/11/2026)
     */
    boolean hasLives() {
        return withLives;
    }

    /**
     * The lives, at the top left as in the other games.
     */
    private void showLives() {
        gameContext.getChildren().remove(livesGroup);
        livesGroup = null;
        if (!hasLives()) {
            return;
        }
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        livesGroup = GameStyle.lives(lives, LIVES, dimension.getHeight() * 0.055, dimension.getWidth() * 0.3);
        livesGroup.setLayoutX(dimension.getHeight() * 0.025);
        livesGroup.setLayoutY(dimension.getHeight() * 0.015);
        gameContext.getChildren().add(livesGroup);
    }

    /**
     * @return the lives shown (for the tests), null without hearts
     */
    Group livesShown() {
        return livesGroup;
    }

    /**
     * A wrong picture chosen: a mistake (17/11/2026: it was not counted, though the guides said so), one life less in a
     * game with the hearts.
     *
     * @return whether it was the last life: the game is lost
     */
    boolean wrongPictureChosen() {
        if (lost) {
            return false;
        }
        stats.incrementNumberOfMistakes();
        if (!hasLives()) {
            return false;
        }
        lives = Math.max(0, lives - 1);
        showLives();
        lost = lives == 0;
        return lost;
    }

    /**
     * No life left: the pictures can no longer be chosen, "Lost!" for 2 s, then the statistics; the "continue" of the
     * statistics starts a new game.
     */
    void lose() {
        ended = true;
        hideReplayHint();
        if (currentRoundDetails != null && currentRoundDetails.getPictureCardList() != null) {
            currentRoundDetails.getPictureCardList().forEach(PictureCard::lock);
        }
        stats.takeScreenShot();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final Group card = GameStyle.card(gameContext.getTranslator().translate("WhereIsItLost"),
            dimension.getHeight() * 0.06, dimension.getWidth() * 0.6);
        card.setLayoutX(dimension.getWidth() / 2 - card.getBoundsInLocal().getWidth() / 2);
        card.setLayoutY(dimension.getHeight() / 2 - card.getBoundsInLocal().getHeight() / 2);
        card.setMouseTransparent(true);
        gameContext.getChildren().add(card);
        lostPause = new PauseTransition(Duration.seconds(2));
        lostPause.setOnFinished(event -> {
            dispose();
            gameContext.clear();
            gameContext.showRoundStats(stats, this);
        });
        lostPause.play();
    }

    /**
     * After a mistake, when the question has a sound: a bubble next to the loudspeaker says it can be heard again, and
     * the loudspeaker pulses (maintainer's request of 16/11/2026).
     */
    void showReplayHint() {
        if (replayButton == null || currentRoundDetails == null || currentRoundDetails.getQuestionSoundPath() == null) {
            return;
        }
        hideReplayHint();
        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final GameSizing sizing = new GameSizingComputer(nbLines, nbColumns, hasLives()).computeGameSizing(dimension);
        final String key = gameType.toString().contains("SOUNDS") ? "WhereIsItReplaySoundHint" : "WhereIsItReplayHint";
        replayHint = GameStyle.pill(gameContext.getTranslator().translate(key), sizing.bandHeight() * 0.16);
        replayHint.setMouseTransparent(true);
        final Bounds bounds = replayHint.getLayoutBounds();
        replayHint.setLayoutX(Math.max(sizing.gap(), dimension.getWidth() - sizing.gap() - bounds.getWidth()));
        replayHint.setLayoutY(sizing.bandTop() - bounds.getHeight() - sizing.gap());
        gameContext.getChildren().add(replayHint);
        replayPulse = new ScaleTransition(Duration.millis(450), replayButton);
        replayPulse.setFromX(1);
        replayPulse.setFromY(1);
        replayPulse.setToX(1.18);
        replayPulse.setToY(1.18);
        replayPulse.setAutoReverse(true);
        replayPulse.setCycleCount(6);
        replayPulse.play();
        hintPause = new PauseTransition(Duration.seconds(4));
        hintPause.setOnFinished(event -> hideReplayHint());
        hintPause.play();
    }

    /**
     * @return the bubble saying that the question can be heard again (for the tests), null when hidden
     */
    Group replayHint() {
        return replayHint;
    }

    private void hideReplayHint() {
        if (hintPause != null) {
            hintPause.stop();
            hintPause = null;
        }
        if (replayPulse != null) {
            replayPulse.stop();
            replayPulse = null;
        }
        if (replayButton != null) {
            replayButton.setScaleX(1);
            replayButton.setScaleY(1);
        }
        gameContext.getChildren().remove(replayHint);
        replayHint = null;
    }

    @Override
    public void launch() {
        // The game counts the mistakes: 0 until the first one (statistics, format 5)
        stats.countMistakes();
        if (ended) {
            ended = false;
            screensFound = 0;
            lives = LIVES;
            lost = false;
            screensToPlay.clear();
        }
        if (gameType == CUSTOMIZED) {
            nextCustomScreen();
        }
        showCounter();
        showLives();
        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;
        log.debug("numberOfImagesToDisplayPerRound = {}", numberOfImagesToDisplayPerRound);

        final int winnerImageIndexAmongDisplayedImages = customScreen != null ? customScreen.getRightAnswer()
            : randomGenerator.nextInt(numberOfImagesToDisplayPerRound);
        log.debug("winnerImageIndexAmongDisplayedImages = {}", winnerImageIndexAmongDisplayedImages);

        if (stats.nbGoalsReached > 0 && stats.nbGoalsReached % 8 == 0) {
            stats.getChiReport().addChiObs(chi2Obs(rightDecision, wrongDecision));
            stats.getChiReport().addChiLevel(level);
            boolean randomness = chi2decision(rightDecision, wrongDecision);
            lvlReplays++;

            if (randomness && rightDecision > wrongDecision) {
                if (level < 5) {
                    level++;
                }
                rightDecision = 0;
                wrongDecision = 0;
                lvlReplays = 1;
            }
            if (!randomness && level > 1) {
                level--;
                rightDecision = 0;
                wrongDecision = 0;
                lvlReplays = 1;
            }
        }

        currentRoundDetails = pickAndBuildRandomPictures(numberOfImagesToDisplayPerRound, randomGenerator,
            winnerImageIndexAmongDisplayedImages);

        if (currentRoundDetails != null) {
            questionAnimation = createQuestionTransition(currentRoundDetails.getQuestion());
            questionAnimation.play();
            if (currentRoundDetails.getQuestionSoundPath() != null) {
                playQuestionSound();
            }
        }

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);

        if (gameType.toString().contains("SOUNDS")) {
            gameContext.pauseBackgroundMusic();
        }
    }

    /**
     * @return the animation showing the question (or a loudspeaker for the sounds) in the middle of the screen, then
     *   moving it to the band at the bottom before the pictures appear
     */
    private Transition createQuestionTransition(final String question) {
        final Dimension2D gamePaneDimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double width = gamePaneDimension2D.getWidth();
        final double height = gamePaneDimension2D.getHeight();
        final GameSizing sizing = new GameSizingComputer(nbLines, nbColumns, hasLives()).computeGameSizing(gamePaneDimension2D);
        // A screen made by the user may have no sentence, and no sound either: its pictures then come at once
        final boolean withoutSentence = gameType == CUSTOMIZED && (question == null || question.isBlank());
        final boolean withSound = currentRoundDetails != null && currentRoundDetails.getQuestionSoundPath() != null;
        final long questionLength = withoutSentence && !withSound ? 0
            : gameContext.getConfiguration().getQuestionLength();

        // In the middle: as large as the screen allows
        final double largeFont = Math.min(height / 9, width / 14);
        questionPill = GameStyle.pill(question == null ? "" : question, largeFont);
        double pillWidth = questionPill.getLayoutBounds().getWidth();
        if (pillWidth > width * 0.9) {
            questionPill = GameStyle.pill(question == null ? "" : question, largeFont * width * 0.9 / pillWidth);
            pillWidth = questionPill.getLayoutBounds().getWidth();
        }
        final double pillHeight = questionPill.getLayoutBounds().getHeight();
        final double pillCenterY = height * 0.45;
        questionPill.setLayoutX(width / 2 - pillWidth / 2);
        questionPill.setLayoutY(pillCenterY - pillHeight / 2);
        questionPill.setVisible(!withoutSentence);
        gameContext.getChildren().add(questionPill);

        final long timeStarted = System.currentTimeMillis();
        final TargetAOI targetAOI = new TargetAOI(width / 2, pillCenterY, (int) pillWidth, timeStarted);
        targetAOI.setTimeEnded(timeStarted + questionLength);
        targetAOIList.add(targetAOI);

        // Shown with the question, removed when the pictures appear
        final List<Node> questionDecorations = new ArrayList<>();

        final List<Transition> decorationAnimations = new ArrayList<>();
        if (gameType.toString().contains("SOUNDS") || withoutSentence && withSound) {
            // A loudspeaker beating above the question while the sound is played
            final double speakerSize = height * 0.22;
            final SVGPath speakerIcon = GameStyle.icon("volume_up_fill1", speakerSize);
            speakerIcon.setStyle("-fx-fill: -gp-accent;");
            final Group speaker = new Group(speakerIcon);
            speaker.setLayoutX(width / 2 - speakerSize / 2);
            speaker.setLayoutY(pillCenterY - pillHeight / 2 - sizing.gap() * 2 - speakerSize);
            final ScaleTransition beat = new ScaleTransition(new Duration(450), speaker);
            beat.setToX(1.15);
            beat.setToY(1.15);
            beat.setAutoReverse(true);
            beat.setCycleCount(Transition.INDEFINITE);
            decorationAnimations.add(beat);
            questionDecorations.add(speaker);
        }
        gameContext.getChildren().addAll(questionDecorations);
        decorationAnimations.forEach(Transition::play);

        // In the band: centered between the counter of the screens and the button to hear the question again
        final double bandFont = sizing.bandHeight() * 0.34;
        final double scale = Math.min(bandFont / Math.min(largeFont, largeFont * width * 0.9 / Math.max(1, pillWidth)),
            (sizing.bandRight() - sizing.bandLeft() - 2 * sizing.gap()) / pillWidth);
        final Duration moveLength = Duration.millis(Math.min(questionLength / 2.0, QUESTION_MOVE_MAX_LENGTH));

        final TranslateTransition move = new TranslateTransition(moveLength, questionPill);
        move.setToX(sizing.bandCenterX() - width / 2);
        move.setToY(sizing.bandTop() + sizing.bandHeight() / 2 - pillCenterY);
        final ScaleTransition shrink = new ScaleTransition(moveLength, questionPill);
        shrink.setToX(scale);
        shrink.setToY(scale);
        final ParallelTransition fullAnimation = new ParallelTransition(move, shrink);
        fullAnimation.setInterpolator(Interpolator.EASE_BOTH);
        fullAnimation.setDelay(Duration.millis(questionLength));

        fullAnimation.setOnFinished(actionEvent -> {
            decorationAnimations.forEach(Transition::stop);
            gameContext.getChildren().removeAll(questionDecorations);

            if (currentRoundDetails != null) {
                gameContext.getChildren().addAll(currentRoundDetails.getPictureCardList());

                for (final PictureCard p : currentRoundDetails.getPictureCardList()) {
                    p.toFront();
                    p.setOpacity(0);
                    final FadeTransition appear = new FadeTransition(Duration.millis(300), p);
                    appear.setToValue(1);
                    appear.play();
                }

                if (currentRoundDetails.getQuestionSoundPath() != null) {
                    final double buttonSize = sizing.replayButtonSize();
                    replayButton = new ReplayQuestionButton(gameContext, width - sizing.gap() - buttonSize / 2,
                        sizing.bandTop() + sizing.bandHeight() / 2, buttonSize, this::playQuestionSound);
                    gameContext.getChildren().add(replayButton);
                }
            }
            questionPill.toFront();

            stats.notifyNewRoundReady();

            gameContext.onGameStarted();
        });

        return fullAnimation;
    }

    /**
     * Plays the sound of the question, stopping the previous sound if it is still playing.
     */
    void playQuestionSound() {
        if (currentRoundDetails == null || currentRoundDetails.getQuestionSoundPath() == null) {
            return;
        }
        ForegroundSoundsUtils.stopSound();
        gameContext.getSoundManager().add(currentRoundDetails.getQuestionSoundPath());
    }

    /**
     * this method should be called when exiting the game, or before starting a new round, in order to clean up all
     * resources in both UI and memory
     */
    @Override
    public void dispose() {
        if (questionAnimation != null) {
            questionAnimation.stop();
            questionAnimation = null;
        }
        gameContext.getChildren().remove(counterPill);
        gameContext.getChildren().remove(livesGroup);
        hideReplayHint();
        if (lostPause != null && lostPause.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            lostPause.stop();
        }
        if (replayButton != null) {
            replayButton.dispose();
            gameContext.getChildren().remove(replayButton);
            replayButton = null;
        }
        if (currentRoundDetails != null) {
            if (currentRoundDetails.getPictureCardList() != null) {
                gameContext.getChildren().removeAll(currentRoundDetails.getPictureCardList());
            }
            currentRoundDetails = null;
        }
        stats.setTargetAOIList(targetAOIList);
    }

    /**
     * Removes all the pictures except the right one, and records the end of the round for the areas of interest.
     */
    void removeAllIncorrectPictureCards() {
        //set the target AOI end time for this round

        final long endTime = System.currentTimeMillis();
        final int numberOfImagesToDisplayPerRound = nbLines * nbColumns;

        for (int i = 1; i <= numberOfImagesToDisplayPerRound; i++) {
            targetAOIList.get(targetAOIList.size() - i).setTimeEnded(endTime);
        }

        if (this.currentRoundDetails == null) {
            return;
        }

        // Collect all items to be removed from the User Interface
        final List<PictureCard> pictureCardsToHide = new ArrayList<>();
        for (final PictureCard pictureCard : this.currentRoundDetails.getPictureCardList()) {
            if (!pictureCard.isWinner()) {
                pictureCardsToHide.add(pictureCard);
            }
        }
        // remove all at once, in order to update the UserInterface only once
        gameContext.getChildren().removeAll(pictureCardsToHide);
    }

    /**
     * @param folders  resource folders of pictures (e.g. "data/whereIsTheLetter/images/A")
     * @param variant  names of the folders of a variant (e.g. "A", "E"); empty for all the folders
     * @return the folders whose name is one of the variant. The whole name is compared: a part of the path used to be
     *   enough, so that the vowels showed every letter ("I" is in "whereIsTheLetter") and the farm the pigeon ("pig")
     */
    static Set<String> filterFolders(final Set<String> folders, final Set<String> variant) {
        if (variant.isEmpty()) {
            return folders;
        }
        return folders.stream()
            .filter(folder -> variant.contains(new File(folder).getName()))
            .collect(Collectors.toSet());
    }

    /**
     * @return the pictures of a round, drawn at random from the folders of the game (or of the custom folder), one of
     *   them the right answer; null when there are not enough folders
     */
    RoundDetails pickAndBuildRandomPictures(final int numberOfImagesToDisplayPerRound, final ReplayablePseudoRandom random,
                                            final int winnerImageIndexAmongDisplayedImages) {
        final Configuration config = gameContext.getConfiguration();

        if (this.gameType == CUSTOMIZED) {
            return buildCustomScreen();
        }

        int directoriesCount;
        final String directoryName;
        List<String> resourcesFolders = new LinkedList<>();
        List<String> winnerFolders = new LinkedList<>();

        if (this.gameType == ANIMALS_DYNAMIC) {
            final String resourcesDirectory = "data/" + this.gameType.getResourcesDirectoryName();
            directoryName = resourcesDirectory;

            final String winnerImagesDirectory = resourcesDirectory + "/images/cats/";
            resourcesFolders.addAll(ResourceFileManager.getResourceFolders(resourcesDirectory + "/images/lvl" + level + "/"));
            winnerFolders.addAll(ResourceFileManager.getResourceFolders(winnerImagesDirectory));
            directoriesCount = resourcesFolders.size();
        } else {
            final String resourcesDirectory = "data/" + this.gameType.getResourcesDirectoryName();
            final String imagesDirectory = resourcesDirectory + "/images/";
            directoryName = imagesDirectory;

            // Here we filter out any unwanted resource folders, based on the variants JSON file
            Set<String> variantSet;
            try {
                SourceSet sourceSet = new SourceSet(resourcesDirectory + "/variants.json");
                variantSet = (sourceSet.getResources(this.gameType.getVariant()));
            } catch (FileNotFoundException fe) {
                log.info("No difficulty file found; Reading from all directories");
                variantSet = Collections.emptySet();
            }

            resourcesFolders.addAll(filterFolders(ResourceFileManager.getResourceFolders(imagesDirectory), variantSet));
            directoriesCount = resourcesFolders.size();
        }

        final String language = config.getLanguage();

        if (directoriesCount == 0) {
            log.warn("No images found in Directory " + directoryName);
            error(language);
            return null;
        }

        // The picture of each place of the grid. All the draws come from the generator of the game, so that a saved
        // game is replayed with the same pictures by this version. The right answer is no longer drawn at random at
        // each screen but taken from a shuffled list (tester's feedback of 20/11/2026, point 15): the replay of a game
        // saved by a former version shows other pictures.
        final List<String> imagePaths = new ArrayList<>();
        String voice = null;
        String questionSoundPath;
        String question;

        if (this.gameType == FIND_ODD || this.gameType == ANIMALS_DYNAMIC) {
            final String folder;
            final String winnerFolder;
            if (this.gameType == FIND_ODD) {
                // The odd one out from the shuffled list, the other pictures from another folder drawn at random
                final String oddName = nextWinner(folderNamesOf(resourcesFolders), random);
                winnerFolder = removeFolderNamed(resourcesFolders, oddName);
                folder = resourcesFolders.remove(random.nextInt(resourcesFolders.size()));
            } else {
                // Where is the animal? (dynamic) is unchanged: one cat among the animals of the level
                folder = resourcesFolders.remove(random.nextInt(resourcesFolders.size()));
                winnerFolder = winnerFolders.remove(0);
            }
            final String folderName = (new File(winnerFolder)).getName();
            log.info("WinnerFolderName = {}", folderName);
            previousWinnerFolderName = folderName;

            // The other pictures are all different, as long as the folder has enough of them
            final List<String> otherFiles = new ArrayList<>(ResourceFileManager.getResourcePaths(folder));
            final List<String> remainingOtherFiles = new ArrayList<>(otherFiles);
            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {
                if (i == winnerImageIndexAmongDisplayedImages) {
                    final Set<String> files = ResourceFileManager.getResourcePaths(winnerFolder);
                    imagePaths.add((String) files.toArray()[random.nextInt(files.size())]);
                    if (this.gameType == ANIMALS_DYNAMIC) {
                        voice = drawVoice(language);
                    }
                } else {
                    if (remainingOtherFiles.isEmpty()) {
                        remainingOtherFiles.addAll(otherFiles);
                    }
                    imagePaths.add(remainingOtherFiles.remove(random.nextInt(remainingOtherFiles.size())));
                }
            }

            if (this.gameType == FIND_ODD) {
                question = MultilinguismFactory.getSingleton().getTranslation("FindTheOddOneOut", language);
                questionSoundPath = getPathSound("odd", language, drawVoice(language));
            } else {
                questionSoundPath = getPathSound(folderName, language, voice);
                question = getQuestionText(folderName, language);
            }
        } else {
            // The right answer from the shuffled list, the other pictures from other folders drawn at random
            final String folderName = nextWinner(folderNamesOf(resourcesFolders), random);
            final String winnerResourceFolder = removeFolderNamed(resourcesFolders, folderName);
            for (int i = 0; i < numberOfImagesToDisplayPerRound; i++) {
                final boolean winnerPlace = i == winnerImageIndexAmongDisplayedImages;
                final String folder = winnerPlace ? winnerResourceFolder
                    : resourcesFolders.remove(random.nextInt(resourcesFolders.size()));
                final Set<String> files = ResourceFileManager.getResourcePaths(folder);
                imagePaths.add((String) files.toArray()[random.nextInt(files.size())]);
                if (winnerPlace) {
                    voice = drawVoice(language);
                }
            }

            previousWinnerFolderName = folderName;
            questionSoundPath = getPathSound(folderName, language, voice);
            question = getQuestionText(folderName, language);
        }

        return new RoundDetails(pictureCards(imagePaths, winnerImageIndexAmongDisplayedImages),
            winnerImageIndexAmongDisplayedImages, questionSoundPath, question);
    }

    /**
     * @return the pictures of a screen at their places, each with its area of interest
     */
    private List<PictureCard> pictureCards(final List<String> imagePaths, final int winner) {
        final GameSizing gameSizing = new GameSizingComputer(nbLines, nbColumns, hasLives())
            .computeGameSizing(gameContext.getGamePanelDimensionProvider().getDimension2D());
        final List<PictureCard> pictureCardList = new ArrayList<>();
        for (int i = 0; i < imagePaths.size(); i++) {
            final double x = gameSizing.cellX(i % nbColumns);
            final double y = gameSizing.cellY(i / nbColumns);
            pictureCardList.add(new PictureCard(x, y, gameSizing.cellWidth(), gameSizing.cellHeight(), gameContext,
                winner == i, imagePaths.get(i), stats, this));

            final TargetAOI targetAOI = new TargetAOI(x + gameSizing.cellWidth() / 2, y + gameSizing.cellHeight() / 2,
                (int) (Math.max(gameSizing.cellWidth(), gameSizing.cellHeight()) / 2), System.currentTimeMillis());
            targetAOIList.add(targetAOI);
        }
        return pictureCardList;
    }

    /**
     * @return the screen being played of the game made by the user; null, with a message, when there is none
     */
    private RoundDetails buildCustomScreen() {
        if (customScreen == null) {
            log.warn("No game of the user to play");
            message(gameContext.getTranslator().translate("WhereIsItNoGame"));
            return null;
        }
        final List<String> imagePaths = customScreen.getPictures().stream().map(path -> path.toUri().toString()).toList();
        final String sound = customScreen.getSound() != null && Files.isRegularFile(customScreen.getSound())
            ? customScreen.getSound().toAbsolutePath().toString() : null;
        return new RoundDetails(pictureCards(imagePaths, customScreen.getRightAnswer()), customScreen.getRightAnswer(),
            sound, customScreen.getSentence());
    }

    /**
     * The right answer of the next screen (tester's feedback of 20/11/2026, point 15): taken from a shuffled list of
     * all the possible answers, shuffled again once it is used up, so that no answer comes back before all the others
     * have been asked, and never twice in a row, even between two lists.
     *
     * @param candidates the names of the folders which can be the right answer
     * @return the name of the folder of the right answer
     */
    String nextWinner(final List<String> candidates, final ReplayablePseudoRandom random) {
        // A folder no longer offered (none in the games of the application, but the user's folder could change)
        winnersToAsk.retainAll(candidates);
        if (winnersToAsk.isEmpty()) {
            // Sorted first: the order of the folders found does not change the draw (replay)
            final List<String> shuffled = new ArrayList<>(candidates.stream().sorted().distinct().toList());
            for (int i = shuffled.size() - 1; i > 0; i--) {
                Collections.swap(shuffled, i, random.nextInt(i + 1));
            }
            if (shuffled.size() > 1 && shuffled.get(0).equals(previousWinnerFolderName)) {
                // The last answer of a list is not the first one of the next list
                Collections.swap(shuffled, 0, shuffled.size() - 1);
            }
            winnersToAsk.addAll(shuffled);
        }
        previousWinnerFolderName = winnersToAsk.remove(0);
        return previousWinnerFolderName;
    }

    /**
     * @return the names of the folders (their last part)
     */
    private static List<String> folderNamesOf(final List<String> folders) {
        return folders.stream().map(folder -> new File(folder).getName()).toList();
    }

    /**
     * Removes a folder from a list of folders.
     *
     * @return the folder whose name is the given one
     */
    private static String removeFolderNamed(final List<String> folders, final String name) {
        final String folder = folders.stream().filter(path -> new File(path).getName().equals(name)).findFirst()
            .orElseThrow();
        folders.remove(folder);
        return folder;
    }

    /**
     * Shows an error message when the pictures cannot be found, on a rounded label in the middle of the game area.
     */
    private void error(final String language) {
        message(MultilinguismFactory.getSingleton().getTranslation("WII-error", language));
    }

    private void message(final String message) {
        gameContext.clear();

        final Dimension2D dimension = gameContext.getGamePanelDimensionProvider().getDimension2D();
        final double width = dimension == null ? 0 : dimension.getWidth();
        final double height = dimension == null ? 0 : dimension.getHeight();

        final double fontSize = Math.max(16, height / 20);
        Group label = GameStyle.pill(message, fontSize);
        final Bounds bounds = label.getLayoutBounds();
        if (width > 0 && bounds.getWidth() > width * 0.9) {
            label = GameStyle.pill(message, fontSize * width * 0.9 / bounds.getWidth());
        }
        label.setLayoutX(width / 2 - label.getLayoutBounds().getWidth() / 2);
        label.setLayoutY(height / 2 - label.getLayoutBounds().getHeight() / 2);
        gameContext.getChildren().add(label);
    }

    /**
     * @return the voice saying the question ("m" or "w", drawn at random), or null in the custom game and in the
     *   languages without voices. It is drawn in the languages where the former versions drew it, so that a saved
     *   game is replayed with the same pictures.
     */
    String drawVoice(final String language) {
        if (!LANGUAGES_WITH_VOICES.contains(language)) {
            return null;
        }
        // Still drawn, so that a saved game is replayed with the same pictures; the voice is the one of the settings
        // since 18/11/2026 (maintainer's request: chosen in the main menu, a woman or a man), no longer at random
        randomGenerator.nextDouble();
        final String voice = gameContext.getConfiguration().getVoice();
        return Configuration.VOICE_MALE.equals(voice) ? Configuration.VOICE_MALE : Configuration.VOICE_FEMALE;
    }

    /**
     * @param voice the voice of the question ({@link #drawVoice(String)})
     * @return the sound of the question of a folder, in the language of the settings; null if there is none in this
     *   language
     */
    String getPathSound(final String folder, final String language, final String voice) {
        log.debug("language is " + language);

        if (gameType.toString().contains("SOUNDS")) {
            // The sound of the animal or the instrument is the same in every language
            return "data/" + this.gameType.getResourcesDirectoryName() + "/sounds/" + folder + ".mp3";
        }
        if (voice == null) {
            return null;
        }

        // The voices of the questions: a folder by language (a single one, "sound", for the odd one out)
        final String path = "data/" + this.gameType.getResourcesDirectoryName()
            + (this.gameType == FIND_ODD ? "/sound/" : "/sounds/") + language + "/" + folder + "." + voice + "."
            + language + ".mp3";
        return resourceExists(path) ? path : null;
    }

    /**
     * @return whether a resource (file or folder) is in the classpath
     */
    private static boolean resourceExists(final String path) {
        return WhereIsIt.class.getClassLoader().getResource(path) != null;
    }

    /**
     * @return the text of the question of a folder, in the language of the settings
     */
    private String getQuestionText(final String folder, final String language) {
        log.debug("folder: {}", folder);
        log.debug("language: {}", language);

        final Multilinguism localMultilinguism = MultilinguismFactory.getForResource(gameType.getLanguageResourceLocation());

        if (gameType.toString().contains("SOUNDS")) {
            return localMultilinguism.getTranslation("listen", language);
        }
        return localMultilinguism.getTranslation(folder, language);
    }

    /**
     * Counts a right answer of the current level.
     */
    public void updateRight() {
        rightDecision++;
    }

    /**
     * Counts a wrong answer of the current level.
     */
    public void updateWrong() {
        wrongDecision++;
    }

    /**
     * @return whether the player chose a wrong picture first in this round
     */
    public boolean getFirstWrong() {
        return firstWrong;
    }

    /**
     * Records that the player chose a wrong picture first.
     */
    public void firstWrongCardSelected() {
        firstWrong = true;
    }

    /**
     * Records that the player chose the right picture first.
     */
    public void firstRightCardSelected() {
        firstWrong = false;
    }

    /**
     * @return n!
     */
    public int factorial(int n) {
        return n == 0 ? 1 : (n * factorial(n - 1));
    }

    /**
     * @return the binomial coefficient "k among n"
     */
    public float compute(int n, int k) {
        return (float) factorial(n) / (factorial(k) * factorial(n - k));
    }

    /**
     * @return the probability of k successes in n trials of probability p (binomial law)
     */
    public double binomProba(int n, int k, double p) {
        return compute(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
    }

    /**
     * @return the chi-squared value of the right ({@code tp}) and wrong ({@code fp}) answers, against answers at random
     *   (one chance in two), over 8 answers per replay of the level
     */
    public double chi2Obs(int tp, int fp) {
        double[] probas = {8 * lvlReplays * binomProba(1, 1, 0.5), 8 * lvlReplays * binomProba(1, 0, 0.5)};

        return Math.pow(tp - probas[0], 2) / probas[0] + Math.pow(fp - probas[1], 2) / probas[1];
    }

    /**
     * Critical values of the chi-squared law with one degree of freedom, for the risks 5 %, 10 %, 25 % and 50 %.
     */
    private static final double[] CHI2_THEORETIC = {3.84, 2.71, 1.32, 0.45};

    /**
     * @return whether the answers differ from random answers: the chi-squared value reaches the critical value of
     *   {@link #chi2Threshold(int)}
     */
    public boolean chi2decision(int tp, int fp) {
        double chi2Obs = chi2Obs(tp, fp);

        log.info("tp = {}, fp = {}", tp, fp);
        log.info("chi2_obs = {}", chi2Obs);
        log.info("chi2_th = {}, replays = {}", chi2Threshold(lvlReplays), lvlReplays);

        return chi2Threshold(lvlReplays) <= chi2Obs;
    }

    /**
     * The critical value used: 0.45 (risk 50 %). From the 5th replay of a level, the index used to be
     * {@code replays - 1}, outside the 4 values: the game ended with an IndexOutOfBoundsException (a player staying
     * at level 1 with answers judged random, after 40 right answers). The index is now kept on the last value, the one
     * of the first 4 replays: the decisions that did not fail are unchanged.
     *
     * @param replays number of replays of the current level
     * @return the critical value the observed chi-squared value is compared with
     */
    static double chi2Threshold(final int replays) {
        final int index = replays > 4 ? replays - 1 : 3;
        return CHI2_THEORETIC[Math.min(index, CHI2_THEORETIC.length - 1)];
    }

    /**
     * @return the settings of the menu (Esc) acting on this game: those by default and the buttons of the games (the
     * loudspeaker saying the question again, 24/11/2026)
     */
    @Override
    public java.util.Set<net.gazeplay.MenuSetting> menuSettings() {
        return net.gazeplay.MenuSetting.fixationWithOwnRing(net.gazeplay.MenuSetting.GAME_BUTTONS);
    }
}
