package com.josin.metronome.slice;

import ohos.app.Context;

public class Settings {
    private static final int DEFAULT_BPM = 80;

    private static final int MIN_BPM = 30;
    private static final int MAX_BPM = 244;

    private int currentBpm = DEFAULT_BPM;

    public Settings(Context context) {
        //no_op
    }

    public int getMinBpm() {
        return MIN_BPM;
    }

    public int getMaxBpm() {
        return MAX_BPM;
    }

    public int getBpm() {
        return currentBpm;
    }

    public void setBpm(int bpm) {
        currentBpm = bpm;
    }
}
