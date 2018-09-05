package com.example.antonio.limotion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity  {

    private SensorManager mSensorManager;
    private Sensor mLight;
    private Double xPoint;
    private LineGraphSeries<DataPoint> series;
    private GraphView graph;
    private Float yLux;


    long startTime = 0;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            xPoint +=.5d;
            if (yLux == null) {
                yLux = 0f;
            }

            series.appendData(new DataPoint(xPoint,yLux),true,25);
            Log.d("MY_APP", String.valueOf(yLux+ " - "+ xPoint));

            timerHandler.postDelayed(this, 500);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        xPoint = 0d;
        graph = findViewById(R.id.graph);

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        series = new LineGraphSeries<>();
        series.appendData(new DataPoint(0,0),true, 25);
        graph.addSeries(series);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setMinY(0);



    }

    private SensorEventListener mLightSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            yLux = sensorEvent.values[0];
            if (yLux == null) {
                yLux = 0f;
            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d("MY_APP", sensor.toString() + " - " + accuracy);
        }


    };



    @Override
    protected void onResume() {
        super.onResume();
        if (mLight != null) {
            mSensorManager.registerListener(mLightSensorListener, mLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLight != null) {
            mSensorManager.unregisterListener(mLightSensorListener);
        }
    }
}
