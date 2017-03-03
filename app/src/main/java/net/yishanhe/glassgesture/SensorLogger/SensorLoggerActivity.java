package net.yishanhe.glassgesture.SensorLogger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import net.yishanhe.glassgesture.R;
import net.yishanhe.glassgesture.WelcomeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Sensor Logger Activity
 * @author syi
 */
public class SensorLoggerActivity extends Activity implements SensorEventListener {


    private final static String TAG="SensorLoggerActivity";

    private File folder;

    /** Audio manager used to play system sound effects. */
    private AudioManager mAudioManager;
    /** Gesture detector used to present the options menu. */
    private GestureDetector mGestureDetector;
    /** Listener that displays the options menu when the touchpad is tapped. */
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


    // Sensor
    private SensorManager mSensorManager;
    private boolean isTracking =  false;
    private boolean showSamplingRate = true;
    private static final int[] REQUIRED_SENSORS = { Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE}; // selected sensors
    private static final int[] SENSOR_RATES = { SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL}; // set sampling rate
    private int samplingRateCtr;
    private long startTime;

    // UI
    private TextView statusTV;
    private TextView moduleNameTV;
    private TextView numSamplesTV;
    private TextView samplingRateTV;

    // IO
    public static String SAVE_DIR = "SensorLogger";
    public static final String EOL = System.getProperty("line.separator");

    private FileOutputStream fosAcc = null;
    private FileOutputStream fosGyro = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);


        statusTV = (TextView)findViewById(R.id.status);
        moduleNameTV = (TextView)findViewById(R.id.module_name);
        moduleNameTV.setText("Sensor Logger");
        numSamplesTV = (TextView) findViewById(R.id.num_samples);
        samplingRateTV = (TextView) findViewById(R.id.sampling_rate);


        folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+SAVE_DIR+File.separator);
        if(! this.folder.exists()) {
            folder.mkdir();
        }

        numSamplesTV.setText((folder.listFiles().length/2)+" samples");

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
                if( !this.isTracking ) {
                    start();
                }
                return true;
            case R.id.stop:
                if( this.isTracking ) {
                    stop();
                }
                return true;
            case R.id.back:
                if( this.isTracking ) {
                    stop();
                }
                finish();
                return true;
            case R.id.clear:
                if (!this.isTracking){
                    clearLogs();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // using first 1s to calculate sample rate
            if (showSamplingRate){
                samplingRateCtr++;

                if(samplingRateCtr >= 50){
                    long now = System.currentTimeMillis();
                    showSamplingRate = false;
//                    Log.d(TAG, "SamplingRate is " + samplingRateCtr / ((now - startTime) / 1000.0));
                    samplingRateTV.setText(String.format("%.1f", samplingRateCtr / ((now - startTime) / 1000.0))+" Hz");
                }

            }


            if (fosAcc!=null) {
                try {
                    fosAcc.write((event.values[0]+","+event.values[1]+","+event.values[2]+","+event.timestamp+"\n").getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            if (fosGyro!=null) {
                try {
                    fosGyro.write((event.values[0]+","+event.values[1]+","+event.values[2]+","+event.timestamp+"\n").getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        if (isTracking){
            stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        samplingRateTV.setText("0 Hz");
        super.onResume();
    }

    public void start() {

        for (int i = 0; i < REQUIRED_SENSORS.length; i++) {
            Sensor sensor = mSensorManager.getDefaultSensor(REQUIRED_SENSORS[i]);
            if (sensor != null) {
                Log.d(TAG, "registered:" + sensor.getName());
                mSensorManager.registerListener(this, sensor, SENSOR_RATES[i]);
            }
        }

        this.isTracking = true;
        statusTV.setText("Logging");

        if (fosAcc == null && fosGyro == null) {
            try {
                fosAcc = new FileOutputStream(createNewFile("acc"));
                fosGyro = new FileOutputStream(createNewFile("gyro"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        samplingRateCtr = 0;
        startTime = System.currentTimeMillis();
        showSamplingRate = true;

    }

    public void stop() {
        if (fosAcc!=null && fosGyro !=null){
            try {
                fosAcc.close();
                fosGyro.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fosAcc = null;
        fosGyro = null;

        mSensorManager.unregisterListener(this);
        this.isTracking = false;
        statusTV.setText("Logging Stopped");
    }

    public File createNewFile(String name){

        int num = 0;

        File file = new File(folder.toString(), String.valueOf(num++)+'_'+name+".txt");

        while(file.exists()){
            file = new File(folder.toString(), String.valueOf(num++)+'_'+name+".txt");
        }

        numSamplesTV.setText(num+" samples");

        return file;
    }


    public void clearLogs(){
        if (folder.isDirectory()) {
            for(File child: folder.listFiles()){
                child.delete();
            }
        }
        numSamplesTV.setText("0 samples");
    }


}
