package net.gazeplay.commons.eval;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Imports the .zip exported by the GazePlay Eval website ("Exporter le .zip"): {@code <name>/evalData.json},
 * {@code <name>/evalInfo.json} and the media in {@code <name>/images}, {@code audio} and {@code videos}.
 * <p>
 * In two steps, so that nothing is replaced before the user agrees: {@link #prepare} extracts the archive in a
 * temporary folder of the evaluations folder and reads it; {@link #commit} then puts it in place (replacing an
 * evaluation of the same name), or {@link #discard} deletes it.
 */
public final class EvalImporter {

    private static final String DATA_SUFFIX = EvalReader.EVAL_DATA;

    // Limits against a corrupted or malicious archive (the evaluations of the website weigh a few tens of MB)
    private static final long MAX_TOTAL_SIZE = 4L * 1024 * 1024 * 1024;
    private static final int MAX_ENTRIES = 20_000;

    /**
     * An extracted archive, not yet in place.
     *
     * @param name             name of the folder of the evaluation, also the name shown in the menu
     * @param replacesExisting whether an evaluation of that name is already imported
     * @param problems         what {@link EvalReader} found (with errors, the evaluation cannot be imported)
     */
    public record Prepared(Path extractedFolder, String name, Path target, boolean replacesExisting,
                           List<EvalProblem> problems) {

        public boolean hasErrors() {
            return problems.stream().anyMatch(EvalProblem::error);
        }
    }

    private EvalImporter() {
    }

    /**
     * Extracts and reads an archive. The temporary folder is deleted when the archive cannot be read.
     *
     * @throws IOException when the file is not a readable archive of an evaluation (no evalData.json)
     */
    public static Prepared prepare(Path zipFile, Path evaluationsFolder) throws IOException {
        Files.createDirectories(evaluationsFolder);
        Path extracted = evaluationsFolder.resolve(".import-" + UUID.randomUUID());
        try {
            String rootName;
            try {
                rootName = extract(zipFile, extracted, StandardCharsets.UTF_8);
            } catch (ZipException | IllegalArgumentException e) {
                // Names not in UTF-8 (archive made again by an old tool): the historic charset of zip files
                deleteQuietly(extracted);
                rootName = extract(zipFile, extracted, Charset.forName("IBM437"));
            }
            EvalReader.Result result = EvalReader.read(extracted);
            String name = folderName(rootName, result, zipFile);
            Path target = evaluationsFolder.resolve(name);
            return new Prepared(extracted, name, target, Files.exists(target), result.problems());
        } catch (IOException | RuntimeException e) {
            deleteQuietly(extracted);
            throw e;
        }
    }

    /**
     * Puts a prepared evaluation in place, replacing the evaluation of the same name.
     *
     * @return the folder of the imported evaluation
     */
    public static Path commit(Prepared prepared) throws IOException {
        if (prepared.hasErrors()) {
            throw new IOException("The evaluation has errors: " + prepared.problems());
        }
        Path target = prepared.target();
        Path replaced = null;
        if (Files.exists(target)) {
            replaced = target.resolveSibling(".replaced-" + UUID.randomUUID());
            Files.move(target, replaced);
        }
        try {
            Files.move(prepared.extractedFolder(), target);
        } catch (IOException e) {
            if (replaced != null) {
                Files.move(replaced, target);
            }
            throw e;
        }
        if (replaced != null) {
            deleteQuietly(replaced);
        }
        return target;
    }

    /**
     * Deletes the extracted archive, when the import is cancelled or refused.
     */
    public static void discard(Prepared prepared) {
        deleteQuietly(prepared.extractedFolder());
    }

    /**
     * Extracts the folder of the evaluation (the one holding evalData.json) into {@code destination}.
     *
     * @return the name of that folder in the archive, empty when evalData.json is at the root
     */
    static String extract(Path zipFile, Path destination, Charset charset) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile(), charset)) {
            String root = null;
            List<ZipEntry> entries = new ArrayList<>();
            Enumeration<? extends ZipEntry> enumeration = zip.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry entry = enumeration.nextElement();
                if (entries.size() >= MAX_ENTRIES) {
                    throw new IOException("Too many files in " + zipFile);
                }
                String entryName = entry.getName().replace('\\', '/');
                if (isIgnored(entryName)) {
                    continue;
                }
                entries.add(entry);
                if (!entry.isDirectory() && (entryName.equals(DATA_SUFFIX) || entryName.endsWith("/" + DATA_SUFFIX))) {
                    String candidate = entryName.substring(0, entryName.length() - DATA_SUFFIX.length());
                    if (root == null || candidate.length() < root.length()) {
                        root = candidate;
                    }
                }
            }
            if (root == null) {
                throw new IOException("No " + DATA_SUFFIX + " in " + zipFile);
            }

            Files.createDirectories(destination);
            Path normalizedDestination = destination.toAbsolutePath().normalize();
            long total = 0;
            for (ZipEntry entry : entries) {
                String entryName = entry.getName().replace('\\', '/');
                if (!entryName.startsWith(root) || entry.isDirectory()) {
                    continue;
                }
                String relative = entryName.substring(root.length());
                if (relative.isEmpty()) {
                    continue;
                }
                Path file = normalizedDestination.resolve(relative).normalize();
                if (!file.startsWith(normalizedDestination) || file.equals(normalizedDestination)) {
                    throw new IOException("Entry outside of the evaluation: " + entry.getName());
                }
                Path parent = file.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                try (InputStream input = zip.getInputStream(entry)) {
                    total += Files.copy(input, file, StandardCopyOption.REPLACE_EXISTING);
                }
                if (total > MAX_TOTAL_SIZE) {
                    throw new IOException("Archive too large: " + zipFile);
                }
            }
            String trimmed = root.endsWith("/") ? root.substring(0, root.length() - 1) : root;
            int slash = trimmed.lastIndexOf('/');
            return slash < 0 ? trimmed : trimmed.substring(slash + 1);
        }
    }

    private static boolean isIgnored(String entryName) {
        String fileName = entryName.substring(entryName.lastIndexOf('/', entryName.length() - 2) + 1);
        return entryName.startsWith("__MACOSX/") || fileName.startsWith("._") || fileName.equals(".DS_Store");
    }

    /**
     * @return the name of the folder of the evaluation: its folder in the archive, else its name in evalInfo.json, else
     * the name of the file
     */
    static String folderName(String rootName, EvalReader.Result result, Path zipFile) {
        String name = sanitize(rootName);
        if (name.isEmpty() && result.definition() != null) {
            name = sanitize(result.definition().name());
        }
        if (name.isEmpty()) {
            String fileName = EvalLibrary.fileName(zipFile).replaceFirst("(?i)\\.zip$", "");
            name = sanitize(fileName.replaceFirst("(?i)-gazeplayEval$", ""));
        }
        return name.isEmpty() ? "Evaluation" : name;
    }

    /**
     * @return a name usable as a folder name on all systems (the website already refuses \ / : * ? " < > |)
     */
    static String sanitize(String name) {
        if (name == null) {
            return "";
        }
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "").trim();
        while (cleaned.startsWith(".")) {
            cleaned = cleaned.substring(1).trim();
        }
        while (cleaned.endsWith(".")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

    private static void deleteQuietly(Path folder) {
        try {
            EvalLibrary.deleteRecursively(folder);
        } catch (IOException ignored) {
            // A temporary folder left behind is ignored by the list of the evaluations
        }
    }

    /**
     * Deletes the temporary folders of interrupted imports, older than a day.
     */
    public static void cleanLeftovers(Path evaluationsFolder) {
        if (!Files.isDirectory(evaluationsFolder)) {
            return;
        }
        long dayAgo = System.currentTimeMillis() - 24L * 3600 * 1000;
        try (var children = Files.list(evaluationsFolder)) {
            children
                .filter(child -> EvalLibrary.fileName(child).startsWith(".import-")
                    || EvalLibrary.fileName(child).startsWith(".replaced-"))
                .filter(child -> {
                    try {
                        return Files.getLastModifiedTime(child).toMillis() < dayAgo;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .sorted(Comparator.naturalOrder())
                .forEach(EvalImporter::deleteQuietly);
        } catch (IOException ignored) {
            // nothing to clean
        }
    }
}
