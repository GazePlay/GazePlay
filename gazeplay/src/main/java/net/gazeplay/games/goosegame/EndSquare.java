package net.gazeplay.games.goosegame;

import net.gazeplay.commons.utils.Position;

public class EndSquare extends Square {
    public EndSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game) {
        super(number, pawnPosition, previousSquare, game);
    }

    @Override
    protected void pawnStays(Pawn pawn) {
        game.winner(pawn);
    }
}
