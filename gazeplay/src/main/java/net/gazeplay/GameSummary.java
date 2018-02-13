package net.gazeplay;

import lombok.Data;
import lombok.Getter;

@Data
public class GameSummary {

    @Getter
    private final String nameCode;

    @Getter
    private final String thumbnailLocation;

}
