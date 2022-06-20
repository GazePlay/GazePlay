package net.gazeplay.games.beraPreTest;

import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
class RoundDetails {
    private final List<net.gazeplay.games.beraPreTest.PictureCard> pictureCardList;
    private final int winnerImageIndexAmongDisplayedImages;
    private final String questionSoundPath;
    private final String question;
    private final List<Image> pictos;
}
