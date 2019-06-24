package net.gazeplay.games.goosegame;

import net.gazeplay.commons.utils.Position;

public class SkipSquare extends Square {
    private final int nbTurnsToSkip;

    public SkipSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game, int nbTurnsToSkip) {
        super(number, pawnPosition, previousSquare, game);
        this.nbTurnsToSkip = nbTurnsToSkip;
    }

    @Override
    protected void pawnStays(Pawn pawn){
        game.endOfTurn();
        pawn.skipTurns(nbTurnsToSkip);
        game.showMessage("Player " + pawn.getNumber() + " is tired and sleeps for " + nbTurnsToSkip + " turns");
    }

}
