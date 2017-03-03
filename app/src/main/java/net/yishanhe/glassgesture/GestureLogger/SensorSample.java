package net.yishanhe.glassgesture.GestureLogger;

import android.hardware.SensorEvent;

/**
 * Created by syi on 2/20/15.
 */
public class SensorSample {
    public float[] values;
    public long timestamp;

    public SensorSample(SensorEvent event) {
        this.values = event.values.clone();
        this.timestamp = event.timestamp;
    }
}
