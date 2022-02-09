package com.josin.metronome.slice;

import ohos.agp.components.Component;
import ohos.multimodalinput.event.RotationEvent;

public class AdjustableRotateListener implements Component.RotationEventListener {
    private final float factor;
    private final Listener listener;

    private boolean isEnabled;

    private float current = 0f;

    public AdjustableRotateListener(int speed, Listener listener) {
        if (speed <= 0) {
            isEnabled = false;
            this.factor = Float.MAX_VALUE;
        } else {
            isEnabled = true;
            this.factor = 1f / speed;
        }
        this.listener = listener;
    }

    @Override
    public boolean onRotationEvent(Component component, RotationEvent rotationEvent) {
        boolean result = false;
        if (isEnabled && rotationEvent != null) {
            float rotationValue = rotationEvent.getRotationValue();
            if (!Float.isInfinite(rotationValue)) {
                current += rotationValue;
                if (Math.abs(current) >= factor) {
                    int value = Math.round(current / factor);
                    listener.onChanged(value);
                    current = 0;
                    result = true;
                }
            }
        }
        return result;
    }

    public interface Listener {
        void onChanged(int value);
    }
}
