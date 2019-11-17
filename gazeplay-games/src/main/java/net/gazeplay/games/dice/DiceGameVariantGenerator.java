package net.gazeplay.games.dice;

public class DiceGameVariantGenerator extends net.gazeplay.GameSpec.IntRangeVariantGenerator {

    public DiceGameVariantGenerator() {
        super("Choose number of dices", 1, 6);
    }

}
