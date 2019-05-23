package net.gazeplay.commons.utils.multilinguism;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class I18N {

    @Data
    @AllArgsConstructor
    protected static class Entry {
        public String key;
        public String language;
    }

    protected static Map<Entry, String> loadFromFile(String resourceLocation) {
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        InputStream is;

        try {
            is = systemClassLoader.getResourceAsStream(resourceLocation);

            if (is == null) {
                // throw new FileNotFoundException("Resource was not found : " + resourceLocation);

                File F = new File(resourceLocation);
                is = new FileInputStream(F);
            }

            if (is == null) {
                throw new FileNotFoundException("Resource was not found : " + resourceLocation);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

                Map<Entry, String> traductions = new HashMap<>(1000);

                String ligne = null;

                boolean firstline = true;

                String[] languages = null, data = null;

                while ((ligne = br.readLine()) != null) {
                    if (firstline) {
                        // Retourner la ligne dans un tableau
                        languages = ligne.split(",");
                        firstline = false;
                    } else {
                        data = ligne.split(",");
                        String key = data[0];
                        for (int i = 1; i < data.length; i++) {

                            // log.info(key + ", " + languages[i] + ", " + data[i]);
                            traductions.put(new Entry(key, languages[i]), data[i]);
                        }
                    }
                }
                return traductions;
            }
        } catch (IOException e) {
            log.error("Exception while loading resource {}", resourceLocation, e);
            throw new RuntimeException(e);
        }
    }

    private final String resourcePath;

    private final Map<Entry, String> traductions;

    public I18N(String resourcePath) {
        this.resourcePath = resourcePath;
        this.traductions = loadFromFile(resourcePath);
    }

    public String translate(String key, String language) {
        return traductions.get(new Entry(key, language));
    }

}
