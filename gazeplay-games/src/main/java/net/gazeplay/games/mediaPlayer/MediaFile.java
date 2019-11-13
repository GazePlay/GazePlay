package net.gazeplay.games.mediaPlayer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class MediaFile {

    @Getter
    @NonNull
    private final String type;
    
    @Getter
    @NonNull
    private final String path;
    
    @Getter
    private final String name;
    
    @Getter
    private final String imagepath;

    public boolean equals(MediaFile mf) {
        return ((path.equals(mf.path)) && (type.equals(mf.type)));
    }

}
