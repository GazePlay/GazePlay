package net.gazeplay.games.opinions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * The answers of the person in the game "Opinions" (2/11/2026), and their file "&lt;time&gt;-opinions.csv", written with
 * the statistics ({@link OpinionsGameStats}).
 * <p>
 * Format of the file, version {@value #FORMAT_VERSION} (a new file: the files written before are not changed): UTF-8
 * with a byte order mark, ";" between the columns (opened directly by a French spreadsheet), a header line
 * {@code image;choix}, then one line per image answered, in the order they were shown: the name of the image file, and
 * {@code aime} or {@code n'aime pas}. Images not answered (the game left before) have no line.
 */
public class OpinionsAnswers {

    static final int FORMAT_VERSION = 1;

    static final String FILE_SUFFIX = "-opinions.csv";

    private final List<String[]> answers = new ArrayList<>();

    /**
     * Records the answer of the person on an image.
     */
    void add(final String image, final OpinionChoice choice) {
        answers.add(new String[]{image, choice.getWritten()});
    }

    /**
     * @return the answers recorded, as written in the file: image, then choice
     */
    List<String[]> getAll() {
        return answers;
    }

    /**
     * Writes the file (see the format above).
     */
    void write(final File file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write('﻿');
            writer.write("image;choix\r\n");
            for (final String[] answer : answers) {
                writer.write(cell(answer[0]) + ";" + cell(answer[1]) + "\r\n");
            }
        }
    }

    private static String cell(final String value) {
        if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
