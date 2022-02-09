package com.josin.metronome.math;

public class Clamp {
    private final int min;
    private final int max;

    public Clamp(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int clamp(int value) {
        return clamp(value, min, max);
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
