package net.gazeplay.games.math101;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * The cards of a round of a Math101 game, and the index of the right answer.
 */
@Data
@AllArgsConstructor
public class RoundDetails {
    protected final List<Card> cardList;
    private final int winnerImageIndexAmongDisplayedImages;
}
