package net.gazeplay.games.goosegame;

import net.gazeplay.commons.utils.Position;

public class SkipSquare extends Square {
    private final int nbTurnsToSkip;

    public SkipSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game, int nbTurnsToSkip) {
        super(number, pawnPosition, previousSquare, game);
        this.nbTurnsToSkip = nbTurnsToSkip;
    }

    @Override
    protected void pawnStays(Pawn pawn) {
        pawn.skipTurns(nbTurnsToSkip);
        game.showMessage("Player %d falls asleep for %d turns", pawn.getNumber(), nbTurnsToSkip);
        game.endOfTurn();
    }

}
