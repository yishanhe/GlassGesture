package net.yishanhe.glassgesture.GestureRecognition;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dtw.TimeWarpInfo;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.widget.CardBuilder;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;

import net.yishanhe.glassgesture.R;


/**
 * Created by syi on 3/15/15.
 */
public class MyDtwTestActivity extends Activity {


    private final Handler handler = new Handler();
    public TextView status;
    public static final String EOL = System.getProperty("line.separator");

    final DistanceFunction distFn=DistanceFunctionFactory.getDistFnByName("EuclideanDistance");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_layout);

//        AssetManager assetManager = this.getAssets();
        status = (TextView) findViewById(R.id.status);
        status.setText("Running DTW test.");


//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                long start = System.currentTimeMillis();
//                //load templates
//                int gestureNum = 4;
//                TimeSeries[] templateSet = new TimeSeries[gestureNum];
//                for (int i = 0; i < gestureNum; i++) {
//                    templateSet[i] = new TimeSeries("/sdcard/templates/"+i+"_gyro.txt",new int[]{0,1,2}, false, false, ',');
//                }
////                Log.d("DTW", "loading time (8 gestures) "+(System.currentTimeMillis()-start)+" ms");
//                status.append("loading time (4 gestures) "+(System.currentTimeMillis()-start)+" ms"+EOL);
//
//                // run matching
//                TimeSeries target = new TimeSeries("/sdcard/templates/target/0_gyro.txt",new int[]{0,1,2}, false, false, ',');
//
//                status.append("Distance: ");
//                start = System.currentTimeMillis();
//                for (int i = 0; i < gestureNum; i++) {
//                    TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(target, templateSet[i],0 , distFn );
//                    status.append(String.format("%.1f", info.getDistance()) + " ");
//                }
//                status.append(EOL+"DTW time (4 gestures) "+(System.currentTimeMillis()-start)+" ms"+EOL);
//            }
//        });


        (new DtwAsyncTask()).execute();

    }

    private class DtwAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            //load templates
            long start = System.currentTimeMillis();
            int gestureNum = 4;
            TimeSeries[] templateSet = new TimeSeries[gestureNum];
            for (int i = 0; i < gestureNum; i++) {
                templateSet[i] = new TimeSeries("/sdcard/templates/"+i+"_gyro.txt",new int[]{0,1,2}, false, false, ',');
            }
            publishProgress("loading time (per gestures) "+(System.currentTimeMillis()-start)/gestureNum+" ms"+EOL);

            // do the calculation

            TimeSeries target = new TimeSeries("/sdcard/templates/target/0_gyro.txt",new int[]{0,1,2}, false, false, ',');

            publishProgress("Distance: "+EOL);
            start = System.currentTimeMillis();
            for (int i = 0; i < gestureNum; i++) {
                TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(target, templateSet[i],0 , distFn );
//                TimeWarpInfo info = com.dtw.DTW.getWarpInfoBetween(target, templateSet[i], distFn );
                publishProgress(String.format("%.1f", info.getDistance()) + " ");
            }
            publishProgress(EOL + "DTW time (per gestures) " + (System.currentTimeMillis() - start) / 4 + " ms" + EOL);

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            status.append(values[0]);
        }
    }
}
