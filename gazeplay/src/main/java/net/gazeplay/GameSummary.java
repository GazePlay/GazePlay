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

    @Getter
    private final String backgroundMusicUrl;

    @Getter
    private final String description;

    public GameSummary(String nameCode, String gameTypeIndicatorImageLocation) {
        this(nameCode, gameTypeIndicatorImageLocation, "data/common/images/bravo.png");
    }

    public GameSummary(String nameCode, String gameTypeIndicatorImageLocation, String thumbnailLocation) {
        this(nameCode, gameTypeIndicatorImageLocation, thumbnailLocation, null);
    }

    public GameSummary(String nameCode, String gameTypeIndicatorImageLocation, String thumbnailLocation,
            final String backgroundMusicUrl) {
        this(nameCode, gameTypeIndicatorImageLocation, thumbnailLocation, backgroundMusicUrl, null);
    }

    public GameSummary(String nameCode, String gameTypeIndicatorImageLocation, String thumbnailLocation,
            String backgroundMusicUrl, final String description) {
        this.nameCode = nameCode;
        this.gameTypeIndicatorImageLocation = gameTypeIndicatorImageLocation;
        this.thumbnailLocation = thumbnailLocation;
        this.backgroundMusicUrl = backgroundMusicUrl;
        this.description = description;
    }

}
