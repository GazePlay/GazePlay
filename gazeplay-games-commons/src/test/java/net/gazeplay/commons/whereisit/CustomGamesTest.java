package net.gazeplay.commons.whereisit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The games made by the user for "Where is it?" (maintainer's request of 25/11/2026, point 86): saved in a folder of
 * their own with copies of the pictures and sounds, read again, changed, deleted.
 */
class CustomGamesTest {

    @TempDir
    Path temp;

    private Path games;

    private Path lion;

    private Path dog;

    private Path sound;

    @BeforeEach
    void setUp() throws IOException {
        games = temp.resolve("ou-est-ce");
        final Path mine = Files.createDirectories(temp.resolve("mes images"));
        lion = Files.writeString(mine.resolve("Le lion été.PNG"), "lion");
        dog = Files.writeString(mine.resolve("chien.jpg"), "dog");
        sound = Files.writeString(mine.resolve("lion.mp3"), "roar");
    }

    private CustomScreen screen() {
        final CustomScreen screen = new CustomScreen();
        screen.setSize(1, 2);
        screen.setPicture(0, lion);
        screen.setPicture(1, dog);
        screen.setRightAnswer(0);
        screen.setSentence("Où est le lion ?");
        screen.setSound(sound);
        return screen;
    }

    private CustomGame game(final String name) {
        final CustomGame game = new CustomGame();
        game.setName(name);
        game.getScreens().add(screen());
        return game;
    }

    private static List<String> files(final Path folder) throws IOException {
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths.filter(Files::isRegularFile).map(path -> folder.relativize(path).toString().replace('\\', '/'))
                .sorted().toList();
        }
    }

    @Test
    void shouldSaveAGameWithCopiesThenReadIt() throws IOException {
        final CustomGame game = game("Les animaux : l'été ?");
        CustomGames.save(games, game);
        // A plain folder, safe in a path, a command line and a variant
        assertEquals("les-animaux-l-ete", game.getFolder());
        assertEquals(List.of("images/chien.jpg", "images/le-lion-ete.png", "jeu.json", "sounds/lion.mp3"),
            files(games.resolve(game.getFolder())));
        // The originals are no longer needed
        Files.delete(lion);
        Files.delete(sound);
        final CustomGame read = CustomGames.list(games).getFirst();
        assertEquals("Les animaux : l'été ?", read.getName());
        final CustomScreen screen = read.getScreens().getFirst();
        assertEquals(1, screen.getLines());
        assertEquals(2, screen.getColumns());
        assertEquals("Où est le lion ?", screen.getSentence());
        assertEquals(0, screen.getRightAnswer());
        assertEquals("lion", Files.readString(screen.getPictures().getFirst()));
        assertEquals("roar", Files.readString(screen.getSound()));
        assertTrue(screen.isComplete());
        assertEquals(1, read.playableScreens().size());
        // The game saved plays its own copies too
        assertTrue(game.getScreens().getFirst().isComplete());
    }

    @Test
    void shouldKeepItsFolderWhenRenamedAndCopyTheSameFileOnce() throws IOException {
        final CustomGame game = game("Un");
        game.getScreens().add(game.getScreens().getFirst().copy());
        CustomGames.save(games, game);
        assertEquals(4, files(games.resolve("un")).size(), "one copy of each file");

        game.setName("Deux");
        game.getScreens().removeLast();
        game.getScreens().getFirst().setSound(null);
        CustomGames.save(games, game);
        assertEquals("un", game.getFolder());
        assertEquals("Deux", CustomGames.load(games, "un").getName());
        // What is no longer used is gone, and nothing of the writing is left
        assertEquals(List.of("images/chien.jpg", "images/le-lion-ete.png", "jeu.json"), files(games.resolve("un")));
        try (Stream<Path> children = Files.list(games)) {
            assertEquals(List.of("un"), children.map(path -> path.getFileName().toString()).toList());
        }

        final CustomGame other = game("Un");
        CustomGames.save(games, other);
        assertEquals("un-2", other.getFolder(), "another game of the same name does not replace it");
        assertEquals(List.of("Deux", "Un"), CustomGames.list(games).stream().map(CustomGame::getName).toList());
    }

    @Test
    void shouldKeepTheIncompleteScreensWithoutPlayingThem() throws IOException {
        final CustomGame game = game("Jeu");
        final CustomScreen empty = new CustomScreen();
        game.getScreens().add(empty);
        assertEquals(CustomScreen.Problem.PICTURES, empty.problem());
        final CustomScreen withoutAnswer = screen();
        withoutAnswer.setRightAnswer(-1);
        game.getScreens().add(withoutAnswer);
        assertEquals(CustomScreen.Problem.ANSWER, withoutAnswer.problem());
        CustomGames.save(games, game);
        final CustomGame read = CustomGames.load(games, "jeu");
        assertEquals(3, read.getScreens().size());
        assertEquals(1, read.playableScreens().size());
        // A copy of a picture deleted by hand: the screen is no longer played
        Files.delete(read.getScreens().getFirst().getPictures().getFirst());
        assertTrue(CustomGames.load(games, "jeu").playableScreens().isEmpty());
    }

    @Test
    void shouldKeepThePicturesBeyondASmallerGrid() {
        final CustomScreen screen = screen();
        screen.setRightAnswer(1);
        screen.setSize(2, 2);
        screen.setPicture(3, dog);
        screen.setRightAnswer(3);
        screen.setSize(1, 2);
        assertEquals(2, screen.getPictures().size());
        assertEquals(-1, screen.getRightAnswer(), "the right answer is beyond the grid");
        screen.setSize(2, 2);
        assertEquals(dog, screen.getPictures().get(3));
        assertEquals(3, screen.getRightAnswer());
    }

    @Test
    void shouldLeaveOutWhatCannotBeReadAndStayInItsFolder() throws IOException {
        CustomGames.save(games, game("Bon"));
        Files.writeString(Files.createDirectories(games.resolve("cassé")).resolve(CustomGames.FILE), "{");
        Files.createDirectories(games.resolve("vide"));
        Files.writeString(Files.createDirectories(games.resolve(".ecriture-1")).resolve(CustomGames.FILE), "{}");
        Files.writeString(Files.createDirectories(games.resolve("taille")).resolve(CustomGames.FILE),
            "{\"screens\":[{\"lines\":9,\"columns\":9,\"pictures\":[],\"rightAnswer\":0}]}");
        assertEquals(List.of("Bon"), CustomGames.list(games).stream().map(CustomGame::getName).toList());
        assertNull(CustomGames.load(games, "../mes images"));
        assertNull(CustomGames.load(games, "absent"));
        // A picture outside the folder of the game is not read
        final Path outside = Files.createDirectories(games.resolve("dehors")).resolve(CustomGames.FILE);
        Files.writeString(outside, "{\"name\":\"Dehors\",\"screens\":[{\"lines\":1,\"columns\":2,\"sentence\":\"\","
            + "\"sound\":null,\"pictures\":[\"../../mes images/chien.jpg\",null],\"rightAnswer\":0}]}",
            StandardCharsets.UTF_8);
        assertNull(CustomGames.load(games, "dehors").getScreens().getFirst().getPictures().getFirst());
    }

    @Test
    void shouldDeleteOnlyAGame() throws IOException {
        final CustomGame game = game("Jeu");
        CustomGames.save(games, game);
        assertThrows(IOException.class, () -> CustomGames.delete(games, null));
        assertThrows(IOException.class, () -> CustomGames.delete(games, "../mes images"));
        CustomGames.delete(games, game.getFolder());
        assertTrue(CustomGames.list(games).isEmpty());
        assertTrue(Files.exists(lion), "the original is kept");
    }
}
