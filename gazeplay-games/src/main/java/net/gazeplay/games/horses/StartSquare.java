package net.gazeplay.games.horses;


import net.gazeplay.components.Position;

public class StartSquare extends Square {

    private final Horses.TEAMS team;

    public StartSquare(final Position pawnPosition, final Horses game, final Horses.TEAMS team) {
        super(pawnPosition, game);
        this.team = team;
    }

    @Override
    protected Square getPreviousSquare(final Pawn pawn) {
        if (pawn.getTeam() == team) {
            return this;
        } else {
            return super.getPreviousSquare(pawn);
        }
    }
}
