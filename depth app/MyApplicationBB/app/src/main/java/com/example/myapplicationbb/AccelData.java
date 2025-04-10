package com.example.myapplicationbb;

import java.util.Arrays;

public class AccelData {
    final long timestamp;
    final float[] values;

    AccelData(long timestamp, float[] values) {
        this.timestamp = timestamp;
        this.values = Arrays.copyOf(values, 3);
    }
}
