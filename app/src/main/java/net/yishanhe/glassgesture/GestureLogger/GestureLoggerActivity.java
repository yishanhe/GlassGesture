package net.yishanhe.glassgesture.GestureLogger;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import net.yishanhe.glassgesture.R;
import net.yishanhe.glassgesture.WelcomeActivity;

import java.io.File;


public class GestureLoggerActivity extends Activity implements HeadGestureDetectorListener {

    private AudioManager mAudioManager;
    private GestureDetector mGestureDetector;
    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {

            switch (gesture) {
                case TAP:
                    mAudioManager.playSoundEffect(Sounds.TAP);
                    openOptionsMenu();
                    return true;
                default:
                    return false;
            }
        }
    };


    // Head Gesture
    private HeadGestureDetector headGestureDetector;

    // Sensor
    private boolean isTracking = false;

    // UI
    private TextView statusTV;
    private TextView moduleNameTV;
    private TextView numSamplesTV;
    private TextView samplingRateTV;
    private final Handler handler = new Handler();

    // IO


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_layout);
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
        headGestureDetector = new HeadGestureDetector(this);

        statusTV = (TextView)findViewById(R.id.status);
        statusTV.setText(R.string.ready);
        moduleNameTV = (TextView)findViewById(R.id.module_name);
        moduleNameTV.setText("Gesture Logger");
        numSamplesTV = (TextView) findViewById(R.id.num_samples);
        samplingRateTV = (TextView) findViewById(R.id.sampling_rate);

        numSamplesTV.setText((headGestureDetector.folder.listFiles().length/2)+" samples");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // prepare options here
        setOptionMenuState(menu.findItem(R.id.start),!isTracking);
        setOptionMenuState(menu.findItem(R.id.clear),!isTracking);
        setOptionMenuState(menu.findItem(R.id.back),!isTracking);
        setOptionMenuState(menu.findItem(R.id.stop),isTracking);
        return super.onPrepareOptionsMenu(menu);
    }

    private static void setOptionMenuState(MenuItem menuItem, boolean enabled){
        menuItem.setVisible(enabled);
        menuItem.setEnabled(enabled);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                if (!isTracking){
                    headGestureDetector.registerListener(this);
                    isTracking = true;
                    statusTV.setText(R.string.act);

                }
                return true;
            case R.id.stop:
                if (isTracking){
                    headGestureDetector.unregisterListener(this);
                    isTracking = false;
                    statusTV.setText(R.string.end);
                }
                return true;
            case R.id.back:
                if (isTracking){
                    headGestureDetector.unregisterListener(this);
                    isTracking = false;
                    statusTV.setText(R.string.end);
                }
                finish();
                return true;
            case R.id.clear:
                if (!this.isTracking){
                    clearLogs();
                }
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onGestureDetected() {
        statusTV.setText(R.string.detected);
    }

    @Override
    public void onGestureSaved(int num) {

        statusTV.setText(R.string.hold);
        numSamplesTV.setText(num+" samples");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 1000ms
                statusTV.setText(R.string.act);
            }
        }, 1000);
    }

    @Override
    public void onSamplingRateUpdated(double samplingRate) {
        samplingRateTV.setText(String.format("%.1f", samplingRate)+" Hz");
    }

    @Override
    protected void onPause() {
        if (isTracking){
            headGestureDetector.unregisterListener(this);
            isTracking = false;
            statusTV.setText(R.string.end);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        samplingRateTV.setText("0 Hz");
        super.onResume();
    }

    public void clearLogs(){
        if (headGestureDetector.folder.isDirectory()) {
            for(File child: headGestureDetector.folder.listFiles()){
                child.delete();
            }
        }
        numSamplesTV.setText("0 samples");
    }

}
