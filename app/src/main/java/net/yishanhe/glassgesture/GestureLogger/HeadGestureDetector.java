package net.yishanhe.glassgesture.GestureLogger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by syi on 2/19/15.
 */
public class HeadGestureDetector implements SensorEventListener {


    boolean isRunning = false;

    private final static String TAG = "GestureDetector";


    private SensorManager mSensorManager;
    private static final int[] REQUIRED_SENSORS = { Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE}; // selected sensors
    private static final int[] SENSOR_RATES = { SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL}; // set sampling rate


    private final static int WINDOWS_SIZE = 30;
    private final static int WINDOWS_STEP = WINDOWS_SIZE /3;
    private final static int HEAD_MOVING_TOLERATE = 2;
    private final static int MIN_GESTURE_SIZE = 30;
    private final static int MAX_GESTURE_SIZE = 250;



    // IO
    public static String SAVE_DIR = "GestureLogger";
    public File folder;
    public static final String EOL = System.getProperty("line.separator");
    private int num;
    private Context context;


    private CircularFifoQueue<SensorSample> accRingBuffer;
    private CircularFifoQueue<SensorSample> gyroRingBuffer;


    private int samplingRateCtr;
    private boolean showSamplingRate;
    private long startTime;

    private double gyroMean;
    private double gyroStd;
    private DescriptiveStatistics gyroStats;
    private int stepCounter;
    private int recordedSteps;
    private int headMovingCtr;
    private boolean isHeadMoving;
    private List<SensorSample> gyroArray = new ArrayList<>();
    private List<SensorSample> accArray = new ArrayList<>();
    private HeadGestureDetectorListener listener;




    public HeadGestureDetector(Context context) {
        this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.context = context;

        folder = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()
                + File.separator + SAVE_DIR);

