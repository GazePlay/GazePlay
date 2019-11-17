package net.gazeplay.games.goosegame;

public class GooseGameVariantGenerator extends net.gazeplay.GameSpec.IntRangeVariantGenerator {

    public GooseGameVariantGenerator() {
        super("Choose number of players", 2, 5);
    }

}
