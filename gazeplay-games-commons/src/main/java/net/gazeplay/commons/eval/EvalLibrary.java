package net.gazeplay.commons.eval;

import net.gazeplay.commons.utils.games.GazePlayDirectories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * The evaluations imported in GazePlay: one folder each in the evaluations folder of the GazePlay folder, named after
 * the evaluation, as in the .zip of the website. The folders whose name starts with a dot are imports in progress.
 */
public final class EvalLibrary {

    private EvalLibrary() {
    }

    /**
     * @return the folder of the imported evaluations, shared by all the profiles
     */
    public static Path folder() {
        return GazePlayDirectories.getEvaluationsFolder().toPath();
    }

    /**
     * @return the names of the imported evaluations, sorted
     */
    public static List<String> names() {
        return names(folder());
    }

    static List<String> names(Path evaluationsFolder) {
        if (!Files.isDirectory(evaluationsFolder)) {
            return List.of();
        }
        try (Stream<Path> children = Files.list(evaluationsFolder)) {
            return children
                .filter(Files::isDirectory)
                .map(EvalLibrary::fileName)
                .filter(name -> !name.isEmpty() && !name.startsWith("."))
                .filter(name -> Files.isRegularFile(evaluationsFolder.resolve(name).resolve(EvalReader.EVAL_DATA)))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * @return the folder of an imported evaluation (which may not exist)
     */
    public static Path evaluationFolder(String name) {
        return folder().resolve(name);
    }

    /**
     * Deletes an imported evaluation (its definition and media; the results of its sessions are kept).
     */
    public static void delete(String name) throws IOException {
        Path evaluation = evaluationFolder(name).normalize();
        if (!folder().normalize().equals(evaluation.getParent())) {
            throw new IOException("Not an evaluation: " + name);
        }
        deleteRecursively(evaluation);
    }

    /**
     * @return the name of the file or folder of a path, empty for a root
     */
    static String fileName(Path path) {
        Path name = path.getFileName();
        return name == null ? "" : name.toString();
    }

    /**
     * Deletes a folder and its content (nothing when it does not exist).
     */
    static void deleteRecursively(Path folder) throws IOException {
        if (!Files.exists(folder)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(folder)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
