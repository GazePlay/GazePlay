package net.gazeplay.games.mediaPlayer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class MediaFile {

    @Getter
    @NonNull
    @EqualsAndHashCode.Include
    private final String type;

    @Getter
    @NonNull
    @EqualsAndHashCode.Include
    private final String path;

    @Getter
    private final String name;

    @Getter
    private final String imagepath;

}
