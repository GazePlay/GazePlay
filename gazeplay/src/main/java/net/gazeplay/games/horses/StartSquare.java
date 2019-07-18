package net.gazeplay.games.horses;

import net.gazeplay.commons.utils.Position;

public class StartSquare extends Square {

    private Horses.TEAMS team;

    public StartSquare(Position pawnPosition, Horses game, Horses.TEAMS team) {
        super(pawnPosition, game);
        this.team = team;
    }

    @Override
    protected Square getPreviousSquare(Pawn pawn) {
        if (pawn.getTeam() == team) {
            return this;
        } else {
            return super.getPreviousSquare(pawn);
        }
    }
}
