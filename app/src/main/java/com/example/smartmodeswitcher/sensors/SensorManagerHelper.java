package com.example.smartmodeswitcher.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class SensorManagerHelper {
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, light, proximity;
    private SensorEventListenerImpl listener;
    private Context context;

    private boolean isAccelerometerAvailable, isGyroscopeAvailable, isLightAvailable, isProximityAvailable;

    public SensorManagerHelper(Context context, SensorEventListenerImpl listener) {
        this.context = context;
        this.listener = listener;
        initializeSensors();
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerAvailable = true;
        } else {
            isAccelerometerAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            isGyroscopeAvailable = true;
        } else {
            isGyroscopeAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            isLightAvailable = true;
        } else {
            isLightAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            isProximityAvailable = true;
        } else {
            isProximityAvailable = false;
        }
    }

    public void refreshSensors() {
        // Unregister current listeners
        unregisterSensors();
        
        // Reinitialize sensors
        initializeSensors();
        
        // Register listeners again
        registerSensors();
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