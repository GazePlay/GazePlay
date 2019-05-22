package net.gazeplay;

import lombok.Data;
import lombok.Getter;

@Data
public class GameSummary {

    @Getter
    private final String nameCode;

    @Getter
    private final String gameThumbnail;

    @Getter
    private final GameCategories.Category category;

    @Getter
    private final String backgroundMusicUrl;

    @Getter
    private final String description;

    public GameSummary(String nameCode, String gameThumbnail, GameCategories.Category category) {
        this(nameCode, gameThumbnail, category, null);
    }

    public GameSummary(String nameCode, String gameThumbnail, GameCategories.Category category,
            final String backgroundMusicUrl) {
        this(nameCode, gameThumbnail, category, backgroundMusicUrl, null);
    }

    public GameSummary(String nameCode, String gameThumbnail, GameCategories.Category category,
            String backgroundMusicUrl, final String description) {
        this.nameCode = nameCode;
        this.gameThumbnail = gameThumbnail;
        this.category = category;
        this.backgroundMusicUrl = backgroundMusicUrl;
        this.description = description;
    }

}
