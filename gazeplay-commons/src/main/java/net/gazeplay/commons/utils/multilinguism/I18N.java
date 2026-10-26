package net.gazeplay.commons.utils.multilinguism;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Translations of a CSV file: the first line gives the languages (from the second column), each other line a key (first
 * column) and its translation in each language. The values must not contain commas.
 */
@Slf4j
public class I18N {

    private final Map<Entry, String> translations;

    /**
     * @param resourcePath a file path, or else a resource of the classpath
     */
    public I18N(String resourcePath) {
        this.translations = loadFromFile(resourcePath);
    }

    /**
     * Key of a translation: a key of the file in a language.
     */
    @Data
    @AllArgsConstructor
    protected static class Entry {
        public String key;
        public String language;
    }

    /**
     * Reads all the translations of a file. A line with more columns than the header is logged as an error and its
     * extra columns are ignored, so that one wrong line does not prevent the game using the file from starting.
     */
    protected static Map<Entry, String> loadFromFile(String resourceLocation) {
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        final InputStream is;

        if (new File(resourceLocation).isFile()) {
            try {
                is = Files.newInputStream(new File(resourceLocation).toPath());
            } catch (IOException ie) {
                log.error("Exception while reading file {}", resourceLocation, ie);
                throw new RuntimeException(ie);
            }
        } else {
            is = systemClassLoader.getResourceAsStream(resourceLocation);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            Map<Entry, String> translations = new HashMap<>(1000);

            String line;

            boolean firstline = true;

            String[] languages = null, data;

            while ((line = br.readLine()) != null) {
                if (firstline) {
                    languages = line.split(",");
                    firstline = false;
                } else {
                    data = line.split(",");
                    String key = data[0].strip();
                    if (data.length > languages.length) {
                        // A malformed line must not prevent the whole file (and the game using it) from loading
                        log.error("Line '{}' of {} has {} columns but the header has only {}, extra columns are ignored",
                            key, resourceLocation, data.length, languages.length);
                    }
                    for (int i = 1; i < Math.min(data.length, languages.length); i++) {
                        translations.put(new Entry(key, languages[i].strip()), data[i].strip());
                    }
                }
            }
            return translations;

        } catch (Exception e) {
            log.error("Exception while loading resource {}", resourceLocation, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the translation of a key in a language, null when the file has none
     */
    public String translate(String key, String language) {
        return translations.get(new Entry(key, language));
    }
}
