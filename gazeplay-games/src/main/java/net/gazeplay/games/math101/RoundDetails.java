package net.gazeplay.games.math101;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RoundDetails {
    protected final List<Card> cardList;
    private final int winnerImageIndexAmongDisplayedImages;
}
