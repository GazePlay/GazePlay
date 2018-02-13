package net.gazeplay;

import lombok.Data;
import lombok.Getter;

@Data
public class GameSummary {

    @Getter
    private final String nameCode;

    @Getter
    private final String gameTypeIndicatorImageLocation;

    @Getter
    private final String thumbnailLocation;

    public GameSummary(String nameCode, String gameTypeIndicatorImageLocation) {
        this(nameCode, gameTypeIndicatorImageLocation, "data/common/images/bravo.png");
    }

    public GameSummary(String nameCode, String gameTypeIndicatorImageLocation, String thumbnailLocation) {
        this.nameCode = nameCode;
        this.gameTypeIndicatorImageLocation = gameTypeIndicatorImageLocation;
        this.thumbnailLocation = thumbnailLocation;
    }

}
