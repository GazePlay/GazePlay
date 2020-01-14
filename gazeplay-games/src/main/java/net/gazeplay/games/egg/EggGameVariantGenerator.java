package net.gazeplay.games.egg;

public class EggGameVariantGenerator extends net.gazeplay.GameSpec.IntRangeVariantGenerator {

    public EggGameVariantGenerator() {
        super("stepsToHatch", 2, 5);
    }

}
