package net.gazeplay.commons.utils;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class FixationPoint {
    private long time;
    private long duration;
    private int x;
    private int y;
}
