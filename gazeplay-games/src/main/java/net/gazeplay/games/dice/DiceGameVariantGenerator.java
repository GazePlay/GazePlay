package net.gazeplay.games.dice;

public class DiceGameVariantGenerator extends net.gazeplay.GameSpec.IntRangeVariantGenerator {

    public DiceGameVariantGenerator() {
        super(1, 6, "dices");
    }

}
