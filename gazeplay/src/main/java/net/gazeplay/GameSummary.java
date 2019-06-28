package net.gazeplay;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.commons.configuration.Configuration;

import java.util.Properties;

@Data
@Slf4j
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

    @Getter
    @Setter
    private boolean favourite;

    // @Getter
    // private BooleanProperty favouriteProperty;

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

        for(BooleanProperty p : Configuration.getInstance().getFavouriteGameProperties()){
            if(p.getName().equals(this.getNameCode().toUpperCase()+" Game fav")){
                this.setFavourite(p.getValue());
            }
        }

        //this.setFavourite(false);
    }

}