        if (!folder.exists()){
            folder.mkdir();
        }
    }

    public void start() {

        Log.d(TAG, "start is called");

        for (int i = 0; i < REQUIRED_SENSORS.length; i++) {
            Sensor sensor = mSensorManager.getDefaultSensor(REQUIRED_SENSORS[i]);
            if (sensor != null) {
                Log.d(TAG, "registered:" + sensor.getName());
                mSensorManager.registerListener(this, sensor, SENSOR_RATES[i]);
            }
        }

        gyroStats = new DescriptiveStatistics();
        gyroStats.setWindowSize(WINDOWS_SIZE);


        this.isRunning = true;

        if (this.accRingBuffer == null) {
            this.accRingBuffer = new CircularFifoQueue<SensorSample>(WINDOWS_SIZE);
        } else {
            this.accRingBuffer.clear();
        }
        if (this.gyroRingBuffer == null) {
            this.gyroRingBuffer = new CircularFifoQueue<SensorSample>(WINDOWS_SIZE);
        } else {
            this.gyroRingBuffer.clear();
        }



        gyroMean = 0.0f;
        gyroStd = 0.0f;
        stepCounter = 0;


        samplingRateCtr = 0;
        startTime = System.currentTimeMillis();
        showSamplingRate = true;





    }


    public void stop() {
        this.mSensorManager.unregisterListener(this);
        isRunning = false;
        this.accRingBuffer = null;
        this.gyroRingBuffer = null;
        this.gyroStats = null;


    }


    @Override
    public void onSensorChanged(SensorEvent event) {


        int sensorType = event.sensor.getType();
        SensorSample sample = new SensorSample(event);


        if (sensorType == Sensor.TYPE_ACCELEROMETER) {


            // using first 1s to calculate sample rate
            if (showSamplingRate){
                samplingRateCtr++;
                if(samplingRateCtr >= 50){
                    long now = System.currentTimeMillis();
                    showSamplingRate = false;
//                    Log.d(TAG, "SamplingRate is " + samplingRateCtr / ((now - startTime) / 1000.0));
                    listener.onSamplingRateUpdated(samplingRateCtr / ((now - startTime) / 1000.0));
                }
            }

            accRingBuffer.offer(sample);


            if (isHeadMoving) {

                if (accArray.isEmpty()) {
                    Iterator<SensorSample> iterator = this.accRingBuffer.iterator();
                    while (iterator.hasNext()) {
                        accArray.add(iterator.next());
                    }

                }

                accArray.add(sample);

            } else {
                if (!accArray.isEmpty()) {


                    Log.d(TAG, "sensor length is "+accArray.size());

                    if ((accArray.size()-WINDOWS_STEP*HEAD_MOVING_TOLERATE)>MIN_GESTURE_SIZE
                            && (accArray.size()-WINDOWS_STEP*HEAD_MOVING_TOLERATE)<MAX_GESTURE_SIZE)
                    {
                        Log.d(TAG,"Potential gesture detected: "+
                                (accArray.size()-WINDOWS_STEP*HEAD_MOVING_TOLERATE));
                        SaveSamples(accArray.subList(0,accArray.size()-WINDOWS_STEP*HEAD_MOVING_TOLERATE), "acc");
                    }

                    listener.onGestureSaved(num);
                    // save to file

                    accArray.clear();
                    // on gesture recorded
                }
            }








        }

        else if (sensorType == Sensor.TYPE_GYROSCOPE) {


            gyroRingBuffer.offer(sample);

            // first time, copy data in the ringbuffer to arraylist.
            if (isHeadMoving) {

                if (gyroArray.isEmpty()) {
                    Iterator<SensorSample> iterator = this.gyroRingBuffer.iterator();
                    while (iterator.hasNext()) {
                        gyroArray.add(iterator.next());
                    }
                    listener.onGestureDetected();
                }

                gyroArray.add(sample);

            } else {
                if (!gyroArray.isEmpty()) {
                    // save to file
                    Log.d(TAG, "sensor length is "+gyroArray.size());

                    if ((gyroArray.size()-WINDOWS_STEP*HEAD_MOVING_TOLERATE)>MIN_GESTURE_SIZE
                            && (gyroArray.size()-WINDOWS_STEP*HEAD_MOVING_TOLERATE)<MAX_GESTURE_SIZE)
                    {
                        Log.d(TAG,"Potential gesture detected: "+
                                (gyroArray.size()-WINDOWS_STEP*HEAD_MOVING_TOLERATE));
                        SaveSamples(gyroArray.subList(0,gyroArray.size()-WINDOWS_STEP*HEAD_MOVING_TOLERATE), "gyro");
                    }

                    gyroArray.clear();
                    // on gesture recorded
                }
            }





            stepCounter++;
            gyroStats.addValue(getL2Norm(event.values));

            if (stepCounter == WINDOWS_STEP) {

                stepCounter = 0;

                // update stats
                gyroMean = gyroStats.getMean();
                gyroStd = gyroStats.getStandardDeviation();

                // update UI



                // detection

                if (gyroStd > 0.2) {

                    if (isHeadMoving == false) {
                        Log.d(TAG, "flag to head moving");
                        recordedSteps = 0;
                        isHeadMoving = true;
                    } else {
                        Log.d(TAG, "refresh counter, keep recording");
                        recordedSteps++;
                        headMovingCtr = 0;
                    }


                } else {
                    if (isHeadMoving == true) {
                        if (headMovingCtr>HEAD_MOVING_TOLERATE){
                            Log.d(TAG,"End of recording: "+ recordedSteps);

                            headMovingCtr = 0;
                            isHeadMoving = false;

                            // send to listener

                        } else {
                            headMovingCtr++;
                            recordedSteps++;
                            Log.d(TAG,"tolerate, keep recording");
                        }
                    }
                }

            }



        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private double getL2Norm(float[] values) {
        float norm = (float) Math.sqrt(
                values[0] * values[0]
                        + values[1] * values[1]
                        + values[2] * values[2]
        );
        return (double)norm;
    }



    public File createNewFile(String name){
        num = 0;

        File file = new File(folder.toString(), String.valueOf(num++)+'_'+name+".txt");

        while(file.exists()){
            file = new File(folder.toString(), String.valueOf(num++)+'_'+name+".txt");
        }
//        Log.d(TAG,"File Num: "+ num);

        return file;
    }

    public void SaveSamples(List<SensorSample> sensorSamples,String name){

        BufferedWriter bw =  null;
        DecimalFormat df = new DecimalFormat("0.0000");
        try {
            bw = new BufferedWriter(new FileWriter(createNewFile(name)));

            Iterator<SensorSample> iterator = sensorSamples.iterator();
            while (iterator.hasNext()) {
                SensorSample item = iterator.next();
//  fos_acc.write((event.values[0]+","+event.values[1]+","+event.values[2]+","+event.timestamp+"\n").getBytes());
                bw.write(df.format(item.values[0]) + "," +
                        df.format(item.values[1]) + "," +
                        df.format(item.values[2]) + "," +
                        item.timestamp + '\n');
            }

            if (bw!=null){

                bw.close();
            }
            Log.d(TAG, "saved as file.");
        } catch (IOException e){
            e.printStackTrace();
        }

    }



    public void registerListener(HeadGestureDetectorListener listener){
        this.listener = listener;
        start();
    }

    public void unregisterListener(HeadGestureDetectorListener listener){
        this.listener = null;
        stop();
    }




}
