package com.example.antonio.limotion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity  {

    private SensorManager mSensorManager;
    private Sensor mLight;
    private Double xPoint;
    private LineGraphSeries<DataPoint> series;
    private GraphView graph;
    private Float curLux;
    private Boolean isUp = true;
    private Boolean isHold = false;
    private Boolean isPlay = false;
    private Integer downCounter = 0;
    private Integer upCounter = 0;
    private TextView counterTxt, waveTxt;
    private Float lastLux ;
    private Long lastWaveTime;
    private Long lastDown = 0l;
    private Integer countWave = 0;
    Long currentTime;


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
            if (curLux == null) {
                curLux = 0f;
            }

            if (lastWaveTime != null ) {
                long delta = (long) ((System.nanoTime() - lastWaveTime) /1e6);


            }

            series.appendData(new DataPoint(xPoint,curLux),true,25);
//            Log.d("MY_APP", String.valueOf(yLux+ " - "+ xPoint));

            timerHandler.postDelayed(this, 500);

        }
    };

    Handler timerHandler2 = new Handler();
    Runnable timerRunnable2 = new Runnable() {

        @Override
        public void run() {
            if (isHold){
                isHold = false;
            }

            timerHandler2.postDelayed(this, 800);

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
        timerHandler2.postDelayed(timerRunnable2, 0);
        series = new LineGraphSeries<>();
        series.appendData(new DataPoint(0,0),true, 25);
        graph.addSeries(series);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setMinY(0);

        counterTxt = findViewById(R.id.counter);
        waveTxt = findViewById( R.id.txt_wave );

    }

    private SensorEventListener mLightSensorListener = new SensorEventListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            curLux = sensorEvent.values[0];
            Float deltaLux = 0f;
            Long deltaWave = 0l;
            Log.d("curLux", String.valueOf(curLux));

            try {

                if (lastLux > curLux ){
                    deltaLux = (lastLux - curLux)/lastLux;
                } else {
                    deltaLux = (curLux - lastLux)/curLux;
                }

                if (deltaLux >= .03) {
                    if (!isUp && ! isHold) {
                        upCounter += 1;
                        isUp = true;
                        isHold = true;

                        long curWaveTime = System.currentTimeMillis();

                        if (lastWaveTime != null ) {
                            deltaWave = (long) ((curWaveTime - lastWaveTime));

                            if (deltaWave <= 2200 && deltaWave >= 100) {
                                countWave = (countWave + 1) % 4;

                            } else {
                                countWave = 1;


                            }
                            Log.d("Wave", countWave + ", "+deltaLux+","+deltaWave );

                        }

                        lastWaveTime = curWaveTime;

                    } else if (isUp && !isHold){
                        isUp = false;
                        downCounter += 1;

                    }
                }


            } catch (Exception e) {
                lastLux = curLux;
                downCounter = 0;
            }

            if (lastLux> curLux) {
                lastLux = curLux;
            } else if (deltaLux >=.03) {
                lastLux = curLux;


            }

            counterTxt.setText("Counter: "+ downCounter + " DOWN; "+ upCounter +" UP");

            switch (countWave){
                case 1:
                    waveTxt.setText("Single Wave");
                    break;
                case 2:
                    waveTxt.setText("Double Wave");
                    break;
                case 3:
                    waveTxt.setText("Triple Wave");
                    break;
                default:
                    waveTxt.setText( "" );
                    break;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//            Log.d("MY_APP", sensor.toString() + " - " + accuracy);
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
