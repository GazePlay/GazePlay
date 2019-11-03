package net.gazeplay.games.horses;

public class HorsesGameVariantGenerator extends net.gazeplay.GameSpec.IntRangeVariantGenerator {

    public HorsesGameVariantGenerator() {
        super("Choose number of players", 2, 4);
    }

}
