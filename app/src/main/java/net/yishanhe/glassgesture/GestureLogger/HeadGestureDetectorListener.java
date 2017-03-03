package net.yishanhe.glassgesture.GestureLogger;

/**
 * Created by syi on 2/19/15.
 */
public interface HeadGestureDetectorListener {

    public void onGestureDetected();

//    public void onGestureRecognized();
    public void onGestureSaved(int num);


    public void onSamplingRateUpdated(double samplingRate);


}
