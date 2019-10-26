package net.gazeplay.games.goosegame;


import net.gazeplay.components.Position;

public class RestartSquare extends Square {

    private Square firstSquare;

    public RestartSquare(int number, Position pawnPosition, Square previousSquare, GooseGame game, Square firstSquare) {
        super(number, pawnPosition, previousSquare, game);
        this.firstSquare = firstSquare;
    }

    @Override
    protected void pawnStays(Pawn pawn) {
        pawn.moveToSquare(firstSquare);
        game.showMessage("Restart from the beginning");
    }
}
