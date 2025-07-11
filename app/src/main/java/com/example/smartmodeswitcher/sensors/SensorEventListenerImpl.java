package com.example.smartmodeswitcher.sensors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import com.example.smartmodeswitcher.models.ContextResult;
import com.example.smartmodeswitcher.modeswitcher.ModeSwitcher;

public class SensorEventListenerImpl implements SensorEventListener {
    private TextView tvContext, tvValues;
    private Context context;

    private float[] accelValues = new float[3];
    private float[] gyroValues = new float[3];
    private float lightValue = -1;
    private float proximityValue = -1;


    public SensorEventListenerImpl(Context context, TextView tvContext, TextView tvValues) {
        this.context = context;
        this.tvContext = tvContext;
        this.tvValues = tvValues;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelValues = event.values.clone();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroValues = event.values.clone();
                break;
            case Sensor.TYPE_LIGHT:
                lightValue = event.values[0];
                break;
            case Sensor.TYPE_PROXIMITY:
                proximityValue = event.values[0];
                break;
        }

        updateUI();
        detectContext();
    }

    private void updateUI() {
        @SuppressLint("DefaultLocale") String values = String.format(
                "Accel: [%.2f, %.2f, %.2f]\nGyro: [%.2f, %.2f, %.2f]\nLight: %.2f lx\nProximity: %.2f cm",
                accelValues[0], accelValues[1], accelValues[2],
                gyroValues[0], gyroValues[1], gyroValues[2],
                lightValue, proximityValue
        );
        tvValues.setText(values);
    }

    @SuppressLint("SetTextI18n")
    private void detectContext() {
        ContextResult contextResult = ContextDetector.detect(accelValues, lightValue, proximityValue, gyroValues);
        tvContext.setText("Detected Context: \n" + String.format("%s\n%s",
                contextResult.getStableContext(), contextResult.getMotionContext()));
        ModeSwitcher.switchMode(context, contextResult.getStableContext());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op
    }
}