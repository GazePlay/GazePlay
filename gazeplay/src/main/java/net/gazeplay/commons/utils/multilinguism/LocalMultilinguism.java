package net.gazeplay.commons.utils.multilinguism;

import jdk.nashorn.internal.objects.annotations.Constructor;
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
public class LocalMultilinguism extends Multilinguism{

    private final Map<Entry, String> gameTraductions;

    public LocalMultilinguism(String path) {this.gameTraductions = super.loadFromFile(path);}

    public String getTrad(String key, String language) {return gameTraductions.get(new Entry(key, language));
    }

}
