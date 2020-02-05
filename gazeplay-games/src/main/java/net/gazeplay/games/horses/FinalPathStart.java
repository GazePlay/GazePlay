package net.gazeplay.games.horses;

import lombok.Getter;
import lombok.Setter;
import net.gazeplay.components.Position;


public class FinalPathStart extends Square {

    private final Horses.TEAMS team;
    @Getter
    @Setter
    private Square pathStart;

    public FinalPathStart(final Position pawnPosition, final Horses game, final Horses.TEAMS team, final Square pathStart) {
        super(pawnPosition, game);
        this.team = team;
        this.pathStart = pathStart;
    }

    @Override
    protected Square getNextSquare(final Pawn pawn) {
        if (pawn.getTeam() == team) {
            return pathStart;
        } else {
            return getNextSquare();
        }
    }
}
