package net.gazeplay.commons.whereisit;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The games made by the user for "Where is it?" (maintainer's request of 25/11/2026, point 86): one folder each in
 * Documents\GazePlay_Learning\ou-est-ce, shared by all the profiles as the evaluations, with {@code jeu.json} and its
 * pictures and sounds, copied so that the game does not depend on the original files. The folders whose name starts with
 * a dot are writings in progress.
 */
@Slf4j
public final class CustomGames {

    /**
     * The description of a game, in its folder.
     */
    static final String FILE = "jeu.json";

    /**
     * Version of {@link #FILE}.
     */
    static final int FORMAT_VERSION = 1;

    private static final int FOLDER_NAME_MAX_LENGTH = 50;

    private CustomGames() {
    }

    /**
     * @return the folder of the games
     */
    public static Path folder() {
        return GazePlayDirectories.getWhereIsItFolder().toPath();
    }

    /**
     * @return the games, in the order of their names; those that cannot be read are left out
     */
    public static List<CustomGame> list() {
        return list(folder());
    }

    static List<CustomGame> list(final Path games) {
        if (!Files.isDirectory(games)) {
            return List.of();
        }
        try (Stream<Path> children = Files.list(games)) {
            final List<CustomGame> found = new ArrayList<>();
            for (final Path child : children.filter(Files::isDirectory).toList()) {
                final CustomGame game = load(games, fileName(child));
                if (game != null) {
                    found.add(game);
                }
            }
            found.sort(Comparator.comparing(CustomGame::getName, String.CASE_INSENSITIVE_ORDER));
            return found;
        } catch (final IOException e) {
            log.warn("Cannot read {}", games, e);
            return List.of();
        }
    }

    /**
     * @return the game of a folder, null when it does not exist or cannot be read
     */
    public static CustomGame load(final String folder) {
        return load(folder(), folder);
    }

    static CustomGame load(final Path games, final String folder) {
        if (folder == null || folder.isBlank() || folder.startsWith(".")) {
            return null;
        }
        final Path gameFolder = games.resolve(folder).normalize();
        final Path file = gameFolder.resolve(FILE);
        if (!games.normalize().equals(gameFolder.getParent()) || !Files.isRegularFile(file)) {
            return null;
        }
        try {
            final JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8))
                .getAsJsonObject();
            final CustomGame game = new CustomGame(folder);
            game.setName(json.has("name") ? json.get("name").getAsString() : folder);
            if (game.getName().isEmpty()) {
                game.setName(folder);
            }
            for (final JsonElement screen : json.getAsJsonArray("screens")) {
                game.getScreens().add(readScreen(screen.getAsJsonObject(), gameFolder));
            }
            return game;
        } catch (final IOException | JsonParseException | IllegalStateException | NullPointerException
                       | ClassCastException | NumberFormatException | UnsupportedOperationException e) {
            log.warn("Cannot read {}", file, e);
            return null;
        }
    }

    private static CustomScreen readScreen(final JsonObject json, final Path gameFolder) {
        final CustomScreen screen = new CustomScreen();
        final int lines = json.get("lines").getAsInt();
        final int columns = json.get("columns").getAsInt();
        if (CustomScreen.SIZES.stream().noneMatch(size -> size[0] == lines && size[1] == columns)) {
            throw new JsonParseException("Unknown size " + lines + "x" + columns);
        }
        screen.setSize(lines, columns);
        screen.setSentence(json.has("sentence") ? json.get("sentence").getAsString() : "");
        screen.setSound(readFile(json.get("sound"), gameFolder));
        final JsonArray pictures = json.getAsJsonArray("pictures");
        for (int i = 0; i < screen.getPlaces() && i < pictures.size(); i++) {
            screen.setPicture(i, readFile(pictures.get(i), gameFolder));
        }
        screen.setRightAnswer(json.get("rightAnswer").getAsInt());
        return screen;
    }

    /**
     * @return a file of the folder of the game, null when there is none or when its path leaves this folder
     */
    private static Path readFile(final JsonElement json, final Path gameFolder) {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        try {
            final Path file = gameFolder.resolve(json.getAsString()).normalize();
            return file.startsWith(gameFolder.normalize()) ? file : null;
        } catch (final InvalidPathException e) {
            return null;
        }
    }

    /**
     * Saves a game: its pictures and sounds are copied into its folder, which is then replaced at once (the game saved
     * before stays as it was when something fails). Its folder is made from its name the first time, then kept. The
     * pictures and sounds of the game become those of its folder.
     */
    public static void save(final CustomGame game) throws IOException {
        save(folder(), game);
    }

    static void save(final Path games, final CustomGame game) throws IOException {
        Files.createDirectories(games);
        final String folder = game.getFolder() != null ? game.getFolder() : newFolder(games, game.getName());
        final Path target = games.resolve(folder);
        final Path written = games.resolve(".ecriture-" + UUID.randomUUID());
        final Path previous = games.resolve(".ancien-" + UUID.randomUUID());
        try {
            Files.createDirectories(written);
            write(game, written);
            if (Files.exists(target)) {
                Files.move(target, previous);
            }
            try {
                Files.move(written, target);
            } catch (final IOException e) {
                if (Files.exists(previous)) {
                    Files.move(previous, target);
                }
                throw e;
            }
        } finally {
            deleteQuietly(written);
        }
        deleteQuietly(previous);
        game.setFolder(folder);
        final CustomGame saved = load(games, folder);
        if (saved == null) {
            throw new IOException("Cannot read " + target);
        }
        game.getScreens().clear();
        game.getScreens().addAll(saved.getScreens());
    }

    private static void write(final CustomGame game, final Path folder) throws IOException {
        // The same file used in several places is copied once
        final Map<Path, String> copies = new HashMap<>();
        final JsonArray screens = new JsonArray();
        for (final CustomScreen screen : game.getScreens()) {
            final JsonObject json = new JsonObject();
            json.addProperty("lines", screen.getLines());
            json.addProperty("columns", screen.getColumns());
            json.addProperty("sentence", screen.getSentence().strip());
            json.addProperty("sound", copy(screen.getSound(), folder, "sounds", copies));
            final JsonArray pictures = new JsonArray();
            for (final Path picture : screen.getPictures()) {
                pictures.add(copy(picture, folder, "images", copies));
            }
            json.add("pictures", pictures);
            json.addProperty("rightAnswer", screen.getRightAnswer());
            screens.add(json);
        }
        final JsonObject json = new JsonObject();
        json.addProperty("formatVersion", FORMAT_VERSION);
        json.addProperty("name", game.getName());
        json.add("screens", screens);
        Files.writeString(folder.resolve(FILE), new GsonBuilder().setPrettyPrinting().serializeNulls()
            .disableHtmlEscaping().create().toJson(json), StandardCharsets.UTF_8);
    }

    /**
     * Copies a file into a folder of the game, under a name of its own.
     *
     * @return its path in the folder of the game, null without a file (or when it no longer exists)
     */
    private static String copy(final Path file, final Path gameFolder, final String kind,
                               final Map<Path, String> copies) throws IOException {
        if (file == null || !Files.isRegularFile(file)) {
            return null;
        }
        final Path source = file.toAbsolutePath().normalize();
        final String known = copies.get(source);
        if (known != null) {
            return known;
        }
        final Path folder = Files.createDirectories(gameFolder.resolve(kind));
        final String name = fileName(source);
        final int dot = name.lastIndexOf('.');
        final String extension = dot < 0 ? "" : name.substring(dot).toLowerCase(Locale.ROOT);
        final String stem = simple(dot < 0 ? name : name.substring(0, dot), "fichier");
        String copied = stem + extension;
        for (int i = 2; Files.exists(folder.resolve(copied)); i++) {
            copied = stem + "-" + i + extension;
        }
        Files.copy(source, folder.resolve(copied));
        final String path = kind + "/" + copied;
        copies.put(source, path);
        return path;
    }

    /**
     * Deletes a game and its copies of the pictures and sounds (the original files are not touched).
     */
    public static void delete(final CustomGame game) throws IOException {
        delete(folder(), game.getFolder());
    }

    static void delete(final Path games, final String folder) throws IOException {
        final Path target = folder == null || folder.isBlank() ? games : games.resolve(folder).normalize();
        if (!games.normalize().equals(target.getParent()) || !Files.isRegularFile(target.resolve(FILE))) {
            throw new IOException("Not a game: " + folder);
        }
        deleteRecursively(target);
    }

    /**
     * @return a name of folder not used yet, from the name of a game
     */
    private static String newFolder(final Path games, final String name) {
        final String base = simple(name, "jeu");
        String folder = base;
        for (int i = 2; Files.exists(games.resolve(folder)); i++) {
            folder = base + "-" + i;
        }
        return folder;
    }

    /**
     * @return a name with plain letters and digits only, safe in a path, a command line and a variant
     */
    private static String simple(final String name, final String otherwise) {
        final String plain = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
            .replaceAll("[^A-Za-z0-9]+", "-").replaceAll("^-+|-+$", "").toLowerCase(Locale.ROOT);
        final String shortened = plain.length() > FOLDER_NAME_MAX_LENGTH
            ? plain.substring(0, FOLDER_NAME_MAX_LENGTH).replaceAll("-+$", "") : plain;
        return shortened.isEmpty() ? otherwise : shortened;
    }

    private static String fileName(final Path path) {
        final Path name = path.getFileName();
        return name == null ? "" : name.toString();
    }

    private static void deleteQuietly(final Path folder) {
        try {
            deleteRecursively(folder);
        } catch (final IOException e) {
            log.warn("Cannot delete {}", folder, e);
        }
    }

    private static void deleteRecursively(final Path folder) throws IOException {
        if (!Files.exists(folder)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(folder)) {
            for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
