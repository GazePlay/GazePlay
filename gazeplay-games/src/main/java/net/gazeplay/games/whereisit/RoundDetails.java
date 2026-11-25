package net.gazeplay.games.whereisit;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public
/**
 * A round of "Where is it?": the pictures, the index of the right one, the question and its sound.
 */
class RoundDetails {
    private final List<PictureCard> pictureCardList;
    private final int winnerImageIndexAmongDisplayedImages;
    private final String questionSoundPath;
    private final String question;
}
