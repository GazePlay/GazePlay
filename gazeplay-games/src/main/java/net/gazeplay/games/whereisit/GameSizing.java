package net.gazeplay.games.whereisit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public
/**
 * Size of the pictures of a round of "Where is it?", and the horizontal shift centering them.
 */
class GameSizing {
    public final double width;
    public final double height;
    public final double shift;
}
