package net.gazeplay.games.trainSwitches;

import net.gazeplay.commons.gamevariants.generators.EnumGameVariantGenerator;

public class TrainSwitchesGameVariantGenerator extends EnumGameVariantGenerator<TrainSwitchesGameVariant> {
    public TrainSwitchesGameVariantGenerator() {
        super(TrainSwitchesGameVariant.values(), TrainSwitchesGameVariant::getLabel);
    }
}
