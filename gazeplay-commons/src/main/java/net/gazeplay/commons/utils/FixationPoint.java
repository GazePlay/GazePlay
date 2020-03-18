package net.gazeplay.commons.utils;

import lombok.*;

@Data
@AllArgsConstructor
public class FixationPoint {

    private long timeGaze;
    private long gazeDuration;
    private int x;
    private int y;

}
