package net.gazeplay.commons.utils.multilinguism;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Multilinguism {

    private static Map<Entry, String> loadFromFile() {
        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = systemClassLoader.getResourceAsStream("data/multilinguism/multilinguism.csv")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

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

                            // log.info(key + ", " + languages[i] + ", " +data[i]);
                            traductions.put(new Entry(key, languages[i]), data[i]);
                        }
                    }
                }
                return traductions;
            }
        } catch (IOException e) {
            log.error("Exception", e);
            throw new RuntimeException(e);
        }
    }

    @Getter
    private static Multilinguism singleton = new Multilinguism();

    private final Map<Entry, String> traductions;

    private Multilinguism() {
        this.traductions = loadFromFile();
    }

    public String getTrad(String key, String language) {
        return traductions.get(new Entry(key, language));
    }

    @Data
    @AllArgsConstructor
    private static class Entry {
        public String key;
        public String language;
    }

}
