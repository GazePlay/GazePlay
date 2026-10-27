package net.gazeplay.games.gazeplayEval;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.gazeplay.GamePanelDimensionProvider;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.configuration.Configuration;
import net.gazeplay.commons.configuration.EvalOptions;
import net.gazeplay.commons.eval.EvalLibrary;
import net.gazeplay.commons.gamevariants.GazeplayEvalGameVariant;
import net.gazeplay.commons.gaze.devicemanager.GazeDeviceManager;
import net.gazeplay.commons.ui.Translator;
import net.gazeplay.commons.utils.games.GazePlayDirectories;
import net.gazeplay.commons.utils.stats.Stats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sessions of GazePlay Eval without window: an evaluation in the format of the website is played to its end (the
 * mouse selects an image), and its results are written. The evaluations folder is the one of the hermetic test folder
 * (build/test-home).
 */
@ExtendWith(ApplicationExtension.class)
class GazeplayEvalTest {

    private Pane gameArea;
    private Scene scene;
    private IGameContext gameContext;
    private Configuration config;
    private Stats stats;
    private String evaluationName;
    /** The sessions in the results folder before the test: the folder is shared by the tests of the class. */
    private List<Path> sessionsBefore;

    @Start
    void start(Stage stage) {
        gameArea = new Pane();
        scene = new Scene(new Pane(gameArea), 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    void setUp() throws IOException {
        evaluationName = "Test passation " + UUID.randomUUID();
        sessionsBefore = sessions();
        config = mock(Configuration.class);
        when(config.getDelayBeforeSelectionTime()).thenReturn(0);
        when(config.getEvalOptions()).thenReturn(EvalOptions.Classique);
        when(config.isEvalSoundBlocksSelection()).thenReturn(true);
        when(config.getProgressBarSize()).thenReturn(50);
        when(config.getProgressBarColor()).thenReturn("#00FF00");
        when(config.getBackgroundColorProperty()).thenReturn(new SimpleStringProperty("#000000"));
        when(config.getEyeTracker()).thenReturn("mouse_control");
        when(config.getFixationLength()).thenReturn(100);
        when(config.getUserName()).thenReturn("");
        when(config.getHeatMapOpacity()).thenReturn(0.7);
        when(config.getHeatMapColors()).thenReturn(List.of(Color.BLUE, Color.YELLOW, Color.RED));

        Translator translator = mock(Translator.class);
        when(translator.translate(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        GamePanelDimensionProvider dimension = mock(GamePanelDimensionProvider.class);
        when(dimension.getDimension2D()).thenReturn(new Dimension2D(800, 600));

        stats = mock(Stats.class);
        gameContext = mock(IGameContext.class);
        when(gameContext.getConfiguration()).thenReturn(config);
        when(gameContext.getTranslator()).thenReturn(translator);
        when(gameContext.getGamePanelDimensionProvider()).thenReturn(dimension);
        when(gameContext.getGazeDeviceManager()).thenReturn(mock(GazeDeviceManager.class));
        when(gameContext.getChildren()).thenAnswer(invocation -> gameArea.getChildren());
        when(gameContext.getRoot()).thenReturn(gameArea);
        when(gameContext.getPrimaryScene()).thenReturn(scene);
        org.mockito.Mockito.doAnswer(invocation -> {
            gameArea.getChildren().clear();
            return null;
        }).when(gameContext).clear();
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteRecursively(EvalLibrary.evaluationFolder(evaluationName));
        for (Path session : newSessions()) {
            deleteRecursively(session);
        }
    }

    @Test
    void shouldPlayAnEvaluationToItsEndAndWriteItsResults() throws Exception {
        createEvaluation("[]", """
            [{"Type": "transition", "Mettre un temps avant passage à l'écran suivant": true, "Combien de temps": 0.2,
              "Mettre une croix de fixation": true, "Mettre un temps de fixation": false,
              "Combien de temps de fixation": 0},
             {"Type": "stimuli", "Nombre de lignes": 1, "Nombre de colonnes": 2,
              "Mettre un temps avant passage à l'écran suivant": false, "Combien de temps": 10,
              "Combien de temps de fixation": 0.2, "Choix de sélection": "Tout", "Combien à sélectionner": 1,
              "Position stimuli aléatoire": false, "Caché stimuli après selection": false, "Mettre un son": false,
              "Nom du fichier": "",
              "Liste des stimuli": {"0": {"imageName": "rouge.png", "soundName": "", "goodAnswer": false},
                                    "1": {"imageName": "bleu.png", "soundName": "", "goodAnswer": true}}}]
            """);
        GazeplayEval eval = new GazeplayEval(gameContext, new GazeplayEvalGameVariant(evaluationName), stats);

        Platform.runLater(eval::launch);

        // The grid appears after the transition: two images, each with its target
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> imageTargets().size() == 2);
        Rectangle blue = imageTargets().get(1);
        double x = blue.getBoundsInParent().getCenterX();
        double y = blue.getBoundsInParent().getCenterY();
        Platform.runLater(() -> {
            // The mouse moves over the blue image (recorded, for the heatmap), then enters it
            scene.getRoot().fireEvent(new MouseEvent(MouseEvent.MOUSE_MOVED, x, y, x, y, MouseButton.NONE, 0, false,
                false, false, false, false, false, false, false, false, false, null));
            blue.fireEvent(mouse(MouseEvent.MOUSE_ENTERED));
        });

        verify(gameContext, timeout(10_000)).showRoundStats(stats, eval);
        Path session = singleSession();
        assertTrue(session.getFileName().toString().matches("Eval\\d+_" + LocalDate.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)), session.toString());
        List<String> results = Files.readAllLines(session.resolve("resultats.csv"), StandardCharsets.UTF_8);
        assertEquals(3, results.size(), results.toString());
        assertTrue(results.get(1).startsWith("1;1;transition;"), results.get(1));
        assertTrue(results.get(1).contains(";délai écoulé;"), results.get(1));
        assertTrue(results.get(2).startsWith("2;1;stimuli;"), results.get(2));
        assertTrue(results.get(2).contains(";validé;1;2;Tout;1;L1C1:rouge.png | L1C2:bleu.png;L1C2:bleu.png;"
            + "L1C2:bleu.png;souris;"), results.get(2));
        List<String> events = Files.readAllLines(session.resolve("evenements.csv"), StandardCharsets.UTF_8);
        assertTrue(events.stream().anyMatch(line -> line.contains(";entree;souris;L1C2:bleu.png;")), events.toString());
        assertTrue(events.stream().anyMatch(line -> line.contains(";selection;souris;L1C2:bleu.png;")), events.toString());
        assertTrue(Files.exists(session.resolve("resultats.xlsx")));
        // A heatmap for each screen: of the mouse on the grid, the image alone on the transition (no position)
        assertTrue(Files.exists(session.resolve("heatmaps").resolve("ecran-02-stimuli-essai-1-souris.png")));
        assertTrue(Files.exists(session.resolve("heatmaps").resolve("ecran-01-transition-essai-1.png")));
        assertFalse(Files.exists(session.resolve("heatmaps").resolve("ecran-02-stimuli-essai-1-regard.png")));
        List<String> samples = Files.readAllLines(session.resolve("echantillons.csv"), StandardCharsets.UTF_8);
        assertTrue(samples.stream().anyMatch(line -> line.endsWith(";souris;" + Math.round(x) + ";" + Math.round(y)
            + ";L1C2:bleu.png")), samples.toString());
        assertTrue(Files.readString(session.resolve("session.json")).contains("\"terminee\": true"));

        Platform.runLater(eval::dispose);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void shouldAskTheParticipantAndWriteAnInterruptedSession() throws Exception {
        createEvaluation("[\"Prénom\"]", """
            [{"Type": "instruction", "Mettre un temps avant passage à l'écran suivant": false, "Combien de temps": 0,
              "Ajouter un media": true, "Type de media": "Texte", "Nom du fichier": "Regarde bien",
              "Mettre un temps de fixation": true, "Combien de temps de fixation": 5},
             {"Type": "transition", "Mettre un temps avant passage à l'écran suivant": false, "Combien de temps": 0,
              "Mettre une croix de fixation": true, "Mettre un temps de fixation": true,
              "Combien de temps de fixation": 5}]
            """);
        GazeplayEval eval = new GazeplayEval(gameContext, new GazeplayEvalGameVariant(evaluationName), stats);

        Platform.runLater(eval::launch);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !find(TextField.class).isEmpty());
        Platform.runLater(() -> {
            find(TextField.class).get(0).setText("Léa");
            key(KeyCode.ENTER);
        });
        // The instruction: its text and its start button
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
            () -> find(Text.class).stream().anyMatch(text -> text.getText().equals("Regarde bien")));
        // The examiner goes to the next screen, then the game is left
        Platform.runLater(() -> key(KeyCode.SPACE));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
            () -> find(Text.class).stream().noneMatch(text -> text.getText().equals("Regarde bien")));
        Platform.runLater(eval::dispose);
        WaitForAsyncUtils.waitForFxEvents();

        Path session = singleSession();
        List<String> results = Files.readAllLines(session.resolve("resultats.csv"), StandardCharsets.UTF_8);
        assertTrue(results.get(1).contains(";passé par l'examinateur;"), results.get(1));
        assertTrue(results.get(2).startsWith("2;1;transition;"), results.get(2));
        assertTrue(results.get(2).contains(";interrompu;"), results.get(2));
        String json = Files.readString(session.resolve("session.json"));
        assertTrue(json.contains("\"Prénom\": \"Léa\""), json);
        assertTrue(json.contains("\"terminee\": false"), json);
    }

