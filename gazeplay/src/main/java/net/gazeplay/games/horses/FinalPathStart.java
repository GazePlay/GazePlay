package net.gazeplay.games.horses;

import lombok.Getter;
import lombok.Setter;
import net.gazeplay.commons.utils.Position;

public class FinalPathStart extends Square{

    private Horses.TEAMS team;
    @Getter
    @Setter
    private Square pathStart;

    public FinalPathStart(Position pawnPosition, Horses game, Horses.TEAMS team, Square pathStart) {
        super(pawnPosition, game);
        this.team = team;
        this.pathStart = pathStart;
    }

    @Override
    protected Square getNextSquare(Pawn pawn) {
        if(pawn.getTeam() == team){
            return pathStart;
        }else{
            return getNextSquare();
        }
    }
}
