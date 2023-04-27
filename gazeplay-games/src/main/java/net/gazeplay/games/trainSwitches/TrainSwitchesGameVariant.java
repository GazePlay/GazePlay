package net.gazeplay.games.trainSwitches;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TrainSwitchesGameVariant {
    pauseTrain("PauseTrain"),
    infiniteTrain("InfiniteTrain"),
    uniqueTrain("UniquesTrain");
    @Getter
    private final String label;
}
