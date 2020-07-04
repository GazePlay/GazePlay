package net.gazeplay.commons.gamevariants.difficulty;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

public class SourceSet {

    private final JsonObject difficulties;

    public SourceSet(String resourceFile) throws FileNotFoundException {
        JsonParser parser = new JsonParser();

        try {
            String path = getClass().getClassLoader().getResource(resourceFile).getPath();
            FileReader reader = new FileReader(path);
            difficulties = (JsonObject) parser.parse(reader);
        } catch (NullPointerException ne) {
            throw new FileNotFoundException(resourceFile);
        }
    }

    public Set<String> getResources(Difficulty difficulty) {
        if (difficulties.has(difficulty.toString())) {
            Type setType = new TypeToken<Set<String>>() {
            }.getType();
            JsonElement element = difficulties.get(difficulty.toString());
            return new Gson().fromJson(element, setType);
        } else {
            return Collections.emptySet();
        }
    }
}
