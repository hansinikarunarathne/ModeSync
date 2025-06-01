package com.example.smartmodeswitcher.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.TextView;

public class SensorManagerHelper {
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, light, proximity;
    private SensorEventListenerImpl listener;

    private boolean isAccelerometerAvailable, isGyroscopeAvailable, isLightAvailable, isProximityAvailable;
    private TextView textView;

    public SensorManagerHelper(Context context, SensorEventListenerImpl listener, TextView textView) {
        this.listener = listener;
        this.textView = textView;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerAvailable = true;
            textView.append("Accelerometer Sensor is available\n");
        } else {
            textView.append("Accelerometer Sensor is not available\n");
            isAccelerometerAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            isGyroscopeAvailable = true;
            textView.append("Gyroscope Sensor is available\n");
        } else {
            textView.append("Gyroscope Sensor is not available\n");
            isGyroscopeAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            isLightAvailable = true;
            textView.append("Light Sensor is available\n");
        } else {
            textView.append("Light Sensor is not available\n");
            isLightAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            isProximityAvailable = true;
            textView.append("Proximity Sensor is available\n");
        } else {
            textView.append("Proximity Sensor is not available\n");
            isProximityAvailable = false;
        }
    }

    public void registerSensors() {
        if (isAccelerometerAvailable)
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        if (isGyroscopeAvailable)
            sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        if (isLightAvailable)
            sensorManager.registerListener(listener, light, SensorManager.SENSOR_DELAY_NORMAL);
        if (isProximityAvailable)
            sensorManager.registerListener(listener, proximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensors() {
            sensorManager.unregisterListener(listener);
        }
    }