package net.gazeplay.commons.utils.multilinguism;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class I18N {

    private final Map<Entry, String> translations;

    public I18N(String resourcePath) {
        this.translations = loadFromFile(resourcePath);
    }

    @Data
    @AllArgsConstructor
    protected static class Entry {
        public String key;
        public String language;
    }

    protected static Map<Entry, String> loadFromFile(String resourceLocation) {
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        final InputStream is;

        if (new File(resourceLocation).isFile()) {
            try {
                is = new FileInputStream(new File(resourceLocation));
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
                    for (int i = 1; i < data.length; i++) {
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

    public String translate(String key, String language) {
        return translations.get(new Entry(key, language));
    }
}
