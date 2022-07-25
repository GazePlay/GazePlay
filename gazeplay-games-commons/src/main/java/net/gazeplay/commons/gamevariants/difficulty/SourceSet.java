package net.gazeplay.commons.gamevariants.difficulty;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class SourceSet {
    private final JsonObject difficulties;

    public SourceSet(String resourceFile) throws FileNotFoundException {
        try (
            InputStream is = getClass().getClassLoader().getResourceAsStream(resourceFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            String contents = reader.lines().collect(Collectors.joining());
            difficulties = (JsonObject) JsonParser.parseString(contents);
        } catch (NullPointerException | IOException e) {
            throw new FileNotFoundException(resourceFile);
        }
    }

    public Set<String> getResources(String variant) {
        if (difficulties.has(variant)) {
            Type setType = new TypeToken<Set<String>>() {}.getType();
            JsonElement element = difficulties.get(variant);
            return new Gson().fromJson(element, setType);
        } else {
            return Collections.emptySet();
        }
    }
}
