package net.gazeplay.games.mediaPlayer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameContext;

@Slf4j
public class MediaFile {

    @Getter
    private final String type;
    @Getter
    private final String path;
    @Getter
    private final String name;

    MediaFile(String t, String p, String n) {
        type = t;
        path = p;
        name = n;
    }

    public boolean equals(MediaFile mf) {
        return ((mf.path == path) && (mf.type == type));
    }
}