    @Test
    void shouldOfferToDoAGridAgainWithTheOralSupport() throws Exception {
        when(config.getEvalOptions()).thenReturn(EvalOptions.Autonomie_Soutien_Oral);
        createEvaluation("[]", """
            [{"Type": "stimuli", "Nombre de lignes": 1, "Nombre de colonnes": 2,
              "Mettre un temps avant passage à l'écran suivant": false, "Combien de temps": 10,
              "Combien de temps de fixation": 0.1, "Choix de sélection": "BonneReponse", "Combien à sélectionner": 1,
              "Position stimuli aléatoire": true, "Caché stimuli après selection": true, "Mettre un son": false,
              "Nom du fichier": "",
              "Liste des stimuli": {"0": {"imageName": "rouge.png", "soundName": "", "goodAnswer": false},
                                    "1": {"imageName": "bleu.png", "soundName": "", "goodAnswer": true}}}]
            """);
        GazeplayEval eval = new GazeplayEval(gameContext, new GazeplayEvalGameVariant(evaluationName), stats);
        Platform.runLater(eval::launch);

        for (int attempt = 1; attempt <= 2; attempt++) {
            // A wrong answer does not count ("BonneReponse") and is hidden; the correct one ends the grid
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> imageTargets().size() == 2);
            for (String image : List.of("rouge.png", "bleu.png")) {
                Rectangle target = targetOf(image);
                Platform.runLater(() -> target.fireEvent(mouse(MouseEvent.MOUSE_ENTERED)));
                WaitForAsyncUtils.sleep(400, TimeUnit.MILLISECONDS);
            }
            // The choice screen: R does the grid again, then C continues
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> find(Text.class).stream().anyMatch(text -> text.getText().contains("EvalRestart")));
            KeyCode code = attempt == 1 ? KeyCode.R : KeyCode.C;
            Platform.runLater(() -> key(code));
        }

        verify(gameContext, timeout(10_000)).showRoundStats(stats, eval);
        Path session = singleSession();
        List<String> results = Files.readAllLines(session.resolve("resultats.csv"), StandardCharsets.UTF_8);
        assertEquals(3, results.size(), results.toString());
        assertTrue(results.get(1).startsWith("1;1;stimuli;"), results.get(1));
        assertTrue(results.get(2).startsWith("1;2;stimuli;"), results.get(2));
        // Same positions for the second attempt
        assertEquals(results.get(1).split(";")[11], results.get(2).split(";")[11]);
        for (String row : results.subList(1, 3)) {
            assertTrue(row.contains(";validé;"), row);
            // Two answers given, one correct
            assertTrue(row.contains(";1;1;oui;"), row);
        }
        List<String> events = Files.readAllLines(session.resolve("evenements.csv"), StandardCharsets.UTF_8);
        assertTrue(events.stream().anyMatch(line -> line.contains(";choix_option;clavier;recommencer;")), events.toString());
        assertTrue(events.stream().anyMatch(line -> line.contains(";choix_option;clavier;continuer;")), events.toString());

        Platform.runLater(eval::dispose);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void shouldSayHowToImportWhenThereIsNoEvaluation() throws Exception {
        GazeplayEval eval = new GazeplayEval(gameContext, null, stats);

        Platform.runLater(eval::launch);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(find(Text.class).stream().anyMatch(text -> text.getText().startsWith("EvalNoEvaluation")));
        Platform.runLater(eval::dispose);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(List.of(), newSessions());
    }

    @Test
    void shouldNumberTheSessionsOfTheDay(@TempDir Path results) throws IOException {
        LocalDate day = LocalDate.of(2026, 10, 27);

        assertEquals(results.resolve("Eval1_2026-10-27"), GazeplayEval.createSessionFolder(results, day));
        assertEquals(results.resolve("Eval2_2026-10-27"), GazeplayEval.createSessionFolder(results, day));
        // The numbers start again the next day
        assertEquals(results.resolve("Eval1_2026-10-28"), GazeplayEval.createSessionFolder(results, day.plusDays(1)));
        assertEquals(results.resolve("Eval3_2026-10-27"), GazeplayEval.createSessionFolder(results, day));
        // The results folder is created when it does not exist
        Path absent = results.resolve("GazePlayLearning_Eval");
        assertEquals(absent.resolve("Eval1_2026-10-27"), GazeplayEval.createSessionFolder(absent, day));
    }

    @Test
    void shouldWriteTheResultsInGazePlayLearningEvalOfDocuments() {
        // In the tests, the Documents folder is replaced (gazeplay.documents) and does not exist: the home folder
        Path results = GazePlayDirectories.getEvaluationResultsFolder().toPath();
        assertEquals("GazePlayLearning_Eval", results.getFileName().toString());
        assertTrue(results.startsWith(Path.of(System.getProperty("user.home"))), "Not in the test folder: " + results);
    }

    private void createEvaluation(String participantFields, String evalData) throws IOException {
        Path folder = EvalLibrary.evaluationFolder(evaluationName);
        Files.createDirectories(folder.resolve("images"));
        Files.writeString(folder.resolve("evalData.json"), evalData, StandardCharsets.UTF_8);
        Files.writeString(folder.resolve("evalInfo.json"), "{\"Nom de l'évaluation\": \"" + evaluationName
            + "\", \"Format choisi\": \"Csv&Xlsx\", \"Informations participant\": " + participantFields + "}",
            StandardCharsets.UTF_8);
        image(folder.resolve("images").resolve("rouge.png"), java.awt.Color.RED);
        image(folder.resolve("images").resolve("bleu.png"), java.awt.Color.BLUE);
    }

    private static void image(Path file, java.awt.Color color) throws IOException {
        BufferedImage image = new BufferedImage(60, 40, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, 60, 40);
        graphics.dispose();
        ImageIO.write(image, "png", file.toFile());
    }

    private static List<Path> sessions() throws IOException {
        Path results = GazePlayDirectories.getEvaluationResultsFolder().toPath();
        if (!Files.isDirectory(results)) {
            return List.of();
        }
        try (Stream<Path> sessions = Files.list(results)) {
            return sessions.toList();
        }
    }

    /**
     * @return the sessions created by the test
     */
    private List<Path> newSessions() throws IOException {
        return sessions().stream().filter(session -> !sessionsBefore.contains(session)).toList();
    }

    private Path singleSession() throws IOException {
        List<Path> created = newSessions();
        assertEquals(1, created.size(), created.toString());
        return created.get(0);
    }

    /**
     * @return the targets of the images of a grid: transparent shapes without border
     */
    private List<Rectangle> imageTargets() {
        return find(Rectangle.class).stream()
            .filter(rectangle -> Color.TRANSPARENT.equals(rectangle.getFill()) && rectangle.getStroke() == null)
            .sorted((a, b) -> Double.compare(a.getX(), b.getX()))
            .toList();
    }

    /**
     * @return the target of the image of a file
     */
    private Rectangle targetOf(String imageFile) {
        ImageView view = find(ImageView.class).stream()
            .filter(image -> image.getImage().getUrl().endsWith(imageFile))
            .findFirst().orElseThrow();
        double centerX = view.getBoundsInParent().getCenterX();
        double centerY = view.getBoundsInParent().getCenterY();
        return imageTargets().stream()
            .filter(target -> target.getBoundsInParent().contains(centerX, centerY))
            .findFirst().orElseThrow();
    }

    private <T> List<T> find(Class<T> type) {
        List<T> found = new ArrayList<>();
        collect(gameArea, type, found);
        return found;
    }

    private static <T> void collect(Node node, Class<T> type, List<T> found) {
        if (type.isInstance(node)) {
            found.add(type.cast(node));
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, type, found);
            }
        }
    }

    private void key(KeyCode code) {
        KeyEvent.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static MouseEvent mouse(javafx.event.EventType<MouseEvent> type) {
        return new MouseEvent(type, 5, 5, 5, 5, MouseButton.NONE, 0, false, false, false, false, false, false,
            false, false, false, false, null);
    }

    private static void deleteRecursively(Path folder) throws IOException {
        if (!Files.exists(folder)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
